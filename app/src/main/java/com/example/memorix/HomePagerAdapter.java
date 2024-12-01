package com.example.memorix;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomePagerAdapter extends RecyclerView.Adapter<HomePagerAdapter.ViewHolder> {

    private final List<LibraryItem> libraryItems;
    private final Context context;
    private final RequestQueue requestQueue;

    public HomePagerAdapter(Context context, List<LibraryItem> libraryItems) {
        this.context = context;
        this.libraryItems = libraryItems;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recent_questionnaire_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LibraryItem item = libraryItems.get(position);

        holder.userNameView.setText(item.getName());
        holder.questionnaireTitleView.setText(item.getTitle());
        holder.items.setText(item.getItemsCount() + " items");

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, StudyGuide.class);
            intent.putExtra("questionnaire_id", item.getQuestionnaireId());
            context.startActivity(intent);
        });

        fetchUser(holder.libraryProf);
    }

    @Override
    public int getItemCount() {
        return Math.min(libraryItems.size(), 5);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userNameView, questionnaireTitleView, items;
        ImageView libraryProf;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameView = itemView.findViewById(R.id.user_name);
            questionnaireTitleView = itemView.findViewById(R.id.title);
            items = itemView.findViewById(R.id.items);
            libraryProf = itemView.findViewById(R.id.profile_img);
        }
    }

    private void fetchUser(ImageView libraryProf) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("UserPreferences", Context.MODE_PRIVATE);
        String userId = sharedPreferences.getString("user_id", "");

        String url = "https://pink-boar-882869.hostingersite.com/user_fetch.php";

        StringRequest request = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("ServerResponse", response);
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        if (jsonResponse.getString("status").equals("success")) {
                            JSONObject user = jsonResponse.getJSONObject("user");
                            String profileImage = "https://pink-boar-882869.hostingersite.com/" + user.getString("profile");

                            loadProfileImageWithAuth(profileImage, libraryProf);

                        } else {
                            Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error parsing user data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
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

        request.setRetryPolicy(new DefaultRetryPolicy(10000, 1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    private void loadProfileImageWithAuth(String imageUrl, ImageView libraryProf) {
        ImageRequest imageRequest = new ImageRequest(imageUrl,
                response -> Glide.with(context)
                        .load(response)
                        .apply(RequestOptions.circleCropTransform())
                        .into(libraryProf),
                0,
                0,
                ImageView.ScaleType.CENTER_CROP,
                Bitmap.Config.RGB_565,
                error -> Log.e("Image Load Error", "Error loading image: " + error.getMessage())) {
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
}
