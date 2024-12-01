package com.example.memorix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class GcashPayment extends AppCompatActivity {

    private ViewPager2 viewPager;
    private View circle1, circle2, circle3;
    private LinearLayout indicators, paymentForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gcash_payment);

        viewPager = findViewById(R.id.viewPager);
        indicators = findViewById(R.id.indicators);
        paymentForm = findViewById(R.id.go_to_payment_form);

        List<ImageData> images = new ArrayList<>();
        circle1 = findViewById(R.id.circle1);
        circle2 = findViewById(R.id.circle2);
        circle3 = findViewById(R.id.circle3);

        images.add(new ImageData(R.drawable.gcash_slide1));
        images.add(new ImageData(R.drawable.gcash_slide2));
        images.add(new ImageData(R.drawable.gcash_slide3));

        ImageSliderAdapter adapter = new ImageSliderAdapter(images);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateIndicators(position);
            }
        });

        updateIndicators(0);

        paymentForm.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), PaymentForm.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void updateIndicators(int position) {
        circle1.setBackgroundResource(position == 0 ? R.drawable.circle_indicator_active : R.drawable.circle_indicator);
        circle2.setBackgroundResource(position == 1 ? R.drawable.circle_indicator_active : R.drawable.circle_indicator);
        circle3.setBackgroundResource(position == 2 ? R.drawable.circle_indicator_active : R.drawable.circle_indicator);
    }
}