package com.example.memorix;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.util.Pair;
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
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.checkbox.MaterialCheckBox;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class EditNote extends AppCompatActivity {

    private View errorToast, errorToastLayout, toastNest;
    TextView toastMessage, okBtn;
    private LinearLayout noteContainer, styles;
    private EditText title;
    private ImageView saveNote, addCheckMark, addImg;
    private RelativeLayout progressBar;
    private String noteId, noteTitle;
    private RequestQueue requestqueue;
    private TextView stylesBtn, boldBtn, italicBtn, underlineBtn, closeBtn;
    private String noteContentsId;
    private ScrollView scrollView;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private LinkedList<Uri> imageUriList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_note);

        errorToast = findViewById(R.id.error_toast);
        errorToastLayout = findViewById(R.id.toast_layout);
        toastNest = findViewById(R.id.toastNest);
        toastMessage = findViewById(R.id.error_message);
        okBtn = findViewById(R.id.ok_btn);
        Animation fadeIn =  AnimationUtils.loadAnimation(this, R.anim.fade_in);
        title = findViewById(R.id.title);
        noteContainer = findViewById(R.id.note_container);
        styles = findViewById(R.id.styles);
        addCheckMark = findViewById(R.id.add_checkmark_btn);
        stylesBtn = findViewById(R.id.styles_btn);
        boldBtn = findViewById(R.id.bold);
        italicBtn = findViewById(R.id.italic);
        underlineBtn = findViewById(R.id.underline);
        closeBtn = findViewById(R.id.close);
        saveNote = findViewById(R.id.saveNote);
        progressBar = findViewById(R.id.progressbar);
        requestqueue = Volley.newRequestQueue(getApplicationContext());
        noteId = getIntent().getStringExtra("note_id");
        noteTitle = getIntent().getStringExtra("note_title");
        scrollView = findViewById(R.id.note_scroll);
        fetchNoteContents(noteId);
        title.setText(noteTitle);
        addImg = findViewById(R.id.add_img);
        imageUriList = new LinkedList<>();

        title.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String titleText = title.getText().toString().trim();
                    if (!titleText.isEmpty()) {
                        addPureNote("", "");
                    } else {
                        progressBar.setVisibility(View.GONE);
                        errorToast.setVisibility(View.VISIBLE);
                        toastMessage.setText("Please add a title to continue.");
                        errorToast.startAnimation(fadeIn);
                    }
                }
                return false;
            }
        });

        stylesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (styles.getVisibility() == View.VISIBLE) {
                    styles.setVisibility(View.GONE);
                } else {
                    styles.setVisibility(View.VISIBLE);
                }
            }
        });

        addImg.setOnClickListener(v -> {
            if (!title.getText().toString().isEmpty()) {
                openFileChooser();
            } else {
                progressBar.setVisibility(View.GONE);
                errorToast.setVisibility(View.VISIBLE);
                toastMessage.setText("Please add a title to continue.");
                errorToast.startAnimation(fadeIn);
            }
        });

        boldBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleBoldText();
            }
        });

        italicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleItalicText();
            }
        });

        underlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleUnderlineText();
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    styles.setVisibility(View.GONE);
            }
        });

        saveNote.setOnClickListener(new View.OnClickListener() {
            Animation fadeIn =  AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
            @Override
            public void onClick(View view) {
                if(noteContainer.getChildCount() < 1 && !title.getText().toString().isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    errorToast.setVisibility(View.VISIBLE);
                    toastMessage.setText("Please add a note to continue.");
                    errorToast.startAnimation(fadeIn);
                    return;
                } else if(title.getText().toString().isEmpty()) {
                    progressBar.setVisibility(View.GONE);
                    errorToast.setVisibility(View.VISIBLE);
                    toastMessage.setText("Please add a title to continue.");
                    errorToast.startAnimation(fadeIn);
                    return;
                }
                updateNoteData();
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

    private boolean isViewTouched(View view, float x, float y) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int viewX = location[0];
        int viewY = location[1];

        return (x >= viewX && x <= (viewX + view.getWidth())) &&
                (y >= viewY && y <= (viewY + view.getHeight()));
    }

    private void toggleBoldText() {
        for (int i = 0; i < noteContainer.getChildCount(); i++) {
            View noteEntryView = noteContainer.getChildAt(i);
            EditText noteEditText = noteEntryView.findViewById(R.id.note);
            if (noteEditText != null && noteEditText.isFocused()) {
                if (noteEditText.getTypeface() != null && noteEditText.getTypeface().getStyle() == Typeface.BOLD) {
                    noteEditText.setTypeface(null, Typeface.NORMAL);
                } else {
                    noteEditText.setTypeface(null, Typeface.BOLD);
                }
            }
        }
    }

    private void toggleItalicText() {
        for (int i = 0; i < noteContainer.getChildCount(); i++) {
            View noteEntryView = noteContainer.getChildAt(i);
            EditText noteEditText = noteEntryView.findViewById(R.id.note);
            if (noteEditText != null && noteEditText.isFocused()) {
                if (noteEditText.getTypeface() != null && noteEditText.getTypeface().getStyle() == Typeface.ITALIC) {
                    noteEditText.setTypeface(null, Typeface.NORMAL);
                } else {
                    noteEditText.setTypeface(null, Typeface.ITALIC);
                }
            }
        }
    }

    private void toggleUnderlineText() {
        for (int i = 0; i < noteContainer.getChildCount(); i++) {
            View noteEntryView = noteContainer.getChildAt(i);
            EditText noteEditText = noteEntryView.findViewById(R.id.note);
            if (noteEditText != null && noteEditText.isFocused()) {
                String currentText = noteEditText.getText().toString();
                SpannableString spannableString = new SpannableString(currentText);

                if (currentText.length() > 0 && spannableString.getSpans(0, currentText.length(), UnderlineSpan.class).length > 0) {
                    spannableString.removeSpan(spannableString.getSpans(0, currentText.length(), UnderlineSpan.class)[0]);
                } else {
                    spannableString.setSpan(new UnderlineSpan(), 0, currentText.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                noteEditText.setText(spannableString);
                noteEditText.setSelection(noteEditText.length());
            }
        }
    }

    private void deleteNote() {
        for (int i = 0; i < noteContainer.getChildCount(); i++) {
            View noteEntryView = noteContainer.getChildAt(i);
            EditText noteEditText = noteEntryView.findViewById(R.id.note);
            if (noteEditText != null && noteEditText.isFocused()) {
                int noteIndex = i;

                noteContainer.removeView(noteEntryView);
                String noteContentsId = (String) noteEntryView.getTag(R.id.note_contents_id_tag);
                if(!noteContentsId.isEmpty()) {
                    deleteNoteContentsData(noteContentsId);
                }

                if (noteContainer.getChildCount() > 0) {
                    int nextIndex = Math.min(noteIndex, noteContainer.getChildCount() - 1);
                    View nextNoteEntryView = noteContainer.getChildAt(nextIndex);
                    EditText nextNoteEditText = nextNoteEntryView.findViewById(R.id.note);
                    nextNoteEditText.requestFocus();
                }
                break;
            }
        }
    }

    private void fetchNoteContents(String noteId) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/note_contents_fetch.php",
                response -> {
                    try {
                        Log.d("Response", response);

                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray notesArray = jsonResponse.optJSONArray("notes");
                        if (notesArray != null) {
                            for (int i = 0; i < notesArray.length(); i++) {
                                JSONObject noteObject = notesArray.getJSONObject(i);
                                noteContentsId = noteObject.getString("note_contents_id");
                                String noteContents = noteObject.getString("note_contents");
                                int isNotesChecked = noteObject.getInt("isChecked");
                                int isPureNote = noteObject.getInt("pureNote");
                                String noteImg = noteObject.getString("imgNote");
                                String noteImgUrl = "https://pink-boar-882869.hostingersite.com/" + noteImg;
                                boolean isChecked = isNotesChecked == 1;
                                boolean pureNote = isPureNote == 1;
                                if(!noteImg.isEmpty()) {
                                    loadNoteContents(noteContentsId, noteContents, isChecked, false, true, noteImgUrl);
                                } else {
                                    loadNoteContents(noteContentsId, noteContents, isChecked, pureNote, false, noteImgUrl);
                                }
                            }
                        } else {
                            addPureNote("", "");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Error parsing response", Toast.LENGTH_SHORT).show();
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
        requestqueue.add(request);
    }

    private void loadNoteContents(String noteContentsId, String noteContents, boolean isChecked, boolean pureNote, boolean isnoteImg, String imgUrl) {
        if(pureNote) {
            progressBar.setVisibility(View.GONE);
            addPureNote(noteContentsId, noteContents);
        } else if (isnoteImg) {
            loadNoteImg(imgUrl, noteContents);
        }else {
            progressBar.setVisibility(View.GONE);
            addNoteWithCheckbox(noteContentsId, noteContents, isChecked);
        }
    }

    private void loadNoteImg(String imageUrl, String noteContents) {
        progressBar.setVisibility(View.VISIBLE);
        View noteEntryView = LayoutInflater.from(this).inflate(R.layout.purely_add_note, noteContainer, false);
        TextView noteEditText = noteEntryView.findViewById(R.id.note);
        noteEditText.setText(noteContents);
        noteEntryView.setTag(R.id.note_contents_id_tag, noteContentsId);
        noteEntryView.setTag(R.id.is_pure_note_tag, true);
        noteEditText.setText(noteContents);

        int childCount = noteContainer.getChildCount();

        if (childCount > 0) {
            noteEditText.setHint("");
        }
        noteEditText.requestFocus();

        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        ImageRequest imageRequest = new ImageRequest(imageUrl,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        int desiredWidth = noteEditText.getWidth();
                        int desiredHeight = 900;
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(response, desiredWidth, desiredHeight, false);

                        Drawable drawable = new BitmapDrawable(getResources(), resizedBitmap);
                        drawable.setBounds(0, 0, desiredWidth, desiredHeight);

                        noteEditText.setCompoundDrawables(null, drawable, null, null);
                        noteEditText.setCompoundDrawablePadding(10);
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
                        Log.e("Image Load Error", "Error loading image: " + error.getMessage());
                    }
                }) {
        };

        imageRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(imageRequest);
        noteContainer.addView(noteEntryView);

        noteEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (noteEditText.getText().length() == 0) {
                        deleteNote();
                        focusOnLastNote();
                        return true;
                    }
                }
                return false;
            }
        });

        noteEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String currentText = noteEditText.getText().toString();
                if (currentText.endsWith("\n")) {
                    Log.d("TextWatcher", "New line character detected, adding new note");
                    noteEditText.setText(currentText.substring(0, currentText.length() - 1));
                    addPureNote("", "");
                    scrollToBottom();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}});

        addCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!title.getText().toString().isEmpty()) {
                    addNoteWithCheckbox("", "", false);
                } else {
                    progressBar.setVisibility(View.GONE);
                    errorToast.setVisibility(View.VISIBLE);
                    toastMessage.setText("Please add a title to continue.");
                    Animation fadeIn =  AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                    errorToast.startAnimation(fadeIn);
                }
            }
        });
    }

    private void updateNoteData() {
        saveNote.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/note_update.php",
                response -> {
                    try {
                        Log.d("Response", response);
                        JSONObject jobj = new JSONObject(response);
                        int success = jobj.optInt("success", -1);
                        if (success == 1) {
                            int nonEmptyNoteCount = 0;
                            for (int i = 0; i < noteContainer.getChildCount(); i++) {
                                View noteEntryView = noteContainer.getChildAt(i);
                                EditText noteEditText = noteEntryView.findViewById(R.id.note);
                                MaterialCheckBox checkBox = noteEntryView.findViewById(R.id.note_check_box);

                                if (noteEditText != null) {
                                    String noteContent = noteEditText.getText().toString().trim();

                                    if (!noteContent.isEmpty()) {
                                        String noteContentsId = (String) noteEntryView.getTag(R.id.note_contents_id_tag);
                                        Boolean isPureNoteTag = (Boolean) noteEntryView.getTag(R.id.is_pure_note_tag);
                                        boolean isPureNote = isPureNoteTag != null && isPureNoteTag;
                                        boolean isChecked = checkBox != null && checkBox.isChecked();
                                        boolean isNoteImg = "imgNote".equals(noteEntryView.getTag());
                                        if (!noteContent.isEmpty()) {
                                            nonEmptyNoteCount++;
                                        }

                                        final int totalNotes = nonEmptyNoteCount;
                                        final int[] insertedNotes = {0};
                                        if (noteContentsId == null || noteContentsId.isEmpty()) {

                                            insertNoteContentsData(noteId, noteContent, isChecked, isPureNote, isNoteImg, () -> {
                                                insertedNotes[0]++;
                                                if (insertedNotes[0] == totalNotes) {
                                                    progressBar.setVisibility(View.GONE);

                                                    new Handler().postDelayed(() -> {
                                                        Intent intent = new Intent(getApplicationContext(), Note.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }, 5000);
                                                }
                                            });
                                        } else {
                                            updateNoteContentsData(noteContentsId, noteContent, isChecked, isPureNote, () -> {
                                                insertedNotes[0]++;
                                                if (insertedNotes[0] == totalNotes) {
                                                    progressBar.setVisibility(View.GONE);

                                                    new Handler().postDelayed(() -> {
                                                        Intent intent = new Intent(getApplicationContext(), Note.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }, 5000);
                                                }
                                            });
                                        }
                                    } else {
                                        Log.d("Skipped", "Empty note at position: " + i);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        saveNote.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    saveNote.setEnabled(false);
                    Toast.makeText(getApplicationContext(), "Error updating note", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("note_id", noteId);
                params.put("title", title.getText().toString().trim());
                return params;
            }
        };

        requestqueue.add(request);
    }

    private void updateNoteContentsData(String noteContentsId, String noteContent, boolean isChecked, boolean isPureNote, Runnable onComplete) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/note_contents_update.php",
                response -> {
                    try {
                        Log.d("Response", response);
                        JSONObject jobj = new JSONObject(response);
                        int success = jobj.optInt("success", -1);
                        if (success == 1) {
                            onComplete.run();
                        }
                    } catch (Exception e) {
                        progressBar.setVisibility(View.GONE);
                        saveNote.setEnabled(true);
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    saveNote.setEnabled(true);
                    Toast.makeText(getApplicationContext(), "Error updating note", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("note_contents_id", noteContentsId);
                params.put("note_contents", noteContent);
                params.put("isChecked", String.valueOf(isChecked ? 1 : 0));
                params.put("pureNote", String.valueOf(isPureNote ? 1 : 0));
                return params;
            }
        };

        requestqueue.add(request);
    }

    private void insertNoteContentsData(String noteId, String noteContent, boolean isChecked, boolean pureNote, boolean isNoteImage, Runnable onComplete) {
        progressBar.setVisibility(View.VISIBLE);

        if(isNoteImage) {
            for(int i = 0; i < imageUriList.size(); i++) {
                Uri imageUri = imageUriList.get(i);
                uploadImageWithNote(imageUri,noteId, noteContent, isChecked, onComplete);
                return;
            }
        }

        if(noteContent.toString().isEmpty()) {
            Log.d("Skipped note", "Empty note");
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentDate = now.format(formatter);

        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/note_contents_insert.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("onResponse", response);
                        try {
                            JSONObject jobj = new JSONObject(response);
                            int success = jobj.optInt("success", -1);
                            if (success == 1) {
                                onComplete.run();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                saveNote.setEnabled(true);
                                Toast.makeText(getApplicationContext(), "Failed to insert note", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            saveNote.setEnabled(true);
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            String responseBody = new String(error.networkResponse.data);
                            Log.e("Error Response", responseBody);
                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }) {

            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("note_id", noteId);
                params.put("note", noteContent);
                params.put("isChecked", String.valueOf(isChecked ? 1 : 0));
                params.put("pureNote", String.valueOf(pureNote ? 1 : 0));
                params.put("added_at", currentDate);
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(5000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestqueue.add(request);
    }

    private void uploadImageWithNote(Uri imageUri, String noteId, String noteContent, boolean isChecked, Runnable onComplete) {
        progressBar.setVisibility(View.VISIBLE);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentDate = now.format(formatter);
        String filePath = getRealPathFromURI(imageUri);
        if (filePath != null) {
            File imageFile = new File(filePath);
            byte[] imageData = getFileDataFromDrawable(imageFile);

            VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                    Request.Method.POST,
                    "https://pink-boar-882869.hostingersite.com/insert_note_img.php",
                    response -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(new String(response.data));
                            Log.d("Image Upload Response", jsonResponse.toString());
                            int success = jsonResponse.optInt("success", -1);
                            if (success == 1) {
                                Log.d("Image Upload", "Image uploaded successfully.");
                                onComplete.run();
                            } else {
                                Log.d("Image Upload", "Image upload failed.");
                            }
                        } catch (Exception e) {
                            saveNote.setEnabled(true);
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    params.put("note_id", noteId);
                    params.put("note", noteContent);
                    params.put("isChecked", String.valueOf(isChecked ? 1 : 0));
                    params.put("pureNote", String.valueOf(true ? 1 : 0));
                    params.put("added_at", currentDate);
                    return params;
                }
            };

            multipartRequest.setRetryPolicy(new DefaultRetryPolicy(5000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            multipartRequest.addDataPart("image", new VolleyMultipartRequest.DataPart(imageFile.getName(), imageData, "image/jpeg"));
            requestqueue.add(multipartRequest);
        } else {
            progressBar.setVisibility(View.GONE);
            saveNote.setEnabled(true);
            Toast.makeText(this, "Invalid image path!", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteNoteContentsData(String noteContentsId) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/note_contents_delete.php",
                response -> {
                    try {
                        Log.d("Response", response);
                        JSONObject jobj = new JSONObject(response);
                        int success = jobj.optInt("success", -1);
                        if (success == 1) {
                            progressBar.setVisibility(View.GONE);
                        } else {
                            Toast.makeText(getApplicationContext(), "Failed to update note contents", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } finally {
                        progressBar.setVisibility(View.GONE);
                    }
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Error updating note", Toast.LENGTH_SHORT).show();
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("note_contents_id", noteContentsId);
                return params;
            }
        };

        requestqueue.add(request);
    }


    private void addNoteWithCheckbox(String noteContentsId, String noteText, boolean isChecked) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View noteEntryView = inflater.inflate(R.layout.add_note_layout, null);

        EditText noteEditText = noteEntryView.findViewById(R.id.note);
        MaterialCheckBox checkBox = noteEntryView.findViewById(R.id.note_check_box);

        if(isChecked) {
            noteEditText.setText(noteText);
            checkBox.setChecked(true);
        } else {
            noteEditText.setText(noteText);
        }
        noteEditText.requestFocus();

        noteEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (noteEditText.getText().length() == 0) {
                        deleteNote();
                        focusOnLastNote();
                        return true;
                    }
                }
                return false;
            }
        });

        noteEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String currentText = noteEditText.getText().toString();
                if (currentText.endsWith("\n")) {
                    Log.d("TextWatcher", "New line character detected, adding new note");
                    noteEditText.setText(currentText.substring(0, currentText.length() - 1));
                    addNoteWithCheckbox("", "", false);
                    scrollToBottom();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (noteContentsId.isEmpty()) {
            noteEntryView.setTag(R.id.note_contents_id_tag, "");
            noteEntryView.setTag(R.id.is_pure_note_tag, false);
        } else {
            noteEntryView.setTag(R.id.note_contents_id_tag, noteContentsId);
            noteEntryView.setTag(R.id.is_pure_note_tag, false);
        }
        noteContainer.addView(noteEntryView);

        addCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!title.getText().toString().isEmpty()) {
                    addPureNote("", "");
                } else {
                    progressBar.setVisibility(View.GONE);
                    errorToast.setVisibility(View.VISIBLE);
                    toastMessage.setText("Please add a title to continue.");
                    Animation fadeIn =  AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                    errorToast.startAnimation(fadeIn);
                }
            }
        });
    }

    private void addPureNote(String noteContentsId, String noteContents) {
        View noteView = LayoutInflater.from(this).inflate(R.layout.purely_add_note, noteContainer, false);
        TextView noteEditText = noteView.findViewById(R.id.note);
        noteEditText.setText(noteContents);
        noteView.setTag(R.id.note_contents_id_tag, noteContentsId);
        noteView.setTag(R.id.is_pure_note_tag, true);

        int childCount = noteContainer.getChildCount();
        if (childCount > 0) {
            noteEditText.setHint("");
        }

        noteContainer.addView(noteView);
        noteEditText.requestFocus();

        noteEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (noteEditText.getText().length() == 0 && noteContainer.indexOfChild(noteView) > 0) {
                        deleteNote();
                        focusOnLastNote();
                        return true;
                    }
                }
                return false;
            }
        });

        noteEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String currentText = noteEditText.getText().toString();
                if (currentText.endsWith("\n")) {
                    Log.d("TextWatcher", "New line character detected, adding new note");
                    noteEditText.setText(currentText.substring(0, currentText.length() - 1));
                    addPureNote("", "");
                    scrollToBottom();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}});

        addCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!title.getText().toString().isEmpty()) {
                    addNoteWithCheckbox("", "", false);
                } else {
                    progressBar.setVisibility(View.GONE);
                    errorToast.setVisibility(View.VISIBLE);
                    toastMessage.setText("Please add a title to continue.");
                    Animation fadeIn =  AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                    errorToast.startAnimation(fadeIn);
                }
            }
        });
    }

    private void addNoteWithImage(Uri imageUri) {
        View noteEntryView = LayoutInflater.from(this).inflate(R.layout.purely_add_note, noteContainer, false);
        TextView noteEditText = noteEntryView.findViewById(R.id.note);
        noteEntryView.setTag(R.id.note_contents_id_tag, "");
        noteEntryView.setTag("imgNote");
        noteEditText.setHint("");

        noteEditText.requestFocus();
        noteEditText.setHint("");
        scrollToBottom();

        imageUriList.add(imageUri);

        Glide.with(this)
                .asBitmap()
                .load(imageUri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        int desiredWidth = noteEditText.getWidth();
                        int desiredHeight = 900;
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(resource, desiredWidth, desiredHeight, false);

                        Drawable drawable = new BitmapDrawable(getResources(), resizedBitmap);
                        drawable.setBounds(0, 0, desiredWidth, desiredHeight);

                        noteEditText.setCompoundDrawables(null, drawable, null, null);
                        noteEditText.setCompoundDrawablePadding(10);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });

        noteEditText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    if (noteEditText.getText().length() == 0) {
                        noteContainer.removeView(noteEntryView);
                        imageUriList.remove(imageUri);
                        focusOnLastNote();
                        return true;
                    }
                }
                return false;
            }
        });

        noteEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String currentText = noteEditText.getText().toString();
                if (currentText.endsWith("\n")) {
                    Log.d("TextWatcher", "New line character detected, adding new note");
                    noteEditText.setText(currentText.substring(0, currentText.length() - 1));
                    addPureNote("", "");
                    scrollToBottom();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        addCheckMark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!title.getText().toString().isEmpty()) {
                    addNoteWithCheckbox("", "", false);
                } else {
                    progressBar.setVisibility(View.GONE);
                    errorToast.setVisibility(View.VISIBLE);
                    toastMessage.setText("Please add a title to continue.");
                    Animation fadeIn =  AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                    errorToast.startAnimation(fadeIn);
                }
            }
        });

        noteContainer.addView(noteEntryView);
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
            addNoteWithImage(imageUri);
        }
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

    private void focusOnLastNote() {
        int childCount = noteContainer.getChildCount();
        if (childCount > 0) {
            View lastNoteView = noteContainer.getChildAt(childCount - 1);
            EditText lastNoteEditText = lastNoteView.findViewById(R.id.note);
            lastNoteEditText.requestFocus();
            lastNoteEditText.setSelection(lastNoteEditText.getText().length());
        }
    }

    private void scrollToBottom() {
        scrollView.post(() -> {
            int childCount = noteContainer.getChildCount();
            if (childCount > 0) {
                View latestNote = noteContainer.getChildAt(childCount - 1);

                latestNote.requestFocus();

                int targetY = latestNote.getBottom();
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

}
