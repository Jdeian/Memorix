package com.example.memorix;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Payment extends AppCompatActivity {

    private ImageView gcash, paymaya, cash;
    private LinearLayout gobackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);

        gcash = findViewById(R.id.gcash);
        paymaya = findViewById(R.id.paymaya);
        cash = findViewById(R.id.cash);
        gobackBtn = findViewById(R.id.goback_btn);

        gcash.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), GcashPayment.class);
            startActivity(intent);
        });

        paymaya.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PayMayaPayment.class);
            startActivity(intent);
        });

        gobackBtn.setOnClickListener(v ->  {
            Intent intent = new Intent(getApplicationContext(), Subcription.class);
            startActivity(intent);
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}