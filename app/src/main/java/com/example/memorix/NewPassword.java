package com.example.memorix;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

public class NewPassword extends AppCompatActivity {

    private RelativeLayout progressBar;;
    private Toast customizedToast;
    private TextView toastMessage;
    private MaterialButton confirmBtn;
    private EditText newPasswordEditText, confirmNewPasswordEditText;
    private RequestQueue requestqueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_password);

        progressBar = findViewById(R.id.progressbar);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.customized_toast, (ViewGroup)findViewById(R.id.toastLayout));
        toastMessage = layout.findViewById(R.id.toastMessage);
        customizedToast = new Toast(getApplicationContext());
        customizedToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 1);
        customizedToast.setDuration(Toast.LENGTH_LONG);
        customizedToast.setView(layout);

        newPasswordEditText = findViewById(R.id.new_password);
        confirmNewPasswordEditText = findViewById(R.id.confirm_new_password);
        confirmBtn = findViewById(R.id.confirmbtn);
        requestqueue = Volley.newRequestQueue(getApplicationContext());

        boolean[] isPasswordVisible = {false};
        newPasswordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (newPasswordEditText.getRight() - newPasswordEditText.getCompoundDrawables()[2].getBounds().width())) {
                    isPasswordVisible[0] = !isPasswordVisible[0];

                    if (isPasswordVisible[0]) {
                        newPasswordEditText.setTransformationMethod(null);
                    } else {
                        newPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
                    }

                    int drawable = isPasswordVisible[0] ? R.drawable.ic_eye_open : R.drawable.ic_eye;
                    newPasswordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);

                    newPasswordEditText.setSelection(newPasswordEditText.getText().length());

                    return true;
                }
            }
            return false;
        });


        boolean[] isConfirmPasswordVisible = {false};
        confirmNewPasswordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (confirmNewPasswordEditText.getRight() - confirmNewPasswordEditText.getCompoundDrawables()[2].getBounds().width())) {
                    isConfirmPasswordVisible[0] = !isConfirmPasswordVisible[0];

                    if (isConfirmPasswordVisible[0]) {
                        confirmNewPasswordEditText.setTransformationMethod(null);
                    } else {
                        confirmNewPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());
                    }

                    int drawable = isConfirmPasswordVisible[0] ? R.drawable.ic_eye_open : R.drawable.ic_eye;
                    confirmNewPasswordEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);

                    confirmNewPasswordEditText.setSelection(confirmNewPasswordEditText.getText().length());

                    return true;
                }
            }
            return false;
        });

        confirmBtn.setOnClickListener(v -> {
            String newPassword = newPasswordEditText.getText().toString().trim();
            String newConfirmPassword = confirmNewPasswordEditText.getText().toString().trim();

            if(newPassword.isEmpty()) {
                newPasswordEditText.setBackgroundResource(R.drawable.email_otp_error);
                toastMessage.setText("Please enter a new password to continue.");
                customizedToast.show();
                progressBar.setVisibility(View.GONE);
                return;
            } else if(newConfirmPassword.isEmpty()) {
                confirmNewPasswordEditText.setBackgroundResource(R.drawable.email_otp_error);
                toastMessage.setText("Please confirm the new password to continue.");
                customizedToast.show();
                progressBar.setVisibility(View.GONE);
                return;
            } else if(newPassword.length() < 8) {
                newPasswordEditText.setBackgroundResource(R.drawable.email_otp_error);
                toastMessage.setText("Password should be at least 8 characters.");
                customizedToast.show();
                progressBar.setVisibility(View.GONE);
                return;
            } else if(!newConfirmPassword.equals(newPassword)) {
                newPasswordEditText.setBackgroundResource(R.drawable.email_otp_error);
                confirmNewPasswordEditText.setBackgroundResource(R.drawable.email_otp_error);
                toastMessage.setText("New password and confirm password do not match.");
                customizedToast.show();
                progressBar.setVisibility(View.GONE);
                return;
            }

            updatePassword(newPassword);
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void updatePassword(String newPassword) {
        progressBar.setVisibility(View.VISIBLE);
        String email = getIntent().getStringExtra("email");

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/update_password.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("onResponse", response);
                        try {
                            JSONObject jobj = new JSONObject(response);
                            int success = jobj.optInt("success", -1);
                            if (success == 1) {
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(getApplicationContext(), Login.class);
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
                params.put("email", email);
                params.put("password", newPassword);
                return params;
            }
        };
        requestqueue.add(request);
    }
}