package np.com.bryant.myapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import np.com.bryant.myapp.model.CustomArticle;
import np.com.bryant.myapp.model.UserModel;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    RecyclerView recyclerView;
    List<CustomArticle> articleList;
    NewsRecyclerAdapter adapter;
    LinearProgressIndicator progressIndicator;
    Button btn1, btn2, btn3, btn4, btn5, btn6, btn7;
    EditText searchEditText; // Added search EditText
    Button searchButton; // Added search Button

    private AdView mAdView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private UserModel userModel;

    private long lastModifiedTimestamp = 0L; // Initialize with a default value
    private static final String FILE_NAME = "news_data.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        fetchUserModelFromFirebase(); // Fetch the UserModel

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this::forceGetNews);

        recyclerView = findViewById(R.id.news_recycler_view);
        progressIndicator = findViewById(R.id.progress_bar);
        btn1 = findViewById(R.id.btn_1);
        btn2 = findViewById(R.id.btn_2);
        btn3 = findViewById(R.id.btn_3);
        btn4 = findViewById(R.id.btn_4);
        btn5 = findViewById(R.id.btn_5);
        btn6 = findViewById(R.id.btn_6);
        btn7 = findViewById(R.id.btn_7);
        searchEditText = findViewById(R.id.searchEditText); // Initialize search EditText
        searchButton = findViewById(R.id.searchButton); // Initialize search Button

        setButtonClickListeners();

        // Set an OnClickListener for the search button
        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().toLowerCase();
            performSearch(query);
        });

        ImageView imageButton = findViewById(R.id.imageButton);
        imageButton.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), MyUserActivity.class);
            startActivity(intent);
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //checkForUpdates();
    }

    private void performSearch(String query) {

        query = query.toString().toLowerCase();

        List<CustomArticle> filteredList = new ArrayList<>();
        for (CustomArticle article : articleList) {
            String title = article.getTitle().toLowerCase();
            String content = article.getContent().toLowerCase();
            String description = article.getDescription().toLowerCase();

            if (title.contains(query) || content.contains(query) || description.contains(query)) {
                filteredList.add(article);
            }
        }

        // Update the adapter with filtered data
        adapter.updateData(filteredList);
    }



    private void fetchUserModelFromFirebase() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getUid(); // Get the current user's ID

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userModel = documentSnapshot.toObject(UserModel.class);
                        if (userModel != null) {
                            setupRecyclerView(userModel);
                            getNews();// Call setupRecyclerView with the fetched userModel
                        } else {
                            // Handle the case where the UserModel is null
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failures in fetching UserModel from Firebase
                });
    }

    private void setupRecyclerView(UserModel currentUser) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        articleList = new ArrayList<>();
        adapter = new NewsRecyclerAdapter(articleList, currentUser, FirebaseAuth.getInstance(), FirebaseFirestore.getInstance());
        recyclerView.setAdapter(adapter);
    }

    void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void checkForUpdates() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("data/news_data.json");

        storageRef.getMetadata().addOnSuccessListener(storageMetadata -> {
            long currentTimestamp = storageMetadata.getUpdatedTimeMillis();
            if (currentTimestamp > lastModifiedTimestamp) {
                lastModifiedTimestamp = currentTimestamp;
                downloadAndReplaceNewsData();
                showToast("Updated from Firebase");
            }
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        }).addOnFailureListener(e -> {
            showToast("Failed to check for updates");
            Log.e("Firebase Storage", "Failed to get file metadata", e);
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void downloadAndReplaceNewsData() {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("data/news_data.json");
        File localFile = new File(getFilesDir(), FILE_NAME);
        storageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            getNewsFromLocalFile(localFile); // Fetch the updated data after download is complete
        }).addOnFailureListener(e -> {
            Log.e("Firebase Storage", "Failed to download file", e);
        });
    }

    private void getNewsFromLocalFile(File localFile) {
        new Thread(() -> {
            boolean dataFound = retrieveDataFromFile(localFile);
            if (!dataFound) {
                showToast("Local data empty or not found. Fetching from Firebase.");
                forceGetNews();
            }
        }).start();
    }

    private boolean retrieveDataFromFile(File file) {
        try {
            if (file.exists()) {
                String jsonFromFile = getJsonFromFile(file);
                if (!jsonFromFile.isEmpty()) {
                    Gson gson = new Gson();
                    List<CustomArticle> allArticles = gson.fromJson(jsonFromFile, new TypeToken<List<CustomArticle>>() {}.getType());

                    runOnUiThread(() -> {
                        changeInProgress(false);
                        if (adapter != null) {
                            articleList.clear();
                            articleList.addAll(allArticles);
                            adapter.updateData(articleList);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    return true;
                }
            }
        } catch (IOException e) {
            Log.e("File Retrieval", "Error reading file", e);
        }
        return false;
    }

    private String getJsonFromFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
        }
        return content.toString();
    }

    private void setButtonClickListeners() {
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btn7.setOnClickListener(this);
    }
    private void changeInProgress(boolean show) {
        if (show) {
            progressIndicator.setVisibility(View.VISIBLE);
        } else {
            progressIndicator.setVisibility(View.INVISIBLE);
        }
    }

    private void getNews() {
        changeInProgress(true);
        new Thread(() -> {
            boolean dataFound = retrieveDataFromFile(new File(getFilesDir(), FILE_NAME));
            if (!dataFound) {
                showToast("Local data empty or not found. Fetching from assets.");
                fetchFromAssets();
            }
        }).start();
    }

    private void forceGetNews() {
        swipeRefreshLayout.setRefreshing(true);
        showToast("Updating...");
        downloadAndReplaceNewsData();
        swipeRefreshLayout.setRefreshing(false); // Stop the refresh animation
        showToast("You are up to date");
    }

    private void fetchFromAssets() {
        new Thread(() -> {
            try {
                String jsonFromAssets = getJsonFromAssets("news_data.json");
                if (!jsonFromAssets.isEmpty()) {
                    Gson gson = new Gson();
                    List<CustomArticle> allArticles = gson.fromJson(jsonFromAssets, new TypeToken<List<CustomArticle>>() {}.getType());

                    runOnUiThread(() -> {
                        changeInProgress(false);
                        if (adapter != null) {
                            articleList.clear();
                            articleList.addAll(allArticles);
                            adapter.updateData(articleList);
                            adapter.notifyDataSetChanged();
                        }
                    });
                } else {
                    Log.e("JSON Reading", "JSON data from assets is empty");
                }
            } catch (IOException e) {
                Log.e("JSON Reading", "Failed to read or parse JSON from assets", e);
            }
        }).start();
    }

    private String getJsonFromAssets(String fileName) throws IOException {
        return new String(getAssets().open(fileName).readAllBytes());
    }

    @Override
    public void onClick(View v) {
        Button btn = (Button) v;
        String query = btn.getText().toString().toLowerCase();

        // Filter the data based on the button click
        List<CustomArticle> filteredList = new ArrayList<>();
        for (CustomArticle article : articleList) {
            String title = article.getTitle().toLowerCase();
            String content = article.getContent().toLowerCase();
            String description = article.getDescription().toLowerCase();

            if (title.contains(query) || content.contains(query) || description.contains(query)) {
                filteredList.add(article);
            }
        }

        // Update the adapter with filtered data
        adapter.updateData(filteredList);
    }
}
