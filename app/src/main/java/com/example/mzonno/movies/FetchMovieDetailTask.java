package com.example.mzonno.movies;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.example.mzonno.movies.beans.Movie;
import com.example.mzonno.movies.database.StoreMoviesIntentService;
import com.example.mzonno.movies.utilities.NetworkUtils;

import org.json.JSONObject;

import java.net.URL;

import static com.example.mzonno.movies.utilities.NetworkUtils.GET_DETAILS;


//============================================================================================
// Query Movie DB Server about Detail Infos (here only the runtime field) on a Movie with an AsyncTask
//============================================================================================
public class FetchMovieDetailTask extends AsyncTask<String, Void, String> {

    private Movie mMovie = null;
    Context context;

    public FetchMovieDetailTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ((MainActivity)context).mLoadingIndicator.setVisibility(View.VISIBLE);

    }
    @Override
    protected String doInBackground(String... params) {

        if (params.length == 0) {
            return null;
        }
        int clickedItem = Integer.parseInt(params[0]);
        mMovie = ((MainActivity)context).mMovieAdapter.getMovieAtPosition(clickedItem);

        URL movieDbRequestUrl = NetworkUtils.buildMovieRequestUrl(  GET_DETAILS,
                                                                    mMovie.getId(),
                                                                    ((MainActivity)context).dbmovieApiKey,
                                                                    context.getString(R.string.language_code));

        try {
            return NetworkUtils.getResponseFromHttpUrl(movieDbRequestUrl);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void onPostExecute(String movieDetails) {
        ((MainActivity)context).mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (movieDetails != null) {
            try {
                JSONObject Json = new JSONObject(movieDetails);

                // Now we have the missed info: runtime. We save it into the movie list of the adapter
                mMovie.setRuntime(Json.getString("runtime"));
                // and We save it into the offline table for forward use
                Intent storeMovieIntent = new Intent(context, StoreMoviesIntentService.class);

                storeMovieIntent.setAction(StoreMoviesIntentService.ACTION_UPDATE_RUNTIME_INFO);
                storeMovieIntent.putExtra("uri", ContentUris.withAppendedId(((MainActivity)context).activeUri, Integer.valueOf(mMovie.getId())).toString());
                storeMovieIntent.putExtra("runtime", mMovie.getRuntime());
                context.startService(storeMovieIntent);

                ((MainActivity)context).startDetailActivity(mMovie);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(context, R.string.details_error_msg, Toast.LENGTH_SHORT).show();
        }
    }
}
