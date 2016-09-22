package miculka.jakub.meteorinfo;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Activity using google maps services, used for showing the location of meteor
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String currentName;
    private String currentInfo;
    private float currentLatitude;
    private float currentLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        currentName = intent.getStringExtra(MainActivity.CURRENT_NAME);
        currentInfo = intent.getStringExtra(MainActivity.CURRENT_INFO);
        currentLatitude = intent.getFloatExtra(MainActivity.CURRENT_LAT, 0);
        currentLongitude = intent.getFloatExtra(MainActivity.CURRENT_LONG, 0);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Meteor " + currentName);
        actionBar.setSubtitle(convertDecimalDegrees(currentLatitude, "N") + " " +
                                convertDecimalDegrees(currentLongitude, "E"));

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        // Add a marker to meteor landing place
        LatLng meteorPos = new LatLng(currentLatitude, currentLongitude);
        final Marker position = mMap.addMarker(new MarkerOptions()
                .position(meteorPos)
                .title(currentName)
                .snippet(currentInfo));

        //Move/zoom camera to our place and show info window after work is done
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(meteorPos, 5), new GoogleMap.CancelableCallback(){
            @Override
            public void onFinish() {
                position.showInfoWindow();
            }

            @Override
            public void onCancel() {}
        });
    }

    private String convertDecimalDegrees(float dd, String direction) {
        int d = (int)dd;
        float t1 = (dd - d) * 60;
        int m = (int)t1;
        float s = (t1 - m) * 60;

        return d + "°" + m + "\'" + String.format("%.02f", s) + "\"" + direction;
    }
}
