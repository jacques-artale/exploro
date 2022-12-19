package com.example.exploro.controllers;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;

import com.example.exploro.BuildConfig;
import com.example.exploro.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.example.exploro.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MapsActivityController extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private String URL = "";

    private static Marker userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Build the url with all destinations
        String[] dsts = getIntent().getExtras().getStringArray("destinationList");
        buildRoute(dsts);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.miniMap);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Send a request to google directions api for the route
        Response response = sendRequest(this.URL);

        // Display the route on the map
        double start_lat = 0.0;
        double start_lng = 0.0;
        try {
            JSONObject jsonResponse = new JSONObject(response.body().string());
            JSONArray routesArray = jsonResponse.getJSONArray("routes");
            JSONObject route = routesArray.getJSONObject(0);
            JSONObject overview_polyline = route.getJSONObject("overview_polyline");
            String encodedString = overview_polyline.getString("points");

            start_lat = route.getJSONArray("legs").getJSONObject(0).getJSONObject("start_location").getDouble("lat");
            start_lng = route.getJSONArray("legs").getJSONObject(0).getJSONObject("start_location").getDouble("lng");

            List<LatLng> list = PolyUtil.decode(encodedString);

            PolylineOptions lineOptions = new PolylineOptions();
            lineOptions.addAll(list);
            lineOptions.width(10);
            lineOptions.color(Color.rgb(241, 131, 131));
            mMap.addPolyline(lineOptions);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (MyLocationListener.currentLocation != null) {
            // add users location
            userLocation = googleMap.addMarker(MyLocationListener.userLocationMarker);
            // move the camera
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MyLocationListener.currentLocation, 15));
        }
    }

// "https://maps.googleapis.com/maps/api/directions/json?origin=Toronto&destination=Montreal&key=AIzaSyBUhyD3CQzp538kladlXAK1dBuZXduTjvs";
// "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=seattle&inputtype=textquery&fields=formatted_address%2Cgeometry&key=AIzaSyBUhyD3CQzp538kladlXAK1dBuZXduTjvs";

    public void buildRoute(String[] destinations) {
        this.URL = "https://maps.googleapis.com/maps/api/directions/json?origin=";
        this.URL += destinations[0];
        this.URL += "&destination=" + destinations[destinations.length-1];
        if (destinations.length > 2) {
            this.URL += "&waypoints=";
            for (int i = 1; i < destinations.length-1; i++) {
                this.URL += "via:" + destinations[i];
                if (i != destinations.length-2) this.URL += "|";
            }
        }
        this.URL += "&key=" + BuildConfig.MAPS_API_KEY;
    }

    public String buildPlace(String place) {
        String query = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=";
        query += place;
        query += "&inputtype=textquery&fields=formatted_address%2Cgeometry&key=AIzaSyBUhyD3CQzp538kladlXAK1dBuZXduTjvs";
        return query;
    }

    public Response sendRequest(String url) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    // Updates the position of the marker
    public static void updateUserLocation(LatLng newPos) {
        if (userLocation != null) {
            userLocation.setPosition(newPos);
        }
    }

}