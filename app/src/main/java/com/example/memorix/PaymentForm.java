package com.example.memorix;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
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
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PaymentForm extends AppCompatActivity {

    private View errorToast, errorToastLayout, toastNest;
    TextView toastMessage, okBtn;
    private TextView fileView;
    private RadioGroup radioGroup;
    private RadioButton selectedRadioButton;
    private String subscriptionType;
    private LinearLayout uploadLayout, submit;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private RequestQueue requestQueue;
    private RelativeLayout progressBar;
    private int selectedAmount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_form);

        errorToast = findViewById(R.id.error_toast);
        errorToastLayout = findViewById(R.id.toast_layout);
        toastNest = findViewById(R.id.toastNest);
        toastMessage = findViewById(R.id.error_message);
        okBtn = findViewById(R.id.ok_btn);
        requestQueue = Volley.newRequestQueue(this);
        progressBar = findViewById(R.id.progressbar);
        fileView = findViewById(R.id.file);
        radioGroup = findViewById(R.id.radio_group);
        uploadLayout = findViewById(R.id.upload);
        submit = findViewById(R.id.submit);

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

        uploadLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.GONE);
                errorToast.setVisibility(View.VISIBLE);
                toastMessage.setText("Please fill all the required data");
                Animation fadeIn =  AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
                errorToast.startAnimation(fadeIn);
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
            String uriString = imageUri.toString();
            String displayText = uriString.length() > 20 ? uriString.substring(0, 20) + "..." : uriString;
            fileView.setText(displayText);

            submit.setOnClickListener(v -> {
                submitSubscriptionAndProof(imageUri);
            });
        }
    }

    private void submitSubscriptionAndProof(Uri imageUri) {
        progressBar.setVisibility(View.VISIBLE);
        SharedPreferences sharedPreferences = getSharedPreferences("UserPreferences", MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");

        RadioGroup radioGroup = findViewById(R.id.radio_group);
        int selectedId = radioGroup.getCheckedRadioButtonId();
        selectedAmount = (selectedId == R.id.monthly) ? 150 : (selectedId == R.id.yearly) ? 999 : 0;

        progressBar.setVisibility(View.VISIBLE);

        if (imageUri != null) {
            String filePath = getRealPathFromURI(imageUri);
            if (filePath != null) {
                File imageFile = new File(filePath);

                byte[] imageData = getFileDataFromDrawable(imageFile);
                Log.d("Debug", "Image data length: " + (imageData != null ? imageData.length : 0));

                if (imageData != null && imageData.length > 0) {
                    VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(
                            Request.Method.POST,
                            "https://pink-boar-882869.hostingersite.com/insert_payment_form.php",
                            response -> {
                                String responseString = new String(response.data);
                                Log.d("Upload Response", responseString);
                                Intent intent = new Intent(getApplicationContext(), AfterPayment.class);
                                startActivity(intent);
                                finish();
                            },
                            error -> {
                                progressBar.setVisibility(View.GONE);
                            }) {
                        @Override
                        protected Map<String, String> getParams() {
                            Map<String, String> params = new HashMap<>();
                            params.put("user_id", userId);
                            params.put("amount", String.valueOf(selectedAmount));
                            return params;
                        }
                    };

                    multipartRequest.addDataPart("image", new VolleyMultipartRequest.DataPart(imageFile.getName(), imageData, "image/jpeg"));
                    requestQueue.add(multipartRequest);
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Invalid image data!", Toast.LENGTH_SHORT).show();
                }
            } else {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Invalid image path!", Toast.LENGTH_SHORT).show();
            }
        } else {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show();
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

}