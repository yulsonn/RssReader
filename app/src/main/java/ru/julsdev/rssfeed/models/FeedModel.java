package ru.julsdev.rssfeed.models;

import java.util.List;

public class FeedModel {

    private String name;
    private String url;
    private List<PostModel> posts;

    public FeedModel() {
    }

    public FeedModel(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public List<PostModel> getPosts() {
        return posts;
    }

    public void setPosts(List<PostModel> posts) {
        this.posts = posts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
