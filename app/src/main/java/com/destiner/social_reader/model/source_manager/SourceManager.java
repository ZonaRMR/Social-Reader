package com.destiner.social_reader.model.source_manager;

import android.content.Context;

import com.destiner.social_reader.R;
import com.destiner.social_reader.model.filter.FilterManager;
import com.destiner.social_reader.model.structs.Post;
import com.destiner.social_reader.model.structs.listeners.articles_load.OnArticleRequestListener;
import com.destiner.social_reader.model.structs.listeners.articles_load.RequestError;
import com.destiner.social_reader.model.structs.source.GroupSource;
import com.destiner.social_reader.model.structs.source.QuerySource;
import com.destiner.social_reader.model.structs.source.Source;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;

import org.joda.time.DateTime;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles all work with sources: loads posts through API, updates source states.
 */
public class SourceManager {
    private static Context context;
    private static Set<Source> sources = new HashSet<>();
    private static SourceDBOpenHelper databaseHelper;

    private SourceManager() {
    }

    public static void setContext(Context c) {
        context = c;
        addSourcesFromResources();
        databaseHelper = new SourceDBOpenHelper(context);
    }

    /**
     * Loads new (ones which wasn't get before) posts from group sources.
     * @param callback callback listener that should be fired when posts retrieval is complete.
     */
    public static void getGroupPosts(OnArticleRequestListener callback) {
        Set<GroupSource> groupSources = getGroupSources();
        Set<Integer> groupSourceIds = getGroupSourceIds(groupSources);
        DateTime endTime = getEarliestPostDate(groupSources);
        DateTime startTime = new DateTime(0);
        OnPostsLoadListener listener = buildListener(groupSources, callback);
        requestPosts(groupSourceIds, startTime, endTime, listener);
    }

    /**
     * Loads new (ones which was not get before) posts from query sources
     * @param callback callback listener that should be fired when posts retrieval is complete.
     */
    public static void getQueryPosts(OnArticleRequestListener callback) {
        Set<QuerySource> querySources = getQuerySources();
        Set<String> queries = getQueries(querySources);
        String query = queries.iterator().next();
        DateTime endTime = getEarliestPostDate(querySources);
        DateTime startTime = new DateTime(0);
        OnPostsLoadListener listener = buildListener(querySources, callback);
        requestPosts(query, startTime, endTime, listener);
    }

    private static void addSourcesFromResources() {
        if (context == null) {
            return;
        }
        // Add sources
        int[] groupIds = context.getResources().getIntArray(R.array.group_sources);
        for (Integer groupId : groupIds) {
            sources.add(new GroupSource(groupId));
        }

        String[] queries = context.getResources().getStringArray(R.array.query_sources);
        for (String query : queries) {
            sources.add(new QuerySource(query));
        }
    }

    /**
     * Returns group sources as set
     * @return group sources
     */
    private static Set<GroupSource> getGroupSources() {
        Set<GroupSource> groupSources = new HashSet<>();
        for (Source source : sources) {
            if (source instanceof GroupSource) {
                groupSources.add((GroupSource) source);
            }
        }
        return groupSources;
    }

    /**
     * Returns query sources as set
     * @return query sources
     */
    private static Set<QuerySource> getQuerySources() {
        Set<QuerySource> querySources = new HashSet<>();
        for (Source source : sources) {
            if (source instanceof QuerySource) {
                querySources.add((QuerySource) source);
            }
        }
        return querySources;
    }

    /**
     * Finds the earliest post date from given source set, i. e. the date after each there were no
     * posts loaded from given sources before
     * @param sources source list
     * @return earliest post date
     */
    private static DateTime getEarliestPostDate(Set<? extends Source> sources) {
        long maxUnixTime = Integer.MAX_VALUE;
        long earliestPostMillis = maxUnixTime * 1000;
        for (Source source : sources) {
            long firstPostMillis = databaseHelper.getDate(source);
            if (firstPostMillis < earliestPostMillis) {
                earliestPostMillis = firstPostMillis;
            }
        }
        return new DateTime(earliestPostMillis);
    }

    /**
     * Extracts group source ids to the set
     * @param groupSources group sources
     * @return set of ids
     */
    private static Set<Integer> getGroupSourceIds(Set<GroupSource> groupSources) {
        Set<Integer> ids = new HashSet<>();
        for (GroupSource source : groupSources) {
            ids.add(- source.getId());
        }
        return ids;
    }

    /**
     * Extracts query source queries to the set
     * @param querySources query sources
     * @return set of queries
     */
    private static Set<String> getQueries(Set<QuerySource> querySources) {
        Set<String> queries = new HashSet<>();
        for (QuerySource source : querySources) {
            queries.add(source.getQuery());
        }
        return queries;
    }

    /**
     * Requests posts from API with given parameters
     * @param sourceIds set of source ids
     * @param startTime first time when posts could be created
     * @param endTime last time when posts could be created
     * @param callback callback listener that will fire when request will complete
     */
    private static void requestPosts(Set<Integer> sourceIds, DateTime startTime, DateTime endTime,
                              OnPostsLoadListener callback) {
        String sourceIdsString = sourceIds.toString();
        VKParameters parameters = VKParameters.from(
                "filters", "post",
                "source_ids", sourceIdsString,
                "count", 100,
                "start_time", startTime.getMillis() / 1000,
                "end_time", endTime.getMillis() / 1000
                );
        VKRequest request = new VKRequest("newsfeed.get", parameters);
        request.executeWithListener(new SourceVKRequestListener(callback));
    }

    /**
     * Requests posts from API with given parameters
     * @param query set of source queries
     * @param startTime first time when posts could be created
     * @param endTime last time when posts could be created
     * @param callback callback listener that will fire when request will complete
     */
    private static void requestPosts(String query, DateTime startTime, DateTime endTime,
                                     OnPostsLoadListener callback) {
        VKParameters parameters = VKParameters.from(
                "q", query,
                "count", 200,
                "start_time", startTime.getMillis() / 1000,
                "end_time", endTime.getMillis() / 1000
        );
        VKRequest request = new VKRequest("newsfeed.search", parameters);
        request.executeWithListener(new SourceVKRequestListener(callback));
    }

    /**
     * Builds OnPostsLoadListener instance with method implementation
     * @param sources queried sources
     * @param callback callback listener
     * @return created instance
     */
    private static OnPostsLoadListener buildListener(Set<? extends Source> sources,
                                                     final OnArticleRequestListener callback) {
        return new OnPostsLoadListener(sources, callback) {
            @Override
            public void onPostsLoad(List<Post> posts, DateTime earliestPostDate) {
                Set<? extends Source> sourceSet = getSources();
                updateSources(sourceSet, earliestPostDate);
                FilterManager.filter(posts, this);
            }

            @Override
            public void onErrorOccur(int errorCode) {
                RequestError error = RequestError.getError(errorCode);
                callback.onError(error);
            }
        };
    }

    /**
     * Updates information about sources
     * @param sources queried sources
     * @param earliestPostDate new earliest post date
     */
    private static void updateSources(Set<? extends Source> sources, DateTime earliestPostDate) {
        for (Source source : sources) {
            databaseHelper.insert(source, earliestPostDate);
        }
    }
}
