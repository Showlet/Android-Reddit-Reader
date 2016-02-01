package com.example.reddit.utilities;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by vincent on 10/17/15.
 */
public class Fetcher  extends AsyncTask<String, Void, String>
{
    List<FetchCompleted> lst_Listener = new ArrayList<FetchCompleted>();

    public void addListener(FetchCompleted listener) {
        lst_Listener.add(listener);
    }

    @Override
    protected String doInBackground(String ... params) {
        try {
            // Prépare la connection
            HttpsURLConnection conn = (HttpsURLConnection) new URL(params[0]).openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(false);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setChunkedStreamingMode(0);

            // Commence la query
            conn.connect();

            //Get input stream
            InputStream is = conn.getInputStream();
            return CharStreams.toString(new InputStreamReader(is, Charsets.UTF_8));
        }
        catch (IOException e) {
            return "{Success:false,Message:\"Server is unreachable\"}";
        }
    }

    @Override
    protected void onPostExecute(String result)
    {
        for (FetchCompleted item :lst_Listener) {
            item.setResult(result);
            item.run();
        }
    }
}
