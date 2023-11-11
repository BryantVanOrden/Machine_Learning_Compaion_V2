package np.com.bryant.myapp;

import android.content.Intent;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import np.com.bryant.myapp.model.CustomArticle;
import np.com.bryant.myapp.model.UserModel;

public class NewsRecyclerAdapter extends RecyclerView.Adapter<NewsRecyclerAdapter.NewsViewHolder> {
    private List<CustomArticle> articleList;
    private UserModel currentUser; // Instance of the user model
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    NewsRecyclerAdapter(List<CustomArticle> articleList, UserModel currentUser, FirebaseAuth auth, FirebaseFirestore firestore) {
        this.articleList = new ArrayList<>(articleList);
        this.currentUser = currentUser;
        this.firebaseAuth = auth;
        this.firebaseFirestore = firestore;
    }


    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_recycler_row, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        CustomArticle article = articleList.get(position);
        holder.titleTextView.setText(article.getTitle());
        holder.sourceTextView.setText(article.getSource().getName());

        // Use Glide to load the image
        Glide.with(holder.itemView)
                .load(article.getUrlToImage())
                .error(R.drawable.no_image_icon)
                .placeholder(R.drawable.no_image_icon)
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), NewsFullActivity.class);
            intent.putExtra("url", article.getUrl());
            v.getContext().startActivity(intent);
        });

        // Long-press listener for adding to favorites
        holder.itemView.setOnLongClickListener(view -> {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
            currentUser.getFavoriteArticles().add(article); // Adding to favorites

            // Save the updated user data back to Firestore
            saveFavoritesToFirestore();

            Toast.makeText(view.getContext(), "Article saved to favorites", Toast.LENGTH_SHORT).show();
            return true; // Consume the long click
        });
    }

    private void saveFavoritesToFirestore() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("users")
                .document(userId)
                .set(currentUser)
                .addOnSuccessListener(aVoid -> {
                    // Optional: Handle success
                })
                .addOnFailureListener(e -> {
                    // Optional: Handle failure
                });
    }


    void updateData(List<CustomArticle> data) {
        articleList.clear();
        articleList.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, sourceTextView;
        ImageView imageView;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.article_title);
            sourceTextView = itemView.findViewById(R.id.article_source);
            imageView = itemView.findViewById(R.id.article_image_view);
        }
    }
}



