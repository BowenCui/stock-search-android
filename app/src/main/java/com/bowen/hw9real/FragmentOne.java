package com.bowen.hw9real;

//import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Bowen Cui on 11/24/2017.
 */

public class FragmentOne extends Fragment{
    String[] detailHeader={"Stock Symbol","Last Price","Change","Timestamp","Open","Close","Day's Range","Volume"};
    String[] detailValue= new String[8];
    final String detailUrl="http://cs571hw9-env.us-east-2.elasticbeanstalk.com/?symbol=";
    final String FAVORLIST = "favorList";
    final String PRICELIST = "priceList";
    final String CHANGELIST ="changeList";
    String lastPrice;
    String changeStringResult;
    ListView stockList;
    WebView webView;
    //ProgressBar bar2;
    SharedPreferences preference;
    String inputSymbol;

    TwoColAdapter stockListAdapter;
    ImageButton favoriteButton;
    ImageButton facebookButton;
    Spinner indiSpinner;
    Button changeButton;
    boolean changePositive = true;
    @Nullable
    @Override

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_one,container,false);

       //bar2 = rootView.findViewById(R.id.progressBar2);
        preference = getActivity().getSharedPreferences("MySavedData", Context.MODE_PRIVATE);
        inputSymbol = preference.getString("symbolName","Not found");
         stockList= (ListView)rootView.findViewById(R.id.stockList);

         /**
         stockList.setOnTouchListener(new View.OnTouchListener() {
             // Setting on Touch Listener for handling the touch inside ScrollView
             @Override
             public boolean onTouch(View v, MotionEvent event) {
                 // Disallow the touch request for parent scroll on touch of child view
                 v.getParent().requestDisallowInterceptTouchEvent(true);
                 return false;
             }
         });
             */
         detailValue[0] = inputSymbol;

        webView =(WebView) rootView.findViewById(R.id.indiWeb);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("https://")||url.startsWith("http://"))) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.addJavascriptInterface(new WebViewInterfaceOne(),"android");
        //webView.loadUrl("file:///android_asset/highstock.html");
        indiSpinner =rootView.findViewById(R.id.indicatorSpinner);
        final ArrayAdapter<CharSequence> indiAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.indicator_name, android.R.layout.simple_spinner_item);
        indiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        indiSpinner.setAdapter(indiAdapter);

        changeButton = (Button) rootView.findViewById(R.id.changeButton);
        changeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                webView.loadUrl("file:///android_asset/highchart.html");
                //String selectedIndi = indiSpinner.getSelectedItem().toString();
                //webView.loadUrl("javascript:");
            }
        });
        favoriteButton =(ImageButton) rootView.findViewById(R.id.favorButton);
        if(hasFavorite(inputSymbol)){
            favoriteButton.setBackgroundResource(R.drawable.filled);
        }
        else {
            favoriteButton.setBackgroundResource(R.drawable.empty);
        }

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hasFavorite(inputSymbol)){
                    int index = getFavoriteIndex(inputSymbol);
                   removeFavorite(inputSymbol);
                    removePrice(index);
                    removeChange(index);
                    favoriteButton.setBackgroundResource(R.drawable.empty);
                }
                else {
                   addFavorite(inputSymbol);
                   addPrice(lastPrice);
                   addChange(changeStringResult);
                    favoriteButton.setBackgroundResource(R.drawable.filled);
                }
                String getstored = preference.getString("symbolName","Not found");
                //Toast.makeText(getContext(),getstored,Toast.LENGTH_LONG).show();
            }
        });

        //bar2.setVisibility(View.VISIBLE);
        RequestQueue queue = Volley.newRequestQueue(getActivity().getApplicationContext());


        JsonObjectRequest arrayReq = new JsonObjectRequest(Request.Method.GET,detailUrl+inputSymbol,null,
                new Response.Listener<JSONObject>() {

                    // Takes the response from the JSON request
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject responseObj = new JSONObject(response.toString());
                            JSONObject series = responseObj.getJSONObject("Time Series (Daily)");
                            JSONObject metaData = responseObj.getJSONObject("Meta Data");
                            Iterator<String> iter = series.keys();
                            String firstKey= iter.next();
                            String secondKey = iter.next();



                            JSONObject firstDay = series.getJSONObject(firstKey);
                            JSONObject secondDay = series.getJSONObject(secondKey);
                            String firstClose = firstDay.getString("4. close");
                            String secondClose = secondDay.getString("4. close");
                            float firstFloat = Float.parseFloat(firstClose);
                            lastPrice =  String.format("%.02f", firstFloat);
                            float secondFloat = Float.parseFloat(secondClose);
                            float change = firstFloat-secondFloat;
                            if(change >= 0 ){
                                changePositive = true;
                            }
                            else {
                                changePositive = false;
                            }
                            float changePercent = (firstFloat-secondFloat)/secondFloat*100;
                            String changeString = String.format("%.02f", change);
                            String percentString = String.format("%.02f", changePercent);
                            changeStringResult = changeString+" ("+percentString+"%)";
                            String lastRefreshed = metaData.getString("3. Last Refreshed");
                            String timeStamp;
                            String close;
                            if(lastRefreshed.length()>10){
                                timeStamp = lastRefreshed+" EDT";
                                close = String.format("%.02f", secondFloat);
                            }
                            else {
                                timeStamp = lastRefreshed+" 16:00:00 EDT";
                                close = String.format("%.02f", firstFloat);
                            }
                            String openString = firstDay.getString("1. open");
                            float openFloat = Float.parseFloat(openString);
                            String open = String.format("%.02f", openFloat);
                            String high = firstDay.getString("2. high");
                            String low = firstDay.getString("3. low");
                            float highFloat = Float.parseFloat(high);
                            float lowFloat = Float.parseFloat(low);
                            String highTwoDeci = String.format("%.02f", highFloat);
                            String lowTwoDeci = String.format("%.02f", lowFloat);
                            String range = lowTwoDeci+" - "+ highTwoDeci;
                            String volume = firstDay.getString("5. volume");
                            detailValue[1] = lastPrice;
                            detailValue[2] = changeStringResult;
                            detailValue[3] =timeStamp;
                            detailValue[4] = open;
                            detailValue[5] = close;
                            detailValue[6] = range;
                            detailValue[7] = volume;

                            stockListAdapter = new TwoColAdapter();
                            stockList.setAdapter(stockListAdapter);




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
                        Toast.makeText(getActivity(),"Error fetching data",Toast.LENGTH_LONG).show();
                        Log.e("Volley", "Error");
                    }
                }
        );
        queue.add(arrayReq);





        return rootView;
    }
    public void saveFavorites( List<String> favorites) {



        SharedPreferences preference = getActivity().getSharedPreferences("MySavedData",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);

        editor.putString(FAVORLIST, jsonFavorites);

        editor.apply();
    }

    public void addFavorite( String symbol) {
        List<String> favorites = getFavorites();
        if (favorites == null)
            favorites = new ArrayList<String >();
        favorites.add(symbol);
        saveFavorites( favorites);
    }

    public void removeFavorite( String symbol) {
        ArrayList<String> favorites = getFavorites();
        if (favorites != null) {
            favorites.remove(symbol);
            saveFavorites(favorites);
        }
    }
    public void removeFavorite(int index) {
        ArrayList<String> favorites = getFavorites();
        if (favorites != null) {
            favorites.remove(index);
            saveFavorites(favorites);
        }
    }

    public ArrayList<String> getFavorites() {

        List<String> favorList;

        SharedPreferences preference = getActivity().getSharedPreferences("MySavedData",
                Context.MODE_PRIVATE);

        if (preference.contains(FAVORLIST)) {
            String jsonFavor = preference.getString(FAVORLIST, "not found");
            Gson gson = new Gson();
            String[] favorArray = gson.fromJson(jsonFavor,
                    String[].class);

            favorList = Arrays.asList(favorArray);
            favorList = new ArrayList<String>(favorList);
        } else {
            return null;
        }
        return (ArrayList<String>) favorList;
    }
    public boolean hasFavorite(String symbol){

        List<String> favorList =   getFavorites();
        if(favorList != null){
        return favorList.contains(symbol);
        }
        else return false;
    }
    public int getFavoriteIndex(String symbol){
        if(hasFavorite(symbol)){
            List<String> favorList =   getFavorites();
            return favorList.indexOf(symbol);
        }
        return -1;
    }

    public void savePrice( List<String> priceList) {



        SharedPreferences preference = getActivity().getSharedPreferences("MySavedData",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();

        Gson gson = new Gson();
        String jsonPrice = gson.toJson(priceList);

        editor.putString(PRICELIST, jsonPrice);

        editor.apply();
    }

    public void addPrice( String symbol) {
        List<String> price = getPrice();
        if (price == null)
            price = new ArrayList<String >();
        price.add(symbol);
        savePrice( price);
    }

    public void removePrice( String symbol) {
        ArrayList<String> price = getPrice();
        if (price != null) {
            price.remove(symbol);
            savePrice(price);
        }
    }
    public void removePrice( int index) {
        ArrayList<String> price = getPrice();
        if (price != null) {
            price.remove(index);
            savePrice(price);
        }
    }
    public ArrayList<String> getPrice() {

        List<String> priceList;

        SharedPreferences preference = getActivity().getSharedPreferences("MySavedData",
                Context.MODE_PRIVATE);

        if (preference.contains(PRICELIST)) {
            String jsonPrice = preference.getString(PRICELIST, "not found");
            Gson gson = new Gson();
            String[] priceArray = gson.fromJson(jsonPrice,
                    String[].class);

            priceList = Arrays.asList(priceArray);
            priceList = new ArrayList<String>(priceList);
        } else {
            return null;
        }
        return (ArrayList<String>) priceList;
    }
    public boolean hasPrice(String symbol){

        List<String> priceList =   getPrice();
        if(priceList != null){
            return priceList.contains(symbol);
        }
        else return false;
    }
    public int getPriceIndex(String symbol){
        if(hasPrice(symbol)){
            List<String> priceList =   getPrice();
            return priceList.indexOf(symbol);
        }
        return -1;
    }
    public void saveChange( List<String> favorites) {



        SharedPreferences preference = getActivity().getSharedPreferences("MySavedData",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);

        editor.putString(CHANGELIST, jsonFavorites);

        editor.apply();
    }

    public void addChange( String symbol) {
        List<String> favorites = getChange();
        if (favorites == null)
            favorites = new ArrayList<String >();
        favorites.add(symbol);
        saveChange( favorites);
    }

    public void removeChange( String symbol) {
        ArrayList<String> favorites = getChange();
        if (favorites != null) {
            favorites.remove(symbol);
            saveChange(favorites);
        }
    }
    public void removeChange( int index) {
        ArrayList<String> favorites = getChange();
        if (favorites != null) {
            favorites.remove(index);
            saveChange(favorites);
        }
    }

    public ArrayList<String> getChange() {

        List<String> favorList;

        SharedPreferences preference = getActivity().getSharedPreferences("MySavedData",
                Context.MODE_PRIVATE);

        if (preference.contains(CHANGELIST)) {
            String jsonFavor = preference.getString(CHANGELIST, "not found");
            Gson gson = new Gson();
            String[] favorArray = gson.fromJson(jsonFavor,
                    String[].class);

            favorList = Arrays.asList(favorArray);
            favorList = new ArrayList<String>(favorList);
        } else {
            return null;
        }
        return (ArrayList<String>) favorList;
    }
    public boolean hasChange(String symbol){

        List<String> favorList =   getChange();
        if(favorList != null){
            return favorList.contains(symbol);
        }
        else return false;
    }
    public int getChangeIndex(String symbol){
        if(hasChange(symbol)){
            List<String> favorList =   getChange();
            return favorList.indexOf(symbol);
        }
        return -1;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public class WebViewInterfaceOne {
        @JavascriptInterface
        public String getIndicator(){
            String selectedIndi = indiSpinner.getSelectedItem().toString();
            return selectedIndi;
        }
        @JavascriptInterface
        public String getSymbol(){

            return inputSymbol;
        }
    }
    class TwoColAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return detailHeader.length;
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
            view = getLayoutInflater().inflate(R.layout.stock_list,null);
            TextView header = (TextView) view.findViewById(R.id.detailHeaderText);
            TextView value = (TextView) view.findViewById(R.id.detailValueText);
            ImageView upOrDown = (ImageView) view.findViewById(R.id.arrow);
            header.setText(detailHeader[position]);
            value.setText(detailValue[position]);
            if(position == 2){
                if(!changePositive){
                    upOrDown.setImageResource(R.drawable.down);
                }
                else {
                    upOrDown.setImageResource(R.drawable.up);
                }
                upOrDown.setVisibility(view.VISIBLE);
            }
            return view;
        }
    }
}
