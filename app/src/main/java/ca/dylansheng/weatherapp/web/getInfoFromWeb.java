package ca.dylansheng.weatherapp.web;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import ca.dylansheng.weatherapp.cityInfo.cityInfo;
import ca.dylansheng.weatherapp.cityInfo.cityInfoOpenWeatherForecast;

/**
 * Created by sheng on 2016/11/10.
 */

public class getInfoFromWeb {
    public cityInfo city = new cityInfo();
    public getInfoFromWeb(String cityName){
        city.cityName = cityName;
    }
    public cityInfo generateCityInfo() throws Exception {
        getOpenWeather();
        getCityImage();
        getInfoTimezone();
        getOpenWeatherForecast();
        return city;
    }
    public void getOpenWeather() throws Exception{
        String key = "3c8b1e15683ae662889c1ed4a06ab1e6";
        String url_Name = "http://api.openweathermap.org/data/2.5/weather?q=" + city.cityName + "&APPID=" + key;
        URL openWeather = new URL(url_Name);
        URLConnection yc = openWeather.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        String inputLine;
        String str = new String();
        while ((inputLine = in.readLine()) != null)
        {
            str = str.concat(inputLine);
        }
        JSONObject obj = new JSONObject(str);

        city.cityInfoOpenWeather.latitude = Double.parseDouble(obj.getJSONObject("coord").getString("lat"));
        city.cityInfoOpenWeather.longitude = Double.parseDouble(obj.getJSONObject("coord").getString("lon"));

        city.cityInfoOpenWeather.weatherId = obj.getJSONArray("weather").getJSONObject(0).getString("id");
        city.cityInfoOpenWeather.condition = obj.getJSONArray("weather").getJSONObject(0).getString("main");
        city.cityInfoOpenWeather.description = obj.getJSONArray("weather").getJSONObject(0).getString("description");
        city.cityInfoOpenWeather.icon = obj.getJSONArray("weather").getJSONObject(0).getString("icon");

        city.cityInfoOpenWeather.temperature = obj.getJSONObject("main").getInt("temp") - 273;
        city.cityInfoOpenWeather.pressure = obj.getJSONObject("main").getInt("pressure");
        city.cityInfoOpenWeather.humidity = obj.getJSONObject("main").getInt("humidity");
        city.cityInfoOpenWeather.temperatureMin = String.format("%.2f", (obj.getJSONObject("main").getDouble("temp_min") - 273.15));
        city.cityInfoOpenWeather.temperatureMax = String.format("%.2f", (obj.getJSONObject("main").getDouble("temp_max") - 273.15));

        city.cityInfoOpenWeather.windSpeed = obj.getJSONObject("wind").getString("speed");
        //city.cityInfoOpenWeather.windDeg = obj.getJSONObject("wind").getString("deg");
        city.cityInfoOpenWeather.cloudiness = obj.getJSONObject("clouds").getString("all");
        in.close();
    }

    public void getOpenWeatherForecast() throws Exception{
        String key = "3c8b1e15683ae662889c1ed4a06ab1e6";
        String url_Name = "http://api.openweathermap.org/data/2.5/forecast?q=" + city.cityName + "&APPID=" + key;
        URL openWeather = new URL(url_Name);
        URLConnection yc = openWeather.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        String inputLine;
        String str = new String();
        while ((inputLine = in.readLine()) != null)
        {
            str = str.concat(inputLine);
        }
        JSONObject obj = new JSONObject(str);

        ArrayList<cityInfoOpenWeatherForecast> cityInfoOpenWeatherForecastArrayList = new ArrayList<>();

        JSONArray jsonArray = obj.getJSONArray("list");
        for(int i = 0; i < jsonArray.length(); ++i){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            cityInfoOpenWeatherForecast cityInfoOpenWeatherForecast = new cityInfoOpenWeatherForecast();
            cityInfoOpenWeatherForecast.dt = jsonObject.getLong("dt");
            cityInfoOpenWeatherForecast.temperature = jsonObject.getJSONObject("main").getDouble("temp");
            cityInfoOpenWeatherForecast.temperatureMin = String.format("%.2f",jsonObject.getJSONObject("main").getDouble("temp_min")- 273.15);
            cityInfoOpenWeatherForecast.temperatureMax = String.format("%.2f",jsonObject.getJSONObject("main").getDouble("temp_max")- 273.15);
            cityInfoOpenWeatherForecast.icon = jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon");
            cityInfoOpenWeatherForecast.weatherId = jsonObject.getJSONArray("weather").getJSONObject(0).getString("id");
            cityInfoOpenWeatherForecastArrayList.add(cityInfoOpenWeatherForecast);
        }
        city.cityInfoOpenWeatherForecastArrayList = cityInfoOpenWeatherForecastArrayList;
        in.close();
    }

    public void getCityImage() throws Exception{
        String googleKey = "AIzaSyBfG7eMBFRS8IfO3evj9DxTb3p35d9YYL8";
        String urlPlaceSearch = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" + "location=" + city.cityInfoOpenWeather.latitude + "," + city.cityInfoOpenWeather.longitude + "&key=" + googleKey + "&radius=500";
        URL openPlaceSearch = new URL(urlPlaceSearch);
        URLConnection yc = openPlaceSearch.openConnection();
        BufferedReader inPlaceSearch = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        String inputLine;
        String strPlaceSearch = new String();
        while ((inputLine = inPlaceSearch.readLine()) != null)
        {
            strPlaceSearch = strPlaceSearch.concat(inputLine);
        }

        JSONObject objPlaceSearch = new JSONObject(strPlaceSearch);
        JSONArray arrayPlaceSearch = objPlaceSearch.getJSONArray("results");
        JSONObject objArrayPlaceSearch = arrayPlaceSearch.getJSONObject(0);
        String photo_reference = objArrayPlaceSearch.getJSONArray("photos").getJSONObject(0).getString("photo_reference");

        inPlaceSearch.close();

        String urlCityImage = "https://maps.googleapis.com/maps/api/place/photo?" + "maxwidth=400&" + "photoreference=" + photo_reference + "&key=" + googleKey;
        URL url = new URL(urlCityImage);
        Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] cityImage = stream.toByteArray();
        city.cityInfoGoogleImage.cityImage = cityImage;
    }

    public void getInfoTimezone() throws Exception{
        String googleKey = "AIzaSyAFJSsk4C0gY1pNwYzfqfVcAnRyfpqTy4Q";
        Calendar c = Calendar.getInstance();
        Long timestamp = c.getTimeInMillis() / 1000;
        String urlPlaceSearch = "https://maps.googleapis.com/maps/api/timezone/json?" + "location=" + city.cityInfoOpenWeather.latitude + "," + city.cityInfoOpenWeather.longitude + "&key=" + googleKey + "&timestamp=" + timestamp.toString();
        URL openPlaceSearch = new URL(urlPlaceSearch);
        URLConnection yc = openPlaceSearch.openConnection();
        BufferedReader inPlaceSearch = new BufferedReader(new InputStreamReader(yc.getInputStream()));
        String inputLine;
        String strPlaceSearch = new String();
        while ((inputLine = inPlaceSearch.readLine()) != null)
        {
            strPlaceSearch = strPlaceSearch.concat(inputLine);
        }

        JSONObject obj = new JSONObject(strPlaceSearch);
        city.cityInfoTimezone.timezone = Long.parseLong(obj.getString("rawOffset"));
        city.cityInfoTimezone.daylight = Long.parseLong(obj.getString("dstOffset"));

        inPlaceSearch.close();
    }
}
