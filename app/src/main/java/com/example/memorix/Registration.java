package com.example.memorix;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class Registration extends AppCompatActivity {

    EditText txtfirstname, txtlastname, txtbirthdate, txtnickname, txtemail, txtpassword, txtconfirm_password;
    MaterialCheckBox termsAndConditions;
    MaterialButton signupbtn;
    TextView loginhere, toastMessage;
    ProgressBar progressBar;
    String TAG_SUCCESS = "success";
    String TAG_MESSAGE = "message";
    RequestQueue requestqueue;
    Toast customizedToast;
    ImageView toastImage;
    SignupFunctions validate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.customized_toast, (ViewGroup)findViewById(R.id.toastLayout));
        toastMessage = layout.findViewById(R.id.toastMessage);

        customizedToast = new Toast(getApplicationContext());
        customizedToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
        customizedToast.setDuration(Toast.LENGTH_LONG);
        customizedToast.setView(layout);

        txtfirstname = findViewById(R.id.firstName);
        txtlastname = findViewById(R.id.lastName);
        txtbirthdate = findViewById(R.id.birthdate);
        txtnickname = findViewById(R.id.nickname);
        txtemail = findViewById(R.id.email);
        txtpassword = findViewById(R.id.password);
        txtconfirm_password = findViewById(R.id.confirmPassword);
        termsAndConditions = findViewById(R.id.termsAndConditions);
        signupbtn = findViewById(R.id.signup);
        loginhere = findViewById(R.id.loginhere);
        progressBar = findViewById(R.id.progressbar);
        requestqueue = Volley.newRequestQueue(Registration.this);

        txtbirthdate.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (txtbirthdate.getRight() - txtbirthdate.getCompoundDrawables()[2].getBounds().width())) {
                    showDatePickerDialog();
                    return true;
                }
            }
            return false;
        });

        validate = new SignupFunctions(txtfirstname, txtlastname, txtbirthdate, txtnickname, txtemail, txtpassword, txtconfirm_password, termsAndConditions);

        signupbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendData();
            }
        });

        loginhere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Registration.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

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

        boolean[] isConfirmPasswordVisible = {false};
        txtconfirm_password.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (txtconfirm_password.getRight() - txtconfirm_password.getCompoundDrawables()[2].getBounds().width())) {
                    isConfirmPasswordVisible[0] = !isConfirmPasswordVisible[0];

                    if (isConfirmPasswordVisible[0]) {
                        txtconfirm_password.setTransformationMethod(null);
                    } else {
                        txtconfirm_password.setTransformationMethod(new PasswordTransformationMethod());
                    }

                    int drawable = isConfirmPasswordVisible[0] ? R.drawable.ic_eye_open : R.drawable.ic_eye;
                    txtconfirm_password.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawable, 0);

                    txtconfirm_password.setSelection(txtconfirm_password.getText().length());

                    return true;
                }
            }
            return false;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;


        });
    }
    private void sendData() {
        progressBar.setVisibility(View.VISIBLE);
        String firstname = String.valueOf(txtfirstname.getText());
        String lastname = String.valueOf(txtlastname.getText());
        String fullname = firstname + " " + lastname;

        boolean isValid = true;

        if(validate.isEmptyFirstname()) {
            toastMessage();
            txtfirstname.setBackgroundResource(R.drawable.errorroundedborder);
            isValid = false;
        }else {
            txtfirstname.setBackgroundResource(R.drawable.validroundedborder);
        }
        if(validate.isEmptyLastname()) {
            toastMessage();
            txtlastname.setBackgroundResource(R.drawable.errorroundedborder);
            isValid = false;
        }else {
            txtlastname.setBackgroundResource(R.drawable.validroundedborder);
        }
        if(validate.isEmptyBirthdate()) {
            toastMessage();
            txtbirthdate.setBackgroundResource(R.drawable.errorroundedborder);
            isValid = false;
        }else if(validate.isValidBirthdate()) {
            toastMessage();
            toastMessage.setText("Invalid birthdate format! Please enter the date in the format: dd-mm-yyyy.");
            txtbirthdate.setBackgroundResource(R.drawable.errorroundedborder);
            isValid = false;
        }else {
            txtbirthdate.setBackgroundResource(R.drawable.validroundedborder);
        }
        if(validate.isEmptyNickname()) {
            toastMessage();
            txtnickname.setBackgroundResource(R.drawable.errorroundedborder);
            isValid = false;
        }else {
            txtnickname.setBackgroundResource(R.drawable.validroundedborder);
        }
        if(validate.isEmptyEmail()) {
            toastMessage();
            txtemail.setBackgroundResource(R.drawable.errorroundedborder);
            isValid = false;
        }else if(validate.isValidEmail()) {
            toastMessage();
            toastMessage.setText("Please enter a valid email");
            txtemail.setBackgroundResource(R.drawable.errorroundedborder);
            isValid = false;
        }else {
            txtemail.setBackgroundResource(R.drawable.validroundedborder);
        }
        if(validate.isEmptyPassword()) {
            toastMessage();
            txtpassword.setBackgroundResource(R.drawable.errorroundedborder);
            isValid = false;
        }else if(validate.isValidPasswordLength()) {
            toastMessage();
            toastMessage.setText("Password should be at least 8 characters");
            txtpassword.setBackgroundResource(R.drawable.errorroundedborder);
            txtconfirm_password.setBackgroundResource(R.drawable.errorroundedborder);
            isValid = false;
        }else {
            txtpassword.setBackgroundResource(R.drawable.validroundedborder);
        }
        if(validate.isEmptyConfirmpassword()) {
            toastMessage();
            txtconfirm_password.setBackgroundResource(R.drawable.errorroundedborder);
            isValid = false;
        }else if(validate.isConfirmpassMatchPass()) {
            toastMessage();
            toastMessage.setText("Confirm password don't match with password");
            txtconfirm_password.setBackgroundResource(R.drawable.errorroundedborder);
            isValid = false;
        } else if(validate.isNotCheckedTermsAndConditions()) {
            toastMessage();
            toastMessage.setText("Please agree to the Terms and Conditions by checking the box to continue");
            isValid = false;
        }else {
            txtconfirm_password.setBackgroundResource(R.drawable.validroundedborder);
        }

        if(isValid == true) {
            signupbtn.setEnabled(false);
            StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/signup.php", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("onResponse", response);

                    try {
                        JSONObject jobj = new JSONObject(response);
                        int success = jobj.optInt(TAG_SUCCESS, -1);
                        if (success == 1) {
                            progressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(Registration.this, Login.class);
                            startActivity(intent);
                            finish();
                        } else if (success == 0) {
                            signupbtn.setEnabled(true);
                            toastMessage();
                            toastMessage.setText("Account already exists. Please use another email");
                            txtemail.setBackgroundResource(R.drawable.errorroundedborder);
                        } else {
                            signupbtn.setEnabled(true);
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(Registration.this, "Unexpected response: " + jobj.getString(TAG_MESSAGE), Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        signupbtn.setEnabled(true);
                        Toast.makeText(Registration.this, "Error: " + e, Toast.LENGTH_SHORT).show();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(View.GONE);
                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        String responseBody = new String(error.networkResponse.data);
                        Log.d("Error Response", responseBody);

                        if (statusCode == 409) {
                            signupbtn.setEnabled(true);
                            toastMessage();
                            toastMessage.setText("Account already exists. Please use another email");
                            txtemail.setBackgroundResource(R.drawable.errorroundedborder);
                        } else {
                            Toast.makeText(Registration.this, "Server error: " + statusCode, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }) {

                public Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("fullname", fullname);
                    params.put("birthdate", String.valueOf(txtbirthdate.getText()));
                    params.put("nickname", String.valueOf(txtnickname.getText()));
                    params.put("email", String.valueOf(txtemail.getText()));
                    params.put("password", String.valueOf(txtpassword.getText()));
                    return params;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(1000, 1, 1.0f));

            if (requestqueue != null) {
                requestqueue.add(request);
            } else {
                Log.e("VolleyError", "RequestQueue is null");
            }
        }
    }
    public void toastMessage() {
        progressBar.setVisibility(View.GONE);
        toastMessage.setText("PLease fill in all the fields!");
        customizedToast.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), Introduction.class);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                Registration.this,
                (DatePicker view, int selectedYear, int selectedMonth, int selectedDay) -> {
                    String selectedDate = selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDay;
                    txtbirthdate.setText(selectedDate);
                },
                year, month, day);

        datePickerDialog.show();
    }
}