package com.example.memorix;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Test extends AppCompatActivity {

    private View submitBtn, errorToast, errorToastLayout, toastNest;
    private ScrollView createScroll;
    ProgressBar progressBar;
    ScrollView scrollView;
    ViewGroup questionsContainer;
    MaterialButton exitBtn;
    RequestQueue requestqueue;
    TextView toastMessage, okBtn;
    private int correctAnswers = 0;
    private int wrongAnswers = 0;
    private String questionnaireId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test);

        progressBar = findViewById(R.id.progressbar);
        createScroll = findViewById(R.id.create_scroll);
        exitBtn = findViewById(R.id.exitbtn);
        errorToast = findViewById(R.id.error_toast);
        errorToastLayout = findViewById(R.id.toast_layout);
        toastNest = findViewById(R.id.toastNest);
        toastMessage = findViewById(R.id.error_message);
        okBtn = findViewById(R.id.ok_btn);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeIn =  AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideDown =  AnimationUtils.loadAnimation(this, R.anim.slide_down);
        scrollView = findViewById(R.id.questions_scroll);
        questionsContainer = findViewById(R.id.questions_container);
        submitBtn = findViewById(R.id.submitbtn);
        requestqueue = Volley.newRequestQueue(getApplicationContext());

        questionnaireId = getIntent().getStringExtra("questionnaire_id");
        getQuestionnaireData(questionnaireId);

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), StudyGuide.class);
                intent.putExtra("questionnaire_id", questionnaireId);
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), StudyGuide.class);
        intent.putExtra("questionnaire_id", questionnaireId);
        startActivity(intent);
        finish();
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

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/questionnaire_fetch.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("onResponse", response);
                try {
                    JSONObject jobj = new JSONObject(response);
                    if (jobj.has("questionnaire")) {
                        JSONArray definitionsArray = jobj.getJSONObject("definitions").names();

                        final HashMap<String, String> savedAnswers = new HashMap<>();
                        List<QuestionData> questionDataList = new ArrayList<>();

                        for (int i = 0; i < definitionsArray.length(); i++) {
                            String defId = definitionsArray.getString(i);
                            JSONObject definition = jobj.getJSONObject("definitions").getJSONObject(defId);
                            JSONArray termsArray = definition.getJSONArray("terms");

                            String correctTerm = "";
                            if (termsArray.length() > 0) {
                                JSONObject correctTermObj = termsArray.getJSONObject(0);
                                correctTerm = correctTermObj.getString("term");
                            }

                            List<String> termList = new ArrayList<>();
                            termList.add(correctTerm);

                            for (int k = 0; k < definitionsArray.length(); k++) {
                                if (!definitionsArray.getString(k).equals(defId)) {
                                    JSONArray otherTerms = jobj.getJSONObject("definitions").getJSONObject(definitionsArray.getString(k)).getJSONArray("terms");
                                    for (int l = 0; l < otherTerms.length(); l++) {
                                        if (termList.size() >= 4) break;
                                        String randomTerm = otherTerms.getJSONObject(l).getString("term");
                                        termList.add(randomTerm);
                                    }
                                }
                                if (termList.size() >= 4) break;
                            }

                            LayoutInflater inflater = LayoutInflater.from(Test.this);
                            View questionsLayout = inflater.inflate(R.layout.questions, questionsContainer, false);

                            String numberQuestion = String.valueOf(i + 1);
                            TextView questionNumber = questionsLayout.findViewById(R.id.question_number);
                            questionNumber.setText(numberQuestion + ".");
                            TextView questionText = questionsLayout.findViewById(R.id.question);
                            questionText.setText(definition.getString("definition"));

                            TextView choice1 = questionsLayout.findViewById(R.id.choice1);
                            TextView choice2 = questionsLayout.findViewById(R.id.choice2);
                            TextView choice3 = questionsLayout.findViewById(R.id.choice3);
                            TextView choice4 = questionsLayout.findViewById(R.id.choice4);

                            Collections.shuffle(termList);
                            choice1.setText(termList.get(0));
                            choice2.setText(termList.get(1));
                            choice3.setText(termList.get(2));
                            choice4.setText(termList.get(3));

                            questionsContainer.addView(questionsLayout);
                            progressBar.setVisibility(View.VISIBLE);

                            submitBtn.setEnabled(false);
                            final String correctAnswer = correctTerm;
                            final int index = i;

                            View.OnClickListener choiceClickListener = new View.OnClickListener() {
                                TextView lastSelectedChoice = null;
                                @Override
                                public void onClick(View view) {
                                    TextView selectedChoice = (TextView) view;
                                    String selectedAnswer = selectedChoice.getText().toString();

                                    savedAnswers.put(defId, selectedAnswer);

                                    QuestionData existingQuestionData = null;
                                    for (QuestionData qData : questionDataList) {
                                        try {
                                            if (qData.getQuestion().equals(definition.getString("definition"))) {
                                                existingQuestionData = qData;
                                                break;
                                            }
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                    }

                                    if (existingQuestionData != null) {
                                        String previousAnswer = existingQuestionData.getSelectedAnswer();
                                        existingQuestionData.setSelectedAnswer(selectedAnswer);

                                        if (previousAnswer.equals(correctAnswer) && !selectedAnswer.equals(correctAnswer)) {
                                            correctAnswers--;
                                            wrongAnswers++;
                                        } else if (!previousAnswer.equals(correctAnswer) && selectedAnswer.equals(correctAnswer)) {
                                            correctAnswers++;
                                            wrongAnswers--;
                                        }
                                    } else {
                                        try {
                                            questionDataList.add(new QuestionData(
                                                    definition.getString("definition"),
                                                    Arrays.asList(choice1.getText().toString(), choice2.getText().toString(), choice3.getText().toString(), choice4.getText().toString()),
                                                    selectedAnswer,
                                                    correctAnswer
                                            ));
                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }

                                        if (selectedAnswer.equals(correctAnswer)) {
                                            correctAnswers++;
                                        } else {
                                            wrongAnswers++;
                                        }
                                    }

                                    if (lastSelectedChoice != null) {
                                        lastSelectedChoice.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.choices));
                                    }
                                    selectedChoice.setBackgroundTintList(ContextCompat.getColorStateList(getApplicationContext(), R.color.bluegreen));

                                    lastSelectedChoice = selectedChoice;

                                    submitBtn.setEnabled(true);

                                    if (index + 1 < definitionsArray.length()) {
                                        View nextQuestion = questionsContainer.getChildAt(index + 1);
                                        scrollToNextQuestion(nextQuestion);
                                    }
                                }
                            };

                            submitBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (savedAnswers.size() < definitionsArray.length()) {
                                        Animation fadeIn =  AnimationUtils.loadAnimation(Test.this, R.anim.fade_in);
                                        progressBar.setVisibility(View.GONE);
                                        errorToast.setVisibility(View.VISIBLE);
                                        toastMessage.setText("Please answer all questions before submitting.");
                                        errorToast.startAnimation(fadeIn);
                                        return;
                                    }

                                    Intent intent = new Intent(Test.this, TestResult.class);
                                    intent.putExtra("questionnaire_id", questionnaireId);
                                    intent.putExtra("QUESTION_DATA", new Gson().toJson(questionDataList));
                                    intent.putExtra("CORRECT_ANSWERS", correctAnswers);
                                    intent.putExtra("WRONG_ANSWERS", wrongAnswers);
                                    startActivity(intent);
                                    finish();
                                }
                            });

                            choice1.setOnClickListener(choiceClickListener);
                            choice2.setOnClickListener(choiceClickListener);
                            choice3.setOnClickListener(choiceClickListener);
                            choice4.setOnClickListener(choiceClickListener);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No Data Found", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                    Log.d("ERROR: ", e.toString());
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
                Map<String, String> params = new HashMap<>();
                params.put("questionnaire_id", questionnaireId);
                return params;
            }
        };

        requestqueue.add(request);
    }


    private void scrollToNextQuestion(View nextQuestion) {
        ScrollView scrollView = findViewById(R.id.questions_scroll);
        int targetY = nextQuestion.getBottom();
        int startY = scrollView.getScrollY();
        int delta = targetY - startY - 1000;

        ValueAnimator animator = ValueAnimator.ofInt(0, delta);
        animator.setDuration(300);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int animatedValue = (int) animation.getAnimatedValue();
                scrollView.scrollTo(0, startY + animatedValue);
            }
        });
        animator.start();
    }
}