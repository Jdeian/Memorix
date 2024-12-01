package com.example.memorix;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StudyGuideOption extends AppCompatActivity {

    private LinearLayout selectImages, createManually;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_study_guide_option);

        selectImages = findViewById(R.id.select_images);
        createManually = findViewById(R.id.create_manually);
        String classId = getIntent().getStringExtra("class_id");
        String class_name = getIntent().getStringExtra("class_name");

        selectImages.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ImageScan.class);
            intent.putExtra("class_id", classId);
            intent.putExtra("class_name", class_name);
            startActivity(intent);
        });

        createManually.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), CreateQuestionnaire.class);
            intent.putExtra("class_id", classId);
            intent.putExtra("class_name", class_name);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}