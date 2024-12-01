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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
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
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudyGuide extends AppCompatActivity {

    private View errorToast, errorToastLayout, toastNest;
    TextView toastMessage, okBtn, edit, addToClass, delete;
    private View createLayout, createNest, createBg, createStudyGuide, createNotes, optionsFeature;
    private ScrollView createScroll;
    private ImageView home, note, create, library, profile, options, createClass;
    ProgressBar progressBar;
    private LinearLayout testBtn, flashcardBtn, addnewQuestionnaire, questionnaireContainer;;
    private RequestQueue requestQueue;
    private String questionnaireId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_study_guide);

        progressBar = findViewById(R.id.progressbar);
        errorToast = findViewById(R.id.error_toast);
        errorToastLayout = findViewById(R.id.toast_layout);
        toastNest = findViewById(R.id.toastNest);
        toastMessage = findViewById(R.id.error_message);
        okBtn = findViewById(R.id.ok_btn);
        createLayout = findViewById(R.id.create_layout);
        createBg = findViewById(R.id.createBg);
        createNest = findViewById(R.id.createNest);
        createScroll = findViewById(R.id.create_scroll);
        createStudyGuide = findViewById(R.id.create_study_guide);
        createNotes = findViewById(R.id.create_notes);
        createClass = findViewById(R.id.create_class);
        options = findViewById(R.id.options);
        optionsFeature = findViewById(R.id.studyguide_option);
        edit = findViewById(R.id.edit);
        addToClass = findViewById(R.id.add_to_class);
        delete = findViewById(R.id.delete);
        home = findViewById(R.id.home);
        note = findViewById(R.id.note);
        create = findViewById(R.id.create);
        library = findViewById(R.id.library);
        profile = findViewById(R.id.profile);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeIn =  AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideDown =  AnimationUtils.loadAnimation(this, R.anim.slide_down);
        testBtn = findViewById(R.id.test);
        flashcardBtn = findViewById(R.id.flashcard);
        addnewQuestionnaire = findViewById(R.id.addnew_questionnaire);
        questionnaireContainer = findViewById(R.id.questionnaire_display_container);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        questionnaireId = getIntent().getStringExtra("questionnaire_id");
        getQuestionnaireData(questionnaireId);

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

        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (optionsFeature.getVisibility() == View.GONE) {
                    optionsFeature.setVisibility(View.VISIBLE);
                }else {
                    optionsFeature.setVisibility(View.GONE);
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteQuestionnaire(questionnaireId);
            }
        });

        addToClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                optionsFeature.setVisibility(View.GONE);
                Intent intent = new Intent(getApplicationContext(), AddToClass.class);
                intent.putExtra("questionnaire_id", questionnaireId);
                startActivity(intent);
            }
        });


        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Test.class);
                intent.putExtra("questionnaire_id", questionnaireId);
                startActivity(intent);
            }
        });

        flashcardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), FlashCard.class);
                intent.putExtra("questionnaire_id", questionnaireId);
                startActivity(intent);
            }
        });

        addnewQuestionnaire.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CreateStudyGuide.class);
                startActivity(intent);
                finish();
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
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

    public void getQuestionnaireData(String questionnaireId) {
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/questionnaire_fetch.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("onResponse", response);
                try {
                    JSONObject jobj = new JSONObject(response);
                    if (jobj.has("questionnaire")) {
                        JSONArray definitionsArray = jobj.getJSONObject("definitions").names();

                        for (int i = 0; i < definitionsArray.length(); i++) {
                            progressBar.setVisibility(View.VISIBLE);
                            String defId = definitionsArray.getString(i);
                            JSONObject definition = jobj.getJSONObject("definitions").getJSONObject(defId);
                            JSONArray termsArray = definition.getJSONArray("terms");
                            String definitionText = definition.getString("definition");

                            LayoutInflater inflater = LayoutInflater.from(StudyGuide.this);
                            View questionnaireLayout = inflater.inflate(R.layout.questionnaire_and_term_layout, questionnaireContainer, false);
                            TextView definitionTextView = questionnaireLayout.findViewById(R.id.definition);
                            TextView termTextView = questionnaireLayout.findViewById(R.id.term);

                            if (termsArray.length() > 0) {
                                String termText = termsArray.getJSONObject(0).getString("term");
                                definitionTextView.setText(definitionText);
                                termTextView.setText(termText);

                                questionnaireContainer.addView(questionnaireLayout);
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                    } else {
                        Toast.makeText(getApplicationContext(), "No Data Found", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Animation fadeIn =  AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                    progressBar.setVisibility(View.GONE);
                    errorToast.setVisibility(View.VISIBLE);
                    toastMessage.setText("Network error. Check your network connection.");
                    errorToast.startAnimation(fadeIn);
                }
                progressBar.setVisibility(View.GONE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Log.d("QUESTIONNAIRE ID", questionnaireId + "HERE QUESTIONNAIRE ID");
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);
                params.put("questionnaire_id", questionnaireId);
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void deleteQuestionnaire(String questionnaireId) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/questionnaire_delete.php",
                response -> {
                    try {
                        Log.d("Response", response);
                        JSONObject jobj = new JSONObject(response);
                        int success = jobj.optInt("success", -1);
                        if (success == 1) {
                            progressBar.setVisibility(View.GONE);
                            Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                            errorToast.setVisibility(View.VISIBLE);
                            toastMessage.setText("Questionnaire deleted successfully!");
                            errorToast.startAnimation(fadeIn);

                            okBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    optionsFeature.setVisibility(View.GONE);
                                    Intent intent = new Intent(getApplicationContext(), Library.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                            errorToastLayout.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View view, MotionEvent event) {
                                    float x = event.getRawX();
                                    float y = event.getRawY();

                                    if (!isViewTouched(toastNest, x, y)) {
                                        errorToast.setVisibility(View.GONE);
                                        optionsFeature.setVisibility(View.GONE);
                                        Intent intent = new Intent(getApplicationContext(), Library.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    return true;
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to delete questionnaire", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error updating note", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("questionnaire_id", questionnaireId);
                return params;
            }
        };

        requestQueue.add(request);
    }


}