package com.example.memorix;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Members extends AppCompatActivity {

    private TextView adminName;
    private RequestQueue requestQueue;
    private RelativeLayout progressBar;
    private LinearLayout memberContainer;
    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_members);

        progressBar = findViewById(R.id.progressbar);
        memberContainer = findViewById(R.id.member_container);
        adminName = findViewById(R.id.admin_name);
        requestQueue = Volley.newRequestQueue(this);

        classId = getIntent().getStringExtra("class_id");
        fetchCreator(classId);
        fetchClassMembers(classId);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void fetchCreator(String classId) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/fetch_class_creator.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.has("creator_fullname")) {
                            progressBar.setVisibility(View.GONE);
                            String creatorId = jsonResponse.getString("creator_id");
                            String userName = jsonResponse.getString("creator_fullname");
                            ImageView adminProf = findViewById(R.id.admin_profile);
                            adminName.setText(userName);
                            fetchCreatorProf(adminProf, creatorId);
                        } else {
                            Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();

                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    if (error.networkResponse != null) {
                        Log.e("FetchUserError", "Error code: " + error.networkResponse.statusCode);
                        if (error instanceof NoConnectionError || error instanceof NetworkError) {
                            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof TimeoutError) {
                            Toast.makeText(getApplicationContext(), "Request Timeout. Please try again.", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ServerError) {
                            Toast.makeText(getApplicationContext(), "Server Error. Please try again later.", Toast.LENGTH_SHORT).show();
                        } else if (error instanceof ParseError) {
                            Toast.makeText(getApplicationContext(), "Parsing Error.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    Toast.makeText(getApplicationContext(), "Error fetching user data", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("class_id", classId);
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void fetchClassMembers(String classId) {
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/fetch_class_member.php",
                response -> {
                    Log.d("ServerResponse", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.has("members")) {
                            JSONArray membersArray = jsonResponse.getJSONArray("members");
                            for (int i = 0; i < membersArray.length(); i++) {
                                JSONObject members = membersArray.getJSONObject(i);
                                String memberId = members.getString("user_id");
                                String fullname = members.getString("fullname");
                                String nickName = members.getString("nickname");
                                addMemberToLayout(fullname, nickName, memberId);
                            }
                        } else if (jsonResponse.has("message")) {
                            String message = jsonResponse.getString("message");
                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("Error", e.getMessage());
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error fetching members", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("class_id", classId);
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void addMemberToLayout(String fullname, String nickname, String memberId) {
        progressBar.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(this);
        View memberLayout = inflater.inflate(R.layout.members_layout, null);

        TextView memberNickName = memberLayout.findViewById(R.id.member_nickname);
        TextView memberName = memberLayout.findViewById(R.id.member_name);
        ImageView memberProf = memberLayout.findViewById(R.id.member_profile);
        fetchMemberProf(memberProf, memberId);
        memberNickName.setText(nickname);
        memberName.setText(fullname);

        memberContainer.addView(memberLayout);
    }

    private void fetchMemberProf(ImageView memberProf, String memberId) {
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/user_fetch.php",
                response -> {
                    Log.d("ServerResponse", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            JSONObject user = jsonResponse.getJSONObject("user");
                            String profileImage = "https://pink-boar-882869.hostingersite.com/" + user.getString("profile");
                            loadProfileImageWithAuth(profileImage, memberProf);

                        } else {
                            Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error parsing user data", Toast.LENGTH_SHORT).show();
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    if (error.networkResponse != null) {
                        String responseBody = new String(error.networkResponse.data);
                        Log.d("Error Response", responseBody);
                    } else {
                        Log.e("Error: ", "Error: " + error.getMessage());
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", memberId);
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void fetchCreatorProf(ImageView creatorProf, String creatorId) {
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/user_fetch.php",
                response -> {
                    Log.d("ServerResponse", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            JSONObject user = jsonResponse.getJSONObject("user");
                            String profileImage = "https://pink-boar-882869.hostingersite.com/" + user.getString("profile");
                            loadProfileImageWithAuth(profileImage, creatorProf);

                        } else {
                            Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error parsing user data", Toast.LENGTH_SHORT).show();
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    if (error.networkResponse != null) {
                        String responseBody = new String(error.networkResponse.data);
                        Log.d("Error Response", responseBody);
                    } else {
                        Log.e("Error: ", "Error: " + error.getMessage());
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", creatorId);
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void loadProfileImageWithAuth(String imageUrl, ImageView userProf) {
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        ImageRequest imageRequest = new ImageRequest(imageUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        Glide.with(Members.this)
                                .load(response)
                                .apply(RequestOptions.circleCropTransform())
                                .into(userProf);
                        progressBar.setVisibility(View.GONE);
                    }
                },
                0,
                0,
                ImageView.ScaleType.CENTER_CROP,
                Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("Image Load Error", "Error loading image: " + error.getMessage());
                    }
                }) {
        };

        imageRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(imageRequest);
    }

}