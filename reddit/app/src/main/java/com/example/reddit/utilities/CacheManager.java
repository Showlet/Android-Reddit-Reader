package com.example.reddit.utilities;

import android.content.Context;

import android.net.http.HttpResponseCache;
import android.util.Log;
import java.io.File;
import java.io.IOException;

/**
 * Helper class pour gérer la cache de l'application.
 * Created by Maxim on 12/17/2015.
 */
public class CacheManager {
    private static final String TAG = "CacheManager";

    private static CacheManager mInstance;      //Instance static du singleton
    private final Context mContext;

    //Définit la grosseur de la cache
    public static final long HTTP_CACHE_SIZE = 10 * 1024 * 1024;

    /**
     * CTOR privé pouvent seulement être appelé par la méthode d'initialisation.
     * @param context Context de l'application
     */
    private CacheManager(Context context) {
        mContext = context;
    }

    /**
     * Permets d'initialiser le singleton des SharedPreferences
     * Note: Je suggères de toujours appeler cette méthode dans le onCreate de la main activity.
     * @param context Application context
     */
    public static synchronized void initializeInstance(Context context) {
        if (mInstance == null) {
            mInstance = new CacheManager(context);
        }
    }

    /**
     * Permets d'obtenir l'instance du CacheManager
     * @return L'instance du singleton
     */
    public static synchronized CacheManager getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(CacheManager.class.getSimpleName() + " is not initialized, call initializeInstance(Context) method first.");
        }
        return mInstance;
    }

    /**
     * Permets d'activer le HttpCaching. Cette méthode crée une cache http et l'installe
     * si elle ne l'est pas.
     * @see CacheManager  HTTP_CACHE_SIZE pour la grosseur de la cache
     */
    public void enableHttpCaching()
    {
        try {
            File httpCacheDir = new File(mContext.getCacheDir(), "http");
            if(HttpResponseCache.getInstalled() == null)
                HttpResponseCache.install(httpCacheDir, HTTP_CACHE_SIZE);
        }
        catch (IOException e) {
            Log.i(TAG, "HTTP response cache installation failed:" + e);
        }
    }

    /**
     * Cette méthode force l'écriture des opérations en buffer sur le système de fichier
     */
    public void flushHttpCache()
    {
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
    }
}
