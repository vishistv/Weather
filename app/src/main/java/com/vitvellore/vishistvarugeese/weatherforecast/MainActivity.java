package com.vitvellore.vishistvarugeese.weatherforecast;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ArrayList<Weather> weatherArrayList = new ArrayList<>();

    private ArrayList<Integer> criticalSunny = new ArrayList<>();
    private ArrayList<Integer> criticalSnow = new ArrayList<>();

    private ArrayList<Integer> order = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        updateStatusBarColor("#FFC700");

        initSunny();
        initSnow();

        URL weatherUrl = NetworkUtils.buildUrlForWeather();
        new FetchWeatherDetails().execute(weatherUrl);


    }

    private void initSunny(){
        criticalSunny.add(1);
        criticalSunny.add(2);
        criticalSunny.add(3);
        criticalSunny.add(30);
    }

    private void initSnow(){
        criticalSnow.add(22);
        criticalSnow.add(23);
        criticalSnow.add(29);
        criticalSnow.add(44);
    }



    private class FetchWeatherDetails extends AsyncTask<URL, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(URL... urls) {
            URL weatherUrl = urls[0];
            String weatherSearchResults = null;

            try {
                weatherSearchResults = NetworkUtils.getResponseFromHttpUrl(weatherUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "doInBackground: weatherSearchResults: " + weatherSearchResults);
            return weatherSearchResults;
        }

        @Override
        protected void onPostExecute(String weatherSearchResults) {
            if(weatherSearchResults != null && !weatherSearchResults.equals("")) {
                weatherArrayList = parseJSON(weatherSearchResults);
                //Just for testing
                Iterator itr = weatherArrayList.iterator();
                while(itr.hasNext()) {
                    Weather weatherInIterator = (Weather) itr.next();
                    Log.i(TAG, "onPostExecute: Date: " + weatherInIterator.getDate()+
                            " Min: " + weatherInIterator.getMinTemp() +
                            " Max: " + weatherInIterator.getMaxTemp());
                }
            }
            ImageView ivIcon = findViewById(R.id.icon);
            ivIcon.setVisibility(View.GONE);
            ViewPager viewPager = findViewById(R.id.view_pager);
            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
            viewPager.setVisibility(View.VISIBLE);

            for(int i=0;i<order.size(); i++){

                if(order.get(i) == 0) {
                    MorningFragment mf = new MorningFragment();
                    mf.passData(weatherArrayList.get(i), i);
                    adapter.addFragment(mf);
                }
                else if(order.get(i) == 1) {
                    NightFragment nf = new NightFragment();
                    nf.passData(weatherArrayList.get(i), i);
                    adapter.addFragment(nf);
                }
                else if(order.get(i) == 2) {
                    SnowyFragment snf = new SnowyFragment();
                    snf.passData(weatherArrayList.get(i), i);
                    adapter.addFragment(snf);
                }
                else {
                    SunnyFragment suf = new SunnyFragment();
                    suf.passData(weatherArrayList.get(i), i);
                    adapter.addFragment(suf);
                }

            }

            viewPager.setAdapter(adapter);
            super.onPostExecute(weatherSearchResults);
        }
    }

    private ArrayList<Weather> parseJSON(String weatherSearchResults) {
        if(weatherArrayList != null) {
            weatherArrayList.clear();
        }

        if(weatherSearchResults != null) {
            try {
                JSONObject rootObject = new JSONObject(weatherSearchResults);
                JSONArray results = rootObject.getJSONArray("DailyForecasts");

                for (int i = 0; i < results.length(); i++) {
                    Weather weather = new Weather();

                    JSONObject resultsObj = results.getJSONObject(i);

                    String date = resultsObj.getString("Date");
                    int l = 0;
                    String label;
                    if(i == 0) {
                        l = Time();
                        weather.setDate("Today");
                    }
                    else {
                        try {
                            label = Week(date);
                            weather.setDate(label);
                        } catch (Exception e) {};
                    }

                    JSONObject temperatureObj = resultsObj.getJSONObject("Temperature");
                    String minTemperature = temperatureObj.getJSONObject("Minimum").getString("Value");
                    weather.setMinTemp(minTemperature);

                    String maxTemperature = temperatureObj.getJSONObject("Maximum").getString("Value");
                    weather.setMaxTemp(maxTemperature);

                    JSONObject dayObj = resultsObj.getJSONObject("Day");
                    String dayPhrase = dayObj.getString("IconPhrase");
                    weather.setDayPhrase(dayPhrase);
                    String dayIcon = dayObj.getString("Icon");
                    int dI = Integer.parseInt(dayIcon);

                    if(i == 0){
                        if(criticalSnow.contains(dI)) {
                            order.add(2);
                            weather.setLabel("snowing");
                        }
                        else if(l == 2 || l == 3) {
                            order.add(1);
                            weather.setLabel("night");
                        }
                        else if(l == 1 || l == 0){
                            if(criticalSunny.contains(dI)) {
                                order.add(3);
                                weather.setLabel("sunny");
                            }
                            else {
                                order.add(0);
                                weather.setLabel("morning");
                            }
                        }
                    } else {
                        if(criticalSnow.contains(dI)){
                            order.add(2);
                            weather.setLabel("snowing");
                        } else if(criticalSunny.contains(dI)){
                            order.add(3);
                            weather.setLabel("sunny");
                        }
                        else {
                            order.add(0);
                            weather.setLabel("morning");
                        }
                    }

                    JSONObject nightObj = resultsObj.getJSONObject("Night");
                    String nightPhrase = nightObj.getString("IconPhrase");
                    weather.setNightPhrase(nightPhrase);

                    weatherArrayList.add(weather);
                }

                return weatherArrayList;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String Week(String date) throws ParseException {
        if(date!=null)
        {
            SimpleDateFormat format1=new SimpleDateFormat("yyyy-MM-dd");
            Date dt1=format1.parse(date);
            DateFormat format2=new SimpleDateFormat("EEEE");
            return format2.format(dt1);
        }
        else{
            return " ";
        }
    }

    private int Time()
    {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if(timeOfDay >= 0 && timeOfDay < 12){
            return 0;
        }else if(timeOfDay >= 12 && timeOfDay < 16){
            return 1;
        }else if(timeOfDay >= 16 && timeOfDay < 18){
            return 2;
        }else if(timeOfDay >= 19 && timeOfDay < 24){
            return 3;
        }

        return 0;
    }

    public void updateStatusBarColor(String color){// Color must be in hexadecimal fromat
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.parseColor(color));
        }
    }
}
