package com.example.reddit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * Activité pour afficher les commentaires d'un post reddit
 * Created by Maxim on 02/5/2016.
 */
public class CommentsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        //Permet de retrouver le id du post.
        Intent intent = getIntent();
        String postId = intent.getStringExtra("postId");

        displayPostComments(postId);
    }


    /**
     *
     * Affiche les commentaires associés au post
     *
     * @param postId Le id du post
     */
    private void displayPostComments(String postId) {
        WebView webview = (WebView)findViewById(R.id.webView);
        webview.getSettings().setJavaScriptEnabled(true); // enable javascript
        webview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(CommentsActivity.this, "Impossible d'afficher les commentaires.", Toast.LENGTH_SHORT).show();
            }
        });
        webview.loadUrl("https://www.reddit.com/comments/" + postId);
    }
}
