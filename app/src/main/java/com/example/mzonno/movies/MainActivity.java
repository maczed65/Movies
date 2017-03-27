package com.example.mzonno.movies;

import android.content.ContentUris;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.res.Configuration;
import android.database.Cursor;

import android.graphics.Point;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mzonno.movies.beans.Movie;
import com.example.mzonno.movies.database.MovieContract;

import com.example.mzonno.movies.database.StoreMoviesIntentService;
import com.example.mzonno.movies.utilities.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;


import static com.example.mzonno.movies.utilities.NetworkUtils.GET_DETAILS;
import static com.example.mzonno.movies.utilities.NetworkUtils.SHOW_FAVORITE;
import static com.example.mzonno.movies.utilities.NetworkUtils.SORT_BY_POPULAR;
import static com.example.mzonno.movies.utilities.NetworkUtils.SORT_BY_RATED;


public class MainActivity extends AppCompatActivity implements MovieAdapter.ListItemClickListener,
                                                               LoaderManager.LoaderCallbacks<ArrayList<Movie>>
{
    // view components
    private RecyclerView mRecyclerView;
    public MovieAdapter mMovieAdapter;
    private TextView     mErrorMessage;
    public ProgressBar  mLoadingIndicator;
    private Menu         mMenu;
    private SharedPreferences    sharedpreferences;

    // Loader Id...
    private static final int ID_FETCH_POPULAR_MOVIES_LOADER     = 100;
    private static final int ID_FETCH_TOPRATED_MOVIES_LOADER    = 110;
    private static final int ID_POPULAR_MOVIES_SQLITE_LOADER    = 120;
    private static final int ID_TOPRATED_MOVIES_SQLITE_LOADER   = 130;
    private static final int ID_FAVORITE_MOVIES_SQLITE_LOADER   = 140;

    // shared preference file
    public static final String MyPREFERENCES = "MoviePrefs" ;

    // default current movie list
    private int currentView = SORT_BY_POPULAR;

    // default current Uri for sqlite
    public Uri activeUri = MovieContract.MovieEntry.CONTENT_URI_POPULAR;

    // the key account to the movieDb server
    public String dbmovieApiKey;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.ic_action_name);

        mErrorMessage = (TextView) findViewById(R.id.tv_error_msg);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        // We use a gridlayout with 2 or 3 columns depends of orientation
        int columns = ((getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) && (getLandscapeScreenWidth() >= 600))? 3 : 2 ;

        GridLayoutManager layoutManager = new GridLayoutManager(this, columns);

        mRecyclerView = (RecyclerView) findViewById(R.id.rcv_moviesImg);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        mMovieAdapter = new MovieAdapter(this);
        mRecyclerView.setAdapter(mMovieAdapter);

        // read from gradle the key account for movieDb
        dbmovieApiKey = BuildConfig.DBMOVIE_API_KEY;

        // create shared preference file
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);

        // enquiring the server to get the movies list
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("currentView")) {
                currentView = savedInstanceState.getInt("currentView");
            }
        }
        else {
            if (sharedpreferences.contains("currentView")) {
                currentView = sharedpreferences.getInt("currentView", 0);
            }
        }
    }

    /*============================================================================================
     * The MainActity is going in stop state
     * we save the info needed for restarting the activity in the last state before stop
     * we use a sharedpreferences because the onSaveInstanceState do not start in this state
     *============================================================================================*/
    @Override
    protected void onStop() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("currentView", currentView);
        editor.apply();
        super.onStop();
    }

    /*============================================================================================
     * The MainActity is going in pause state
     * we save the info needed for restarting the activity in the last state before paused
     *
     * @param outState - the bundle where to save data.
     *============================================================================================*/
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(MainActivity.class.getSimpleName(), "onSaveInstanceState");
        outState.putInt("currentView", currentView);
        super.onSaveInstanceState(outState);
    }

    /*============================================================================================
     * Event menu created
     * we are waiting this event to start the first enquiry on the server or on sqlite tables
     *
     * @param Menu - the menu created.
     *============================================================================================*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        mMenu = menu;

        if (currentView == SORT_BY_POPULAR)
            onOptionsItemSelected(mMenu.findItem(R.id.btn_sort_popular));
        else if (currentView == SORT_BY_RATED)
            onOptionsItemSelected(mMenu.findItem(R.id.btn_sort_toprated));
        else
            onOptionsItemSelected(mMenu.findItem(R.id.btn_show_favorite));
        return true;
    }

    /*============================================================================================
     * Event item click - The user has clicked a menu item from the menu
     *
     * @param MenuItem - item of the menu.
     *============================================================================================*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        hideErrorMsg();
        switch (item.getItemId()) {
            case R.id.btn_sort_popular:
                setTitle(" "+getString(R.string.app_name) + " - "+ getString(R.string.popular_menu_item) );
                selectedPopularMoviesList(item);
                return true;

            case R.id.btn_sort_toprated:
                setTitle(" "+getString(R.string.app_name) + " - "+ getString(R.string.rated_menu_item) );
                selectedTopRatedMoviesList(item);
                return true;

            case R.id.btn_show_favorite:
                setTitle(" "+getString(R.string.app_name) + " - "+ getString(R.string.favorite_menu_item) );
                selectedFavoriteMovieList(item);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*============================================================================================
     * The user want to see the popular o top rated movies
     * if we are connected to internet, we'll call the relative loader for enquiry the server
     * otherwise we'll call the relative loader for enquiry the sqlite database for offline consultation
      *
     * @param criteria - popular or top rated.
     *============================================================================================*/
    private void getListOfMovies(int criteria) {

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if (NetworkUtils.isOnline(MainActivity.this)) {
            getSupportLoaderManager().restartLoader(criteria == SORT_BY_POPULAR ? ID_FETCH_POPULAR_MOVIES_LOADER : ID_FETCH_TOPRATED_MOVIES_LOADER, null, this);
            actionBar.setSubtitle(null);
        }
        else {
            getSupportLoaderManager().restartLoader(criteria == SORT_BY_POPULAR ? ID_POPULAR_MOVIES_SQLITE_LOADER : ID_TOPRATED_MOVIES_SQLITE_LOADER, null, this);
            actionBar.setSubtitle(" - " +getString(R.string.offline) +" - ");
        }
    }

    //============================================================================================
    // Query Movie DB Server about Detail Infos (here only the runtime field) on a Movie with an AsyncTask
    //============================================================================================
    /*
    private class FetchMovieDetailsTask extends AsyncTask<String, Void, String> {

        private Movie mMovie = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);

        }
       @Override
        protected String doInBackground(String... params) {

            if (params.length == 0) {
                return null;
            }
            int clickedItem = Integer.parseInt(params[0]);
            mMovie = mMovieAdapter.getMovieAtPosition(clickedItem);

            URL movieDbRequestUrl = NetworkUtils.buildMovieRequestUrl(GET_DETAILS, mMovie.getId(), dbmovieApiKey, getResources().getString(R.string.language_code));

            try {
                return NetworkUtils.getResponseFromHttpUrl(movieDbRequestUrl);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String movieDetails) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (movieDetails != null) {
                try {
                    JSONObject Json = new JSONObject(movieDetails);

                    // Now we have the missed info: runtime. We save it into the movie list of the adapter
                    mMovie.setRuntime(Json.getString("runtime"));
                    // and We save it into the offline table for forward use
                    Intent storeMovieIntent = new Intent(MainActivity.this, StoreMoviesIntentService.class);

                    storeMovieIntent.setAction(StoreMoviesIntentService.ACTION_UPDATE_RUNTIME_INFO);
                    storeMovieIntent.putExtra("uri", ContentUris.withAppendedId(activeUri, Integer.valueOf(mMovie.getId())).toString());
                    storeMovieIntent.putExtra("runtime", mMovie.getRuntime());
                    startService(storeMovieIntent);

                    startDetailActivity(mMovie);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(MainActivity.this, R.string.details_error_msg, Toast.LENGTH_SHORT).show();
            }
        }
    }
    */

    /*============================================================================================
     * Loader function called by LoaderManager.
     * Here we have all the loaders needed by the MainActivity:
     * ID_FETCH_POPULAR_MOVIES_LOADER & ID_FETCH_TOPRATED_MOVIES_LOADER are relative to enquiry the movie server on internet
     * ID_POPULAR_MOVIES_LOADER is relative to enquiry the sqlite table for offline consultation for popular movies
     * ID_TOPRATED_MOVIES_LOADER is relative to enquiry the sqlite table for offline consultation for top rated movies
     * ID_FAVORITE_MOVIES_LOADER is relative to enquiry the sqlite table for display my favorite movies
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     * ============================================================================================*/
    @Override
    public Loader<ArrayList<Movie>> onCreateLoader(final int loaderId, final Bundle loaderArgs) {

        switch (loaderId) {

            case ID_FETCH_POPULAR_MOVIES_LOADER:
            case ID_FETCH_TOPRATED_MOVIES_LOADER:
                return new AsyncTaskLoader<ArrayList<Movie>>(this) {

                    ArrayList<Movie> aMovies = null;

                    @Override
                    protected void onStartLoading() {
                        if (aMovies != null) {
                            deliverResult(aMovies);
                        } else {
                            mLoadingIndicator.setVisibility(View.VISIBLE);

                            TruncateOfflineTable(); // Before saving movie into the offline table, we delete all data in it
                            forceLoad();
                        }
                    }

                    @Override
                    public ArrayList<Movie> loadInBackground() {
                        int opcode = (loaderId == ID_FETCH_POPULAR_MOVIES_LOADER)? SORT_BY_POPULAR: SORT_BY_RATED;
                        URL movieDbRequestUrl = NetworkUtils.buildMovieRequestUrl(opcode, null, dbmovieApiKey, getResources().getString(R.string.language_code));

                        try {
                            String rawData = NetworkUtils.getResponseFromHttpUrl(movieDbRequestUrl);
                            if (rawData != null) {
                                try {
                                    aMovies = new ArrayList<>();

                                    JSONObject Json = new JSONObject(rawData);
                                    JSONArray moviesJArray = Json.getJSONArray("results");

                                    for (int i = 0; i < moviesJArray.length(); i++) {
                                        aMovies.add(new Movie(moviesJArray.getJSONObject(i)));
                                    }

                                    return aMovies;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                showErrorMsg(getResources().getString(R.string.serverdown_error_msg));
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    public void deliverResult(ArrayList<Movie> data) {
                        aMovies = data;
                        super.deliverResult(data);
                    }
                };

            case ID_POPULAR_MOVIES_SQLITE_LOADER: {
                return new AsyncTaskLoader<ArrayList<Movie>>(this) {

                    Uri popularQueryUri = MovieContract.MovieEntry.CONTENT_URI_POPULAR;
                    ArrayList<Movie> aMovies = null;

                    @Override
                    protected void onStartLoading() {
                        if (aMovies != null) {
                            deliverResult(aMovies);
                        } else {
                            mLoadingIndicator.setVisibility(View.VISIBLE);
                            forceLoad();
                        }
                    }

                    @Override
                    public ArrayList<Movie> loadInBackground() {
                        Cursor cursor = getContentResolver().query(popularQueryUri, null, null, null, null);
                        if ((cursor != null) && cursor.getCount() > 0)
                            try {
                                aMovies = new ArrayList<>();
                                while (cursor.moveToNext()) {
                                    aMovies.add(new Movie(cursor));
                                }
                            }
                            catch (Exception e) { e.printStackTrace(); }
                            finally {
                                cursor.close();
                            }
                        return aMovies;
                    }
                    @Override
                    public void deliverResult(ArrayList<Movie> data) {
                        aMovies = data;
                        super.deliverResult(data);
                    }
                };
            }

            case ID_TOPRATED_MOVIES_SQLITE_LOADER: {
                return new AsyncTaskLoader<ArrayList<Movie>>(this) {
                    Uri topratedQueryUri = MovieContract.MovieEntry.CONTENT_URI_TOPRATED;
                    ArrayList<Movie> aMovies = null;

                    @Override
                    protected void onStartLoading() {
                        if (aMovies != null) {
                            deliverResult(aMovies);
                        } else {
                            mLoadingIndicator.setVisibility(View.VISIBLE);
                            forceLoad();
                        }
                    }

                    @Override
                    public ArrayList<Movie> loadInBackground() {
                        Cursor cursor = getContentResolver().query(topratedQueryUri, null, null, null, null);
                        if ((cursor != null) && cursor.getCount() > 0)
                            try {
                                aMovies = new ArrayList<>();
                                while (cursor.moveToNext()) {
                                    aMovies.add(new Movie(cursor));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            finally {
                                cursor.close();
                            }
                        return aMovies;
                    }
                    @Override
                    public void deliverResult(ArrayList<Movie> data) {
                        aMovies = data;
                        super.deliverResult(data);
                    }
                };
            }
            case ID_FAVORITE_MOVIES_SQLITE_LOADER:  {
                return new AsyncTaskLoader<ArrayList<Movie>>(this) {
                    Uri favoriteQueryUri = MovieContract.MovieEntry.CONTENT_URI_FAVORITE;
                    ArrayList<Movie> aMovies = null;

                    @Override
                    protected void onStartLoading() {
                        if (aMovies != null) {
                            deliverResult(aMovies);
                        } else {
                            mLoadingIndicator.setVisibility(View.VISIBLE);
                            forceLoad();
                        }
                    }

                    @Override
                    public ArrayList<Movie> loadInBackground() {
                        Cursor cursor = getContentResolver().query(favoriteQueryUri, null, null, null, null);
                        if ((cursor != null) && cursor.getCount() > 0)
                            try {
                                aMovies = new ArrayList<>();
                                while (cursor.moveToNext()) {
                                    aMovies.add(new Movie(cursor));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            finally {
                                cursor.close();
                            }
                        return aMovies;
                    }

                    @Override
                    public void deliverResult(ArrayList<Movie> data) {
                        aMovies = data;
                        super.deliverResult(data);
                    }
                };
            }

            default:
                return null;
        }
    }

    /*============================================================================================
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     * ============================================================================================*/
    @Override
    public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        switch (loader.getId()) {
            case ID_FETCH_POPULAR_MOVIES_LOADER:
            case ID_FETCH_TOPRATED_MOVIES_LOADER:
                mMovieAdapter.setMoviesData(data, activeUri, false);
                if (null == data) {
                    showErrorMsg(getResources().getString(R.string.serverdown_error_msg));
                }
                break;
            default:
                mMovieAdapter.setMoviesData(data, activeUri, true);
                if (null == data) {
                    showErrorMsg(getResources().getString(R.string.cursor_error_msg));
                }
                break;
        }
    }

    /*============================================================================================
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     * ============================================================================================*/
    @Override
    public void onLoaderReset(Loader<ArrayList<Movie>> loader) {
        switch (loader.getId()) {
            case ID_FETCH_POPULAR_MOVIES_LOADER:
            case ID_FETCH_TOPRATED_MOVIES_LOADER:
                mMovieAdapter.setMoviesData(null, null, false);
                break;
            default:
                mMovieAdapter.setMoviesData(null, null, true);
                break;
        }
    }


    /*============================================================================================
     * The user has selected a movie....so he starts an activity to display movie details
     * we pass to the activity the movie object and the key to query more info (reviews & trailers)
     *
     * @param mMovie the movie object.
     * ============================================================================================*/
    public void startDetailActivity(Movie mMovie) {

        Intent startChildActivityIntent = new Intent(MainActivity.this, DetailActivity.class);
        Bundle bundle = new Bundle();

        bundle.putParcelable("movie", mMovie);
        startChildActivityIntent.putExtras(bundle);
        startChildActivityIntent.putExtra("dbmovieApiKey", dbmovieApiKey);
        startActivity(startChildActivityIntent);
    }

    /**============================================================================================
     * This callback is invoked when you click on an item in the list.
     *
     * @param clickedItemIndex Index in the list of the item that was clicked.
     *============================================================================================*/
    @Override
    public void onListItemClick(int clickedItemIndex) {
        // get the clicked movie object from the adapter
        Movie movie = mMovieAdapter.getMovieAtPosition(clickedItemIndex);
        // the default api movie-list, called to get movies, do not contains info about runtime
        // so, an other api call is necessary: we ask info about the single movie selected
        // if we are connected to internet
        if ((movie.getRuntime() == null) &&  NetworkUtils.isOnline(MainActivity.this) ) {
            //new FetchMovieDetailsTask().execute(Integer.toString(clickedItemIndex));
            new FetchMovieDetailTask(MainActivity.this).execute(Integer.toString(clickedItemIndex));
        }
        else
        {
            // if we are here is because the user has clicked the movie poster yet.
            // In fact we saved the runtime info into the movie object in the adapter movies list
            startDetailActivity(movie);
        }
    }

    /**============================================================================================
     * No movie found , we display the error or information msg.
     *
     * @param msg Index in the list of the item that was clicked.
     *============================================================================================*/
    public void showErrorMsg(String msg) {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessage.setText(msg);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    /**============================================================================================
     * Hide the error info message and redisplay the recycler view.
     *============================================================================================*/
    public void hideErrorMsg() {
        mErrorMessage.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**============================================================================================
     * We delete all movies in offline tables for popular or top-rated movies before load new movies
     * We start an intentservice in background to achieve that
     *============================================================================================*/
    private void TruncateOfflineTable() {
        Intent storeMovieIntent = new Intent(MainActivity.this, StoreMoviesIntentService.class);

        if (activeUri.equals(MovieContract.MovieEntry.CONTENT_URI_POPULAR))
            storeMovieIntent.setAction(StoreMoviesIntentService.ACTION_DELETE_ALL_POPULAR);
        else if (activeUri.equals(MovieContract.MovieEntry.CONTENT_URI_TOPRATED))
            storeMovieIntent.setAction(StoreMoviesIntentService.ACTION_DELETE_ALL_TOPRATED);

        startService(storeMovieIntent);
    }

    /**============================================================================================
     * this function disable the menuItem "Get Popular Movies" clicked and
     * enable the others such a toggle menu - then start the relative action
     * @param item MenuItem clicked.
     *============================================================================================*/
    private void selectedPopularMoviesList(MenuItem item) {
        activeUri = MovieContract.MovieEntry.CONTENT_URI_POPULAR;

        item.setEnabled(false);
        mMenu.findItem(R.id.btn_sort_toprated).setEnabled(true);
        mMenu.findItem(R.id.btn_show_favorite).setEnabled(true);

        currentView = SORT_BY_POPULAR;
        getListOfMovies(currentView);

    }
    /**============================================================================================
     * this function disable the menuItem "Get Top Rated Movies" clicked and
     * enable the others such a toggle menu - then start the relative action
     * @param item MenuItem clicked.
     *============================================================================================*/
    private void selectedTopRatedMoviesList(MenuItem item) {
        activeUri = MovieContract.MovieEntry.CONTENT_URI_TOPRATED;

        item.setEnabled(false);
        mMenu.findItem(R.id.btn_sort_popular).setEnabled(true);
        mMenu.findItem(R.id.btn_show_favorite).setEnabled(true);

        currentView = SORT_BY_RATED;
        getListOfMovies(currentView);
    }
    /**============================================================================================
     * this function disable the menuItem "Get Favorite Movies" clicked and
     * enable the others such a toggle menu - then start the relative action
     * @param item MenuItem clicked.
     *============================================================================================*/
    private void selectedFavoriteMovieList(MenuItem item) {
        activeUri = MovieContract.MovieEntry.CONTENT_URI_FAVORITE;

        item.setEnabled(false);
        mMenu.findItem(R.id.btn_sort_popular).setEnabled(true);
        mMenu.findItem(R.id.btn_sort_toprated).setEnabled(true);

        currentView = SHOW_FAVORITE;
        getSupportLoaderManager().restartLoader(ID_FAVORITE_MOVIES_SQLITE_LOADER, null, this);
    }

    //
    // Return the max width of the screen of the device in pixels
    //
    private int getLandscapeScreenWidth() {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        return (width < height)? height : width;
    }

}
