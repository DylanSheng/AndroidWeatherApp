package ca.dylansheng.weatherapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.concurrent.TimeUnit;
import ca.dylansheng.weatherapp.R;
import ca.dylansheng.weatherapp.cityInfo.cityInfo;
import ca.dylansheng.weatherapp.db.MyDatabaseHelper;
import ca.dylansheng.weatherapp.web.getInfoFromWeb;

/**
 * Created by sheng on 2016/10/10.
 */

public class getInfoActivity extends Activity implements View.OnClickListener {
    /* define interface parameters */
    private TextView getInfoActivityTextViewCityName;
    private TextView getInfoActivityTextViewTemp;
    private Button getInfoActivityButtonBackMain;
    private ImageView getInfoActivityImageViewCityImage;
    private TextView getInfoActivityTextViewCondition;

    /* define variables and dbs */
    //private cityInfo city = new cityInfo();
    private MyDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_info_activity);

        /* init layout and db */
        getInfoActivityTextViewCityName = (TextView) findViewById(R.id.getInfoActivityTextViewCityName);
        getInfoActivityTextViewTemp = (TextView) findViewById(R.id.getInfoActivityTextViewTemp);
        getInfoActivityButtonBackMain = (Button) findViewById(R.id.getInfoActivityButtonBackMain);
        getInfoActivityButtonBackMain.setOnClickListener(this);
        getInfoActivityImageViewCityImage = (ImageView) findViewById(R.id.getInfoActivityImageViewCityImage);
        getInfoActivityTextViewCondition = (TextView) findViewById(R.id.getInfoActivityTextViewCondition);

        dbHelper = new MyDatabaseHelper(getInfoActivity.this, "weatherDB.db", null, 1);


        String cityName = new String();
        /* check if there is any info passing by other activity, if so, get cityName*/
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            cityName = extras.getString("cityNameKey");
        }

        /* AsyncTask for network connection branch */
        /* task1 for get city longitude, latitude, temperature by OpenWeather API*/
        AsyncTask task1 = new getWeather().execute(cityName);
        try {
            task1.get(10000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getInfoActivityButtonBackMain:
                Intent intent = new Intent(getInfoActivity.this, MainActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    class getWeather extends AsyncTask<String, Void, cityInfo> {
        private Exception exception;

        protected cityInfo doInBackground(String... strings) {
            try {
                Log.d("getWeather", "getweather back");

                getInfoFromWeb getInfoFromWeb = new getInfoFromWeb(strings[0]);
                cityInfo city = getInfoFromWeb.generateCityInfo();

                return city;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(cityInfo city) {
            super.onPostExecute(city);
            Log.d("getWeather", "getweather post");

            getInfoActivity.this.getInfoActivityTextViewCityName.setText(city.cityName);
            getInfoActivity.this.getInfoActivityTextViewTemp.setText(Integer.toString(city.cityInfoOpenWeather.temperature) + "°");
            getInfoActivity.this.getInfoActivityTextViewCondition.setText(city.cityInfoOpenWeather.condition + ": " + city.cityInfoOpenWeather.description);

            Bitmap bitmap = BitmapFactory.decodeByteArray(city.cityInfoGoogleImage.cityImage, 0, city.cityInfoGoogleImage.cityImage.length);
            BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);
            getInfoActivityImageViewCityImage.setBackground(ob);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            dbHelper.buildDatabaseValue(db, city);
            dbHelper.insertCityImage(db, city.cityName, city.cityInfoGoogleImage.cityImage);
            dbHelper.insertTimezone(db, city.cityName, city.cityInfoTimezone.timezone, city.cityInfoTimezone.daylight);
        }
    }
}
