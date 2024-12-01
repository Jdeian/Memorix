package com.example.memorix;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class CreateStudyGuide extends AppCompatActivity {

    private View createLayout, createNest, createBg, errorToast, errorToastLayout, toastNest, createStudyGuide, createNotes;;
    private ScrollView createScroll;
    private ImageView home, note, create, library, profile, createClass;
    ProgressBar progressBar;
    EditText title, description;
    MaterialButton continueBtn;
    RequestQueue requestqueue;
    TextView toastMessage, okBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_study_guide);

        progressBar = findViewById(R.id.progressbar);
        createLayout = findViewById(R.id.create_layout);
        createBg = findViewById(R.id.createBg);
        createNest = findViewById(R.id.createNest);
        createScroll = findViewById(R.id.create_scroll);
        createStudyGuide = findViewById(R.id.create_study_guide);
        createNotes = findViewById(R.id.create_notes);
        createClass = findViewById(R.id.create_class);
        home = findViewById(R.id.home);
        note = findViewById(R.id.note);
        create = findViewById(R.id.create);
        library = findViewById(R.id.library);
        profile = findViewById(R.id.profile);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeIn =  AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideDown =  AnimationUtils.loadAnimation(this, R.anim.slide_down);
        title = findViewById(R.id.title);
        description = findViewById(R.id.description);
        continueBtn = findViewById(R.id.continuebtn);
        title.requestFocus();
        errorToast = findViewById(R.id.error_toast);
        errorToastLayout = findViewById(R.id.toast_layout);
        toastNest = findViewById(R.id.toastNest);
        toastMessage = findViewById(R.id.error_message);
        okBtn = findViewById(R.id.ok_btn);
        requestqueue = Volley.newRequestQueue(getApplicationContext());

        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String category = sharedPreferences.getString("category", "");
        if ((category.equals("Premium") || category.equals("Basic"))) {
            createClass.setImageResource(R.drawable.create_class);

            createClass.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), CreateClass.class);
                startActivity(intent);
            });
        } else {
            createClass.setImageResource(R.drawable.create_class_lock);
        }

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createLayout.setVisibility(View.VISIBLE);
                createScroll.post(() -> createScroll.scrollTo(0, 500));
                createLayout.startAnimation(fadeIn);
                createNest.startAnimation(slideUp);
            }
        });

        createStudyGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.remove("questionnaire_id");
                editor.apply();

                Intent intent = new Intent(getApplicationContext(), CreateStudyGuide.class);
                startActivity(intent);
                finish();
            }
        });

        createNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), CreateNote.class);
                startActivity(intent);
                finish();
            }
        });

        createBg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                float x = event.getRawX();
                float y = event.getRawY();

                if (!isViewTouched(createNest, x, y)) {
                    createNest.startAnimation(slideDown);
                    slideDown.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            // empty
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            createLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                            //empty
                        }
                    });
                    return true;
                }
                return false;
            }
        });

        createScroll.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                Log.d("ScrollTest", "Scroll detected, scrollY: " + scrollY + ", oldScrollY: " + oldScrollY);

                if (scrollY < oldScrollY) {
                    createNest.startAnimation(slideDown);
                    slideDown.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            // empty
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            createLayout.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                            //empty
                        }
                    });

                }
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), Note.class);
                startActivity(intent);
                finish();
            }
        });

        library.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), Library.class);
                startActivity(intent);
                finish();
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), Profile.class);
                startActivity(intent);
                finish();
            }
        });

        title.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    title.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.bluegreen));
                } else {
                    title.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.studySet));
                }
            }
        });

        description.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    description.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.bluegreen));
                } else {
                    description.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.studySet));
                }
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                if (!title.getText().toString().isEmpty()) {
                    String classId = getIntent().getStringExtra("class_id");
                    String class_name = getIntent().getStringExtra("class_name");
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("title", title.getText().toString());
                    editor.putString("description", description.getText().toString());
                    editor.apply();

                    Intent intent = new Intent(getApplicationContext(), StudyGuideOption.class);
                    intent.putExtra("class_id", classId);
                    intent.putExtra("class_name", class_name);
                    startActivity(intent);
                    finish();
                }else {
                    progressBar.setVisibility(View.GONE);
                    errorToast.setVisibility(View.VISIBLE);
                    toastMessage.setText("Please add a title to continue.");
                    errorToast.startAnimation(fadeIn);
                }
            }
        });

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
        createLayout.setVisibility(View.GONE);
    }


    private boolean isViewTouched(View view, float x, float y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        return (x >= viewX && x <= (viewX + view.getWidth())) &&
                (y >= viewY && y <= (viewY + view.getHeight()));
    }
}