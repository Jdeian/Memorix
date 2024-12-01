package com.example.memorix;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

public class OTPVerification extends AppCompatActivity {

    private RelativeLayout progressBar;;
    private Toast customizedToast;
    private TextView toastMessage;
    private MaterialButton confirmBtn;
    private EditText otp1, otp2, otp3, otp4, otp5, otp6;
    private RequestQueue requestqueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_otpverification);

        progressBar = findViewById(R.id.progressbar);
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.customized_toast, (ViewGroup)findViewById(R.id.toastLayout));
        toastMessage = layout.findViewById(R.id.toastMessage);
        customizedToast = new Toast(getApplicationContext());
        customizedToast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 1);
        customizedToast.setDuration(Toast.LENGTH_LONG);
        customizedToast.setView(layout);

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        confirmBtn = findViewById(R.id.confirmbtn);
        requestqueue = Volley.newRequestQueue(getApplicationContext());

        setOtpTextWatchers();

        confirmBtn.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String otp1text = otp1.getText().toString().trim();
            String otp2text = otp2.getText().toString().trim();
            String otp3text = otp3.getText().toString().trim();
            String otp4text = otp4.getText().toString().trim();
            String otp5text = otp5.getText().toString().trim();
            String otp6text = otp6.getText().toString().trim();
            if (otp1text.isEmpty() || otp2text.isEmpty() || otp3text.isEmpty()
               || otp4text.isEmpty() || otp5text.isEmpty() || otp6text.isEmpty()) {
                toastMessage.setText("Please enter a 6-digit OTP to continue.");
                otp1.setBackgroundResource(R.drawable.email_otp_error);
                otp2.setBackgroundResource(R.drawable.email_otp_error);
                otp3.setBackgroundResource(R.drawable.email_otp_error);
                otp4.setBackgroundResource(R.drawable.email_otp_error);
                otp5.setBackgroundResource(R.drawable.email_otp_error);
                otp6.setBackgroundResource(R.drawable.email_otp_error);
                customizedToast.show();
                progressBar.setVisibility(View.GONE);
                return;
            }
            String enteredOtp = otp1text + otp2text + otp3text + otp4text + otp5text + otp6text;
            verifyOtp(enteredOtp);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setOtpTextWatchers() {
        EditText[] otpFields = {otp1, otp2, otp3, otp4, otp5, otp6};

        for (int i = 0; i < otpFields.length; i++) {
            final int currentIndex = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 1 && currentIndex < otpFields.length - 1) {
                        otpFields[currentIndex + 1].requestFocus();
                    } else if (s.length() == 0 && currentIndex > 0) {
                        otpFields[currentIndex - 1].requestFocus();
                    }
                }
            });
        }
    }

    public void verifyOtp(String enteredOtp) {
        String otp = getIntent().getStringExtra("otp");
        String email = getIntent().getStringExtra("email");

        if(enteredOtp.equals(otp)) {
            Intent intent = new Intent(getApplicationContext(), NewPassword.class);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
        } else {
            progressBar.setVisibility(View.VISIBLE);
            toastMessage.setText("The OTP you entered is incorrect. Please try again.");
            otp1.setBackgroundResource(R.drawable.email_otp_error);
            otp2.setBackgroundResource(R.drawable.email_otp_error);
            otp3.setBackgroundResource(R.drawable.email_otp_error);
            otp4.setBackgroundResource(R.drawable.email_otp_error);
            otp5.setBackgroundResource(R.drawable.email_otp_error);
            otp6.setBackgroundResource(R.drawable.email_otp_error);
            customizedToast.show();
            progressBar.setVisibility(View.GONE);
        }
    }
}