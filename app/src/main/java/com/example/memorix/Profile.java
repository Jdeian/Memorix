package com.example.memorix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.loader.content.CursorLoader;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import com.bumptech.glide.request.RequestOptions;

public class Profile extends AppCompatActivity {

    private View logoutConfirmation, errorToastLayout, toastNest;
    private MaterialButton cancelBtn, confirmBtn;
    private View createLayout, createNest, createBg, createStudyGuide, createNotes;
    private ScrollView createScroll;
    private ImageView home, note, create, library, uploadProfile, createClass, lock;
    LinearLayout classes, logoutBtn;
    ProgressBar progressBar;
    private TextView textClass;
    private RequestQueue requestQueue;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        lock = findViewById(R.id.lock);
        textClass = findViewById(R.id.textclasses);
        logoutConfirmation = findViewById(R.id.logout_confirmation);
        errorToastLayout = findViewById(R.id.toast_layout);
        toastNest = findViewById(R.id.toastNest);
        confirmBtn = findViewById(R.id.yesbtn);
        cancelBtn = findViewById(R.id.cancelbtn);
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
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeIn =  AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideDown =  AnimationUtils.loadAnimation(this, R.anim.slide_down);
        logoutBtn = findViewById(R.id.logout);
        classes = findViewById(R.id.classes);
        uploadProfile = findViewById(R.id.default_profile);
        requestQueue = Volley.newRequestQueue(this);

        fetchUser();

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

        library.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(getApplicationContext(), Library.class);
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
                    logoutConfirmation.setVisibility(View.GONE);
                }
                return true;
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutConfirmation.setVisibility(View.GONE);
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(getApplicationContext(), Introduction.class);
                startActivity(intent);
                finish();
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutConfirmation.setVisibility(View.VISIBLE);
            }
        });

        uploadProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
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
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
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

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            uploadImage(imageUri);
        }
    }

    private void uploadImage(Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");
        progressBar.setVisibility(View.VISIBLE);

        String filePath = getRealPathFromURI(imageUri);
        if (filePath != null) {
            File imageFile = new File(filePath);

            byte[] imageData = getFileDataFromDrawable(imageFile);
            Map<String, VolleyMultipartRequest.DataPart> byteData = new HashMap<>();
            byteData.put("profile", new VolleyMultipartRequest.DataPart(imageFile.getName(), imageData, "image/jpeg"));

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                    Request.Method.POST,
                    "https://pink-boar-882869.hostingersite.com/upload_profile.php",
                    response -> {
                        String responseString = new String(response.data);
                        Log.d("Upload Response", responseString);
                        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        fetchUser();
                    },
                    error -> {
                        progressBar.setVisibility(View.GONE);
                        if (error.networkResponse != null) {
                            progressBar.setVisibility(View.GONE);
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

            multipartRequest.addDataPart("profile", new VolleyMultipartRequest.DataPart(imageFile.getName(), imageData, "image/jpeg"));
            requestQueue.add(multipartRequest);
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Invalid image path!", Toast.LENGTH_SHORT).show();
        }
    }

    private byte[] getFileDataFromDrawable(File file) {
        byte[] fileData = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            fileData = bos.toByteArray();
            fis.close();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileData;
    }


    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private void fetchUser() {
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
                            String nickname = user.getString("nickname");
                            String profileImage = "https://pink-boar-882869.hostingersite.com/" + user.getString("profile");

                            TextView nickName = findViewById(R.id.user_name);
                            nickName.setText(nickname);
                            loadProfileImageWithAuth(profileImage);

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

    private void loadProfileImageWithAuth(String imageUrl) {
        progressBar.setVisibility(View.VISIBLE);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        ImageRequest imageRequest = new ImageRequest(imageUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        Glide.with(Profile.this)
                                .load(response)
                                .apply(RequestOptions.circleCropTransform())
                                .into(uploadProfile);
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

}