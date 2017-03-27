package com.example.mzonno.movies.beans;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * The Movie class. Beans to store movie data
 */

public class Trailer {
    private String id;
    private String iso_639_1;
    private String iso_3166_1;
    private String key;
    private String name;
    private String site;
    private int size;
    private String type;

    public Trailer() {

    }
    public Trailer(JSONObject json) {
        getMovieFromJson(json);
    }

    public String getId() { return id;}

    public void setId(String id) { this.id = id;}

    public String getIso_639_1() {
        return iso_639_1;
    }

    public void setIso_639_1(String value) {
        this.iso_639_1 = value;
    }

    public String getIso_3166_1() {
        return iso_3166_1;
    }

    public void setIso_3166_1(String value) {
        this.iso_3166_1 = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String value) { this.key = value;}

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String value) { this.site = value;  }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getSize() { return size; }

    public void setSize(int value) { this.size = value; }

    //----------------------------------------------------------------------------------------
    private void getMovieFromJson(JSONObject jmovie) {

        try {
            setId(jmovie.getString("id"));
            setIso_639_1(jmovie.getString("iso_639_1"));
            setIso_3166_1(jmovie.getString("iso_3166_1"));
            setKey(jmovie.getString("key"));
            setName(jmovie.getString("name"));
            setSite(jmovie.getString("site"));
            setType(jmovie.getString("type"));
            setSize(jmovie.getInt("size"));
        }
        catch (JSONException e ) {
            e.printStackTrace();
        }
    }
}
