package com.example.richard.vybe.Map;


import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
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
import java.util.ArrayList;
import java.util.List;


public class MapFragment extends Fragment {


    private String TAG = "MapFragment";

    private final int MAX_RADIUS = 2000;
    public int radius = 1000;
    public String location = "";

    private ImageButton btnUp;
    private ImageButton btnDown;
    private ImageButton btnLocation;
    public LatLng midLatLng;

    public List<Marker> markers;
    public List<User> users;
    public Circle circle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);


        btnUp = view.findViewById(R.id.btnUp);
        btnDown = view.findViewById(R.id.btnDown);
        btnLocation = view.findViewById(R.id.btnLocation);

        // Initialize map fragment

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

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

                SharedPreferences sharedPreferences = getContext().getSharedPreferences("SPOTIFY", 0);
                String currentUser = sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "");

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser);
                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        String location = dataSnapshot.child("location").getValue().toString();

                        if (!location.equals("") && location != null) {
                            midLatLng = getLatLng(location);
                        } else {
                            midLatLng = new LatLng(0, 0);
                        }


                        // When map is loaded
                        googleMap.clear();
                        showMarkers(googleMap, radius);

                        btnUp.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (radius >= 2000) {
                                    radius = 2000;
                                    Toast.makeText(getContext(), getContext().getString(R.string.maximum_radius), Toast.LENGTH_SHORT).show();
                                } else {
                                    radius += 200;
                                }
                                googleMap.clear();
                                showMarkers(googleMap, radius);
                                Log.i(TAG, "NEW RADIUS: " + radius);
                            }
                        });

                        btnDown.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (radius <= 0) {
                                    radius = 0;
                                    Toast.makeText(getContext(), getContext().getString(R.string.minimum_radius), Toast.LENGTH_SHORT).show();
                                } else {
                                    radius -= 200;
                                }
                                googleMap.clear();
                                showMarkers(googleMap, radius);
                                Log.i(TAG, "NEW RADIUS: " + radius);
                            }
                        });

                        btnLocation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (midLatLng != null) {
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(midLatLng, 14));
                                } else {
                                    Toast.makeText(getContext(), getContext().getString(R.string.no_valid_location), Toast.LENGTH_SHORT).show();
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 14));
                                }
                            }
                        });

                        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(@NonNull Marker marker) {
                                Intent intent = new Intent(getContext(), MessageActivity.class);
                                intent.putExtra("userFullId", marker.getTitle().toString());
                                getContext().startActivity(intent);
                                radius = 1000;
                                googleMap.clear();
                                return false;
                            }
                        });

                        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(@NonNull LatLng latLng) {

                            }
                        });

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }
        });

        progressDialog.dismiss();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void showMarkers(GoogleMap googleMap, int radius) {


        final SharedPreferences sharedPreferences = getContext().getSharedPreferences("SPOTIFY", 0);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        String currentUser = sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "");

        googleMap.clear();

        if (markers == null) {
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    markers = new ArrayList<>();
                    users = new ArrayList<>();

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        final String getUser = snapshot.getKey();

                        if (snapshot.hasChild("id") && snapshot.hasChild("name") && snapshot.hasChild("profileImage") && snapshot.hasChild("location")) {
                            String location  = snapshot.child("location").getValue().toString();

                            // Eliminate users not in max radius

                            if (isInMaxRadius(location, midLatLng)) {
                                String username = snapshot.child("name").getValue().toString();
                                String userid = snapshot.child("id").getValue().toString();
                                String imageURL = snapshot.child("profileImage").getValue().toString();
                                User newUser = new User();
                                newUser.setDisplay_name(username);
                                newUser.setId(userid);
                                newUser.setProfileImageURL(imageURL);

                                assert newUser != null;
                                assert currentUser != null;

                                if (newUser != null) {
                                    users.add(newUser);
                                }

                                if (!getUser.equals(currentUser) && !location.equals("")) {
                                    LatLng newLatLng = getLatLng(location);

                                    if (newLatLng != null) {
                                        Marker marker = googleMap.addMarker(
                                                new MarkerOptions()
                                                        .position(newLatLng)
                                                        .title(username + " " + userid)
                                                        .visible(false)
                                        );

                                        BitmapDrawable bitmapdraw = (BitmapDrawable)getResources().getDrawable(R.drawable.man);
                                        Bitmap b = bitmapdraw.getBitmap();
                                        Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                                        if (imageURL.equals("")) {
                                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                                        } else {
                                            Bitmap imageBitmap = Bitmap.createScaledBitmap(getBitmapFromLink(imageURL), 100, 100, false);
                                            Bitmap croppedImageBitmap = getCroppedBitmap(imageBitmap);
                                            marker.setIcon(BitmapDescriptorFactory.fromBitmap(croppedImageBitmap));
                                        }

                                        if (midLatLng != null) {
                                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(midLatLng, 14));
                                        } else {
                                            Toast.makeText(getContext(), getContext().getString(R.string.no_valid_location), Toast.LENGTH_SHORT).show();
                                            midLatLng = new LatLng(0, 0);
                                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(midLatLng, 14));
                                        }

                                        markers.add(marker);

                                        for (Marker marker1 : markers) {
                                            if (midLatLng != null) {
                                                double calculatedDistance = distance(midLatLng.latitude, midLatLng.longitude, marker1.getPosition().latitude, marker1.getPosition().longitude);
                                                if (calculatedDistance < (radius / 2)) {
                                                    marker1.setVisible(true);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }

                    }

                    if (midLatLng != null) {
                        circle = googleMap.addCircle(new CircleOptions()
                                .center(midLatLng)
                                .radius(radius)
                                .strokeColor(Color.rgb(0, 136, 255))
                                .fillColor(Color.argb(20, 0, 136, 255)));
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        else {
            for (Marker marker1 : markers) {
                if (midLatLng != null) {
                    double calculatedDistance = distance(midLatLng.latitude, midLatLng.longitude, marker1.getPosition().latitude, marker1.getPosition().longitude);
                    if (calculatedDistance < (radius / 2)) {
                        Marker marker = googleMap.addMarker(
                                new MarkerOptions()
                                        .position(marker1.getPosition())
                                        .title(marker1.getTitle())
                                        .visible(true)
                        );

                        for (User user1 : users) {
                            if (marker1.getTitle().equals(user1.display_name + " " + user1.getId())) {
                                BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.man);
                                Bitmap b = bitmapdraw.getBitmap();
                                Bitmap smallMarker = Bitmap.createScaledBitmap(b, 100, 100, false);
                                if (user1.getProfileImageURL().equals("")) {
                                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                                } else {
                                    try {
                                        Bitmap imageBitmap = Bitmap.createScaledBitmap(getBitmapFromLink(user1.getProfileImageURL()), 100, 100, false);
                                        Bitmap croppedImageBitmap = getCroppedBitmap(imageBitmap);
                                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(croppedImageBitmap));
                                    } catch (Exception e) {
                                        Bitmap croppedImageBitmap = smallMarker;
                                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(croppedImageBitmap));
                                    }

                                }
                            }
                        }

                    }
                }
            }


            if (midLatLng != null) {
                circle = googleMap.addCircle(new CircleOptions()
                        .center(midLatLng)
                        .radius(radius)
                        .strokeColor(Color.rgb(0, 136, 255))
                        .fillColor(Color.argb(20, 0, 136, 255)));
            }

        }

    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1000;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
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

                if (addressList != null && addressList.size() != 0) {
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

    private boolean isInMaxRadius(String location, LatLng midLatLng) {

        LatLng newUserLatLng = getLatLng(location);

        if (midLatLng != null) {
            double calculatedDistance = distance(midLatLng.latitude, midLatLng.longitude, newUserLatLng.latitude, newUserLatLng.longitude);
            if (calculatedDistance < (MAX_RADIUS / 2)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
