package com.example.memorix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class AddToClass extends AppCompatActivity {

    private View errorToast, errorToastLayout, toastNest, progressBar;
    private TextView toastMessage, okBtn, yourClassesText;
    private LinearLayout classContainer, emptyClass;
    private ImageView options;
    private RequestQueue requestQueue;
    private String questionnaireId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_class);

        progressBar = findViewById(R.id.progressbar);
        errorToast = findViewById(R.id.error_toast);
        options = findViewById(R.id.options);
        errorToastLayout = findViewById(R.id.toast_layout);
        toastNest = findViewById(R.id.toastNest);
        toastMessage = findViewById(R.id.error_message);
        okBtn = findViewById(R.id.ok_btn);
        classContainer = findViewById(R.id.class_container);
        yourClassesText = findViewById(R.id.textView21);
        requestQueue = Volley.newRequestQueue(this);
        emptyClass = findViewById(R.id.empty_class);
        options.setVisibility(View.GONE);
        yourClassesText.setText("Add to your course");

        questionnaireId = getIntent().getStringExtra("questionnaire_id");
        fetchClass();

        errorToastLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                float x = event.getRawX();
                float y = event.getRawY();

                if (!isViewTouched(toastNest, x, y)) {
                    errorToast.setVisibility(View.GONE);
                }
                return true;
            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errorToast.setVisibility(View.GONE);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean isViewTouched(View view, float x, float y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        return (x >= viewX && x <= (viewX + view.getWidth())) &&
                (y >= viewY && y <= (viewY + view.getHeight()));
    }

    private void fetchClass() {
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/class_fetch.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray classArray = jsonResponse.getJSONArray("class");

                        if (classArray.length() < 1) {
                            emptyClass.setVisibility(View.VISIBLE);
                        }

                        for (int i = 0; i < classArray.length(); i++) {
                            JSONObject classObject = classArray.getJSONObject(i);
                            String classId = classObject.getString("class_id");
                            String className = classObject.getString("class_name");
                            fetchQuestionnaireCount(classId, className);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                        progressBar.setVisibility(View.GONE);
                        errorToast.setVisibility(View.VISIBLE);
                        toastMessage.setText("Network error. Check your network connection.");
                        errorToast.startAnimation(fadeIn);
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error fetching notes", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void fetchQuestionnaireCount(String classId, String className) {
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/class_questionnaire_fetch.php",
                response -> {
                    Log.d("ServerResponse", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray questionnaireArray = jsonResponse.getJSONArray("questionnaire");
                        int questionnaireCount = questionnaireArray.length();
                        addClassToLayout(classId, className, questionnaireCount);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Animation fadeIn =  AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                        progressBar.setVisibility(View.GONE);
                        errorToast.setVisibility(View.VISIBLE);
                        toastMessage.setText("Network error. Check your network connection.");
                        errorToast.startAnimation(fadeIn);
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error fetching notes", Toast.LENGTH_SHORT).show();
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

    private void addClassToLayout(String classId, String className, int questionnaireCount) {
        progressBar.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(AddToClass.this);
        View classLayout = inflater.inflate(R.layout.created_class_layout, null);

        TextView classNameView = classLayout.findViewById(R.id.class_name);
        TextView questionnaireCountText = classLayout.findViewById(R.id.questionnaire_count);

        classNameView.setText(className);
        questionnaireCountText.setText(questionnaireCount + " questionnaire/s");

        classLayout.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            classLayout.setEnabled(false);
            LinearLayout createdClassBg = classLayout.findViewById(R.id.created_class_bg);
            createdClassBg.setBackgroundResource(R.drawable.class_add_bg);
            insertClassData(classId, questionnaireId, className);
        });

        classContainer.addView(classLayout);
    }

    private void insertClassData(String classId, String questionnaireId, String className) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/class_questionnaire_insert.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("onResponse", response);
                        try {
                            JSONObject jobj = new JSONObject(response);
                            int success = jobj.optInt("success", -1);
                            if (success == 1) {
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(getApplicationContext(), ClassContents.class);
                                intent.putExtra("class_id", classId);
                                intent.putExtra("class_name", className);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to insert class", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String responseBody = new String(error.networkResponse.data);
                            Log.d("Error Response", responseBody);

                            if (statusCode == 404) {
                                Animation fadeIn =  AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                                progressBar.setVisibility(View.GONE);
                                errorToast.setVisibility(View.VISIBLE);
                                toastMessage.setText("Questionnaire already exist in the class.");
                                errorToast.startAnimation(fadeIn);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = "11193618:60-dayfreetrial";
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", auth);
                return headers;
            }

            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("questionnaire_id", questionnaireId);
                params.put("class_id", classId);
                Log.d("Params", params.toString());
                return params;
            }
        };

        requestQueue.add(request);
    }
}