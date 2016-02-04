package com.example.reddit.utilities;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Helper class qui wrap les SharedPreferences dans un singleton.
 * Created by Maxim on 12/7/2015.
 */
public class PreferencesManager {
    private static final String TAG = "PreferencesManager";
    private static PreferencesManager mInstance;            //Instance static du singleton
    private final SharedPreferences mSharedPreferences;

    //Définition des dictio(s)
    private static final String PREF_NAME = "APP_PREFERENCES";

    //Définition des clefs pour l'app
    public static final String USER_ALIAS_KEY = "USER_ALIAS";
    public static final String USER_AVATAR_PATH_KEY = "USER_AVATAR_PATH";


    /**
     * CTOR privé pouvent seulement être appelé par la méthode d'initialisation.
     * @param context Context de l'application
     */
    private PreferencesManager(Context context) {
        mSharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Permets d'initialiser le singleton des SharedPreferences
     * Note: Je suggères de toujours appeler cette méthode dans le onCreate de la main activity.
     * @param context Application context
     */
    public static synchronized void initializeInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PreferencesManager(context);
        }
    }

    /**
     * Permets d'obtenir l'instance des SharedPreferences
     * @return L'instance du singleton
     */
    public static synchronized PreferencesManager getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(PreferencesManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(Context) method first.");
        }
        return mInstance;
    }

    /**
     * Permets de stocker l'alias de l'utilisateur
     * @param value l'alias de l'utilisateur
     */
    public void setUserAlias(String value) {
        mSharedPreferences.edit()
                .putString(USER_ALIAS_KEY, value)
                .apply();
    }

    /**
     * Permets de stocket le path vers l'image (Avatar) de l'utilisateur
     * @param value Le path vers l'image
     */
    public void setUserAvatarPath(String value) {
        mSharedPreferences.edit()
                .putString(USER_AVATAR_PATH_KEY, value)
                .apply();
    }


    /**
     * Permets de retrouver l'alias servant de nom pour l'utilisateur
     * @return L'alias de l'utilisateur
     */
    public String getUserAlias() {
        return mSharedPreferences.getString(USER_ALIAS_KEY, "Invalid Name");
    }

    /**
     * Permets de retrouver le path vers l'image servant d'avatar pour l'utilisateur.
     * @return Le file path vers l'avatar de l'utilisateur
     */
    public String getUserAvatarPath() {
        return mSharedPreferences.getString(USER_AVATAR_PATH_KEY, "Invalid Path");
    }

    /**
     * Permet d'enregistrer un parametre
     * @param Key
     * @param Value
     */
    public void setPreference(String Key, String Value)
    {
        mSharedPreferences.edit()
                .putString(Key, Value)
                .apply();
    }

    /**
     * Permet de retrouve un parametre enregistrer
     * @param Key
     * @return
     */
    public String getPreference(String Key)
    {
        return mSharedPreferences.getString(Key, "Invalid Key");
    }

    /**
     * Efface la pair clef/valeur spécifiée si elle existe.
     */
    public void remove(String key) {
        mSharedPreferences.edit()
                .remove(key)
                .apply();
    }

    /**
     * Efface chaque clefs/valeurs des SharedPreferences
     */
    public void clear() {
        mSharedPreferences.edit()
                .clear()
                .apply();
    }


}