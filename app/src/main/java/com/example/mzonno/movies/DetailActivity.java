package com.example.mzonno.movies;


import android.content.Intent;

import android.database.Cursor;
import android.databinding.DataBindingUtil;

import android.net.Uri;

import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.mzonno.movies.beans.Movie;
import com.example.mzonno.movies.beans.Review;
import com.example.mzonno.movies.beans.Trailer;
import com.example.mzonno.movies.database.MovieContract;
import com.example.mzonno.movies.database.StoreMoviesIntentService;
import com.example.mzonno.movies.databinding.MovieInfoBinding;
import com.example.mzonno.movies.utilities.NetworkUtils;


import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import java.util.ArrayList;

import static com.example.mzonno.movies.utilities.NetworkUtils.GET_REVIEW;
import static com.example.mzonno.movies.utilities.NetworkUtils.GET_TRAILERS;


public class DetailActivity extends AppCompatActivity implements TrailersAdapter.ListItemClickListener,
                                                                 LoaderManager.LoaderCallbacks<String> {
    // view components
    private TrailersAdapter mTrailersAdapter;
    private ReviewsAdapter  mReviewsAdapter;
    private ToggleButton    bFavorite;
    private Menu            mMenu;
    MovieInfoBinding        mBinding;

    // movie object, contains all info about movie selected by the user
    private static Movie mMovie ;

    // the key account to the movieDb server
    private static String apiKey;

    // Loader id
    private static final int ID_FETCH_TRAILERS_LOADER        = 160;
    private static final int ID_FETCH_REVIEWS_LOADER         = 170;
    private static final int ID_FAVORITE_MOVIES_CHECK_LOADER = 180;

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.movie_info);
        setTitle(getString(R.string.movie_details));

        final Intent intent = getIntent();

        ActionBar actionBar = this.getSupportActionBar();

        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // We create the recycler view for trailers records
        LinearLayoutManager layoutManager_trailers = new LinearLayoutManager(this);

        RecyclerView mRecyclerView_Trailers = (RecyclerView) findViewById(R.id.rcv_trailers);
        mRecyclerView_Trailers.setLayoutManager(layoutManager_trailers);
        mRecyclerView_Trailers.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView_Trailers.setHasFixedSize(true);

        mTrailersAdapter = new TrailersAdapter(this);
        mRecyclerView_Trailers.setAdapter(mTrailersAdapter);

         // We create the recycler view for reviews records
        LinearLayoutManager layoutManager_reviews = new LinearLayoutManager(this);

        RecyclerView mRecyclerView_Reviews = (RecyclerView) findViewById(R.id.rcv_reviews);
        mRecyclerView_Reviews.setLayoutManager(layoutManager_reviews);
        mRecyclerView_Reviews.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView_Reviews.setHasFixedSize(true);

        mReviewsAdapter = new ReviewsAdapter();
        mRecyclerView_Reviews.setAdapter(mReviewsAdapter);

        // get the movie object from intent through the Parcelable interface of movie class
        mMovie =  intent.getParcelableExtra("movie");

        mBinding = DataBindingUtil.setContentView(this, R.layout.movie_info);

        MovieInfo movieInfo = new MovieInfo(mMovie, this);
         mBinding.setMovie(movieInfo);
         mBinding.ivThumbnail.setImageBitmap(movieInfo.posterImg);
         /*
         // Get Bitmap image from movie record field byte array
        Bitmap bitmap = BitmapFactory.decodeByteArray(mMovie.getPosterImg(), 0, mMovie.getPosterImg().length);
        ((ImageView)findViewById(R.id.iv_thumbnail)).setImageBitmap(bitmap);

        // Display movie Title
        ((TextView) findViewById(R.id.tv_original_title)).setText(mMovie.getTitle());
        // Display movie release data
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);  // string date format from moviedb
        SimpleDateFormat sdf1 = new SimpleDateFormat(getResources().getString(R.string.date_format), Locale.ENGLISH);
        try {
            ((TextView) findViewById(R.id.tv_release_date)).setText(sdf1.format(sdf.parse(mMovie.getRelease_date())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        // Display movie runtime if exists
        //if (mMovie.getRuntime() != null) {
        //    String duration = mMovie.getRuntime() + " " + getResources().getString(R.string.minutes);
            //((TextView) findViewById(R.id.tv_runtime)).setText(duration);
        //}
        // Display movie vote average
        String vote = mMovie.getVote_average() + "/10";
        ((TextView) findViewById(R.id.tv_vote_average)).setText(vote);
         // Display movie overview
        ((TextView) findViewById(R.id.tv_overview)).setText(mMovie.getOverview());
        */
        /*
        mBinding.ivThumbnail.setImageBitmap(movieInfo.posterImg);
        mBinding.tvOriginalTitle.setText(movieInfo.title);

        mBinding.tvReleaseDate.setText(movieInfo.release_date);

        mBinding.tvRuntime.setText(movieInfo.runtime);

        mBinding.tvVoteAverage.setText(movieInfo.vote_average);

        mBinding.tvOverview.setText(movieInfo.overview);
        */
        // Get Toggle button for favorite action
        bFavorite = (ToggleButton) findViewById(R.id.btn_favorite);

         // assign a listener onClick event to button: the action consists in storing a new record in Favorite Sqlite table
         // through a intent service
        bFavorite.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v)
                                             {
                                             Intent storeMovieIntent = new Intent(DetailActivity.this, StoreMoviesIntentService.class);

                                             if (bFavorite.isChecked()) {

                                                 Bundle bundle = new Bundle();
                                                 bundle.putParcelable("movie", mMovie);

                                                 storeMovieIntent.putExtras(bundle);
                                                 storeMovieIntent.setAction(StoreMoviesIntentService.ACTION_INSERT_INTO_FAVORITE);
                                                 startService(storeMovieIntent);

                                                 bFavorite.setChecked(true);
                                                 Toast.makeText(DetailActivity.this, getString(R.string.success_add_msg), Toast.LENGTH_SHORT).show();
                                             }
                                             else {

                                                 storeMovieIntent.putExtra("movieid", mMovie.getId());
                                                 storeMovieIntent.setAction(StoreMoviesIntentService.ACTION_DELETE_FAVORITE);
                                                 startService(storeMovieIntent);

                                                 bFavorite.setChecked(false);
                                                 Toast.makeText(DetailActivity.this, getString(R.string.success_rem_msg), Toast.LENGTH_SHORT).show();
                                             }

                                             }
                                         });

        // Now We have to ask the reviews data to the MovieDb server.
        // if we are online and we have the key account to the server, we start the loaders
        if (intent.hasExtra("dbmovieApiKey") && NetworkUtils.isOnline(this)) {
            apiKey = intent.getStringExtra("dbmovieApiKey");

            getSupportLoaderManager().initLoader(ID_FETCH_REVIEWS_LOADER, null, this);
        }
        // We need also to know if the movie is in the "favorite" sqlite table already
        getSupportLoaderManager().initLoader(ID_FAVORITE_MOVIES_CHECK_LOADER, null, this);
     }


    /*============================================================================================
     * Event menu created
     *
     * @param Menu - the menu created.
     *============================================================================================*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detailmenu, menu);
        mMenu = menu;

        // we are waiting here to call the MovieDb server for trailer because the first trailer is
        // associated to a menu item action.
        if (apiKey != null)
            getSupportLoaderManager().initLoader(ID_FETCH_TRAILERS_LOADER, null, this);

        return true;
    }

    /*============================================================================================
     * Event item click - The user has clicked a menu item from the menu
     *
     * @param MenuItem - item of the menu.
     *============================================================================================*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the home button is pressed, take the user back to the VisualizerActivity
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    /**============================================================================================
     * This callback is invoked when you click on an item in the list trailers
     *
     * @param clickedItemIndex Index in the list of the item that was clicked.
     *============================================================================================*/
    @Override
    public void onListItemClick(int clickedItemIndex) {
        Trailer trailer = mTrailersAdapter.getTrailerAtPosition(clickedItemIndex);

        // Start Youtube app if exists or web browser
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailer.getKey()));

        if (appIntent.resolveActivity(getPackageManager()) != null)
            startActivity(appIntent);
        else {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.youtube.com/watch?v=" + trailer.getKey()));
            if (webIntent.resolveActivity(getPackageManager()) != null)
                startActivity(webIntent);
        }
    }

    /*============================================================================================
     * Loader function called by LoaderManager.
     * Here we have all the loaders needed by the DetailActivity:
     * ID_FETCH_TRAILERS_LOADER is relative to enquiry the movie server on internet about trailers
     * ID_FETCH_REVIEWS_LOADER  is relative to enquiry the movie server on internet about reviews
     * ID_FAVORITE_MOVIES_CHECK_LOADER is relative to enquiry the sqlite table for check if movie is in favorite table yet
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     * ============================================================================================*/
    @Override
    public Loader<String> onCreateLoader(final int loaderId, final Bundle loaderArgs) {

        switch (loaderId) {
            case ID_FETCH_TRAILERS_LOADER:
                return new AsyncTaskLoader<String>(this) {

                    String rawData = null;

                    @Override
                    protected void onStartLoading() {
                        if (rawData != null) {
                            deliverResult(rawData);
                        } else {
                            forceLoad();
                        }
                    }

                    @Override
                    public String loadInBackground() {
                        String movieId = mMovie.getId();
                        String dbmovieApiKey = apiKey;

                        URL movieDbRequestUrl = NetworkUtils.buildMovieRequestUrl(GET_TRAILERS, movieId, dbmovieApiKey, getResources().getString(R.string.language_code));

                        try {
                            return NetworkUtils.getResponseFromHttpUrl(movieDbRequestUrl);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    public void deliverResult(String data) {
                        rawData = data;
                        super.deliverResult(rawData);
                    }
                };
            case ID_FETCH_REVIEWS_LOADER:
                return new AsyncTaskLoader<String>(this) {

                    String rawData = null;

                    @Override
                    protected void onStartLoading() {
                        if (rawData != null) {
                            deliverResult(rawData);
                        } else {
                            forceLoad();
                        }
                    }

                    @Override
                    public String loadInBackground() {
                        String movieId = mMovie.getId();
                        String dbmovieApiKey = apiKey;

                        URL movieDbRequestUrl = NetworkUtils.buildMovieRequestUrl(GET_REVIEW, movieId, dbmovieApiKey, getResources().getString(R.string.language_code));

                        try {
                            return NetworkUtils.getResponseFromHttpUrl(movieDbRequestUrl);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }

                    public void deliverResult(String data) {
                        rawData = data;
                        super.deliverResult(rawData);
                    }
                };

                case ID_FAVORITE_MOVIES_CHECK_LOADER:
                    return new AsyncTaskLoader<String>(this) {
                        String rawData = null;
                        Uri favoriteQueryUri = MovieContract.MovieEntry.CONTENT_URI_FAVORITE;

                        @Override
                        protected void onStartLoading() {
                            if (rawData != null) {
                                deliverResult(rawData);
                            } else {
                                forceLoad();
                            }
                        }

                        @Override
                        public String loadInBackground() {
                            Cursor cursor = getContentResolver().query(favoriteQueryUri,
                                                                        new String[] {MovieContract.MovieEntry.COLUMN_MOVIE_ID},
                                                                        MovieContract.MovieEntry.COLUMN_MOVIE_ID +"= ?",
                                                                        new String[] {mMovie.getId()},
                                                                        null);
                            if (cursor != null)
                                try {
                                    if (cursor.moveToNext()) {
                                        rawData = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID));
                                    }
                                    else
                                        rawData = null;
                                }
                                catch (Exception d) { d.printStackTrace();}
                                finally {
                                    cursor.close();
                                }

                            return rawData;
                        }

                        @Override
                        public void deliverResult(String data) {
                            rawData = data;
                            super.deliverResult(data);
                        }
                    };
        }
        return null;
    }

    /*============================================================================================
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     * ============================================================================================*/
    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        switch (loader.getId()) {
            case ID_FETCH_TRAILERS_LOADER:
                if (data != null) {
                    try {
                        JSONObject Json = new JSONObject(data);
                        JSONArray trailersJArray = Json.getJSONArray("results");
                        ArrayList<Trailer> aTrailers = new ArrayList<>();

                        for (int i = 0; i < trailersJArray.length(); i++) {
                            aTrailers.add(new Trailer(trailersJArray.getJSONObject(i)));
                        }
                        mTrailersAdapter.setTrailersData(aTrailers);

                        // if the trailer list is not empty, the first trailer is associated
                        // to the share menu item for sharing url
                        MenuItem menuItem = mMenu.findItem(R.id.btn_share_trailer);
                        if (aTrailers.size() > 0) {
                            menuItem.setVisible(true);
                            menuItem.setIntent(createShareTrailerIntent(aTrailers.get(0)));
                        } else
                            menuItem.setVisible(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case ID_FETCH_REVIEWS_LOADER:
                if (data != null) {
                    try {
                        JSONObject Json = new JSONObject(data);
                        JSONArray reviewsArray = Json.getJSONArray("results");
                        ArrayList<Review> aReviews = new ArrayList<>();

                        for (int i = 0; i < reviewsArray.length(); i++) {
                            aReviews.add(new Review(reviewsArray.getJSONObject(i)));
                        }
                        mReviewsAdapter.setReviewData(aReviews);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;

            case ID_FAVORITE_MOVIES_CHECK_LOADER:
                if (data != null)
                    if (data.equals(mMovie.getId()))
                        bFavorite.setChecked(true);
                    else
                        bFavorite.setChecked(false);
                else
                    bFavorite.setChecked(false);
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
    public void onLoaderReset(Loader<String> loader) {
        switch (loader.getId()) {
            case ID_FETCH_REVIEWS_LOADER:
                mReviewsAdapter.setReviewData(null);
                break;
            case ID_FETCH_TRAILERS_LOADER:
                mTrailersAdapter.setTrailersData(null);
                break;
            case ID_FAVORITE_MOVIES_CHECK_LOADER:
                break;
        }
    }

    /*============================================================================================
    * Intent associated to menu for sharing the Url of the first trailer in the list
    *
    * @param loader The Loader that is being reset.
    * ============================================================================================*/
    private Intent createShareTrailerIntent(Trailer trailer) {

        return ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setText("http://www.youtube.com/watch?v=" + trailer.getKey())
                .getIntent();
    }

}
