package com.example.memorix;

import android.content.Intent;
import android.content.SharedPreferences;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Note extends AppCompatActivity {

    private View errorToast, errorToastLayout, toastNest;
    TextView toastMessage, okBtn;
    private View createLayout, createNest, createBg, createStudyGuide, createNotes, emptyNote;
    private ScrollView createScroll;
    private ImageView home, note, create, library, profile, delete, createClass;
    ProgressBar progressBar;
    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note);

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
        home = findViewById(R.id.home);
        note = findViewById(R.id.note);
        create = findViewById(R.id.create);
        library = findViewById(R.id.library);
        profile = findViewById(R.id.profile);
        emptyNote = findViewById(R.id.empty_notes);
        delete = findViewById(R.id.delete_btn);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        Animation fadeIn =  AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation slideDown =  AnimationUtils.loadAnimation(this, R.anim.slide_down);
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        fetchNotes();

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

    private void fetchNotes() {
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/note_fetch.php",
                response -> {
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray notesArray = jsonResponse.getJSONArray("notes");

                        if(notesArray.length() < 1) {
                            emptyNote.setVisibility(View.VISIBLE);
                        }

                        for (int i = 0; i < notesArray.length(); i++) {
                            JSONObject noteObject = notesArray.getJSONObject(i);
                            String noteId = noteObject.getString("note_id");
                            String noteTitle = noteObject.getString("title");
                            String createdAt = noteObject.getString("created_at");

                            fetchNoteContents(noteId, noteTitle, createdAt);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error fetching notes", Toast.LENGTH_SHORT).show();
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

    private void fetchNoteContents(String noteId, String noteTitle, String createdAt) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/note_contents_fetch.php",
                response -> {
                    try {
                        Log.d("Response", response);

                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray notesArray = jsonResponse.optJSONArray("notes");
                        if (notesArray != null && notesArray.length() > 0) {
                            JSONObject firstNoteObject = notesArray.getJSONObject(0);
                            String noteContents = firstNoteObject.getString("note_contents");

                            String previewText = noteContents.length() > 20
                                    ? noteContents.substring(0, 20) + "..."
                                    : noteContents;

                            addNoteToLayout(noteId, noteTitle, previewText, createdAt);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error fetching note contents", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("note_id", noteId);
                return params;
            }
        };
        requestQueue.add(request);
    }

    private void addNoteToLayout(String noteId, String noteTitle, String note, String createdAt) {
        progressBar.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(this);
        View noteLayout = inflater.inflate(R.layout.created_note_layout, null);
        ImageView delete = noteLayout.findViewById(R.id.delete_btn);

        TextView noteTitleView = noteLayout.findViewById(R.id.note_title);
        TextView createdAtView = noteLayout.findViewById(R.id.note_date);
        TextView descriptionView = noteLayout.findViewById(R.id.note_description);
        noteTitleView.setText(noteTitle);
        descriptionView.setText(note);

        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm a", Locale.getDefault());

        try {
            Date date = originalFormat.parse(createdAt);
            if (date != null) {
                Calendar createdCalendar = Calendar.getInstance();
                createdCalendar.setTime(date);

                Calendar now = Calendar.getInstance();
                Calendar yesterday = Calendar.getInstance();
                yesterday.add(Calendar.DAY_OF_YEAR, -1);

                if (createdCalendar.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    createdCalendar.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                    String time = timeFormat.format(date).toString();
                    createdAtView.setText(time.toUpperCase());
                } else if (createdCalendar.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    createdCalendar.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)) {
                    createdAtView.setText("Yesterday");
                } else {
                    createdAtView.setText(dateFormat.format(date));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        noteLayout.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), EditNote.class);
            intent.putExtra("note_id", noteId);
            intent.putExtra("note_title", noteTitle);
            startActivity(intent);
        });

        noteLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (delete.getVisibility() == View.GONE) {
                    delete.setVisibility(View.VISIBLE);
                } else {
                    delete.setVisibility(View.GONE);
                }
                return true;
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteNote(noteId);
            }
        });

        LinearLayout notesContainer = findViewById(R.id.created_note_container);
        notesContainer.addView(noteLayout);
    }

    private void deleteNote(String noteId) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/note_delete.php",
                response -> {
                    try {
                        JSONObject jobj = new JSONObject(response);
                        int success = jobj.optInt("success", -1);
                        if (success == 1) {
                            progressBar.setVisibility(View.GONE);
                            errorToast.setVisibility(View.VISIBLE);
                            toastMessage.setText("Note deleted successfully.");
                            Animation fadeIn =  AnimationUtils.loadAnimation(this, R.anim.fade_in);
                            errorToast.startAnimation(fadeIn);

                            okBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    recreate();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error processing response", Toast.LENGTH_SHORT).show();
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error deleting note", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("note_id", noteId);
                return params;
            }
        };

        requestQueue.add(request);
    }

}