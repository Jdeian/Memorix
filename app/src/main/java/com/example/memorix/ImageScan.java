package com.example.memorix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import com.google.android.material.button.MaterialButton;
import com.googlecode.tesseract.android.TessBaseAPI;
import java.io.File;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageScan extends AppCompatActivity {

    private View errorToast, errorToastLayout, toastNest;
    private TextView toastMessage, okBtn, file;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private Uri imageUri;
    private LinearLayout uploadFileButton;
    private RequestQueue requestQueue;
    private RelativeLayout progressBar;
    private MaterialButton generateBtn;
    private AtomicInteger pendingRequests = new AtomicInteger(0);
    private String classId;
    private String class_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_image_scan);

        file = findViewById(R.id.file);
        generateBtn = findViewById(R.id.generatebtn);
        errorToast = findViewById(R.id.error_toast);
        errorToast = findViewById(R.id.error_toast);
        errorToastLayout = findViewById(R.id.toast_layout);
        toastNest = findViewById(R.id.toastNest);
        toastMessage = findViewById(R.id.error_message);
        okBtn = findViewById(R.id.ok_btn);
        uploadFileButton = findViewById(R.id.upload_file_button);
        requestQueue = Volley.newRequestQueue(this);
        progressBar = findViewById(R.id.progressbar);

        classId = getIntent().getStringExtra("class_id");
        class_name = getIntent().getStringExtra("class_name");

        uploadFileButton.setOnClickListener(v -> openImagePicker());

        generateBtn.setOnClickListener(v -> {
            toastMessage.setText("Please select an image to continue");
            errorToast.setVisibility(View.VISIBLE);
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            errorToast.startAnimation(fadeIn);
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

    private void openImagePicker() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Image Source")
                .setItems(new String[]{"Gallery", "Camera"}, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, PICK_IMAGE_REQUEST);
                    } else {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            imageUri = Uri.fromFile(new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "temp.jpg"));
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
                        }
                    }
                });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                Uri fileUri = data.getData();
                String uriString = fileUri.toString();
                String displayText = uriString.length() > 20 ? uriString.substring(0, 20) + "..." : uriString;
                Log.d("adasdasdasd", " sdsadasdasd  "+ displayText);
                file.setText(displayText);
                if (fileUri != null) {
                    generateBtn.setOnClickListener(v -> {
                        progressBar.setVisibility(View.VISIBLE);
                        processImage(fileUri);
                    });
                }
            } else if (requestCode == CAPTURE_IMAGE_REQUEST) {
                generateBtn.setOnClickListener(v -> {
                    progressBar.setVisibility(View.VISIBLE);
                    processImage(imageUri);
                });
            }
        }
    }

    private void processImage(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
            String extractedText = performOCRFromBitmap(bitmap);
            processExtractedText(extractedText);
        } catch (IOException e) {
            showErrorToast("Error loading image: " + e.getMessage());
        }
    }

    private void copyTessDataFiles() {
        try {
            String dirPath = getFilesDir() + "/tessdata/";
            File dir = new File(dirPath);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            String filepath = dirPath + "eng.traineddata";

            File trainedDataFile = new File(filepath);
            if (!trainedDataFile.exists()) {
                InputStream inputStream = getAssets().open("tessdata/eng.traineddata");
                OutputStream outputStream = new FileOutputStream(filepath);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }

                inputStream.close();
                outputStream.flush();
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String performOCRFromBitmap(Bitmap image) {
        TessBaseAPI tesseract = new TessBaseAPI();

        String datapath = getFilesDir() + "/";

        copyTessDataFiles();

        Log.d("OCR", "Data path: " + datapath);
        File trainedDataFile = new File(datapath + "tessdata/eng.traineddata");
        Log.d("OCR", "Trained data absolute path: " + trainedDataFile.getAbsolutePath());
        Log.d("OCR", "Does the file exist? " + trainedDataFile.exists());

        File tessdataDir = new File(datapath + "tessdata");
        Log.d("OCR", "Does tessdata folder exist? " + tessdataDir.exists());

        if (tessdataDir.exists() && trainedDataFile.exists()) {
            tesseract.init(datapath, "eng");
        } else {
            Log.e("OCR", "Tessdata directory or trained data file missing!");
            return "Error: Missing tessdata or traineddata";
        }

        tesseract.setImage(image);
        String extractedText = tesseract.getUTF8Text();
        tesseract.end();

        return extractedText;
    }

    private void processExtractedText(String extractedText) {
        parseDefinitionsAndTerms(extractedText);
    }

    private void parseDefinitionsAndTerms(String extractedText) {
        String[] lines = extractedText.split("\n");
        ArrayList<String> terms = new ArrayList<>();
        ArrayList<String> definitions = new ArrayList<>();

        String currentTerm = "";
        StringBuilder currentDefinition = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.contains("com.example.memorix") || line.matches(".*\\d{2}:\\d{2}:\\d{2}.*")) {
                continue;
            }

            if ((line.matches("^[A-Z].{0,100}$") && line.split(" ").length <= 7) || line.contains(":") || line.contains("-")) {
                if (!currentTerm.isEmpty()) {
                    String defText = currentDefinition.toString().trim();
                    if (!defText.isEmpty()) {
                        definitions.add(cleanText(defText));
                    }
                    terms.add(cleanTerm(currentTerm));
                }
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
                    currentTerm = cleanTerm(parts[0].trim());
                    currentDefinition = new StringBuilder(parts[1].trim() + " ");
                } else if (line.contains("-")) {
                    String[] parts = line.split("-", 2);
                    currentTerm = cleanTerm(parts[0].trim());
                    currentDefinition = new StringBuilder(parts[1].trim() + " ");
                } else {
                    currentTerm = cleanTerm(line);
                    currentDefinition = new StringBuilder();
                }
            } else {
                currentDefinition.append(line).append(" ");
            }
        }

        if (!currentTerm.isEmpty() && currentDefinition.length() > 0) {
            String defText = currentDefinition.toString().trim();
            if (!defText.isEmpty()) {
                definitions.add(cleanText(defText));
            }
            terms.add(cleanTerm(currentTerm));
        }

        if (terms.size() >= 4 && definitions.size() >= 4) {
            int numberOfPairs = Math.min(terms.size(), definitions.size());
            ArrayList<String> termsToInsert = new ArrayList<>(terms.subList(0, numberOfPairs));
            ArrayList<String> definitionsToInsert = new ArrayList<>(definitions.subList(0, numberOfPairs));
            sendQuestionnaireData(definitionsToInsert, termsToInsert);
        } else {
            showErrorToast("The image you selected is invalid, please select another one.");
        }

    }

    private String cleanTerm(String term) {
        term = term.replaceAll("(?i)^what is ", "").trim();
        return term.replaceAll("[^a-zA-Z0-9 ]", "").trim();
    }


    private String cleanText(String text) {
        return text.replaceAll("[^a-zA-Z0-9 .,]", "").trim();
    }

    public void sendQuestionnaireData(ArrayList<String> definitions, ArrayList<String> terms) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");
        String title = sharedPreferences.getString("title", "");
        String description = sharedPreferences.getString("description", "");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String currentDate = now.format(formatter);

        if (!isNetworkAvailable()) {
            showErrorToast("Network error, Please connect to a network and try again");
            return;
        }

        pendingRequests.set(1);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/questionnaire_insert.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("onResponse", response);
                        try {
                            JSONObject jobj = new JSONObject(response);
                            int success = jobj.optInt("success", -1);
                            if (success == 1) {
                                JSONObject data = jobj.getJSONObject("data");
                                String questionnaireId = data.optString("questionnaire_id", "N/A");

                                for (int i = 0; i < terms.toArray().length; i++) {
                                    String term = terms.get(i);
                                    String definition = definitions.get(i);

                                    if (!term.isEmpty() && !definition.isEmpty()) {
                                        pendingRequests.incrementAndGet();
                                        sendDefinitionToDatabase(questionnaireId, definition, term);
                                    } else {
                                        Log.d("Skipped", "Empty definition or term at index: " + i);
                                    }
                                }
                                pendingRequests.decrementAndGet();
                                checkPendingRequests(questionnaireId);
                            } else {
                                showErrorToast("Failed to insert questionnaire data");
                            }
                        } catch (Exception e) {
                            showErrorToast("Error processing response: " + e.getMessage());
                        } finally {
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        handleVolleyError(error);
                        progressBar.setVisibility(View.GONE);
                    }
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);
                params.put("title", title);
                params.put("description", description);
                params.put("current_date", currentDate);
                return params;
            }
        };

        requestQueue.add(request);
    }


    private void sendDefinitionToDatabase(String questionnaireId, String definition, String termData) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/definition_insert.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("onResponse", response);
                        try {
                            JSONObject jobj = new JSONObject(response);
                            int success = jobj.optInt("success", -1);
                            if (success == 1) {
                                JSONObject data = jobj.getJSONObject("data");
                                String definitionId = data.optString("definition_id", "N/A");
                                pendingRequests.incrementAndGet();
                                sendTermToDatabase(definitionId, termData, questionnaireId);
                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Inserting description failed", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                        pendingRequests.decrementAndGet();
                        checkPendingRequests(questionnaireId);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        if (error.networkResponse != null) {
                            progressBar.setVisibility(View.GONE);
                            String responseBody = new String(error.networkResponse.data);
                            Log.d("Error Response", responseBody);
                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }) {

            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("questionnaire_id", questionnaireId);
                params.put("definition", definition);
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void sendTermToDatabase(String definitionId, String term, String questionnaireId) {
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/term_insert.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("onResponse", response);
                        try {
                            JSONObject jobj = new JSONObject(response);
                            int success = jobj.optInt("success", -1);
                            pendingRequests.decrementAndGet();
                            checkPendingRequests(questionnaireId);
                            if (success == 1) {

                            } else {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(getApplicationContext(), "Inserting term failed", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        if (error.networkResponse != null) {
                            String responseBody = new String(error.networkResponse.data);
                            Log.d("Error Response", responseBody);
                        } else {
                            Log.e("Error: ", "Error: " + error.getMessage());
                        }
                    }
                }) {

            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("definition_id", definitionId);
                params.put("term", term);
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void checkPendingRequests(String questionnaireId) {
        Log.e("asdasda", "CLASS ID: " + classId);
        if (pendingRequests.get() == 0 && classId == null) {
            progressBar.setVisibility(View.GONE);
            Intent intent = new Intent(getApplicationContext(), StudyGuide.class);
            intent.putExtra("questionnaire_id", questionnaireId);
            startActivity(intent);
            finish();
        } else if (pendingRequests.get() == 0 && classId != null) {
            insertClassData(questionnaireId);
        }
    }

    private void insertClassData(String questionnaireId) {
        progressBar.setVisibility(View.VISIBLE);
        StringRequest request = new StringRequest(Request.Method.POST, "https://pink-boar-882869.hostingersite.com/class_questionnaire_insert.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("onResponse", response);
                        try {
                            JSONObject jobj = new JSONObject(response);
                            int success = jobj.optInt("success", -1);
                            if (success == 1) {
                                progressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(getApplicationContext(), ClassContents.class);
                                intent.putExtra("class_id", classId);
                                intent.putExtra("class_name", class_name);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to insert class", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            String responseBody = new String(error.networkResponse.data);
                            Log.d("Error Response", responseBody);

                            if (statusCode == 404) {
                                Animation fadeIn =  AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                                progressBar.setVisibility(View.GONE);
                                errorToast.setVisibility(View.VISIBLE);
                                toastMessage.setText("Questionnaire already exist in the class.");
                                errorToast.startAnimation(fadeIn);
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }) {

            @Override
            public Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("questionnaire_id", questionnaireId);
                params.put("class_id", classId);
                Log.d("Params", params.toString());
                return params;
            }
        };

        requestQueue.add(request);
    }

    private void showErrorToast(String message) {
        progressBar.setVisibility(View.GONE);
        toastMessage.setText(message);
        errorToast.setVisibility(View.VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        errorToast.startAnimation(fadeIn);
    }

    private void handleVolleyError(VolleyError error) {
        progressBar.setVisibility(View.GONE);
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

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}