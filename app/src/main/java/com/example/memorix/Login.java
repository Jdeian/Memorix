package com.example.memorix;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity {

    EditText txtemail, txtpassword;
    MaterialButton loginbtn;
    ProgressBar progressBar;
    TextView signupNow, toastMessage, forgotPass;
    RequestQueue requestQueue;
    String TAG_SUCCESS = "success";
    Toast customizedToast;
    LoginFunctions validate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.customized_toast, (ViewGroup)findViewById(R.id.toastLayout));
        toastMessage = layout.findViewById(R.id.toastMessage);

        customizedToast = new Toast(getApplicationContext());
        customizedToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 1);
        customizedToast.setDuration(Toast.LENGTH_LONG);
        customizedToast.setView(layout);

        txtemail = findViewById(R.id.editTextTextEmailAddress);
        txtpassword = findViewById(R.id.editTextTextPassword);
        progressBar = findViewById(R.id.progressbar);
        loginbtn = findViewById(R.id.login);
        forgotPass = findViewById(R.id.forgotpass);
        signupNow = findViewById(R.id.signupnow);
        requestQueue = Volley.newRequestQueue(this);

        boolean[] isPasswordVisible = {false};
        txtpassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (txtpassword.getRight() - txtpassword.getCompoundDrawables()[2].getBounds().width())) {
                    isPasswordVisible[0] = !isPasswordVisible[0];

                    if (isPasswordVisible[0]) {
                        txtpassword.setTransformationMethod(null);
                    } else {
                        txtpassword.setTransformationMethod(new PasswordTransformationMethod());
                    }

                    int drawable = isPasswordVisible[0] ? R.drawable.ic_eye_open : R.drawable.ic_eye;
                    txtpassword.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);

                    txtpassword.setSelection(txtpassword.getText().length());

                    return true;
                }
            }
            return false;
        });

        validate = new LoginFunctions(txtemail, txtpassword);

        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                performLogin();
            }
        });

        signupNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Registration.class);
                startActivity(intent);
                finish();
            }
        });

        forgotPass.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ForgotPassword.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void performLogin() {
        progressBar.setVisibility(View.VISIBLE);
        String email = String.valueOf(txtemail.getText());
        String password = String.valueOf(txtpassword.getText());

        boolean isValid = true;

        if (validate.isEmptyEmail()) {
            toastMessage();
            txtemail.setBackgroundResource(R.drawable.errorroundedborder);
            isValid = false;
        }else if(validate.isValidEmail()) {
            toastMessage();
            toastMessage.setText("The email you entered is invalid");
            txtemail.setBackgroundResource(R.drawable.errorroundedborder);
            isValid = false;
        } else {
            txtemail.setBackgroundResource(R.drawable.validroundedborder);
        }
        if (validate.isEmptyPassword()) {
            toastMessage();
            txtpassword.setBackgroundResource(R.drawable.errorroundedborder);
            isValid = false;
        } else {
            txtpassword.setBackgroundResource(R.drawable.validroundedborder);
        }

        if (isValid == true) {
            loginbtn.setEnabled(false);
            StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/login.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("onResponse", response);

                    try {
                        JSONObject jobj = new JSONObject(response);
                        int success = jobj.optInt(TAG_SUCCESS, -1);
                        if (success == 1) {
                            progressBar.setVisibility(View.GONE);
                            JSONObject data = jobj.getJSONObject("data");
                            String userId = data.optString("user_id", "N/A");

                            SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("user_id", userId);
                            editor.putBoolean("isLoggedIn", true);
                            editor.apply();

                            updateUserLastLogin(userId);

                            Intent intent = new Intent(Login.this, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else{
                            loginbtn.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            toastMessage();
                            toastMessage.setText("Incorrect Credentials");
                            txtemail.setBackgroundResource(R.drawable.errorroundedborder);
                            txtpassword.setBackgroundResource(R.drawable.errorroundedborder);
                        }
                    } catch (Exception e) {
                        loginbtn.setEnabled(true);
                        Toast.makeText(Login.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(View.GONE);
                    String errorMessage = error.getMessage();
                    if(errorMessage.equals(null)) {
                        Toast.makeText(Login.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                    Toast.makeText(Login.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }) {

                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("email", email);
                    params.put("password", password);
                    return params;
                }
            };
            request.setRetryPolicy(new DefaultRetryPolicy(1000, 1, 1.0f));

            if (requestQueue != null) {
                requestQueue.add(request);
            } else {
                Log.e("VolleyError", "RequestQueue is null");
            }
        }
    }
    public void toastMessage() {
        progressBar.setVisibility(View.GONE);
        toastMessage.setText("PLease fill in all the credentials");
        customizedToast.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), Introduction.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    private void updateUserLastLogin(String userId) {
        progressBar.setVisibility(View.VISIBLE);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentDate = now.format(formatter);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/update_user_lastlogin.php",
                response -> {
                    try {
                        Log.d("Response", response);
                        JSONObject jobj = new JSONObject(response);
                        int success = jobj.optInt("success", -1);
                        if (success == 1) {
                            Log.e("Updating users login", "users last login updated successfully");
                        }
                    } catch (Exception e) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error updating users last login", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);
                params.put("current_date", currentDate);
                return params;
            }
        };

        requestQueue.add(request);
    }

}