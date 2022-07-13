package com.example.richard.vybe.Map;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.richard.vybe.Message.MessageActivity;
import com.example.richard.vybe.Model.User;
import com.example.richard.vybe.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class MapFragment extends Fragment {


    private String TAG = "MapFragment";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Initialize map fragment

        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);

        // Handles getBitMapFromLink error
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // Async map
        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                // When map is loaded
                showMarkers(googleMap);

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        Intent intent = new Intent(getContext(), MessageActivity.class);
                        intent.putExtra("userFullId", marker.getTitle().toString());
                        getContext().startActivity(intent);
                        if (marker.getTitle().equals("Richard")) {

                        }
                        return false;
                    }
                });

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(@NonNull LatLng latLng) {

                    }
                });

            }
        });


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void showMarkers(GoogleMap googleMap) {

        final SharedPreferences sharedPreferences = getContext().getSharedPreferences("SPOTIFY", 0);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        String currentUser = sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final String getUser = snapshot.getKey();
                    User user = new User();

                    if (snapshot.hasChild("id") && snapshot.hasChild("name") && snapshot.hasChild("profileImage")) {
                        String location  = snapshot.child("location").getValue().toString();
                        String username = snapshot.child("name").getValue().toString();
                        String userid = snapshot.child("id").getValue().toString();
                        String imageURL = snapshot.child("profileImage").getValue().toString();
                        assert user != null;
                        assert currentUser != null;

                        if (!getUser.equals(currentUser) && !location.equals("")) {
                            LatLng newLatLng = getLatLng(location);

                            if (newLatLng != null) {
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(newLatLng);
                                markerOptions.title(username + " " + userid);
                                BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.man);
                                Bitmap b = bitmapdraw.getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                                if (imageURL.equals("")) {
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                                } else {
                                    Bitmap imageBitmap = Bitmap.createScaledBitmap(getBitmapFromLink(imageURL), 100, 100, false);
                                    Bitmap croppedImageBitmap = getCroppedBitmap(imageBitmap);
                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(croppedImageBitmap));
                                }

                                googleMap.addMarker(markerOptions);
                                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newLatLng, 15));

                            }
                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public Bitmap getBitmapFromLink(String link) {
        try {
            URL url = new URL(link);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            try {
                connection.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }


    private LatLng getLatLng(String location) {
        LatLng latLng = null;
        if (getContext() != null) {
            Geocoder geocoder = new Geocoder(getContext());
            List<Address> addressList;

            try {
                addressList = geocoder.getFromLocationName(location, 1);

                if (addressList != null) {
                    double lat = addressList.get(0).getLatitude();
                    double lng = addressList.get(0).getLongitude();
                    latLng = new LatLng(lat, lng);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return latLng;
    }

}

