package com.example.memorix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

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
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private View errorToast, errorToastLayout, toastNest;
    TextView toastMessage, okBtn, textView14, textclasses;
    private View createLayout, createNest, createBg, homeCreateStudyGuide, homeTakeNote, createStudyGuide, createNotes, upgrade;
    private ScrollView createScroll;
    private ImageView home, note, create, library, profile, homeCreateClass, createClass;
    private ProgressBar progressBar;
    private RequestQueue requestQueue;
    private ViewPager2 questionnaireContainer;
    private HomePagerAdapter homePagerAdapter;
    private List<LibraryItem> libraryItems;
    private boolean backPressedOnce = false;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout classContainer;
    int questionnaireCount = 1;
    int classCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        textView14 = findViewById(R.id.textView14);
        textclasses = findViewById(R.id.textclasses);
        errorToast = findViewById(R.id.error_toast);
        errorToastLayout = findViewById(R.id.toast_layout);
        toastNest = findViewById(R.id.toastNest);
        toastMessage = findViewById(R.id.error_message);
        okBtn = findViewById(R.id.ok_btn);
        progressBar = findViewById(R.id.progressbar);
        createLayout = findViewById(R.id.create_layout);
        createBg = findViewById(R.id.createBg);
        createNest = findViewById(R.id.createNest);
        createScroll = findViewById(R.id.create_scroll);
        createStudyGuide = findViewById(R.id.create_study_guide);
        createNotes = findViewById(R.id.create_notes);
        createClass = findViewById(R.id.create_class);
        homeCreateStudyGuide = findViewById(R.id.home_create_study_guide);
        homeTakeNote = findViewById(R.id.home_create_notes);
        homeCreateClass = findViewById(R.id.home_create_class);
        upgrade = findViewById(R.id.upgrade_memorix);
        home = findViewById(R.id.home);
        note = findViewById(R.id.note);
        create = findViewById(R.id.create);
        library = findViewById(R.id.library);
        profile = findViewById(R.id.profile);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeIn =  AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideDown =  AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_down);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        questionnaireContainer = findViewById(R.id.questionnaire_container);
        libraryItems = new ArrayList<>();
        homePagerAdapter = new HomePagerAdapter(this, libraryItems);
        questionnaireContainer.setAdapter(homePagerAdapter);
        classContainer = findViewById(R.id.class_container);

        questionnaireContainer.setClipToPadding(false);
        questionnaireContainer.setClipChildren(false);
        questionnaireContainer.setOffscreenPageLimit(3);

        int peekWidth = getResources().getDimensionPixelSize(R.dimen.peek_width);
        questionnaireContainer.setPadding(peekWidth, 0, peekWidth, 0);

        questionnaireContainer.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float pageTranslation = position * -(2 * page.getWidth() / 8);
                if (position < -1) {
                    page.setTranslationX(-pageTranslation);
                } else if (position <= 1) {
                    page.setTranslationX(pageTranslation);
                } else {
                    page.setTranslationX(pageTranslation);
                }
                float scaleFactor = Math.max(0.85f, 1 - Math.abs(position));
                page.setScaleY(scaleFactor);
                page.setAlpha(0.5f + (scaleFactor - 0.85f) / (1 - 0.85f) * 0.5f);
            }
        });

        fetchQuestionnaire();

        homeCreateStudyGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), CreateStudyGuide.class);
                startActivity(intent);
            }
        });

        homeTakeNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), CreateNote.class);
                startActivity(intent);
            }
        });

        createStudyGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(MainActivity.this, CreateStudyGuide.class);
                startActivity(intent);
            }
        });

        createNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(MainActivity.this, CreateNote.class);
                startActivity(intent);
            }
        });

        upgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Subcription.class);
                startActivity(intent);
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createLayout.setVisibility(View.VISIBLE);
                createScroll.post(() -> createScroll.scrollTo(0, 500));
                createLayout.startAnimation(fadeIn);
                createNest.startAnimation(slideUp);
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

        note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), Note.class);
                startActivity(intent);
            }
        });

        library.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), Library.class);
                startActivity(intent);
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), Profile.class);
                startActivity(intent);
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

    public void getUsersCategory() {
        noQuestionnaireClass();
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/fetch_subcriptions.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                upgrade.setVisibility(View.VISIBLE);
                homeCreateClass.setImageResource(R.drawable.create_class_lock);
                createClass.setImageResource(R.drawable.create_class_lock);
                Log.d("onResponse", response);
                try {
                    JSONObject jobj = new JSONObject(response);
                    if (jobj.has("subscriptions")) {
                        JSONArray subscriptionsArray = jobj.getJSONArray("subscriptions");
                        if (subscriptionsArray.length() > 0) {
                            JSONObject subscription = subscriptionsArray.getJSONObject(0);
                            String category = subscription.getString("category");
                            String expdate = subscription.getString("expdate");
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                            Date expirationDate = dateFormat.parse(expdate);
                            Date currentDate = new Date();

                            if ((category.equals("Premium") || category.equals("Basic")) && expirationDate.after(currentDate)) {
                                progressBar.setVisibility(View.GONE);
                                upgrade.setVisibility(View.GONE);
                                homeCreateClass.setImageResource(R.drawable.create_class);
                                createClass.setImageResource(R.drawable.create_class);

                                SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("category", category);
                                editor.apply();

                                homeCreateClass.setOnClickListener(v -> {
                                    Intent intent = new Intent(getApplicationContext(), CreateClass.class);
                                    startActivity(intent);
                                });

                                createClass.setOnClickListener(v -> {
                                    Intent intent = new Intent(getApplicationContext(), CreateClass.class);
                                    startActivity(intent);
                                });

                                fetchClass();

                            } else {
                                progressBar.setVisibility(View.GONE);
                                upgrade.setVisibility(View.VISIBLE);
                                homeCreateClass.setImageResource(R.drawable.create_class_lock);
                                createClass.setImageResource(R.drawable.create_class_lock);
                                updateUserCategory();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            upgrade.setVisibility(View.VISIBLE);
                            homeCreateClass.setImageResource(R.drawable.create_class_lock);
                            createClass.setImageResource(R.drawable.create_class_lock);
                            classCount--;
                            noQuestionnaireClass();
                            Log.e("message", "No subscriptions found.");
                        }
                    }else {
                        classCount--;
                        noQuestionnaireClass();
                        progressBar.setVisibility(View.GONE);
                        upgrade.setVisibility(View.VISIBLE);
                        homeCreateClass.setImageResource(R.drawable.create_class_lock);
                        createClass.setImageResource(R.drawable.create_class_lock);
                        classCount--;
                        noQuestionnaireClass();
                        Log.e("message", "No subscriptions found.");
                    }

                } catch (Exception e) {
                    progressBar.setVisibility(View.GONE);
                    upgrade.setVisibility(View.VISIBLE);
                    homeCreateClass.setImageResource(R.drawable.create_class_lock);
                    createClass.setImageResource(R.drawable.create_class_lock);
                    Animation fadeIn =  AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                    errorToast.setVisibility(View.VISIBLE);
                    toastMessage.setText("Network error. Check your network connection.");
                    errorToast.startAnimation(fadeIn);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressBar.setVisibility(View.GONE);
                upgrade.setVisibility(View.VISIBLE);
                homeCreateClass.setImageResource(R.drawable.create_class_lock);
                createClass.setImageResource(R.drawable.create_class_lock);
                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {

            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);
                return params;
            }
        };

        requestQueue.add(request);
    }

    public void updateUserCategory() {
        noQuestionnaireClass();
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/update_user_category.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);
                        Log.d("Update user response", "User's category updated successfully");
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
                params.put("user_id", userId);
                params.put("category", "Free");
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void fetchQuestionnaire() {
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/library_fetch.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray questionnaireArray = jsonResponse.getJSONArray("questionnaire");

                        if(questionnaireArray.length() < 1) {
                            questionnaireCount--;
                            getUsersCategory();
                            return;
                        }

                        for (int i = 0; i < questionnaireArray.length(); i++) {
                            textView14.setText("Latest questionnaires");
                            JSONObject noteObject = questionnaireArray.getJSONObject(i);
                            String questionnaireId = noteObject.getString("questionnaire_id");
                            String questionnaireTitle = noteObject.getString("title");
                            fetchCreator(questionnaireId, questionnaireTitle);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Animation fadeIn =  AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                        progressBar.setVisibility(View.GONE);
                        errorToast.setVisibility(View.VISIBLE);
                        toastMessage.setText("Network error. Check your network connection.");
                        errorToast.startAnimation(fadeIn);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("FetchUserError", "Error code: " + error.networkResponse.statusCode);
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

    private void fetchCreator(String questionnaireId, String questionnaireTitle) {

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/fetch_creator.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.has("creator_fullname")) {
                            String userName = jsonResponse.getString("creator_fullname");
                            getItemsCount(userName, questionnaireId, questionnaireTitle);
                        } else {
                            Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    if (error.networkResponse != null) {
                        Log.e("FetchUserError", "Error code: " + error.networkResponse.statusCode);
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
                    Toast.makeText(getApplicationContext(), "Error fetching user data", Toast.LENGTH_SHORT).show();
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

    public void getItemsCount(String userName, String questionnaireId, String questionnaireTitle) {
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
                        int itemsCount = definitionsArray.length();
                        addLibraryToLayout(userName, questionnaireId, questionnaireTitle, itemsCount);
                    } else {
                        Toast.makeText(getApplicationContext(), "No Data Found", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("Exception: ", e.getMessage());
                }
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
                params.put("user_id", userId);
                params.put("questionnaire_id", questionnaireId);
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void addLibraryToLayout(String name, String questionnaireId, String title, int itemsCount) {
        progressBar.setVisibility(View.GONE);
        LibraryItem item = new LibraryItem(name, questionnaireId, title, itemsCount);
        libraryItems.add(item);

        homePagerAdapter.notifyDataSetChanged();

        if(questionnaireCount == 0) {
            return;
        }
        getUsersCategory();
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

                        if(classArray.length() < 1) {
                            classCount--;
                        }

                        for (int i = 0; i < classArray.length(); i++) {
                            textclasses.setVisibility(View.VISIBLE);
                            JSONObject classObject = classArray.getJSONObject(i);
                            String classId = classObject.getString("class_id");
                            String className = classObject.getString("class_name");
                            fetchQuestionnaireCount(classId, className);
                        }

                    } catch (JSONException e) {
                        handleError("Network error. Check your network connection.");
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> handleError("Error fetching classes")) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void fetchQuestionnaireCount(String classId, String className) {
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/class_questionnaire_fetch.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray questionnaireArray = jsonResponse.getJSONArray("questionnaire");
                        int questionnaireCount = questionnaireArray.length();
                        addClassToLayout(classId, className, questionnaireCount);
                    } catch (JSONException e) {
                        handleError("Network error. Check your network connection.");
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> handleError("Error fetching questionnaire counts")) {
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
            Intent intent = new Intent(getApplicationContext(), ClassContents.class);
            intent.putExtra("class_id", classId);
            intent.putExtra("class_name", className);
            startActivity(intent);
        });

        classContainer.addView(classLayout);
    }

    private void noQuestionnaireClass() {
        Log.d("StateCheck", "questionnaireCount: " + questionnaireCount);
        Log.d("StateCheck", "classCount: " + classCount);
        if(questionnaireCount < 1 && classCount < 1 ) {
            textView14.setText("Here’s how to get started!");
            homeCreateStudyGuide.setVisibility(View.VISIBLE);
            homeTakeNote.setVisibility(View.VISIBLE);
            homeCreateClass.setVisibility(View.VISIBLE);
        }
    }

    private void handleError(String message) {
        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        progressBar.setVisibility(View.GONE);
        errorToast.setVisibility(View.VISIBLE);
        toastMessage.setText(message);
        errorToast.startAnimation(fadeIn);
    }

    @Override
    public void onBackPressed() {
        if (backPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.backPressedOnce = true;
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
        new android.os.Handler().postDelayed(() -> backPressedOnce = false, 2000);
    }
}