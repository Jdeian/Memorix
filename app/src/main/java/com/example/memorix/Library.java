package com.example.memorix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.appcompat.app.AppCompatActivity;
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
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Library extends AppCompatActivity {

    private View errorToast, errorToastLayout, toastNest, emptylibrary;
    private View createLayout, createNest, createBg, createStudyGuide, createNotes, classes;
    private ScrollView createScroll;
    private ImageView home, note, create, profile, createClass, lock;
    TextView toastMessage, okBtn, textClass;
    ProgressBar progressBar;
    RequestQueue requestQueue;
    LinearLayout questionnaireContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_library);

        lock = findViewById(R.id.lock);
        textClass = findViewById(R.id.textclasses);
        progressBar = findViewById(R.id.progressbar);
        emptylibrary = findViewById(R.id.empty_library);
        createLayout = findViewById(R.id.create_layout);
        createBg = findViewById(R.id.createBg);
        createNest = findViewById(R.id.createNest);
        createScroll = findViewById(R.id.create_scroll);
        createStudyGuide = findViewById(R.id.create_study_guide);
        createNotes = findViewById(R.id.create_notes);
        createClass = findViewById(R.id.create_class);
        errorToast = findViewById(R.id.error_toast);
        errorToastLayout = findViewById(R.id.toast_layout);
        toastNest = findViewById(R.id.toastNest);
        toastMessage = findViewById(R.id.error_message);
        okBtn = findViewById(R.id.ok_btn);
        home = findViewById(R.id.home);
        note = findViewById(R.id.note);
        create = findViewById(R.id.create);
        profile = findViewById(R.id.profile);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeIn =  AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideDown =  AnimationUtils.loadAnimation(this, R.anim.slide_down);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        questionnaireContainer = findViewById(R.id.questionnaire_container);
        classes = findViewById(R.id.classes);

        fetchQuestionnaire();

        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String category = sharedPreferences.getString("category", "");
        if ((category.equals("Premium") || category.equals("Basic"))) {
            lock.setVisibility(View.GONE);
            textClass.getPaint().setMaskFilter(null);
            textClass.invalidate();
            createClass.setImageResource(R.drawable.create_class);

            classes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), Class.class);
                    startActivity(intent);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setVisibility(View.GONE);
                        }
                    }, 500);
                }
            });

            createClass.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), CreateClass.class);
                startActivity(intent);
            });
        } else {
            lock.setVisibility(View.VISIBLE);
            textClass.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            float radius = 5f;
            BlurMaskFilter blurMaskFilter = new BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL);
            textClass.getPaint().setMaskFilter(blurMaskFilter);
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
                Intent intent = new Intent(getApplicationContext(), CreateStudyGuide.class);
                startActivity(intent);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                }, 500);
            }
        });

        createNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), CreateNote.class);
                startActivity(intent);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                }, 500);
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
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                }, 500);
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), Profile.class);
                startActivity(intent);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setVisibility(View.GONE);
                    }
                }, 500);
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
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        progressBar.setVisibility(View.GONE);
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
                            emptylibrary.setVisibility(View.VISIBLE);
                        }

                        for (int i = 0; i < questionnaireArray.length(); i++) {
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
                    } finally {
                        progressBar.setVisibility(View.GONE);
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = "11193618:60-dayfreetrial";
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", auth);
                return headers;
            }

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
        progressBar.setVisibility(View.VISIBLE);

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

                    } finally {
                        progressBar.setVisibility(View.GONE);
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
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = "11193618:60-dayfreetrial";
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", auth);
                return headers;
            }

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
                params.put("user_id", userId);
                params.put("questionnaire_id", questionnaireId);
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void addLibraryToLayout(String name, String questionnaireId, String title, int itemsCount) {
        progressBar.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(this);
        View questionnaireLayout = inflater.inflate(R.layout.questionnaire_library_layout, null);

        TextView userNameView = questionnaireLayout.findViewById(R.id.user_name);
        TextView questionnaireTitleView = questionnaireLayout.findViewById(R.id.title);
        TextView items = questionnaireLayout.findViewById(R.id.items);
        ImageView libraryProf = questionnaireLayout.findViewById(R.id.profile_img);

        questionnaireTitleView.setText(title);
        userNameView.setText(name);
        items.setText(itemsCount + " items");

        questionnaireLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), StudyGuide.class);
            intent.putExtra("questionnaire_id", questionnaireId);
            startActivity(intent);
            finish();
        });

        fetchUser(libraryProf);
        questionnaireContainer.addView(questionnaireLayout);
    }

    private void fetchUser(ImageView libraryProf) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/user_fetch.php",
                response -> {
                    Log.d("ServerResponse", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            JSONObject user = jsonResponse.getJSONObject("user");
                            String profileImage = "https://pink-boar-882869.hostingersite.com/" + user.getString("profile");

                            loadProfileImageWithAuth(profileImage, libraryProf);

                        } else {
                            Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error parsing user data", Toast.LENGTH_SHORT).show();
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    if (error.networkResponse != null) {
                        String responseBody = new String(error.networkResponse.data);
                        Log.d("Error Response", responseBody);
                    } else {
                        Log.e("Error: ", "Error: " + error.getMessage());
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

    private void loadProfileImageWithAuth(String imageUrl, ImageView libraryProf) {
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        ImageRequest imageRequest = new ImageRequest(imageUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        Glide.with(Library.this)
                                .load(response)
                                .apply(RequestOptions.circleCropTransform())
                                .into(libraryProf);
                        progressBar.setVisibility(View.GONE);
                    }
                },
                0,
                0,
                ImageView.ScaleType.CENTER_CROP,
                Bitmap.Config.RGB_565,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Log.e("Image Load Error", "Error loading image: " + error.getMessage());
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                String credentials = "11193618:60-dayfreetrial";
                String auth = "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                headers.put("Authorization", auth);
                return headers;
            }
        };

        imageRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(imageRequest);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}