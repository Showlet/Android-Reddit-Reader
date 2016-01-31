package com.example.reddit.drawer;

/**
 *
 * Interface pour les callback du drawer
 *
 * Created by Maxim on 1/30/2016.
 */
public interface DrawerCallbacks {
    /**
     *
     * Callback lorsqu'un item du drawer est cliqué
     *
     * @param position La position sélectionné
     */
    void onDrawerItemSelected(int position);
}
