package com.example.memorix;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class TestResult extends AppCompatActivity {

    private View retakeTestBtn;
    private PieChart pieChart;
    private LinearLayout resultContainer;
    private ScrollView resultScroll;
    private ProgressBar progressBar;
    MaterialButton exitBtn;
    int questionsCount = 0;
    TextView percentText, correctAnswersText, incorrectAnswersText;
    View correctAndIncorrectTexts;
    private String questionnaireId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_test_result);

        progressBar = findViewById(R.id.progressbar);
        resultContainer = findViewById(R.id.result_container);
        pieChart = findViewById(R.id.pie_chart);
        resultScroll = findViewById(R.id.result_scroll);
        percentText = findViewById(R.id.holeText);
        correctAnswersText = findViewById(R.id.correct_answers);
        incorrectAnswersText = findViewById(R.id.incorrect_answers);
        correctAndIncorrectTexts = findViewById(R.id.correct_incorrect_result_layout);
        exitBtn = findViewById(R.id.exitbtn);
        retakeTestBtn = findViewById(R.id.retakeTestBtn);
        questionnaireId = getIntent().getStringExtra("questionnaire_id");

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

        retakeTestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), Test.class);
                intent.putExtra("questionnaire_id", questionnaireId);
                startActivity(intent);
                finish();
            }
        });

        String jsonQuestionData = getIntent().getStringExtra("QUESTION_DATA");
        if (jsonQuestionData != null) {
            List<QuestionData> questionDataList = new Gson().fromJson(jsonQuestionData, new TypeToken<List<QuestionData>>(){}.getType());
            int correctAnswers = getIntent().getIntExtra("CORRECT_ANSWERS", 0);
            int wrongAnswers = getIntent().getIntExtra("WRONG_ANSWERS", 0);

            displayResults(questionDataList);
            setupPieChart(correctAnswers, wrongAnswers);
        } else {
            TextView noDataTextView = findViewById(R.id.textView21);
            noDataTextView.setVisibility(View.VISIBLE);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void displayResults(List<QuestionData> questionDataList) {
        resultContainer.removeAllViews();

        Log.d("TestResult", "Number of questions: " + questionDataList.size());

        for (QuestionData data : questionDataList) {
            questionsCount++;
            Log.d("TestResult", "Displaying question: " + data.getQuestion());

            View resultView = getLayoutInflater().inflate(R.layout.questions_result, null);
            TextView questionText = resultView.findViewById(R.id.result_question);
            TextView choice1 = resultView.findViewById(R.id.result_choice1);
            TextView choice2 = resultView.findViewById(R.id.result_choice2);
            TextView choice3 = resultView.findViewById(R.id.result_choice3);
            TextView choice4 = resultView.findViewById(R.id.result_choice4);
            ImageView resultStatus = resultView.findViewById(R.id.checker_icon);

            String count = String.valueOf(questionsCount);
            String numberQuestion = (count);
            TextView questionNumber = resultView.findViewById(R.id.question_number);
            questionNumber.setText(numberQuestion + ".");

            questionText.setText(data.getQuestion());
            List<String> choices = data.getChoices();
            if (choices.size() >= 4) {
                choice1.setText(choices.get(0));
                choice2.setText(choices.get(1));
                choice3.setText(choices.get(2));
                choice4.setText(choices.get(3));
            }

            if (!data.getSelectedAnswer().equals(data.getCorrectAnswer())) {
                resultStatus.setImageResource(R.drawable.wrong_icon);
                changeChoicesBorderWrong(choice1, data.getSelectedAnswer(), data.getCorrectAnswer());
                changeChoicesBorderWrong(choice2, data.getSelectedAnswer(), data.getCorrectAnswer());
                changeChoicesBorderWrong(choice3, data.getSelectedAnswer(),  data.getCorrectAnswer());
                changeChoicesBorderWrong(choice4, data.getSelectedAnswer(), data.getCorrectAnswer());
            } else {
                changeChoicesBorderCorrect(choice1, data.getSelectedAnswer(), data.getCorrectAnswer());
                changeChoicesBorderCorrect(choice2, data.getSelectedAnswer(), data.getCorrectAnswer());
                changeChoicesBorderCorrect(choice3, data.getSelectedAnswer(),  data.getCorrectAnswer());
                changeChoicesBorderCorrect(choice4, data.getSelectedAnswer(), data.getCorrectAnswer());
            }
            resultContainer.addView(resultView);
        }
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
    }

    private boolean isViewTouched(View view, float x, float y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        return (x >= viewX && x <= (viewX + view.getWidth())) &&
                (y >= viewY && y <= (viewY + view.getHeight()));
    }


    private void setupPieChart(int correctCount, int wrongCount) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(correctCount, ""));
        entries.add(new PieEntry(wrongCount, ""));

        PieDataSet dataSet = new PieDataSet(entries, "Results");
        List<Integer> colors = new ArrayList<>();
        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.bluegreen));
        colors.add(ContextCompat.getColor(getApplicationContext(), R.color.piechart_wrong));
        dataSet.setColors(colors);

        dataSet.setDrawValues(false);
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.invalidate();
        pieChart.getLegend().setEnabled(false);

        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(10f);
        pieChart.setHoleColor(ContextCompat.getColor(getApplicationContext(), R.color.black));

        int totalQuestions = correctCount + wrongCount;
        int correctPercent = (correctCount * 100) / totalQuestions;
        percentText.setText(correctPercent + "%");

        if (correctPercent == 100) {
            ViewGroup.LayoutParams layoutParams = pieChart.getLayoutParams();
            layoutParams.width = 510;
            layoutParams.height = 530;
            pieChart.setLayoutParams(layoutParams);
        } else if (correctPercent == 0) {
            LinearLayout.LayoutParams layoutParams =  (LinearLayout.LayoutParams) pieChart.getLayoutParams();
            layoutParams.width = 580;
            layoutParams.height = 560;
            layoutParams.setMargins(-20, 0, 10, 0);
            pieChart.setLayoutParams(layoutParams);
        }

        String correctCountText = String.valueOf(correctCount);
        String incorrectCountText = String.valueOf(wrongCount);
        correctAnswersText.setText(correctCountText);
        incorrectAnswersText.setText(incorrectCountText);

    }

    private void changeChoicesBorderWrong(TextView choice, String selectedChoice, String correctChoice) {
        String choiceText = choice.getText().toString();
        if (choiceText.equals(correctChoice)) {
            choice.setBackgroundResource(R.drawable.result_correct_question_bg);
        } else if(choiceText.equals(selectedChoice)) {
            choice.setBackgroundResource(R.drawable.wrong_question_result);
        }else {
            choice.setBackgroundResource(R.drawable.questions_bg);
        }
    }
    private void changeChoicesBorderCorrect(TextView choice, String selectedChoice, String correctChoice) {
        String choiceText = choice.getText().toString();
        if(choiceText.equals(selectedChoice) && choiceText.equals(correctChoice)) {
            choice.setBackgroundResource(R.drawable.result_correct_question_bg);
        }else {
            choice.setBackgroundResource(R.drawable.questions_bg);
        }
    }
}
