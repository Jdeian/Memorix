package com.example.memorix;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CreateQuestionnaire extends AppCompatActivity {

    private View createLayout, createNest, createBg, addQuestionnaireBtn, errorToast, errorToastLayout, toastNest, createStudyGuide, createNotes, createClass;
    private ScrollView createScroll;
    private ImageView home, note, create, library, profile;
    ProgressBar progressBar;
    EditText definition, term;
    MaterialButton createBtn;
    ScrollView scrollView;
    ViewGroup questionnaireContainer;
    TextView questionnaireNumber;
    int questionnaireCount = 1;
    TextView toastMessage, okBtn;
    RequestQueue requestqueue;
    private AtomicInteger pendingRequests = new AtomicInteger(0);
    private String classId;
    private String class_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_questionnaire);

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
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        scrollView = findViewById(R.id.questionnaire_scroll);
        definition = findViewById(R.id.definition);
        term = findViewById(R.id.term);
        term.requestFocus();
        term.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.bluegreen));
        setScrollOnFocus(term);
        setScrollOnFocus(definition);
        questionnaireContainer = findViewById(R.id.questionnaire_container);
        addQuestionnaireBtn = findViewById(R.id.addQuestionnaireBtn);
        createBtn = findViewById(R.id.createbtn);
        questionnaireNumber = findViewById(R.id.questionnaire_number);
        errorToast = findViewById(R.id.error_toast);
        errorToastLayout = findViewById(R.id.toast_layout);
        toastNest = findViewById(R.id.toastNest);
        toastMessage = findViewById(R.id.error_message);
        okBtn = findViewById(R.id.ok_btn);
        requestqueue = Volley.newRequestQueue(getApplicationContext());

        classId = getIntent().getStringExtra("class_id");
        class_name = getIntent().getStringExtra("class_name");

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

        definition.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    definition.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.bluegreen));
                } else {
                    definition.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.studySet));
                }
            }
        });

        term.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    term.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.bluegreen));
                } else {
                    term.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.studySet));
                }
            }
        });

        addQuestionnaireBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewQuestionnaire();
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
                createBtn.setEnabled(true);
                errorToast.setVisibility(View.GONE);
            }
        });

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendQuestionnaireData();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    private boolean isViewTouched(View view, float x, float y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        return (x >= viewX && x <= (viewX + view.getWidth())) &&
                (y >= viewY && y <= (viewY + view.getHeight()));
    }

    private void addNewQuestionnaire() {
        questionnaireCount++;
        LayoutInflater inflater = LayoutInflater.from(this);
        View questionnaireLayout = inflater.inflate(R.layout.activity_questionnaire, questionnaireContainer, false);

        EditText definition = questionnaireLayout.findViewById(R.id.definition);
        EditText term = questionnaireLayout.findViewById(R.id.term);
        setScrollOnFocus(definition);
        setScrollOnFocus(term);
        TextView questionnaireNum = questionnaireLayout.findViewById(R.id.questionnaire_number);

        String numberQuestionnaire = String.valueOf(questionnaireCount);
        questionnaireNum.setText(numberQuestionnaire + ".");

        questionnaireContainer.addView(questionnaireLayout);
        scrollToBottom();
    }

    private void scrollToBottom() {
        scrollView.post(() -> {
            int childCount = questionnaireContainer.getChildCount();
            if (childCount > 0) {
                View latestQuestionnaire = questionnaireContainer.getChildAt(childCount - 1);

                latestQuestionnaire.requestFocus();

                int targetY = latestQuestionnaire.getBottom();
                int startY = scrollView.getScrollY();
                int delta = targetY - startY;

                ValueAnimator animator = ValueAnimator.ofInt(0, delta);
                animator.setDuration(300);
                animator.addUpdateListener(animation -> {
                    int animatedValue = (int) animation.getAnimatedValue();
                    scrollView.scrollTo(0, startY + animatedValue);
                });
                animator.start();
            } else {
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    private void setScrollOnFocus(EditText editText) {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    editText.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.bluegreen));
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            int scrollViewTop = scrollView.getTop();
                            int[] location = new int[2];
                            v.getLocationInWindow(location);

                            int y = location[1] - scrollViewTop;
                            scrollView.smoothScrollBy(0, y - 100);
                        }
                    });
                }else {
                    editText.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.studySet));
                }
            }
        });
    }

    public void sendQuestionnaireData() {
        createBtn.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");
        String title = sharedPreferences.getString("title", "");
        String description = sharedPreferences.getString("description", "");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentDate = now.format(formatter);

        if (!isNetworkAvailable()) {
            progressBar.setVisibility(View.GONE);
            errorToast.setVisibility(View.VISIBLE);
            toastMessage.setText("Network error, Please connect to a network and try again");
            errorToast.startAnimation(fadeIn);
            return;
        }

        AtomicInteger validCount = new AtomicInteger(0);
        for (int i = 0; i < questionnaireContainer.getChildCount(); i++) {
            View questionnaireLayout = questionnaireContainer.getChildAt(i);
            EditText definitionEditText = questionnaireLayout.findViewById(R.id.definition);
            EditText termEditText = questionnaireLayout.findViewById(R.id.term);
            String definitionData = definitionEditText.getText().toString();
            String termData = termEditText.getText().toString();

            if (!definitionData.isEmpty() && !termData.isEmpty()) {
                validCount.getAndIncrement();
            }
        }
        if (validCount.get() < 4 && definition.getText().toString().isEmpty() || term.getText().toString().isEmpty()) {
            progressBar.setVisibility(View.GONE);
            errorToast.setVisibility(View.VISIBLE);
            toastMessage.setText("You must have at least 4 definitions and terms to proceed.");
            errorToast.startAnimation(fadeIn);
            return;
        } else if (validCount.get() < 3 && !definition.getText().toString().isEmpty() && !term.getText().toString().isEmpty()) {
            progressBar.setVisibility(View.GONE);
            errorToast.setVisibility(View.VISIBLE);
            toastMessage.setText("You must have at least 4 definitions and terms to proceed.");
            errorToast.startAnimation(fadeIn);
            return;
        }

        pendingRequests.set(1);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/questionnaire_insert.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("onResponse", response);

                try {
                    JSONObject jobj = new JSONObject(response);
                    int success = jobj.optInt("success", -1);
                    if (success == 1) {
                        JSONObject data = jobj.getJSONObject("data");
                        String questionnaireId = data.optString("questionnaire_id", "N/A");
                        if (!definition.getText().toString().isEmpty() && !term.getText().toString().isEmpty()) {
                            pendingRequests.incrementAndGet();
                            sendDefinitionToDatabase(questionnaireId, definition.getText().toString(), term.getText().toString());
                        } else {
                            Log.d("Skipped", "Empty definition or term at position");
                        }

                        for (int i = 0; i < questionnaireContainer.getChildCount(); i++) {
                            View questionnaireLayout = questionnaireContainer.getChildAt(i);

                            EditText definitionEditText = questionnaireLayout.findViewById(R.id.definition);
                            EditText termEditText = questionnaireLayout.findViewById(R.id.term);
                            String definitionData = definitionEditText.getText().toString();
                            String termData = termEditText.getText().toString();

                            if (!definitionData.isEmpty() && !termData.isEmpty()) {
                                pendingRequests.incrementAndGet();
                                sendDefinitionToDatabase(questionnaireId, definitionData, termData);
                            } else {
                                Log.d("Skipped", "Empty definition or term at position: " + i);
                            }
                        }
                        pendingRequests.decrementAndGet();
                        checkPendingRequests(questionnaireId);
                    } else{
                        createBtn.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    createBtn.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                createBtn.setEnabled(true);
                if (error instanceof NoConnectionError || error instanceof NetworkError) {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(getApplicationContext(), "Request Timeout. Please try again.", Toast.LENGTH_SHORT).show();
                } else if (error instanceof ServerError) {
                    Toast.makeText(getApplicationContext(), "Server Error. Please try again later.", Toast.LENGTH_SHORT).show();
                } else if (error instanceof ParseError) {
                    Toast.makeText(getApplicationContext(), "Parsing Error.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }) {

            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("user_id", userId);
                params.put("title", title);
                params.put("description", description);
                params.put("current_date", String.valueOf(currentDate));
                return params;
            }
        };

        requestqueue.add(request);
    }

    private void sendDefinitionToDatabase(String questionnaireId, String definition, String termData) {
        createBtn.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/definition_insert.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("onResponse", response);
                        try {
                            JSONObject jobj = new JSONObject(response);
                            int success = jobj.optInt("success", -1);
                            if (success == 1) {
                                JSONObject data = jobj.getJSONObject("data");
                                String definitionId = data.optString("definition_id", "N/A");
                                pendingRequests.incrementAndGet();
                                sendTermToDatabase(definitionId, termData, questionnaireId);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Inserting description failed", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            createBtn.setEnabled(true);
                            Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                        pendingRequests.decrementAndGet();
                        checkPendingRequests(questionnaireId);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        if (error.networkResponse != null) {
                            progressBar.setVisibility(View.GONE);
                            createBtn.setEnabled(true);
                            String responseBody = new String(error.networkResponse.data);
                            Log.d("Error Response", responseBody);
                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }) {

            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("questionnaire_id", questionnaireId);
                params.put("definition", definition);
                return params;
            }
        };

        requestqueue.add(request);
    }

    private void sendTermToDatabase(String definitionId, String term, String questionnaireId) {
        createBtn.setEnabled(false);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/term_insert.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("onResponse", response);
                        pendingRequests.decrementAndGet();
                        checkPendingRequests(questionnaireId);
                        try {
                            JSONObject jobj = new JSONObject(response);
                            int success = jobj.optInt("success", -1);
                            if (success == 1) {

                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Inserting term failed", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            createBtn.setEnabled(true);
                            Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        pendingRequests.decrementAndGet(); // Decrement for this term insert request
                        checkPendingRequests(questionnaireId);
                        progressBar.setVisibility(View.GONE);
                        if (error.networkResponse != null) {
                            progressBar.setVisibility(View.GONE);
                            createBtn.setEnabled(true);
                            String responseBody = new String(error.networkResponse.data);
                            Log.d("Error Response", responseBody);
                        } else {
                            Log.e("Error: ", "Error: " + error.getMessage());
                        }
                    }
                }) {

            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("definition_id", definitionId);
                params.put("term", term);
                return params;
            }
        };

        requestqueue.add(request);
    }

    private void checkPendingRequests(String questionnaireId) {
        if (pendingRequests.get() == 0 && classId == null) {
            progressBar.setVisibility(View.GONE);
            Intent intent = new Intent(getApplicationContext(), StudyGuide.class);
            intent.putExtra("questionnaire_id", questionnaireId);
            startActivity(intent);
            finish();
        } else if (pendingRequests.get() == 0 && classId != null) {
            insertClassData(questionnaireId);
        }
    }

    private void insertClassData(String questionnaireId) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/class_questionnaire_insert.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("onResponse", response);
                        try {
                            JSONObject jobj = new JSONObject(response);
                            int success = jobj.optInt("success", -1);
                            if (success == 1) {
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(getApplicationContext(), ClassContents.class);
                                intent.putExtra("class_id", classId);
                                intent.putExtra("class_name", class_name);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to insert class", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String responseBody = new String(error.networkResponse.data);
                            Log.d("Error Response", responseBody);

                            if (statusCode == 404) {
                                Animation fadeIn =  AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                                progressBar.setVisibility(View.GONE);
                                errorToast.setVisibility(View.VISIBLE);
                                toastMessage.setText("Questionnaire already exist in the class.");
                                errorToast.startAnimation(fadeIn);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }) {

            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("questionnaire_id", questionnaireId);
                params.put("class_id", classId);
                Log.d("Params", params.toString());
                return params;
            }
        };

        requestqueue.add(request);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}