package com.example.memorix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class FlashCard extends AppCompatActivity {

    private View errorToast, errorToastLayout, toastNest;
    TextView toastMessage, okBtn;
    private ViewPager2 viewPager;
    private RelativeLayout progressBar;
    private RequestQueue requestQueue;
    List<FlashCardData> flashcardList = new ArrayList<>();
    private FlashCardAdapter adapter;
    private ImageView shuffleBtn, playBtn;
    private MaterialButton exitBtn;
    private Handler handler;
    private Runnable autoPlayRunnable;
    private boolean isAutoPlaying = false;
    private final int AUTO_PLAY_DELAY = 4000;
    private int currentIndex = 0;
    private String questionnaireId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flash_card);

        progressBar = findViewById(R.id.progressbar);
        errorToast = findViewById(R.id.error_toast);
        errorToastLayout = findViewById(R.id.toast_layout);
        toastNest = findViewById(R.id.toastNest);
        toastMessage = findViewById(R.id.error_message);
        okBtn = findViewById(R.id.ok_btn);
        exitBtn = findViewById(R.id.exitbtn);
        viewPager = findViewById(R.id.cards_container);
        viewPager.setOffscreenPageLimit(3);
        shuffleBtn = findViewById(R.id.shuffle);
        playBtn = findViewById(R.id.play);
        handler = new Handler();
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        questionnaireId = getIntent().getStringExtra("questionnaire_id");
        getFlashcardData(questionnaireId);

        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StudyGuide.class);
                intent.putExtra("questionnaire_id", questionnaireId);
                startActivity(intent);
                finish();
            }
        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shuffleFlashcards();
            }
        });

        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentIndex = 0;
                toggleAutoPlay();
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

    public void getFlashcardData(String questionnaireId) {
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/questionnaire_fetch.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("onResponse", response);
                try {
                    JSONObject jobj = new JSONObject(response);
                    if (jobj.has("questionnaire")) {
                        JSONArray definitionsArray = jobj.getJSONObject("definitions").names();

                        for (int i = 0; i < definitionsArray.length(); i++) {
                            String defId = definitionsArray.getString(i);
                            JSONObject definition = jobj.getJSONObject("definitions").getJSONObject(defId);
                            JSONArray termsArray = definition.getJSONArray("terms");

                            String definitionText = definition.getString("definition");
                            if (termsArray.length() > 0) {
                                String termText = termsArray.getJSONObject(0).getString("term");
                                flashcardList.add(new FlashCardData(termText, definitionText));
                            }
                        }
                        displayFlashcards();
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
                Map<String, String> params = new HashMap<>();
                params.put("questionnaire_id", questionnaireId);
                return params;
            }
        };

        requestQueue.add(request);
    }

    public void displayFlashcards() {
        adapter = new FlashCardAdapter(flashcardList);
        viewPager.setAdapter(adapter);
        viewPager.setPageTransformer(new FlipPageTransformer());
    }

    private void shuffleFlashcards() {
        stopAutoPlay();
        for (int i = 0; i < flashcardList.size(); i++) {
            View cardView = viewPager.findViewWithTag("front_" + i);
            if (cardView != null) {
                cardView.setScaleX(0);
                cardView.setScaleY(0);
                cardView.animate()
                        .scaleX(1)
                        .scaleY(1)
                        .setDuration(350)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
            }
        }

        handler.postDelayed(() -> {
            Collections.shuffle(flashcardList);
            displayFlashcards();
        }, 300);
    }

    private void toggleAutoPlay() {
        isAutoPlaying = !isAutoPlaying;
        if (isAutoPlaying) {
            startAutoPlay();
        } else {
            stopAutoPlay();
        }
    }

    private void startAutoPlay() {
        playBtn.setImageResource(R.drawable.pause_icon);
        isAutoPlaying = true;
        flipToNextCard();
    }


    private void flipToNextCard() {
        if (currentIndex < flashcardList.size()) {
            viewPager.setCurrentItem(currentIndex, true);

            handler.postDelayed(() -> {
                flipCard(currentIndex, new FlipCallback() {
                    @Override
                    public void onFlipComplete() {
                        currentIndex++;
                        if (currentIndex < flashcardList.size()) {
                            handler.postDelayed(() -> flipToNextCard(), 6000);
                        } else {
                            stopAutoPlay();
                        }
                    }
                });
            }, AUTO_PLAY_DELAY);
        } else {
            stopAutoPlay();
        }
    }

    private void flipCard(int index, FlipCallback callback) {
        if (index >= 0 && index < flashcardList.size()) {
            View front = viewPager.findViewWithTag("front_" + index);
            View back = viewPager.findViewWithTag("back_" + index);
            if (front != null && back != null) {
                adapter.flipCard(front, back, callback);
            }
        }
    }

    private void stopAutoPlay() {
        playBtn.setImageResource(R.drawable.play_icon);
        isAutoPlaying = false;
        handler.removeCallbacksAndMessages(null);
    }

    interface FlipCallback {
        void onFlipComplete();
    }


}