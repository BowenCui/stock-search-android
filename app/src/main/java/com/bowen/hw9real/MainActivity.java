package com.bowen.hw9real;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.database.Cursor;
import android.os.Bundle;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import android.support.v7.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    Spinner spinner1, spinner2;
    AutoCompleteTextView actv;
    ArrayAdapter<String> adapter;
    ProgressBar bar;
    Button getQuote,clear;
    ImageButton refresh;
    SharedPreferences sharedPreferences;
    List<String> symbolList;
    List<String> priceList;
    List<String> changeList;
    ListView favorListView;
    FavorAdapter favorAdapter;
    final String AUTOURL = "http://cs571-env.us-east-2.elasticbeanstalk.com/?q=";
    final String detailUrl="http://cs571hw9-env.us-east-2.elasticbeanstalk.com/?symbol=";
    final String FAVORLIST = "favorList";
    final String PRICELIST = "priceList";
    final String CHANGELIST ="changeList";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner1 = (Spinner)findViewById(R.id.sortBy);
        spinner2 = (Spinner)findViewById(R.id.order);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,
                R.array.sort_By_Array, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.order_Array, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);
        spinner2.setAdapter(adapter2);
        sharedPreferences = getSharedPreferences("MySavedData", Context.MODE_PRIVATE);
        /**
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        */


        favorListView =(ListView) findViewById(R.id.favoriteList);
        if(countFavorite()!=0) {
            //updatePriceChange();
            symbolList = getFavorites();
            priceList = getPrice();
            changeList = getChange();
            favorAdapter = new FavorAdapter();
            favorListView.setAdapter(favorAdapter);
        }
        favorListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String inputString = symbolList.get(position);

                sharedPreferences = getSharedPreferences("MySavedData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();


                editor.putString("symbolName",inputString);
                editor.apply();
                startActivity(new Intent(MainActivity.this,Main2Activity.class));
            }
        });
        actv = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        bar.setVisibility(View.INVISIBLE);

        getQuote = (Button)findViewById(R.id.getQuote);
        clear = (Button)findViewById(R.id.clear);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actv.setText("");
            }
        });
        getQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEmpty(actv)){
                    Toast toast = Toast.makeText(getApplicationContext(), "Please enter a stock name or symbol", Toast.LENGTH_SHORT);
                    View view = toast.getView();

                    //view.setBackgroundColor(Color.parseColor("#842f2c2c"));
                    toast.show();
                }
                else {

                    String input = actv.getText().toString();
                    Toast.makeText(getApplicationContext(),input,Toast.LENGTH_LONG);
                    Pattern p = Pattern.compile("^\\S*");
                    Matcher m = p.matcher(input);

                    if(m.find()){
                        String result = m.group(0).toString().toUpperCase();
                        sharedPreferences = getSharedPreferences("MySavedData", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        /**
                        sharedPreferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                            @Override
                            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                            }
                        });
                         */

                        editor.putString("symbolName",result);
                        editor.apply();
                    }
                    else {
                        Toast.makeText(MainActivity.this,"Wrong input",Toast.LENGTH_LONG);
                    }

                    startActivity(new Intent(MainActivity.this,Main2Activity.class));
                }
            }
        });
        refresh = (ImageButton)findViewById(R.id.fresh);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(countFavorite()!=0) {
                    //updatePriceChange();
                    symbolList = getFavorites();
                    priceList = getPrice();
                    changeList = getChange();
                    favorAdapter = new FavorAdapter();
                    favorListView.setAdapter(favorAdapter);
                }
            }
        });

        //Toast.makeText(this, symbol_auto, Toast.LENGTH_LONG).show();
        //RequestQueue queue = Volley.newRequestQueue(this);
        actv.addTextChangedListener(new TextWatcher() {
                                        @Override
                                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                        }

                                        @Override
                                        public void onTextChanged(CharSequence s, int start, int before, int count) {
                                           //actv.getText()
                                            if(!isEmpty(actv)&& actv.getText().toString().trim().length()<6){
                                                bar.setVisibility(View.VISIBLE);
                                                RequestQueue queue = Volley.newRequestQueue(getApplication().getApplicationContext());
                                                String symbol_auto = String.valueOf(actv.getText());

                                                JsonArrayRequest arrayReq = new JsonArrayRequest(AUTOURL +symbol_auto,
                                                        new Response.Listener<JSONArray>() {

                                                            // Takes the response from the JSON request
                                                            @Override
                                                            public void onResponse(JSONArray response) {
                                                                try {
                                                                    List<String> mylist = new ArrayList<>();

                                                                    for(int i = 0;i < response.length();i++) {
                                                                        if(i <= 4) {
                                                                            JSONObject jsonObj = response.getJSONObject(i);
                                                                            String symbol = jsonObj.getString("Symbol");
                                                                            String name = jsonObj.getString("Name");
                                                                            String exchange = jsonObj.getString("Exchange");
                                                                            String autoResult = symbol + " - " + name + "(" + exchange + ")";
                                                                            mylist.add(autoResult);
                                                                            String[] completeData = new String[mylist.size()];
                                                                            completeData = mylist.toArray(completeData);

                                                                            Log.i("here", mylist.toString());
                                                                            //Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
                                                                        }
                                                                        else break;
                                                                    }
                                                                    adapter = new ArrayAdapter<String>
                                                                            (MainActivity.this,android.R.layout.select_dialog_item,mylist);
                                                                    //AutoCompleteTextView actv= (AutoCompleteTextView)findViewById(R.id.autoCompleteTextView);
                                                                    //actv.setThreshold(1);//will start working from first character
                                                                    Toast.makeText(getApplicationContext(),"call", Toast.LENGTH_SHORT);
                                                                    actv.setAdapter(adapter);
                                                                    actv.showDropDown();
                                                                    bar.setVisibility(View.INVISIBLE);
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

                                            }
                                            else {
                                                actv.setAdapter(null);
                                                bar.setVisibility(View.INVISIBLE);
                                            }

                                        }

                                        @Override
                                        public void afterTextChanged(Editable s) {

                                        }
                                    }

        );




    }

    public void saveFavorites( List<String> favorites) {



        SharedPreferences preference = getSharedPreferences("MySavedData",
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

    public ArrayList<String> getFavorites() {

        List<String> favorList;

        SharedPreferences preference = getSharedPreferences("MySavedData",
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
    public int countFavorite(){
        List<String> favorList =   getFavorites();
        if(favorList != null){
            return favorList.size();
        }
        else return 0;
    }
    public void savePrice( List<String> priceList) {



        SharedPreferences preference =  getSharedPreferences("MySavedData",
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

        SharedPreferences preference = getSharedPreferences("MySavedData",
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



        SharedPreferences preference = getSharedPreferences("MySavedData",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preference.edit();

        Gson gson = new Gson();
        String jsonFavorites = gson.toJson(favorites);

        editor.putString(PRICELIST, jsonFavorites);

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

        SharedPreferences preference = getSharedPreferences("MySavedData",
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

    public void updatePriceChange(){
        if(symbolList != null && symbolList.size()!=0){
            int size = symbolList.size();
            priceList = new ArrayList<>();
            changeList = new ArrayList<>();
            RequestQueue queue = Volley.newRequestQueue(getApplication().getApplicationContext());
            for (int i = 0; i < size; i++){
                String inputSymbol = symbolList.get(i);



                JsonObjectRequest arrayReq = new JsonObjectRequest(Request.Method.GET,detailUrl+inputSymbol,null,
                        new Response.Listener<JSONObject>() {

                            // Takes the response from the JSON request
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONObject responseObj = new JSONObject(response.toString());
                                    JSONObject series = responseObj.getJSONObject("Time Series (Daily)");

                                    Iterator<String> iter = series.keys();
                                    String firstKey= iter.next();
                                    String secondKey = iter.next();



                                    JSONObject firstDay = series.getJSONObject(firstKey);
                                    JSONObject secondDay = series.getJSONObject(secondKey);
                                    String firstClose = firstDay.getString("4. close");
                                    String secondClose = secondDay.getString("4. close");
                                    float firstFloat = Float.parseFloat(firstClose);
                                    String lastPrice =  String.format("%.02f", firstFloat);
                                    float secondFloat = Float.parseFloat(secondClose);
                                    float change = firstFloat-secondFloat;

                                    float changePercent = (firstFloat-secondFloat)/secondFloat*100;
                                    String changeString = String.format("%.02f", change);
                                    String percentString = String.format("%.02f", changePercent);
                                    String changeStringResult = changeString+" ("+percentString+"%)";


                                    priceList.add(lastPrice) ;
                                    changeList.add(changeStringResult);


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
            }

        }
    }

    private boolean isEmpty(EditText myeditText) {
        return myeditText.getText().toString().trim().length() == 0;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    class FavorAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return countFavorite();
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
            view = getLayoutInflater().inflate(R.layout.favorite_list,null);
            TextView symbolName = (TextView) view.findViewById(R.id.favorSymbol);
            TextView price = (TextView) view.findViewById(R.id.favorPrice);
            TextView change = (TextView)view.findViewById(R.id.favorChange);
            symbolName.setText(symbolList.get(position));
            price.setText(priceList.get(position));
            change.setText(changeList.get(position));

            return view;
        }
    }
}
