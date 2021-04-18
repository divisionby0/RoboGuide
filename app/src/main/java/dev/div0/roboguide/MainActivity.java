package dev.div0.roboguide;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import dev.div0.roboguide.socket.AppSocket;

public class MainActivity extends AppCompatActivity implements ILocationChanged, ISocketStateProvider {
    private TextToSpeech textToSpeech;

    //private String helloPhrase = "ПРИВЕТ !!! Вы запустили приложение Робо Гайд";
    private String helloPhrase = "ПРИВЕТ";
    private String ttlVolume = "0.1";

    private TextView tvEnabledGPS;
    private TextView tvStatusGPS;
    private TextView tvLocationGPS;
    private TextView tvEnabledNet;
    private TextView tvStatusNet;
    private TextView tvLocationNet;

    private TextView socketStateInfo;

    private LocationManager locationManager;
    StringBuilder sbGPS = new StringBuilder();
    StringBuilder sbNet = new StringBuilder();

    private AppLocationListener locationListener;

    private boolean ttsReady = false;
    private String currentPhrase = "";
    private String userId = "AndroidUser_0";
    private String tag = "";

     // TODO Get Device Location - [Android Google Maps Course] https://www.youtube.com/watch?v=fPFr0So1LmI

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tag = this.getClass().getSimpleName();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // LOCATION
        tvEnabledGPS = (TextView) findViewById(R.id.tvEnabledGPS);
        tvStatusGPS = (TextView) findViewById(R.id.tvStatusGPS);
        tvLocationGPS = (TextView) findViewById(R.id.tvLocationGPS);
        tvEnabledNet = (TextView) findViewById(R.id.tvEnabledNet);
        tvStatusNet = (TextView) findViewById(R.id.tvStatusNet);
        tvLocationNet = (TextView) findViewById(R.id.tvLocationNet);

        socketStateInfo = (TextView) findViewById(R.id.socketStateInfo);

        createSocket();

        // TTS
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.e(tag, "TextToSpeech.OnInitListener.onInit...");
                setTextToSpeechLanguage();
                if (currentPhrase != "") {
                    speakOut(currentPhrase);
                } else {
                    speakOut(helloPhrase);
                }
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        Log.d(tag, "gpsEnabled=" + gpsEnabled);

        if (!gpsEnabled) {
            enableLocationSettings();
        } else {
            createLocationListener();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            //return;
        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 10, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, locationListener);
        }
        //checkEnabled();
    }

    @Override
    public void onPause() {
        /*
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        */
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    public void onClickLocationSettings(View view) {
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    };

    private void createLocationListener(){
        Log.d(tag, "createLocationListener()");
        locationListener = new AppLocationListener(this);
    }

    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    private void showLocation(Location location) {
        if (location == null){
            return;
        }

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            tvLocationGPS.setText(formatLocation(location));
        } else if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            tvLocationNet.setText(formatLocation(location));
        }
    }

    private String formatLocation(Location location) {
        if (location == null){
            return "";
        }
        //speakOut(phrase);

        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));
    }

    private void checkEnabled() {
        tvEnabledGPS.setText("Enabled: " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        tvEnabledNet.setText("Enabled: " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    private void speakOut(String phrase) {
        if (!ttsReady) {
            Toast.makeText(this, "Text to Speech not ready", Toast.LENGTH_LONG).show();
            return;
        }
        // Text to Speak
        String utteranceId = UUID.randomUUID().toString();
        Bundle params = new Bundle();
        params.putString(TextToSpeech.Engine.KEY_PARAM_VOLUME, ttlVolume);
        textToSpeech.speak(phrase, TextToSpeech.QUEUE_FLUSH, params, utteranceId);
    }

    private void setTextToSpeechLanguage() {
        Locale language = this.getUserSelectedLanguage();
        if (language == null) {
            this.ttsReady = false;
            Toast.makeText(this, "Not language selected", Toast.LENGTH_SHORT).show();
            return;
        }
        int result = textToSpeech.setLanguage(language);
        if (result == TextToSpeech.LANG_MISSING_DATA) {
            this.ttsReady = false;
            Toast.makeText(this, "Missing language data", Toast.LENGTH_SHORT).show();
            return;
        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            this.ttsReady = false;
            Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
            return;
        } else {
            this.ttsReady = true;
            Locale currentLanguage = textToSpeech.getVoice().getLocale();
            Toast.makeText(this, "Language " + currentLanguage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(tag, "onLocationChanged location="+location);
        showLocation(location);

        /*
        Number lat = location.getLatitude();
        Number lon = location.getLongitude();

        currentPhrase = "Координаты изменились. Широта "+ String.valueOf(lat);
        currentPhrase += " Долгота "+ String.valueOf(lon);

        if(ttsReady){
            speakOut(currentPhrase);
        }
        */
    }

    private Locale getUserSelectedLanguage() {
        return new Locale("ru","RU");
    }

    private void createSocket(){

        final MainActivity that = this;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AppSocket(userId, that);
            }
        });
    }

    @Override
    public void onSocketConnected() {
        Log.d(tag, "socket connected");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                socketStateInfo.setText("Socket connected");
            }
        });
    }

    @Override
    public void onSocketDisconnected() {
        Log.d(tag, "socket disconnected");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                socketStateInfo.setText("Socket disconnected");
            }
        });
    }

    @Override
    public void onSocketConnectError(final String error) {
        Log.d(tag, "socket error: "+error);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                socketStateInfo.setText(error);
            }
        });
    }
}