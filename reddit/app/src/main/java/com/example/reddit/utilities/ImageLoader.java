package com.example.reddit.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


/**
 * Helper class pour gérer le chargement d'image en background à partir d'une URL.
 * Created by Maxim on 12/14/2015.
 */
public class ImageLoader extends AsyncTask<String, String, Bitmap> {
    private static final String TAG = "ImageLoader";
    private final ImageView mImageview;
    private final ProgressBar mProgressBar;
    private final int mErrorResId;

    /**
     * CTOR
     * @param imageView ImageView pour l'image
     * @param progressBar ProgressBar pour l'affichage de chargement
     * @param errorResId Ressource to display on error
     */
    public ImageLoader(ImageView imageView, ProgressBar progressBar, int errorResId){
        mImageview = imageView;
        mProgressBar = progressBar;
        mErrorResId = errorResId;
        mImageview.setVisibility(ImageView.GONE);
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mImageview.setImageBitmap(null);
    }

    /**
     *
     * Permets d'éffectuer la tache en arrière plan
     *
     * @param args Arguments
     * @return
     */
    protected Bitmap doInBackground(String... args) {
        Bitmap bitmap = null;
        try {
            URLConnection conn = new URL(args[0]).openConnection();
            conn.setUseCaches(true);
            bitmap = BitmapFactory.decodeStream((InputStream) conn.getContent());

        } catch (Exception e) {
            Log.d(TAG, "Error while procession background task.");
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * Si l'image n'est pas null, on remplace le progressbar par celle-ci. Sinon
     * on affiche l'image d'erreur
     * @param image à afficher.
     */
    protected void onPostExecute(Bitmap image) {

        mProgressBar.setVisibility(ProgressBar.GONE);
        mImageview.setVisibility(ImageView.VISIBLE);
        if (image != null) {
            mImageview.setImageBitmap(image);
            mImageview.setBackground(null);
        }
        else
        {
            mImageview.setImageResource(mErrorResId);
            mImageview.setBackground(null);
        }
    }
}

