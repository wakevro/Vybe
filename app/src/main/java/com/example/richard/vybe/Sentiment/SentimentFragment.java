package com.example.richard.vybe.Sentiment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.richard.vybe.R;
import com.example.richard.vybe.Swipe.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

public class SentimentFragment extends Fragment {

    private String TAG = "SentimentFragment";


    private EditText etUserMood;
    private Button btnMood;
    private TextView tvSentiment;
    private ViewPager2 viewPager2;

    private DatabaseReference sentimentDB;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;

    private final double OLD_MAX = 1.0;
    private final double OLD_MIN = -1.0;
    private final double NEW_MAX = 1.0;
    private final double NEW_MIN = 0.0;

    public SentimentFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sentiment, container, false);

        etUserMood = rootView.findViewById(R.id.etUserMood);
        btnMood = rootView.findViewById(R.id.btnMood);
        viewPager2 = rootView.findViewById(R.id.pager);

        sharedPreferences = getActivity().getSharedPreferences("SPOTIFY", 0);
        sentimentDB = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", "")).child("Sentiment");

        RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        btnMood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String sentence = etUserMood.getText().toString();
                String regex = "[0-9]+";
                String splChrs = "-/@#$%^&_+=()";
                if (!sentence.isEmpty() && !sentence.startsWith(" ")) {
                    ProgressDialog progressDialog = new ProgressDialog(getContext());
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Loading...");
                    progressDialog.show();

                    String URL = "https://vybe-0.uc.r.appspot.com/sentiment/" + sentence;

                    JsonObjectRequest objectRequest = new JsonObjectRequest(
                            Request.Method.GET,
                            URL,
                            null,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        double sentiment = response.getDouble("sentiment");

                                        sentiment = convertToNewRange(sentiment);
                                        sentimentDB.child("sentiment").setValue(sentiment);
                                        etUserMood.setText("");
                                        deleteTracks();
                                        progressDialog.dismiss();
                                        Intent intent = new Intent(getActivity().getBaseContext(), MainActivity.class);
                                        intent.putExtra("page_number", 2);  //2 or whatever you want
//                                        intent.putExtra("sentiment", sentiment);
                                        getActivity().startActivityForResult(intent, 0);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.e(TAG, "Response: " + error.toString());
                                }
                            }
                    );
                    requestQueue.add(objectRequest);
                } else {
                    Toast.makeText(getContext(), "INVALID SENTENCE!", Toast.LENGTH_SHORT).show();
                }

            }

        });


        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    private double convertToNewRange(double OldValue) {
        double OldRange = (OLD_MAX - OLD_MIN);
        double NewRange = (NEW_MAX - NEW_MIN);
        double NewValue;
        if (OldRange == 0)
            NewValue = NEW_MIN;
        else
        {
            NewValue = (((OldValue - OLD_MIN) * NewRange) / OldRange) + NEW_MIN;
        }
        return NewValue;
    }

    private void deleteTracks() {
        databaseReference = FirebaseDatabase.getInstance().getReference().child(sharedPreferences.getString("username", "") + " " + sharedPreferences.getString("userid", ""));
        databaseReference.child("Tracks").removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                }
                else {
                    Toast.makeText(getActivity(), "Failed to delete!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


}