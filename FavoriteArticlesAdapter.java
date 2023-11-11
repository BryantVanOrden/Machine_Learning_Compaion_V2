package np.com.bryant.myapp;

import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import np.com.bryant.myapp.model.CustomArticle;

public class FavoriteArticlesAdapter extends RecyclerView.Adapter<FavoriteArticlesAdapter.ArticleViewHolder> {
    private List<CustomArticle> articles;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public FavoriteArticlesAdapter(Context context, List<CustomArticle> articles, FirebaseFirestore firebaseFirestore, FirebaseAuth firebaseAuth) {
        this.context = context;
        this.articles = articles;
        this.firebaseFirestore = firebaseFirestore;
        this.firebaseAuth = firebaseAuth;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_recycler_row, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        CustomArticle article = articles.get(position);

        // Set your article details to the views in the view holder
        holder.titleTextView.setText(article.getTitle());
        holder.sourceTextView.setText(article.getSource().getName());

        // Load the image using Glide or any other image loading library
        Glide.with(holder.itemView)
                .load(article.getUrlToImage())
                .into(holder.imageView);

        // Set OnClickListener for the itemView
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), NewsFullActivity.class);
            intent.putExtra("url", article.getUrl()); // Pass the URL to the NewsFullActivity
            v.getContext().startActivity(intent);
        });

        // Set OnLongClickListener for the itemView to remove the article on long press
        holder.itemView.setOnLongClickListener(v -> {
            removeArticle(position); // Function to remove the article from favorites
            return true; // Consume the long-click event
        });
    }

    // Method to remove an article from the list and Firebase

    private void removeArticle(int position) {
        CustomArticle removedArticle = articles.get(position);

        // Delete the article from Firebase
        if (firebaseAuth.getCurrentUser() != null) {
            String userId = firebaseAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<Object> favoriteArticles = (List<Object>) documentSnapshot.get("favoriteArticles");
                            int index = getIndexOfArticle(removedArticle, favoriteArticles);
                            if (index != -1) {
                                favoriteArticles.remove(index);
                                documentSnapshot.getReference().update("favoriteArticles", favoriteArticles)
                                        .addOnSuccessListener(aVoid -> {
                                            // Successfully deleted from Firebase
                                            Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Failed to delete from Firebase, handle accordingly
                                            Toast.makeText(context, "Failed to remove from favorites", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    });
        }

        // Remove the article from the local list
        articles.remove(position);
        notifyItemRemoved(position);
    }

    private int getIndexOfArticle(CustomArticle removedArticle, List<Object> favoriteArticles) {
        for (int i = 0; i < favoriteArticles.size(); i++) {
            Map<String, Object> articleMap = (Map<String, Object>) favoriteArticles.get(i);
            if (isSameArticle(removedArticle, articleMap)) {
                return i; // Return the index if found
            }
        }
        return -1; // Return -1 if not found
    }

    private boolean isSameArticle(CustomArticle removedArticle, Map<String, Object> articleMap) {
        // Compare the details of the articles to check for equality
        // This may involve comparing fields like author, title, URL, etc.
        return removedArticle.getAuthor().equals(articleMap.get("author")) &&
                removedArticle.getTitle().equals(articleMap.get("title")) &&
                // Add other fields to compare
                // ...
                true; // Return true if all fields are equal
    }






    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void setArticles(List<CustomArticle> articles) {
        this.articles = articles;
        notifyDataSetChanged();
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, sourceTextView;
        ImageView imageView;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.article_title);
            sourceTextView = itemView.findViewById(R.id.article_source);
            imageView = itemView.findViewById(R.id.article_image_view);
        }
    }
}
