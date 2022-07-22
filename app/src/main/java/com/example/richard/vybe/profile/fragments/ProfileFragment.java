package com.example.richard.vybe.profile.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.richard.vybe.model.User;
import com.example.richard.vybe.R;
import com.example.richard.vybe.SplashActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class ProfileFragment extends Fragment {

    private String TAG = "ProfileFragment";


    private ImageView image_profile;
    private TextView username;
    private TextView tvSave;
    private TextView tvLogout;

    private EditText etLocation;

    String currentUser;
    SharedPreferences sharedPreferences;
    DatabaseReference databaseReference;

    public static final int REQUEST_CODE = 1337;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        image_profile = view.findViewById(R.id.profile_image);
        username = view.findViewById(R.id.username);
        etLocation = view.findViewById(R.id.etLocation);
        tvSave = view.findViewById(R.id.tvSaveLocation);

        tvLogout = view.findViewById(R.id.tvLogout);

        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout(getContext());
            }
        });

        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!etLocation.getText().toString().equals("")) {
                    DatabaseReference locationReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser);
                    locationReference.child("location").setValue(etLocation.getText().toString());
                    etLocation.setText(R.string.empty);
                }
            }
        });

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(true);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        sharedPreferences = getContext().getSharedPreferences("SPOTIFY", 0);
        currentUser = sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "");
        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(currentUser);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                final String getUser = dataSnapshot.getKey();
                User user = new User();

                String id = dataSnapshot.child("id").getValue().toString();
                String name = dataSnapshot.child("name").getValue().toString();
                String profileImageURL = dataSnapshot.child("profileImage").getValue().toString();

                user.setDisplay_name(name);
                user.setId(id);
                user.setProfileImageURL(profileImageURL);

                username.setText(user.getDisplay_name());

                String userProfileImageURL = user.getProfileImageURL();

                if (getActivity() == null) {
                    return;
                }

                if (userProfileImageURL.equals("")) {
                    Glide.with(getContext())
                            .load(R.drawable.man)
                            .transform(new CircleCrop())
                            .into(image_profile);
                } else {
                    Glide.with(getContext())
                            .load(userProfileImageURL)
                            .transform(new CircleCrop())
                            .into(image_profile);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        progressDialog.dismiss();
        return view;
    }

    private void logout(Context context) {
        String packageName = "com.spotify.music";
        PackageManager packageManager = context.getPackageManager();

        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        View bottomSheetView = LayoutInflater.from(context)
                .inflate(
                        R.layout.layout_bottom_logout,
                        (ConstraintLayout) getActivity().findViewById(R.id.clBottomLogout)

                );
        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();

        TextView tvCancel = bottomSheetView.findViewById(R.id.tvCancel);
        LottieAnimationView ltSpotify = bottomSheetView.findViewById(R.id.ltSpotify);
        TextView tvRedirect = bottomSheetView.findViewById(R.id.tvRedirect);
        TextView tvRestart = bottomSheetView.findViewById(R.id.tvRestart);

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetDialog.dismiss();
            }
        });

        if (isPackageInstalled(packageName, packageManager)) {
            ltSpotify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent launchIntent = getContext().getPackageManager().getLaunchIntentForPackage(packageName);
                    startActivity(launchIntent);
                    bottomSheetDialog.dismiss();
                    getActivity().finish();
                }
            });
        } else {

            tvRedirect.setText(R.string.you_will_be_redirected_to_spotify_authentication);
            tvRestart.setText(R.string.kindly_make_changes);

            ltSpotify.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent logOutIntent = new Intent(getActivity(), SplashActivity.class);
                    logOutIntent.putExtra("logOut", "true");
                    startActivity(logOutIntent);
                    bottomSheetDialog.dismiss();
                    getActivity().finish();
                }
            });
        }


    }

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


}