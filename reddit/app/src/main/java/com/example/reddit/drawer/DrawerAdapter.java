package com.example.reddit.drawer;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.reddit.R;
import java.util.List;

/**
 *
 * Adapter pour le drawer de navigation
 *
 * Created by Maxim on 1/29/2016.
 */
public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {
    private static final String TAG = "DrawerAdapter";

    //Permets de différencier le header des items
    private static final int VIEW_HOLDER_TYPE_BANNIERE = 0;
    private static final int VIEW_HOLDER_TYPE_ITEM = 1;


    //Gestion des items
    private int mPositionSelectionne;
    private int mPositionTouchee = -1;
    private List<DrawerItem> mItemsDrawer;

    //Pointeur vers l'object qui implémente l'iterface de callbacks
    private DrawerCallbacks mDrawerCallbacks;


    /**
     *
     * CTOR
     *
     * @param itemsDrawer La liste d'item du drawer
     * @param callbacks L'object qui implement les callback
     */
    public DrawerAdapter(List<DrawerItem> itemsDrawer, DrawerCallbacks callbacks){
        this.mItemsDrawer = itemsDrawer;
        this.mDrawerCallbacks = callbacks;
    }





    /**
     *
     * ViewHolder du drawer qui va stocker les views afin de les recycler.
     *
     */
    public static class ViewHolder extends RecyclerView.ViewHolder /* implements View.OnClickListener */{
        int mTypeViewHolder;
        TextView mTextView;
        ImageView mImageView;
        Context mContext;


        /**
         *
         * CTOR
         *
         * @param itemView La vue
         * @param viewType Le type de la vue
         * @param context Le context
         */
        public ViewHolder(View itemView,int viewType, Context context) {
            super(itemView);
            mContext = context;

            //On assigne les propriétés pour permettre les cliques
            itemView.setClickable(true);
            //itemView.setOnClickListener(this);

            // On assigne la vue en conséquence du type de vue passsé Header/Item
            if(viewType == VIEW_HOLDER_TYPE_ITEM) {
                //On crée la vue en utilisant drawer_row.xml
                mTextView = (TextView) itemView.findViewById(R.id.rowText);
                mImageView = (ImageView) itemView.findViewById(R.id.rowIcon);
                mTypeViewHolder = VIEW_HOLDER_TYPE_ITEM;
            }
            else{
                mTypeViewHolder = VIEW_HOLDER_TYPE_BANNIERE;
            }
        }

    }


    /**
     *
     * Lorsque le view holder est créé on inflate le layout de rangé (drawer_row.xml) ou le
     * layout du header (drawer_banner.xml) celon le viewType reçu. Ensuite le view holder est retourné.
     *
     * @param parent La view parent
     * @param viewType Le type de la view
     * @return Le view holder créé
     */
    @Override
    public DrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //Check qu'elle type de view holder on doit créé
        if (viewType == VIEW_HOLDER_TYPE_ITEM) {

            //On inflate le layout de rangé (drawer_row.xml)
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_row,parent,false);

            final ViewHolder viewHolderItem = new ViewHolder(v,viewType,parent.getContext());

            //Set les TouchListener afin de gérer les motions event
            viewHolderItem.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            toucherPosition(viewHolderItem.getPosition());
                            return false;
                        case MotionEvent.ACTION_CANCEL:
                            //Rien pour l'instant
                            return false;
                        case MotionEvent.ACTION_MOVE:
                            toucherPosition(-1);
                            return false;
                        case MotionEvent.ACTION_UP:
                            toucherPosition(-1);
                            return false;
                    }
                    return true;
                }
            });

            // Set le click listener sur la rangée du drawer afin de délégué
            // les événements vers l'object qui implémente l'interface de callbacks
            viewHolderItem.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDrawerCallbacks != null)
                        mDrawerCallbacks.onDrawerItemSelected(viewHolderItem.getPosition());
                }
            });

            return viewHolderItem;

        } else if (viewType == VIEW_HOLDER_TYPE_BANNIERE) {

            //Dans ce cas-ci on inflat le layout du  header (drawer_banner.xml)
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_banner,parent,false);
            return new ViewHolder(view,viewType,parent.getContext());
        }

        //Null si ce n'est pas une rangée ou un header.
        return null;
    }



    /**
     *
     * On override la method appelé lorsque le recycler view doit afficher un item dans le drawer
     * afin de pouvoir gérer entre le Header et les items
     *
     * @param viewHolder Le viewHolder
     * @param position La position dans le drawer. Incluant le header
     */
    @Override
    public void onBindViewHolder(DrawerAdapter.ViewHolder viewHolder, int position) {

        //Pour un item
        if(viewHolder.mTypeViewHolder == VIEW_HOLDER_TYPE_ITEM) {

            //On va chercher l'item du drawer à la position courrante - 1 (puisque le header n'est pas dans la liste)
            DrawerItem itemDrawer = mItemsDrawer.get(position -1);

            //On assigne le texte et l'icon de l'item
            viewHolder.mTextView.setText(itemDrawer.getText());
            viewHolder.mImageView.setImageResource(itemDrawer.getIcon());

            //Permets d'appliquer une couleur de sélection et de toucher.
           if (mPositionSelectionne == position || mPositionTouchee == position) {
               viewHolder.itemView.setBackgroundColor(viewHolder.itemView.getContext().getResources().getColor(R.color.selectedGray));
            } else {
               viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT); //Transparent parce que le background du drawer est déja de la bonne couleur.
            }
        }
    }


    /**
     *
     * Retourne le nombre d'item dans le drawer + 1 afin d'inclure l'en-tête.
     *
     * @return le nombre d'item dans le drawer
     */
    @Override
    public int getItemCount() {
        return mItemsDrawer.size() + 1;
    }


    public DrawerItem getItem(int position)
    {
        return mItemsDrawer.get(position - 1);
    }

    /**
     *
     * Met à jour la position touchée et fait appel à notifyItemChanged
     * Qui est une méthode qui elle fait appel à l'observer du recycler view
     * afin de faire des demandes d'update.
     *
     * @param position La position touché
     */
    private void toucherPosition(int position) {
        int dernierePosition = mPositionTouchee;
        mPositionTouchee = position;
        if (dernierePosition >= 0) notifyItemChanged(dernierePosition);
        if (position >= 0) notifyItemChanged(position);
    }

    /**
     *
     * Met à jour la position sélectionné et fait appel à notifyItemChanged
     * Qui est une méthode qui elle fait appel à l'observer du recycler view
     * afin de faire des demandes d'update.
     *
     * @param position La position sélectionné
     */
    public void selectionnerPosition(int position) {
        int dernierePosition = mPositionSelectionne;
        mPositionSelectionne = position;
        notifyItemChanged(dernierePosition);
        notifyItemChanged(position);
    }



    /**
     *
     * Permets de savoir de qu'elle type est la view à une position.
     *
     * @param position La position de la view
     * @return Le type de la view Header = 0, Item = 1
     */
    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_HOLDER_TYPE_BANNIERE;
        return VIEW_HOLDER_TYPE_ITEM;
    }
}