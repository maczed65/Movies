package com.example.mzonno.movies;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.mzonno.movies.beans.Movie;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by m.zonno on 22/03/17.
 */

public class MovieInfo {
    public String title;
    public Bitmap posterImg;
    public String overview;
    public String vote_average;
    public String release_date;
    public String runtime = "";

    public MovieInfo(Movie movie, Context context) {
        this.title = movie.getTitle();
        this.overview = movie.getOverview();
        this.vote_average = movie.getVote_average() + "/10";
        this.release_date = movie.getRelease_date();
        if (movie.getRuntime() != null)
            this.runtime = movie.getRuntime() + context.getResources().getString(R.string.minutes);

        this.posterImg = BitmapFactory.decodeByteArray(movie.getPosterImg(), 0, movie.getPosterImg().length);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);  // string date format from moviedb
        SimpleDateFormat sdf1 = new SimpleDateFormat(context.getResources().getString(R.string.date_format), Locale.ENGLISH);
        try {
            this.release_date = sdf1.format(sdf.parse(movie.getRelease_date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
