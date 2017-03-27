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
package com.example.mzonno.movies.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;


/**
 * These utilities will be used to communicate with the moviedb servers.
 */
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();                   // class name to display in log message

    public static final int SORT_BY_POPULAR = 0;
    public static final int SORT_BY_RATED   = 1;
    public static final int SHOW_FAVORITE   = 2;
    public static final int GET_DETAILS     = 3;
    public static final int GET_TRAILERS    = 4;
    public static final int GET_REVIEW      = 5;

    private static final String MovieDbRequest = "http://api.themoviedb.org/3/movie/";      // url of moviedb server
    private static final String QUERY_PARAM = "api_key";                                    // query parameter


    /**
     * Builds the URL used to talk to the movie server using a private key and language id
     *
     * @param op Operation code.
     * @param movieId  identifier of the movie upon server
     * @param key MovieDb api key.
     * @return The URL to use to query the moviedb server.
     */
    public static URL buildMovieRequestUrl(int op, String movieId, String key, String langcode) {
        Uri builtUri;

        switch (op) {
            case SORT_BY_POPULAR:
                builtUri = Uri.parse(MovieDbRequest+"popular").buildUpon().appendQueryParameter(QUERY_PARAM, key).appendQueryParameter("language", langcode).build();
                break;
            case SORT_BY_RATED:
                builtUri = Uri.parse(MovieDbRequest+"top_rated").buildUpon().appendQueryParameter(QUERY_PARAM, key).appendQueryParameter("language", langcode).build();
                break;
            case GET_DETAILS:
                builtUri = Uri.parse(MovieDbRequest+movieId).buildUpon().appendQueryParameter(QUERY_PARAM, key).appendQueryParameter("language", langcode).build();
                break;
            case GET_TRAILERS:
                builtUri = Uri.parse(MovieDbRequest+movieId+"/videos").buildUpon().appendQueryParameter(QUERY_PARAM, key).appendQueryParameter("language", langcode).build();
                break;
            case GET_REVIEW:
                builtUri = Uri.parse(MovieDbRequest+movieId+"/reviews").buildUpon().appendQueryParameter(QUERY_PARAM, key).appendQueryParameter("language", langcode).build();
                break;
            default:
                return null;
        }

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.v(TAG, "Built URI " + url);

        return url;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {

            // set the connection timeout to 5 seconds and the read timeout to 10 seconds
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(10000);

            // get a stream to read data from
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    //============================================================================================
    // Check if an internet connection is available
    //============================================================================================
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }


}