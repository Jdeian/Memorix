package com.example.memorix;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
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

public class ClassContents extends AppCompatActivity {

    private View errorToast, errorToastLayout, toastNest, emptyClass, classCodePopUp, classOption, progressBar;
    private TextView toastMessage, okBtn, className;
    private TextView addQuestionnaire, shareClass, classMember, deleteClass, closeClassCode, classCode;
    private ImageView options, copyCode;
    private LinearLayout  codeLayout, questionnaireContainer;
    private RequestQueue requestQueue;
    private String classId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_class_contents);

        progressBar = findViewById(R.id.progressbar);
        toastMessage = findViewById(R.id.error_message);
        errorToast = findViewById(R.id.error_toast);
        errorToastLayout = findViewById(R.id.toast_layout);
        toastNest = findViewById(R.id.toastNest);
        okBtn = findViewById(R.id.ok_btn);
        addQuestionnaire = findViewById(R.id.add_questionnaire);
        options = findViewById(R.id.options);
        copyCode = findViewById(R.id.copy_code);
        classMember = findViewById(R.id.class_member);
        classOption = findViewById(R.id.class_option);
        shareClass = findViewById(R.id.share_class);
        deleteClass = findViewById(R.id.delete_class);
        codeLayout = findViewById(R.id.code_layout);
        classCodePopUp =  findViewById(R.id.class_code_popup);
        classCode = findViewById(R.id.class_code);
        closeClassCode = findViewById(R.id.close_class_code);
        className = findViewById(R.id.class_name);
        questionnaireContainer = findViewById(R.id.questionnaire_container);
        emptyClass = findViewById(R.id.empty_class);
        requestQueue = Volley.newRequestQueue(this);

        classId = getIntent().getStringExtra("class_id");
        String class_name = getIntent().getStringExtra("class_name");
        className.setText(class_name);
        fetchQuestionnaire(classId);

        addQuestionnaire.setOnClickListener(v ->  {
            classOption.setVisibility(View.GONE);
            Intent intent = new Intent(getApplicationContext(), CreateStudyGuide.class);
            intent.putExtra("class_id", classId);
            intent.putExtra("class_name", class_name);
            startActivity(intent);
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
                if(classOption.getVisibility() == View.GONE) {
                    classOption.setVisibility(View.VISIBLE);
                }else {
                    classOption.setVisibility(View.GONE);
                }
            }
        });

        classMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classOption.setVisibility(View.GONE);
                Intent intent = new Intent(getApplicationContext(), Members.class);
                intent.putExtra("class_id", classId);
                startActivity(intent);
            }
        });

        shareClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classOption.setVisibility(View.GONE);
                classId = getIntent().getStringExtra("class_id");
                fetchClassCode(classId);
                classCodePopUp.setVisibility(View.VISIBLE);
            }
        });

        copyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyClassCodeToClipboard();
            }
        });

        codeLayout.setOnTouchListener(new View.OnTouchListener() {
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

        closeClassCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                classCodePopUp.setVisibility(View.GONE);
            }
        });

        deleteClass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteClass(classId);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void copyClassCodeToClipboard() {
        String code = classCode.getText().toString();

        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Class Code", code);
        clipboard.setPrimaryClip(clip);

        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        progressBar.setVisibility(View.GONE);
        errorToast.setVisibility(View.VISIBLE);
        toastMessage.setText("Course code copied to clipboard. Go share it!");
        errorToast.startAnimation(fadeIn);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        progressBar.setVisibility(View.GONE);
        Intent intent = new Intent(getApplicationContext(), Class.class);
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

    private void fetchQuestionnaire(String classId) {
        progressBar.setVisibility(View.VISIBLE);
        fetchCreator(classId);

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/class_questionnaire_fetch.php",
                response -> {
                    Log.d("ServerResponse", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray questionnaireArray = jsonResponse.getJSONArray("questionnaire");

                        if(questionnaireArray.length() < 1) {
                            emptyClass.setVisibility(View.VISIBLE);
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
                    Toast.makeText(getApplicationContext(), "Error fetching courses", Toast.LENGTH_SHORT).show();
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

    private void fetchCreator(String questionnaireId, String questionnaireTitle) {
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/fetch_creator.php",
                response -> {
                    Log.d("ServerResponse", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.has("creator_fullname")) {
                            String creatorId = jsonResponse.getString("creator_id");
                            String userName = jsonResponse.getString("creator_fullname");
                            getItemsCount(userName, questionnaireId, questionnaireTitle, creatorId);
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
                        Log.e("FetchUserError", "Error code: " + error.networkResponse.statusCode);
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

    public void getItemsCount(String userName, String questionnaireId, String questionnaireTitle, String creatorId) {
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/questionnaire_fetch.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("onResponse", response);
                try {
                    JSONObject jobj = new JSONObject(response);
                    if (jobj.has("questionnaire")) {
                        JSONArray definitionsArray = jobj.getJSONObject("definitions").names();
                        int itemsCount = definitionsArray.length();
                        addLibraryToLayout(userName, questionnaireId, questionnaireTitle, itemsCount, creatorId);
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

    private void addLibraryToLayout(String name, String questionnaireId, String title, int itemsCount, String creatorId) {
        progressBar.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(this);
        View questionnaireLayout = inflater.inflate(R.layout.questionnaire_library_layout, null);

        TextView userNameView = questionnaireLayout.findViewById(R.id.user_name);
        TextView questionnaireTitleView = questionnaireLayout.findViewById(R.id.title);
        ImageView libraryProf = questionnaireLayout.findViewById(R.id.profile_img);
        TextView items = questionnaireLayout.findViewById(R.id.items);

        userNameView.setText(name);
        questionnaireTitleView.setText(title);
        items.setText(itemsCount + " items");

        questionnaireLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClassContents.this, StudyGuide.class);
                intent.putExtra("questionnaire_id", questionnaireId);
                startActivity(intent);
            }
        });
        fetchUser(libraryProf, creatorId);
        questionnaireContainer.addView(questionnaireLayout);

        progressBar.setVisibility(View.GONE);
    }

    private void fetchClassCode(String classId) {
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/share_class.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.has("class_code")) {
                            String classCodeValue = jsonResponse.getString("class_code");
                            classCode.setText(classCodeValue);
                        } else {
                            Toast.makeText(getApplicationContext(), "Class code not found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error fetching class code", Toast.LENGTH_SHORT).show();
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

    private void deleteClass(String classId) {
        progressBar.setVisibility(View.VISIBLE);
        classOption.setVisibility(View.GONE);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/class_delete.php",
                response -> {
                    Log.d("ServerResponse", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getInt("success") == 1) {
                            Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                            progressBar.setVisibility(View.GONE);
                            errorToast.setVisibility(View.VISIBLE);
                            toastMessage.setText("Course deleted successfully!");
                            errorToast.startAnimation(fadeIn);

                            okBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(getApplicationContext(), Class.class);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                        errorToast.setVisibility(View.VISIBLE);
                        toastMessage.setText("Network error. Check your network connection.");
                        errorToast.startAnimation(fadeIn);
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error deleting class", Toast.LENGTH_SHORT).show();
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

    private void fetchUser(ImageView libraryProf, String creatorId) {
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
                params.put("user_id", creatorId);
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
                        Glide.with(ClassContents.this)
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
        };

        imageRequest.setRetryPolicy(new DefaultRetryPolicy(10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(imageRequest);
    }

    private void fetchCreator(String classId) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/fetch_class_creator.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);

                        if (jsonResponse.has("creator_fullname")) {
                            progressBar.setVisibility(View.GONE);
                            String creatorId = jsonResponse.getString("creator_id");
                            optionsHandler(creatorId);
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
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("class_id", classId);
                return params;
            }
        };

        requestQueue.add(request);
    }

    public void optionsHandler(String creatorId) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");

        if (!userId.equals(creatorId)) {
            deleteClass.setText("Leave course");

            deleteClass.setOnClickListener(v -> {
                leaveMember(userId);
            });
        }
    }

    private void leaveMember(String memberId) {
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/class_member_delete.php",
                response -> {
                    Log.d("ServerResponse", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getInt("success") == 1) {
                            progressBar.setVisibility(View.GONE);
                            Intent intent = new Intent(getApplicationContext(), Class.class);
                            startActivity(intent);
                            finish();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Animation fadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                        errorToast.setVisibility(View.VISIBLE);
                        toastMessage.setText("Network error. Check your network connection.");
                        errorToast.startAnimation(fadeIn);
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error deleting class", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", memberId);
                return params;
            }
        };
        requestQueue.add(request);
    }

}