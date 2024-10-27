package com.example.travelapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap; // Google Map object (demands import of GoogleMap class)
    private Spinner citySpinner; // Spinner for selecting cities (cities are hardcoded in the spinner)
    private Marker currentMarker; // Marker for locations (demands import of Marker class)
    private TextView tempText; // TextView for displaying temperature
    private ImageView weatherIcon; // ImageView for displaying weather icon
    private Switch tempSwitch; // Switch for changing temperature units (demands import of Switch class)
    private String weatherAPIKey; // OpenWeatherMap API key
    private boolean isCelsius = true; // Boolean to check if the temperature is in Celsius or Fahrenheit

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize OpenWeatherMap API key
        weatherAPIKey = getString(R.string.open_weather_maps_key);

        // Initialize weather elements
        tempText = findViewById(R.id.tempText);
        weatherIcon = findViewById(R.id.weatherIcon);
        tempSwitch = findViewById(R.id.tempSwitch);

        // Initialize city spinner
        citySpinner = findViewById(R.id.citySpinner);
        // Request location permissions
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1002);
        }

        //switch between C and F
        tempSwitch.setChecked(true);  // False means the default is Celsius (checked)
        tempSwitch.setOnCheckedChangeListener((buttonView, isChecked)->{
            isCelsius = isChecked;
            String selectedCity = (String) citySpinner.getSelectedItem();
            fetchWeatherData(selectedCity);
        });

        // Initialize map (this demands the implementation of OnMapReadyCallback interface)
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if(mapFragment!=null){
            mapFragment.getMapAsync(this);
        }

        // Set an OnItemSelectedListener for the city spinner
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String cityName = (String) adapterView.getItemAtPosition(position);
                LatLng cityCoordination = getCityCoordination(cityName);
                updateMapLocation(cityCoordination, cityName);
                fetchWeatherData(cityName);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // This method checks if the app has the permission to post notifications
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        // Calls the method to create a notification channel as soon as the app is created
        // It only runs on Android 8.0 (API level 26) and above
        createNotificationChannel();
    } // OnCreate method ends here

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1002){ // This is the request code for location permissions
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
                enableUserLocation();
                Toast.makeText(this, "Location permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permission denied. Map functionality may be limited.", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == 1001){ // This is the request code for notification permissions
            if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Notification permission granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied. You won't receive weather updates.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // This method is called when the user grants the location permission
    private void enableUserLocation(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            mMap.setMyLocationEnabled(true);
        }
    }

    // This method is called when the map is ready for use.
    // It sets a default location and initializes a marker to show the selected city on the map.
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Set a default location and add a marker to initialize the map
        LatLng defaultLocation = getCityCoordination("New York");
        updateMapLocation(defaultLocation, "New York");
    }

    // This method is called when the user selects a city from the spinner.
    // It updates the marker on the map to show the selected city.
    private void updateMapLocation(LatLng coordinates, String cityName){
        if(currentMarker!=null){
            currentMarker.remove(); // Remove the previous marker
        }
        currentMarker = mMap.addMarker(new MarkerOptions().position(coordinates).title(cityName)); // Add a new marker
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates)); // Move the camera to the new marker
    }

    // This method returns the coordinates of the selected city.
    private LatLng getCityCoordination(String cityNames){
        switch (cityNames){
            case "New York":
                return new LatLng(40.7128, -74.0060);
            case "San Francisco":
                return new LatLng(37.7749, -122.4194);
            case "Los Angeles":
                return new LatLng(34.0522, -118.2437);
            case "Chicago":
                return new LatLng(41.8781, -87.6298);
            case "Houston":
                return new LatLng(29.7604, -95.3698);
            case "Phoenix":
                return new LatLng(33.4484, -112.0740);
            case "Montreal":
                return new LatLng(45.5017, -73.5673);
            default:
                return new LatLng(0, 0);
        }
    }

    // This method fetches the weather data for the selected city.
    // It calls WeatherTask class to make an HTTP request to the OpenWeatherMap API.
    private void fetchWeatherData(String cityName){
        new WeatherTask().execute(cityName);
    }

    // This is an inner class that extends AsyncTask, which is used to perform network operations in a background thread.
    private class WeatherTask extends AsyncTask<String,Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... params) {
            // The city name is passed as a parameter
            String cityName = params[0];
            // The temperature unit is set based on the value of isCelsius
            String unit = isCelsius? "metric":"imperial";
            // The URL is constructed using the OpenWeatherMap API + the city name + the temperature unit + the API key
            String urlString = "https://api.openweathermap.org/data/2.5/weather?q=" +cityName +"&units="+unit+"&appid="+weatherAPIKey;
            try {
                URL url = new URL(urlString); // Create a URL object
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); // Open a connection
                connection.setRequestMethod("GET"); // Set the request method
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream())); // This is used to read the response data
                StringBuilder response = new StringBuilder(); // This is used to store the response data
                String inputLine; // This is used to read the response data line by line
                while ((inputLine = in.readLine())!=null){
                    response.append(inputLine);
                } // Append the response data of each line to the StringBuilder
                in.close(); // Close the BufferedReader
                return new JSONObject(response.toString()); // Return the response data as a JSONObject
            }catch (IOException | JSONException e){
                e.printStackTrace();
            } // Catch any exceptions and print the stack trace
            return null; // Return null if there is an exception
        }

        @Override
        protected void onPostExecute(JSONObject weathedData) {
            if(weathedData !=null){
                try {
                    double temp = weathedData.getJSONObject("main").getDouble("temp");
                    String temperature = String.format("%.1f",temp)+ (isCelsius?" C":" F");
                    tempText.setText(temperature);
                    String iconCode = weathedData.getJSONArray("weather").getJSONObject(0).getString("icon");
                    String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@2x.png";
                    Picasso.get().load(iconUrl).into(weatherIcon);
                    String cityName = citySpinner.getSelectedItem().toString();
                    sendWeatherNotification(cityName, temperature);

                }catch (JSONException e){
                    e.printStackTrace();
                }
            }else {
                tempText.setText("--");
                weatherIcon.setImageDrawable(null);
            }
        }
    }

    // This method creates a notification channel for the weather updates
    // Required for Android 8.0 (API level 26) and above
    // Channels are used to group notifications with similar characteristics
    private void createNotificationChannel(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CharSequence name = "Weather Update Channel";
            String description = "Channel for weather update notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT; // Set the importance level
            NotificationChannel channel = new NotificationChannel("weatherUpdate", name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    // This method sends a weather notification to the user when new weather data is fetched and displayed in the app
    private void sendWeatherNotification(String cityName, String temp){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Request the POST_NOTIFICATIONS permission if it is not granted
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1003);
                return; // Returns either way
                // The result will be handled in onRequestPermissionsResult, which is called when the user responds to the permission request
                // If the user grants the permission, onRequestPermissionsResult will call this method again
                // When permission is granted the first if statement will be true, so this part will be skipped and the notification will be created
            }
        }
        // Create a notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "weather_update_channel")
                .setSmallIcon(R.drawable.ic_weather_notification)
                .setContentTitle("Weather Update")
                .setContentText("Current temperature in " + cityName + ": " + temp)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        // Send the notification using the NotificationManagerCompat
        // The notification ID is set to 1001, which can be used to update or remove the notification later
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(MainActivity.this);
        notificationManager.notify(1001, builder.build());
    }
}