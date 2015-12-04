package ru.julsdev.rssfeed.models;


public class PostModel {

    String title;
    String description;
    String publishDate;
    int feedId;

    public PostModel() {
    }

    public PostModel( String title, String description, String publishDate, int feedId) {
        this.title = title;
        this.description = description;
        this.publishDate = publishDate;
        this.feedId = feedId;
    }

    public int getFeedId() {
        return feedId;
    }

    public void setFeedId(int feedId) {
        this.feedId = feedId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }
}
