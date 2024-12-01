package com.example.memorix;

import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ForgotPassword extends AppCompatActivity {

    private RelativeLayout progressBar;;
    private Toast customizedToast;
    private TextView toastMessage;
    private MaterialButton signup, continueBtn;
    private EditText emailEditText;
    private RequestQueue requestqueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        progressBar = findViewById(R.id.progressbar);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.customized_toast, (ViewGroup)findViewById(R.id.toastLayout));
        toastMessage = layout.findViewById(R.id.toastMessage);
        customizedToast = new Toast(getApplicationContext());
        customizedToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 1);
        customizedToast.setDuration(Toast.LENGTH_LONG);
        customizedToast.setView(layout);

        signup = findViewById(R.id.signin);
        emailEditText = findViewById(R.id.email);
        continueBtn = findViewById(R.id.continuebtn);
        requestqueue = Volley.newRequestQueue(getApplicationContext());

        signup.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        });

        continueBtn.setOnClickListener(v -> {
            if (emailEditText.getText().toString().isEmpty()) {
                toastMessage.setText("Please enter your email to continue");
                emailEditText.setBackgroundResource(R.drawable.email_otp_error);
                customizedToast.show();
                return;
            }
            String email = emailEditText.getText().toString().trim();
            checkUser(email);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void checkUser(String email) {
        progressBar.setVisibility(View.VISIBLE);
        String url = "https://pink-boar-882869.hostingersite.com/check_user_email.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int success = jsonObject.optInt("success", -1);
                            if (success == 1) {
                                sendEmailOTP(email);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                toastMessage.setText("The email you entered is not registered");
                                emailEditText.setBackgroundResource(R.drawable.email_otp_error);
                                customizedToast.show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };
        requestqueue.add(request);
    }

    private void sendEmailOTP(String emailAddress) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/phpMailer/otp-email.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("onResponse", response);
                        try {
                            JSONObject jobj = new JSONObject(response);
                            int success = jobj.optInt("success", -1);
                            if (success == 1) {
                                progressBar.setVisibility(View.GONE);
                                String otp = jobj.optString("otp", "");
                                Intent intent = new Intent(getApplicationContext(), OTPVerification.class);
                                intent.putExtra("otp", otp);
                                intent.putExtra("email", emailAddress);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to send email.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } finally {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        if (error.networkResponse != null) {
                            String responseBody = new String(error.networkResponse.data);
                            Log.e("Error Response", responseBody);
                        } else {
                            Log.e("Error Response", "Error: " + error.getMessage());
                        }
                    }
                }) {

            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("email", emailAddress);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestqueue.add(request);
    }
}