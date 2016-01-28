package com.example.reddit;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by vincent on 2016-01-28.
 */
public class ImageLoader extends AsyncTask<String, String, Bitmap> {

    ImageView imgview;

    public ImageLoader(ImageView i, int defaultimg){
        imgview = i;
        imgview.setBackgroundResource(defaultimg);
        imgview.setImageBitmap(null);
    }

    protected Bitmap doInBackground(String... args) {
        Bitmap bitmap = null;
        try {
            URLConnection conn = new URL(args[0]).openConnection();
            conn.setUseCaches(true);
            bitmap = BitmapFactory.decodeStream((InputStream) conn.getContent());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    protected void onPostExecute(Bitmap image) {

        if (image != null) {
            imgview.setImageBitmap(image);
            imgview.setBackground(null);
        }
    }
}
