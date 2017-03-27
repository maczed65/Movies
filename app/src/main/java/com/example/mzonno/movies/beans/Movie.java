package com.example.mzonno.movies.beans;

import android.database.Cursor;

import com.example.mzonno.movies.database.MovieContract;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * The Movie class. Beans to store movie data
 */

public class Movie implements Parcelable{


    private String id;
    private String title;
    private String posterId;
    private byte[] posterImg;
    private String overview;
    private String vote_average;
    private String release_date;
    private String runtime;

    private boolean favorite;

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }


    public String getRuntime() {

        return runtime;
    }

    public void setRuntime(String runtime) {

        this.runtime = runtime;
    }

    public String getOverview() {

        return overview;
    }

    public void setOverview(String overview) {

        this.overview = overview;
    }

    public String getVote_average() {

        return vote_average;
    }

    public void setVote_average(String vote_average) {

        this.vote_average = vote_average;
    }

    public String getRelease_date() {

        return release_date;
    }

    public void setRelease_date(String release_date) {

        this.release_date = release_date;
    }

    public byte[] getPosterImg() {

        return posterImg;
    }

    public void setPosterImg(byte[] posterImg) {

        this.posterImg = posterImg;
    }

    public String getTitle() {

        return title;
    }

    void setTitle(String title) {

        this.title = title;
    }

    public String getPosterId() {

        return posterId;
    }

    void setPosterId(String posterId) {

        this.posterId = posterId;
    }

    public String getId() {

        return id;
    }
    public void setId(String id) {

        this.id = id;
    }
    //----------------------------------------------------------------------------------------
    /**
     * A constructor that initializes the Movie object
     **/
    public Movie(JSONObject jmovie) {

        try {
            setId(jmovie.getString("id"));
            setPosterId(jmovie.getString("poster_path"));
            setTitle(jmovie.getString("title"));
            // runtime data not available here
            setOverview(jmovie.getString("overview"));
            setVote_average(jmovie.getString("vote_average"));
            setRelease_date(jmovie.getString("release_date"));
            setFavorite(false);
        }
        catch (JSONException e ) {
            e.printStackTrace();
        }
    }
    /**
     * A constructor that initializes the Movie object
     **/
    public Movie(Cursor cursor) {
        try {
            setId(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID)));
            setPosterId(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_ID)));
            setTitle(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE)));
            setOverview(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW)));
            setVote_average(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RATING)));
            setRelease_date(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE)));
            setRuntime(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_RUNTIME)));
            setPosterImg(cursor.getBlob(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_IMG)));
        }
        catch (Exception d) {
            d.printStackTrace();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Storing the Movie data to Parcel object
     **/
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(posterId);
        dest.writeString(overview);
        dest.writeString(vote_average);
        dest.writeString(release_date);
        dest.writeString(runtime);
        dest.writeByteArray(posterImg);
        dest.writeByte((byte) (favorite ? 1 : 0));
    }
    /**
     * Retrieving Movie data from Parcel object
     * This constructor is invoked by the method createFromParcel(Parcel source) of
     * the object CREATOR
     **/
    private Movie(Parcel in){
        this.id = in.readString();
        this.title = in.readString();
        this.posterId = in.readString();
        this.overview = in.readString();
        this.vote_average = in.readString();
        this.release_date = in.readString();
        this.runtime = in.readString();
        this.posterImg = in.createByteArray();
        this.favorite = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
