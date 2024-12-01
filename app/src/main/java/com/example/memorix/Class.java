package com.example.memorix;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class Class extends AppCompatActivity {

    private View errorToast, errorToastLayout, toastNest, progressBar;
    private TextView toastMessage, okBtn, joinBtn;
    private View classOption, classCodeJoin;
    private TextView addClass, joinClass, closePopup;
    private EditText joinCode;
    private ImageView options, pasteCode;
    private LinearLayout classCodeLayout, classCodeNest, classContainer, emptyClass;
    private RequestQueue requestQueue;
    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_class);

        progressBar = findViewById(R.id.progressbar);
        emptyClass = findViewById(R.id.empty_class);
        errorToast = findViewById(R.id.error_toast);
        errorToastLayout = findViewById(R.id.toast_layout);
        toastNest = findViewById(R.id.toastNest);
        toastMessage = findViewById(R.id.error_message);
        okBtn = findViewById(R.id.ok_btn);
        options = findViewById(R.id.options);
        pasteCode = findViewById(R.id.paste);
        addClass = findViewById(R.id.add_class);
        joinClass = findViewById(R.id.join_class);
        joinCode = findViewById(R.id.join_class_code);
        joinBtn = findViewById(R.id.join);
        classOption = findViewById(R.id.class_option);
        classCodeLayout = findViewById(R.id.code_layout);
        classCodeNest = findViewById(R.id.class_code_nest);
        classCodeJoin = findViewById(R.id.join_class_popup);
        closePopup = findViewById(R.id.close_class_code);
        classContainer = findViewById(R.id.class_container);
        requestQueue = Volley.newRequestQueue(this);

        fetchClass();

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
                if(classOption.getVisibility() == View.GONE) {
                    classOption.setVisibility(View.VISIBLE);
                }else {
                    classOption.setVisibility(View.GONE);
                }
            }
        });

        addClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), CreateClass.class);
                startActivity(intent);
            }
        });

        joinClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classOption.setVisibility(View.GONE);
                classCodeJoin.setVisibility(View.VISIBLE);
            }
        });

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkClassCode(joinCode.getText().toString());
            }
        });


        pasteCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pasteClassCodeFromClipboard();
            }
        });

        closePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classCodeJoin.setVisibility(View.GONE);
            }
        });

        classCodeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                float x = event.getRawX();
                float y = event.getRawY();

                if (!isViewTouched(classCodeNest, x, y)) {
                    classCodeJoin.setVisibility(View.GONE);
                }
                return true;
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
        Intent intent = new Intent(getApplicationContext(), Library.class);
        startActivity(intent);
        finish();
    }

    private void pasteClassCodeFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = clipboard.getPrimaryClip();

        if (clip != null && clip.getItemCount() > 0) {
            String classCode = clip.getItemAt(0).getText().toString();
            joinCode.setText(classCode);
        } else {
            Toast.makeText(this, "No class code in clipboard.", Toast.LENGTH_SHORT).show();
        }
    }


    private boolean isViewTouched(View view, float x, float y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        return (x >= viewX && x <= (viewX + view.getWidth())) &&
                (y >= viewY && y <= (viewY + view.getHeight()));
    }

    private void fetchClass() {
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/class_fetch.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray classArray = jsonResponse.getJSONArray("class");

                        if (classArray.length() < 1) {
                            emptyClass.setVisibility(View.VISIBLE);
                        }

                        for (int i = 0; i < classArray.length(); i++) {
                            JSONObject classObject = classArray.getJSONObject(i);
                            classId = classObject.getString("class_id");
                            String className = classObject.getString("class_name");
                            fetchQuestionnaireCount(classId, className);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                        progressBar.setVisibility(View.GONE);
                        errorToast.setVisibility(View.VISIBLE);
                        toastMessage.setText("Network error. Check your network connection.");
                        errorToast.startAnimation(fadeIn);
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error fetching classes", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void checkClassCode(final String classCode) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST,
                "https://pink-boar-882869.hostingersite.com/join_class.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");

                        if (status.equals("success")) {
                            String classId = jsonResponse.getString("class_id");
                            String className = jsonResponse.getString("class_name");
                            fetchQuestionnaireCount(classId, className);
                            classCodeJoin.setVisibility(View.GONE);
                            Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                            progressBar.setVisibility(View.GONE);
                            errorToast.setVisibility(View.VISIBLE);
                            toastMessage.setText("Successfully joined the course!");
                            errorToast.startAnimation(fadeIn);
                            emptyClass.setVisibility(View.GONE);
                        } else {
                            Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                            progressBar.setVisibility(View.GONE);
                            errorToast.setVisibility(View.VISIBLE);
                            toastMessage.setText("Invalid class code, Please enter a valid one.");
                            errorToast.startAnimation(fadeIn);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error processing response.", Toast.LENGTH_SHORT).show();
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error checking class code.", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("class_code", classCode);
                params.put("member_id", userId);
                Log.d("PARAMS: ", params.toString());
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void fetchQuestionnaireCount(String classId, String className) {
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/class_questionnaire_fetch.php",
                response -> {
                    Log.d("ServerResponse", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray questionnaireArray = jsonResponse.getJSONArray("questionnaire");
                        int questionnaireCounts = questionnaireArray.length();
                        addClassToLayout(classId, className, questionnaireCounts);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Animation fadeIn =  AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                        progressBar.setVisibility(View.GONE);
                        errorToast.setVisibility(View.VISIBLE);
                        toastMessage.setText("Network error. Check your network connection.");
                        errorToast.startAnimation(fadeIn);
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error fetching questionnaire counts", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("class_id", classId);
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void addClassToLayout(String classId, String className, int questionnaireCount) {
        progressBar.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(this);
        View classLayout = inflater.inflate(R.layout.created_class_layout, null);

        TextView classNameView = classLayout.findViewById(R.id.class_name);
        TextView questionnaireCountText = classLayout.findViewById(R.id.questionnaire_count);

        classNameView.setText(className);
        questionnaireCountText.setText(questionnaireCount + " questionnaire/s");

        classLayout.setOnClickListener(v -> {
            classOption.setVisibility(View.GONE);
            Intent intent = new Intent(getApplicationContext(), ClassContents.class);
            intent.putExtra("class_id", classId);
            intent.putExtra("class_name", className);
            startActivity(intent);
        });

        classContainer.addView(classLayout);
    }
}