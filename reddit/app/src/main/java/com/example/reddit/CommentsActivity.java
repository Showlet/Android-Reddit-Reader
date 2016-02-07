package com.example.reddit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


/**
 * Activité pour afficher les commentaires d'un post reddit
 * Created by Maxim on 02/5/2016.
 */
public class CommentsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        //Permet de retrouver le id du post.
        Intent intent = getIntent();
        String postId = intent.getStringExtra("postId");

        initialiserToolbar();
        afficherCommentaires(postId);
    }


    /**
     * Initialise la toolbar. Elle est ajoutée et attach�e au layout
     */
    private void initialiserToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.action_bar);
        setTitle("Commentaires");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    /**
     *
     * Affiche les commentaires associés au post dans une webview
     *
     * @param postId Le id du post
     */
    private void afficherCommentaires(String postId) {
        WebView webview = (WebView)findViewById(R.id.webView);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(CommentsActivity.this, "Impossible d'afficher les commentaires.", Toast.LENGTH_SHORT).show();
            }
        });
        webview.loadUrl("https://www.reddit.com/comments/" + postId);
    }
}
