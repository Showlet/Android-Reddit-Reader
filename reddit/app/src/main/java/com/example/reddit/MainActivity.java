package com.example.reddit;

import android.net.http.HttpResponseCache;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private RecyclerView _recyclelst_post;
    private SwipeRefreshLayout _swipe_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _recyclelst_post = (RecyclerView) findViewById(R.id.recyclelist_post);
        _swipe_layout = (SwipeRefreshLayout) findViewById(R.id.swipe_user);

        _recyclelst_post.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        _recyclelst_post.setLayoutManager(llm);

        _swipe_layout.setColorSchemeResources(R.color.colorPrimary);
        _swipe_layout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        MainActivity.this.startRefresh();
                    }
                }
        );
        startRefresh();
    }

    public void enableCache()
    {
        try {
            File httpCacheDir = new File(getApplicationContext().getCacheDir(), "http");
            long httpCacheSize = 10 * 1024 * 1024;
            HttpResponseCache.install(httpCacheDir, httpCacheSize);
        } catch (IOException ex) {
        }
    }

    public void startRefresh()
    {
        String[] params = {
                "https://www.reddit.com/hot.json", ""
        };
        Fetcher fetcher = new Fetcher();
        fetcher.addListener(new FetchCompleted() {
            @Override
            public void run() {
                Gson gson = new GsonBuilder().create();
                try {

                    FrontPage fp = gson.fromJson(m_result, FrontPage.class);
                    _recyclelst_post.setAdapter(new PostAdapter(fp.data.children));
                    _swipe_layout.setRefreshing(false);

                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "Error while parsing server data", Toast.LENGTH_LONG);
                    _swipe_layout.setRefreshing(false);
                }
            }
        });
        fetcher.execute(params);
    }
}
