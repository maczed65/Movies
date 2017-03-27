package com.example.mzonno.movies.beans;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The Movie class. Beans to store movie data
 */

public class Review {
    private String id;
    private String author;
    private String content;
    private String url;

    public Review() {

    }
    public Review(JSONObject json) {
        getMovieFromJson(json);
    }

    public String getId() { return id;}

    public void setId(String id) { this.id = id;}

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String value) {
        this.author = value;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String value) {
        this.content = value;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String value) { this.url = value;}



    //----------------------------------------------------------------------------------------
    private void getMovieFromJson(JSONObject jmovie) {

        try {
            setId(jmovie.getString("id"));
            setAuthor(jmovie.getString("author"));
            setContent(jmovie.getString("content"));
            setUrl(jmovie.getString("url"));
        }
        catch (JSONException e ) {
            e.printStackTrace();
        }
    }
}
