package com.bowen.hw9real;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Created by Bowen Cui on 11/24/2017.
 */

public class FragmentThree extends Fragment{
    ListView newsList;
    SharedPreferences preferenceThree;
    String inputSymbol;
    String[] titleList = new String[5];
    String[] urlList = new String[5];
    String[] dateList = new String[5];
    String[] authorList = new String[5];
    NewsAdapter newsAdapter;
    final String url1 = "http://cs571-env.us-east-2.elasticbeanstalk.com/?symbol=";
    final String url2 = "&news=true";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView =inflater.inflate(R.layout.fragment_three,container,false);
        newsList = (ListView)rootView.findViewById(R.id.newList);
        preferenceThree = getActivity().getSharedPreferences("MySavedData", Context.MODE_PRIVATE);
        inputSymbol = preferenceThree.getString("symbolName","Not found");
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());
        JsonObjectRequest arrayReq = new JsonObjectRequest(Request.Method.GET,url1+inputSymbol+url2,null,
                new Response.Listener<JSONObject>() {

                    // Takes the response from the JSON request
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject responseObj = new JSONObject(response.toString());
                            for(int i = 0; i<5; i++){
                                String keyName = "news"+i;
                                if(responseObj.has(keyName)){
                                    JSONObject newsResult=responseObj.getJSONObject(keyName);
                                    String title = newsResult.getString("title");
                                    String url = newsResult.getString("link");
                                    String date = newsResult.getString("date");
                                    date = "Date: "+date.substring(0,26)+" EDT";
                                    String author = newsResult.getString("author");
                                    author = "Author: "+author;
                                    titleList[i] = title;
                                    urlList[i] = url;
                                    dateList[i] = date;
                                    authorList[i] = author;
                                }
                            }

                            newsAdapter = new NewsAdapter();
                            newsList.setAdapter(newsAdapter);




                            //bar2.setVisibility(View.INVISIBLE);
                        }
                        // Try and catch are included to handle any errors due to JSON
                        catch (JSONException e) {
                            // If an error occurs, this prints the error to the log
                            e.printStackTrace();
                            //bar.setVisibility(View.INVISIBLE);
                        }
                    }
                },
                // The final parameter overrides the method onErrorResponse() and passes VolleyError
                //as a parameter
                new Response.ErrorListener() {
                    @Override
                    // Handles errors that occur due to Volley
                    public void onErrorResponse(VolleyError error) {

                        Log.e("Volley", "Error");
                    }
                }
        );
        queue.add(arrayReq);
        newsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = Uri.parse(urlList[position]);
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
    class NewsAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            view = getLayoutInflater().inflate(R.layout.news_list,null);
            TextView titleView = (TextView) view.findViewById(R.id.titleText);
            TextView authorView = (TextView) view.findViewById(R.id.authorText);
            TextView dateView = (TextView)view.findViewById(R.id.dateText);
            titleView.setText(titleList[position]);
            authorView.setText(authorList[position]);
            dateView.setText(dateList[position]);

            return view;
        }
    }
}
