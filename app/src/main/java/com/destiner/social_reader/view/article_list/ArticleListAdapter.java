package com.destiner.social_reader.view.article_list;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.destiner.social_reader.R;
import com.destiner.social_reader.model.structs.Article;

import java.util.List;

/**
 * Adapter for article list
 */
public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ViewHolder> {
    private ArticleViewer viewer;

    private List<Article> dataSet;

    /**
     * Custom ViewHolder
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView articleTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            articleTextView = (TextView) itemView.findViewById(R.id.article_text);
        }

        @Override
        public void onClick(View v) {
            viewer.showArticle(dataSet.get(getAdapterPosition()));
        }
    }

    public ArticleListAdapter(ArticleViewer viewer, List<Article> articles) {
        this.viewer = viewer;
        dataSet = articles;
    }

    /**
     * Adds article to the data set and notifies RecyclerView
     * @param article article to be added
     */
    public void add(Article article) {
        dataSet.add(article);
        notifyItemInserted(dataSet.size() - 1);
    }

    /**
     * Adds a list of articles to the top of data set. Notifies about that.
     * @param articles list to be added
     */
    public void addToStart(List<Article> articles) {
        dataSet.addAll(0, articles);
        notifyItemRangeInserted(0, articles.size());
    }

    /**
     * Removes element from list.
     * @param position position of element
     */
    public void remove(int position) {
        dataSet.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Retrieves element from data set.
     * @param position position of element
     * @return retrieved element
     */
    public Article get(int position) {
        return dataSet.get(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_element_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Article article = dataSet.get(position);
        holder.articleTextView.setText(article.getText());
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
