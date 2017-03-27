package com.example.mzonno.movies.database;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.example.mzonno.movies.beans.Movie;


public class StoreMoviesIntentService extends IntentService {
    public static final String ACTION_INSERT_INTO_POPULAR   = "insert-into-popular-table";
    public static final String ACTION_INSERT_INTO_TOPRATED  = "insert-into-toprated-table";
    public static final String ACTION_INSERT_INTO_FAVORITE  = "insert-into-favorite-table";

    public static final String ACTION_DELETE_ALL_POPULAR    = "delete_all-popular-table";
    public static final String ACTION_DELETE_ALL_TOPRATED   = "delete-all-toprated-table";
    public static final String ACTION_DELETE_FAVORITE       = "delete-favorite";

    public static final String ACTION_UPDATE_RUNTIME_INFO   = "update-runtime-info";

    public StoreMoviesIntentService() {

        super("StoreMoviesIntentService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ContentValues contentValues = new ContentValues();

        try {
            String action = intent.getAction();

            if (action != null)
                switch (action) {
                    // Actions relative to delete all records inside Sqlite tables for popular and top rated movies
                    case ACTION_DELETE_ALL_POPULAR:
                        getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI_POPULAR, null, null);
                        break;
                    case ACTION_DELETE_ALL_TOPRATED:
                        getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI_TOPRATED, null, null);
                        break;
                    // Action relative to remove a movie record from favorite Sqlite table
                    case ACTION_DELETE_FAVORITE: {
                        String movieId = intent.getStringExtra("movieid");
                        getContentResolver().delete(ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI_FAVORITE, Integer.valueOf(movieId)), null, null);
                        break;
                    }
                    // Action relative to update the movie record with runtime info
                    case ACTION_UPDATE_RUNTIME_INFO: {
                        String runtime = intent.getStringExtra("runtime");
                        Uri uri = Uri.parse(intent.getStringExtra("uri"));

                        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RUNTIME, runtime);

                        getContentResolver().update(uri, contentValues, null, null);
                        break;
                    }
                    // Actions relative to insert the movie record into the Sqlite table
                    case ACTION_INSERT_INTO_POPULAR:
                    case ACTION_INSERT_INTO_TOPRATED:
                    case ACTION_INSERT_INTO_FAVORITE: {
                        Movie movie = (Movie) intent.getExtras().getParcelable("movie");

                        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getId());
                        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, movie.getTitle());
                        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_OVERVIEW, movie.getOverview());
                        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RATING, movie.getVote_average());
                        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RELEASE_DATE, movie.getRelease_date());
                        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_RUNTIME, movie.getRuntime());
                        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_ID, movie.getPosterId());
                        contentValues.put(MovieContract.MovieEntry.COLUMN_MOVIE_POSTER_IMG, movie.getPosterImg());

                        switch (action) {
                            case ACTION_INSERT_INTO_POPULAR:
                                getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI_POPULAR, contentValues);
                                break;
                            case ACTION_INSERT_INTO_TOPRATED:
                                getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI_TOPRATED, contentValues);
                                break;
                            case ACTION_INSERT_INTO_FAVORITE:
                                getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI_FAVORITE, contentValues);
                                break;
                        }
                        break;
                    }
                }
        }
        catch (Exception e ) { e.printStackTrace();  }
    }
}
