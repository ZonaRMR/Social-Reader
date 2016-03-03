package com.destiner.social_reader.model.cache;

import com.destiner.social_reader.presenter.article_list.OnArticlesLoadListener;

/**
 * Keeps count of articles and the offset in list.
 */
public abstract class OnOffsetArticlesLoadListener implements OnArticlesLoadListener {
    private int count;
    private int offset;

    public OnOffsetArticlesLoadListener(int count, int offset) {
        this.count = count;
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public int getOffset() {
        return offset;
    }
}
