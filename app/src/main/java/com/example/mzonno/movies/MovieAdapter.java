/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.mzonno.movies;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import com.example.mzonno.movies.beans.Movie;
import com.example.mzonno.movies.database.MovieContract;
import com.example.mzonno.movies.database.StoreMoviesIntentService;
import com.squareup.picasso.*;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * {@link MovieAdapter} exposes a list of movies
 * {@link android.support.v7.widget.RecyclerView}
 */
class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private ArrayList<Movie> mMoviesData;
    private Context mContext;
    private Uri activeUri;
    private boolean offline;

    private ListItemClickListener mOnClickListener;

    /**
     * The interface that receives onClick messages.
     */
    interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    MovieAdapter(ListItemClickListener listener) {
        mOnClickListener = listener;
    }

    /**
     * Cache of the children views for a movie list item.
     */
    class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final ImageView mMovieImgView;

        MovieAdapterViewHolder(View view) {
            super(view);
            mMovieImgView = (ImageView) view.findViewById(R.id.moveImgView);
            view.setOnClickListener(this);
        }

        /**
         * Called whenever a user clicks on an item in the list.
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            mOnClickListener.onListItemClick(getAdapterPosition());
        }
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item you can use this viewType
     *                  integer to provide a different layout.
     * @return  A new MovieAdapterViewHolder that holds the View for each list item
     */
    @Override
    public MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        mContext = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new MovieAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the movie
     * poster and title for this particular position, using the "position" argument.
     *
     * @param movieAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(final MovieAdapterViewHolder movieAdapterViewHolder, final int position) {

        final Movie movie = mMoviesData.get(position);

        if (!offline) {
            String url = mContext.getResources().getString(R.string.picasso_url) + mContext.getResources().getString(R.string.poster_width) + movie.getPosterId();

            Picasso.with(mContext).load(url).into(movieAdapterViewHolder.mMovieImgView,
                    new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            // if we aren't displaying favorite movies but popular or top rated from the server
                            // it's time to insert every record in a sqlite table for offline consultation
                            if (!activeUri.equals(MovieContract.MovieEntry.CONTENT_URI_FAVORITE)) {
                                // generate a byte array for store the bitmap into the movie object
                                Bitmap bitmap = ((BitmapDrawable) movieAdapterViewHolder.mMovieImgView.getDrawable()).getBitmap();
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                movie.setPosterImg(baos.toByteArray());

                                // we create the intent
                                Intent storeMovieIntent = new Intent(mContext, StoreMoviesIntentService.class);

                                // define which kind of action is required
                                if (activeUri.equals(MovieContract.MovieEntry.CONTENT_URI_POPULAR))
                                    storeMovieIntent.setAction(StoreMoviesIntentService.ACTION_INSERT_INTO_POPULAR);
                                else if (activeUri.equals(MovieContract.MovieEntry.CONTENT_URI_TOPRATED))
                                    storeMovieIntent.setAction(StoreMoviesIntentService.ACTION_INSERT_INTO_TOPRATED);

                                // we prepare the data for the intent
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("movie", movie);
                                storeMovieIntent.putExtras(bundle);

                                // and finally we start the intent service
                                mContext.startService(storeMovieIntent);
                            }
                        }

                        @Override
                        public void onError() {

                        }
                    });
        }
        else {
            Bitmap bitmap = BitmapFactory.decodeByteArray(movie.getPosterImg(), 0, movie.getPosterImg().length);
            movieAdapterViewHolder.mMovieImgView.setImageBitmap(bitmap);
        }
    }

    /**
     * This method simply returns the number of items to display.
     *
     * @return The number of items available in our arraylist
     */
    @Override
    public int getItemCount() {
        if (null == mMoviesData) return 0;
        return mMoviesData.size();
    }

    /**
     * This method is used to set the movie data on a MovieAdapter if we've already
     * created one.
     *
     * @param moviesData The new movie data to be displayed.
     * @param activeUri The Uri where insert record for offline consultation.
     * @param offline true if we are disconnected from internet or id we are displaying favorite movies
     */
    void setMoviesData(ArrayList<Movie> moviesData, Uri activeUri, boolean offline) {
        mMoviesData = moviesData;
        this.activeUri = activeUri;
        this.offline = offline;

        notifyDataSetChanged();
    }

    /*
    * This method is used to get the movie data at the specified position from the arraylist
    */
    Movie getMovieAtPosition(int position) {

        return mMoviesData.get(position);
    }
}