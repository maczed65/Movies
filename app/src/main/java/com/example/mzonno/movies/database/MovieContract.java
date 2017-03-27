package com.example.mzonno.movies.database;


import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {

    // The authority, which is how your code knows which Content Provider to access
    public static final String AUTHORITY = "com.example.mzonno.movies";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Define the possible paths for accessing data in this contract
    // This is the path for the "movies" directory
    public static final String PATH_MOVIES_FAVORITE = "favorite_movies";
    public static final String PATH_MOVIES_POPULAR  = "popular_movies";
    public static final String PATH_MOVIES_TOPRATED = "toprated_movies";

    /* MovieEntry is an inner class that defines the contents of the movies table */

    public static final class MovieEntry implements BaseColumns {

        public static final String FAVORITE_TABLE_NAME  = PATH_MOVIES_FAVORITE;
        public static final String POPULAR_TABLE_NAME   = PATH_MOVIES_POPULAR;
        public static final String TOPRATED_TABLE_NAME  = PATH_MOVIES_TOPRATED;

        public static final Uri CONTENT_URI_FAVORITE    = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES_FAVORITE).build();
        public static final Uri CONTENT_URI_POPULAR     = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES_POPULAR).build();
        public static final Uri CONTENT_URI_TOPRATED    = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES_TOPRATED).build();

        // COLUMNS
        public static final String COLUMN_MOVIE_ID          = "movie_id";
        public static final String COLUMN_MOVIE_TITLE       = "movie_title";
        public static final String COLUMN_MOVIE_OVERVIEW    = "movie_overview";
        public static final String COLUMN_MOVIE_RATING      = "movie_rating";
        public static final String COLUMN_MOVIE_RELEASE_DATE= "movie_release_date";
        public static final String COLUMN_MOVIE_RUNTIME     = "movie_runtime";
        public static final String COLUMN_MOVIE_POSTER_ID   = "movie_poster_id";
        public static final String COLUMN_MOVIE_POSTER_IMG  = "movie_poster_img";


    }
}
