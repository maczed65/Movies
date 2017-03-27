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
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mzonno.movies.beans.Trailer;

import java.util.ArrayList;

/**
 * {@link TrailersAdapter} exposes a list of movie trailers
 * {@link RecyclerView}
 */
class TrailersAdapter extends RecyclerView.Adapter<TrailersAdapter.TrailersAdapterViewHolder> {

    private ArrayList<Trailer> mTrailerData;
    private ListItemClickListener mOnClickListener;

    /**
     * The interface that receives onClick messages.
     */
    interface ListItemClickListener {
        void onListItemClick(int clickedItemIndex);
    }

    TrailersAdapter(ListItemClickListener listener) {
        mOnClickListener = listener;
    }

    /**
     * Cache of the children views for a movie list item.
     */
    class TrailersAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView tvTrailerName;

        TrailersAdapterViewHolder(View view) {
            super(view);
            tvTrailerName = (TextView)view.findViewById(R.id.tv_trailer_name);
            view.setOnClickListener(this);
        }
        public ViewDataBinding getBinding() {
            return DataBindingUtil.getBinding(itemView);
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
    public TrailersAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context mContext = viewGroup.getContext();
        int layoutIdForListItem = R.layout.trailer_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new TrailersAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the movie
     * poster and title for this particular position, using the "position" argument.
     *
     * @param trailersAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(TrailersAdapterViewHolder trailersAdapterViewHolder, int position) {
        Trailer trailer = mTrailerData.get(position);

        trailersAdapterViewHolder.tvTrailerName.setText(trailer.getName());
        trailersAdapterViewHolder.getBinding().executePendingBindings();
    }

    /**
     * This method simply returns the number of items to display.
     *
     * @return The number of items available in our arraylist
     */
    @Override
    public int getItemCount() {
        if (null == mTrailerData) return 0;
        return mTrailerData.size();
    }

    /**
     * This method is used to set the movie data on a MovieAdapter if we've already
     * created one.
     *
     * @param moviesData The new movie data to be displayed.
     */
    void setTrailersData(ArrayList<Trailer> moviesData) {
        mTrailerData = moviesData;
        notifyDataSetChanged();
    }

    /*
    * This method is used to get the movie data at the specified position from the arraylist
    */
    Trailer getTrailerAtPosition(int position) {
        if (mTrailerData != null)
            return mTrailerData.get(position);
        else
            return null;
    }
}