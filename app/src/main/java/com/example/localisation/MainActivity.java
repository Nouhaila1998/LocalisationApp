package com.example.localisation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_ID = 55;
    private double latitude;
    private double longitude;
    private double altitude;
    private float accuracy;
    RequestQueue requestQueue;
    String insertUrl = "http://192.168.1.4/Position/createPosition.php";

    private boolean checkPermissions() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED;

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ID);
    }

    // method to check
    // if location is enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Log.e("Log", "Start");
        if (checkPermissions()) {
            Log.e("Log", "Has Permissions");
            // check if location is enabled
            if (isLocationEnabled()) {

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions();
                    return;
                }
                Log.e("Log", "here 1");
                // addPosition(12345, 2580);
                LocationListener locationListener = new
                        LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                Log.e("Log", "new_location");
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                altitude = location.getAltitude();
                                accuracy = location.getAccuracy();
                                String msg = "new_location";
                                addPosition(latitude, longitude);
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onStatusChanged(String provider, int status, Bundle extras) {
                                String newStatus = "";
                                switch (status) {
                                    case LocationProvider.OUT_OF_SERVICE:
                                        newStatus = "OUT_OF_SERVICE";
                                        break;
                                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                                        newStatus = "TEMPORARILY_UNAVAILABLE";
                                        break;
                                    case LocationProvider.AVAILABLE:
                                        newStatus = "AVAILABLE";
                                        break;
                                }
                                String msg = format(getResources().getString(R.string.provider_new_status),
                                        provider, newStatus);
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onProviderEnabled(String provider) {
                                String msg = format(getResources().getString(R.string.provider_enabled),
                                        provider);
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onProviderDisabled(String provider) {
                                String msg = format(getResources().getString(R.string.provider_disabled),
                                        provider);
                                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                            }
                        };
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 1, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locationListener);
            }
        }else{
            requestPermissions();
        }
    }
    void addPosition(final double lat, final double lon) {
        StringRequest request = new StringRequest(Request.Method.POST,
                insertUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Success", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Error", error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                TelephonyManager telephonyManager =
                        (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                HashMap<String, String> params = new HashMap<String, String>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                params.put("latitude", lat + "");
                params.put("longitude", lon + "");
                params.put("date", sdf.format(new Date()) + "");
                try{
                    params.put("imei", telephonyManager.getDeviceId());
                }catch (SecurityException e){
                    params.put("imei", "ERROR-ID");
                    Log.e("Error", e.getMessage());
                }

                return params;
            }
        };
        requestQueue.add(request);
    }
}