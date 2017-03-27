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

import com.example.mzonno.movies.beans.Review;

import java.util.ArrayList;

/**
 * {@link ReviewsAdapter} exposes a list of movie reviews ( no click handler on this list)
 * {@link RecyclerView}
 */
class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsAdapterViewHolder> {

    private ArrayList<Review> mReviewData;

    ReviewsAdapter() {}

    /**
     * Cache of the children views for a movie list item.
     */
    class ReviewsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView tvReviewAuthor;
        final TextView tvReviewContent;

        ReviewsAdapterViewHolder(View view) {
            super(view);
            tvReviewAuthor = (TextView)view.findViewById(R.id.tv_review_author);
            tvReviewContent = (TextView)view.findViewById(R.id.tv_review_content);
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
    public ReviewsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context mContext = viewGroup.getContext();
        int layoutIdForListItem = R.layout.review_list_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);

        View view = inflater.inflate(layoutIdForListItem, viewGroup, false);
        return new ReviewsAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the movie
     * poster and title for this particular position, using the "position" argument.
     *
     * @param reviewsAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ReviewsAdapterViewHolder reviewsAdapterViewHolder, int position) {
        Review review = mReviewData.get(position);

        reviewsAdapterViewHolder.tvReviewAuthor.setText(review.getAuthor());
        reviewsAdapterViewHolder.tvReviewContent.setText(review.getContent());

        reviewsAdapterViewHolder.getBinding().executePendingBindings();

    }

    /**
     * This method simply returns the number of items to display.
     *
     * @return The number of items available in our arraylist
     */
    @Override
    public int getItemCount() {
        if (null == mReviewData) return 0;
        return mReviewData.size();
    }

    /**
     * This method is used to set the movie data on a MovieAdapter if we've already
     * created one.
     *
     * @param moviesData The new movie data to be displayed.
     */
    void setReviewData(ArrayList<Review> moviesData) {
        mReviewData = moviesData;
        notifyDataSetChanged();
    }

}