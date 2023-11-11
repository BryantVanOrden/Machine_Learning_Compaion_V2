package np.com.bryant.myapp.model;

import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;
import java.util.Objects;

public class CustomArticle {
    private CustomSource source;
    private String author;
    private String title;
    private String description;
    private String url;
    private String urlToImage;
    private String publishedAt;
    private String content;

    public CustomSource getSource() {
        return source;
    }

    public void setSource(CustomSource source) {
        this.source = source;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getArticleId(List<DocumentSnapshot> favoriteArticlesSnapshots) {
        for (int i = 0; i < favoriteArticlesSnapshots.size(); i++) {
            DocumentSnapshot snapshot = favoriteArticlesSnapshots.get(i);
            CustomArticle favoriteArticle = snapshot.toObject(CustomArticle.class);

            if (this.equals(favoriteArticle)) {
                return i; // Return the index if found
            }
        }
        return -1; // Return -1 if not found
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CustomArticle that = (CustomArticle) obj;
        // You can define your own criteria for equality here, e.g., compare URLs
        return Objects.equals(this.url, that.url);
    }

    @Override
    public int hashCode() {
        // You can implement your own hashCode if needed
        return Objects.hash(this.url);
    }
}


