package com.destiner.social_reader.model.structs.listeners.articles_load;

/**
 * Keeps count of articles and the offset in list.
 */
public abstract class OnArticleRequestListener {
    private ArticleRequest request;

    public OnArticleRequestListener(ArticleRequest request) {
        this.request = request;
    }

    public abstract void onContentReady(Content content);

    public abstract void onError(RequestError error);

    public ArticleRequest getRequest() {
        return request;
    }
}
