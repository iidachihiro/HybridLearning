package model.fsp;

import java.util.ArrayList;
import java.util.List;

public class Process {
    private String action;
    private List<String> posts;
    
    public Process(String action, String post) {
        this.action = action;
        this.posts = new ArrayList<>();
        posts.add(post);
    }
    
    public Process(String action, List<String> posts) {
        this.action = action;
        this.posts = posts;
    }
    
    public String getAction() {
        return this.action;
    }
    
    public List<String> getPosts() {
        return this.posts;
    }
    
    public void addPosts(List<String> posts) {
        List<String> newPosts = new ArrayList<>();
        for (String post : posts) {
            if (!this.posts.contains(post)) {
                newPosts.add(post);
            }
        }
        this.posts.addAll(newPosts);
    }
}
