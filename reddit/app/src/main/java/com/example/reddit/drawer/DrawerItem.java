package com.example.reddit.drawer;

import android.util.Log;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Modèle pour un item du drawer
 * Created by Maxim on 1/29/2016.
 */
public class DrawerItem {
    private static final String TAG = "DrawerItem";
    private int mResId;
    private URL mUrl;
    private String mText;

    public DrawerItem(String text,String url, int resId)  {
        mText = text;

        mResId = resId;

        try {
            mUrl = new URL(url);
        } catch (MalformedURLException e) {
            Log.d(TAG, "Error while converting string to url.");
            e.printStackTrace();
        } finally {
            mUrl = null;
        }
    }

    public int getIcon() {
        return mResId;
    }

    /**
     *
     * Getter pour accéder au nom d'affichage (Subreddit).
     *
     * @return Le nom de l'item
     */
    public String getText() {
        return mText;
    }

    /**
     *
     * Getter pour accéder à l'url su subreddit.
     * Il est important de valider qu'elle n'est pas nul.
     *
     * @return L'url pour accéder au subreddit.
     */
    public URL getmUrl() {
        return mUrl;
    }
}


