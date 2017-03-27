package com.example.mzonno.movies.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import static com.example.mzonno.movies.database.MovieContract.MovieEntry.COLUMN_MOVIE_ID;
import static com.example.mzonno.movies.database.MovieContract.MovieEntry.FAVORITE_TABLE_NAME;
import static com.example.mzonno.movies.database.MovieContract.MovieEntry.FAVORITE_TABLE_NAME;
import static com.example.mzonno.movies.database.MovieContract.MovieEntry.POPULAR_TABLE_NAME;
import static com.example.mzonno.movies.database.MovieContract.MovieEntry.TOPRATED_TABLE_NAME;

public class MovieContentProvider extends ContentProvider {
    // IDs for Uri Matcher
    public static final int MOVIES_FAVORITE             = 100;
    public static final int MOVIES_FAVORITE_WITH_ID     = 101;
    public static final int MOVIES_POPULAR              = 110;
    public static final int MOVIES_POPULAR_WITH_ID      = 111;
    public static final int MOVIES_TOPRATED             = 120;
    public static final int MOVIES_TOPRATED_WITH_ID     = 121;

    // Member variable for a TaskDbHelper that's initialized in the onCreate() method
    private MovieDbHelper mMovieDbHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mMovieDbHelper = new MovieDbHelper(context);
        return true;
    }


    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        long id;

        int match = sUriMatcher.match(uri);
        Uri returnUri; // URI to be returned

        switch (match) {
            case MOVIES_FAVORITE:
                // Inserting values into movies favourites table
                id = db.insert(FAVORITE_TABLE_NAME, null, values);
                if ( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI_FAVORITE, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case MOVIES_POPULAR:
                // Inserting values into movies popular table
                id = db.insert(POPULAR_TABLE_NAME, null, values);
                if ( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI_POPULAR, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case MOVIES_TOPRATED:
                // Inserting values into movies toprated table
                id = db.insert(TOPRATED_TABLE_NAME, null, values);
                if ( id > 0 ) {
                    returnUri = ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI_TOPRATED, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }

    /**
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database.
     *               This must not be {@code null}.
     *
     * @return The number of values that were inserted.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        int rowsInserted = 0;
        switch (sUriMatcher.match(uri)) {

            case MOVIES_POPULAR:
                db.beginTransaction();
                rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.POPULAR_TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            case MOVIES_TOPRATED:
                db.beginTransaction();
                rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.POPULAR_TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        final SQLiteDatabase db = mMovieDbHelper.getReadableDatabase();

        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case MOVIES_POPULAR:
                retCursor =  db.query(MovieContract.MovieEntry.POPULAR_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIES_TOPRATED:
                retCursor =  db.query(MovieContract.MovieEntry.TOPRATED_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIES_FAVORITE:
                retCursor =  db.query(MovieContract.MovieEntry.FAVORITE_TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // COMPLETED (4) Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the desired Cursor
        return retCursor;
    }


    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        int movieDeleted = 0;

        switch (match) {
            case MOVIES_FAVORITE_WITH_ID:
                String id = uri.getPathSegments().get(1);
                movieDeleted = db.delete(FAVORITE_TABLE_NAME, COLUMN_MOVIE_ID+"=?", new String[]{id});
                break;
            case MOVIES_POPULAR:
                movieDeleted = db.delete(POPULAR_TABLE_NAME, null, null);
                break;
            case MOVIES_TOPRATED:
                movieDeleted = db.delete(TOPRATED_TABLE_NAME, null, null);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (movieDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return movieDeleted;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        final SQLiteDatabase db = mMovieDbHelper.getWritableDatabase();
        int upd = 0;
        int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIES_POPULAR_WITH_ID: {
                String movieId = uri.getPathSegments().get(1);
                upd = db.update(POPULAR_TABLE_NAME, values, MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?" , new String[]{movieId});
                break;
            }
            case MOVIES_TOPRATED_WITH_ID: {
                String movieId = uri.getPathSegments().get(1);
                upd = db.update(TOPRATED_TABLE_NAME, values, MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?" , new String[]{movieId});
                break;
            }
            case MOVIES_FAVORITE_WITH_ID: {
                String movieId = uri.getPathSegments().get(1);
                upd = db.update(FAVORITE_TABLE_NAME, values, MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?", new String[]{movieId});
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
            // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

            // Return constructed uri (this points to the newly inserted row of data)
        return upd;
    }


    @Override
    public String getType(@NonNull Uri uri) {

        throw new UnsupportedOperationException("Not yet implemented");
    }


    // Define a static buildUriMatcher method that associates URI's with their int match
    /**
     Initialize a new matcher object without any matches,
     then use .addURI(String authority, String path, int match) to add matches
     */
    public static UriMatcher buildUriMatcher() {

        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /*
          All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the task directory and a single item by ID.
         */
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES_TOPRATED, MOVIES_TOPRATED);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES_POPULAR, MOVIES_POPULAR);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES_FAVORITE, MOVIES_FAVORITE);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES_FAVORITE + "/#", MOVIES_FAVORITE_WITH_ID);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES_POPULAR  + "/#", MOVIES_POPULAR_WITH_ID);
        uriMatcher.addURI(MovieContract.AUTHORITY, MovieContract.PATH_MOVIES_TOPRATED + "/#", MOVIES_TOPRATED_WITH_ID);


        return uriMatcher;
    }

}