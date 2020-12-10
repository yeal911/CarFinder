package com.hsd.contest.spain.test.hsd;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;


    private LocationManager locManager;
    private Location loc;
    private LatLng ubicacion;
    private Marker marker;

    double latitud = 0.0;
    double longitud = 0.0;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String fechaParking="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.v("******************************Location Changed******************************", location.getLatitude() + " and " + location.getLongitude());
                latitud = location.getLatitude();
                longitud = location.getLongitude();
                ubicacion = new LatLng(latitud, longitud);
                marker = mMap.addMarker(new MarkerOptions().position(ubicacion).title("Mi ubicacion"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacion));
            }


            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }

        };

        ActivityCompat.requestPermissions(MapsActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //No hay permisos
            // Add a marker in Sydney and move the camera
             LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        }else {
            locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2, 2, locationListener);
            loc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            ubicacion = new LatLng(loc.getLatitude(), loc.getLongitude());
            mMap.addMarker(new MarkerOptions().position(ubicacion).title("Mi ubicacion"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(ubicacion));

            latitud = loc.getLatitude();
            longitud = loc.getLongitude();
        }

    }



    public void saveLocation(View view)
    {
        Toast toast = Toast.makeText(getApplicationContext(), "Guardando aparcamiento", Toast.LENGTH_SHORT);
        toast.show();
        java.util.Date fecha = new Date();
        fechaParking = fecha.toString();

        DatabaseReference mDatabase;
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference(currentUser.getUid());
        User user = new User((float) latitud, (float) longitud, fechaParking);

        mDatabase.setValue(user);
    }

    public void getParking(View view)
    {
        Toast toast = Toast.makeText(getApplicationContext(), "Obteniendo aparcamiento", Toast.LENGTH_SHORT);
        toast.show();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference(currentUser.getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User post = dataSnapshot.getValue(User.class);
                LatLng positionFirebase = new LatLng(post.lat, post.lon);
                marker = mMap.addMarker(new MarkerOptions().position(positionFirebase).title("Última ubicacion"));
                marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(positionFirebase));
                Toast toast = Toast.makeText(getApplicationContext(), "Fecha y hora de parking: "+post.date, Toast.LENGTH_LONG);
                toast.show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //Fallo de la base de datos
                System.out.println("Fallo en la base de datos: "+databaseError.getCode());
            }
        });
    }

    @IgnoreExtraProperties
    static class User {

        public float lat;
        public float lon;
        public String date;

        public User() {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }

        public User(float latitud, float longitud, String fecha) {
            this.lat = latitud;
            this.lon = longitud;
            this.date = fecha;
        }
    }

}

