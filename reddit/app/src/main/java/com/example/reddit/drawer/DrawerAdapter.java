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
    private static final int VIEW_HOLDER_TYPE_HEADER = 0;
    private static final int VIEW_HOLDER_TYPE_ITEM = 1;

    //Photo et display name du user
    private int avatar;
    private String alias;

    //Gestion des items
    private int mSelectedPosition;
    private int mTouchedPosition = -1;
    private List<DrawerItem> mDrawerItems;

    //Pointeur vers l'object qui implémente l'iterface de callbacks
    private DrawerCallbacks mDrawerCallbacks;


    /**
     *
     * CTOR
     *
     * @param Alias Nom d'affichage de l'utilisateur
     * @param Avatar Image de l'utilisateur
     * @param drawerItems La liste d'item du drawer
     * @param callbacks L'object qui implement les callback
     */
    public DrawerAdapter(String Alias, int Avatar, List<DrawerItem> drawerItems, DrawerCallbacks callbacks){
        this.mDrawerItems = drawerItems;
        this.alias = Alias;
        this.avatar = Avatar;
        this.mDrawerCallbacks = callbacks;
    }




    /**
     *
     * ViewHolder du drawer qui va stocker les views afin de les recycler.
     *
     */
    public static class ViewHolder extends RecyclerView.ViewHolder /* implements View.OnClickListener */{
        int viewHolderType;
        TextView textView;
        ImageView imageView;
        ImageView Avatar;
        TextView Alias;
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
                textView = (TextView) itemView.findViewById(R.id.rowText);
                imageView = (ImageView) itemView.findViewById(R.id.rowIcon);
                viewHolderType = VIEW_HOLDER_TYPE_ITEM;
            }
            else{
                //On crée la vue en utilisant drawer_banner.xml
                Alias = (TextView) itemView.findViewById(R.id.alias);
                Avatar = (ImageView) itemView.findViewById(R.id.avatar);
                viewHolderType = VIEW_HOLDER_TYPE_HEADER;
            }
        }


        /**
         *
         * Les opérations
         *
         * @param v La vue
         */
        //@Override
        //public void onClick(View v) {
            //Pour l'instance rien
        //}

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

            final ViewHolder vhItem = new ViewHolder(v,viewType,parent.getContext());

            //Set les TouchListener afin de gérer les motions event
            vhItem.itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            touchPosition(vhItem.getPosition());
                            return false;
                        case MotionEvent.ACTION_CANCEL:
                            //Rien pour l'instant
                            return false;
                        case MotionEvent.ACTION_MOVE:
                            touchPosition(-1);
                            return false;
                        case MotionEvent.ACTION_UP:
                            touchPosition(-1);
                            return false;
                    }
                    return true;
                }
            });

            // Set le click listener sur la rangée du drawer afin de délégué
            // les événements vers l'object qui implémente l'interface de callbacks
            vhItem.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mDrawerCallbacks != null)
                        mDrawerCallbacks.onDrawerItemSelected(vhItem.getPosition());
                }
            });

            return vhItem;

        } else if (viewType == VIEW_HOLDER_TYPE_HEADER) {

            //Dans ce cas-ci on inflat le layout du  header (drawer_banner.xml)
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.drawer_banner,parent,false);
            return new ViewHolder(v,viewType,parent.getContext());
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
        if(viewHolder.viewHolderType == VIEW_HOLDER_TYPE_ITEM) {

            //On va chercher l'item du drawer à la position courrante - 1 (puisque le header n'est pas dans la liste)
            DrawerItem item = mDrawerItems.get(position -1);

            //On assigne le text et l'icon de l'item
            viewHolder.textView.setText(item.getText());
            viewHolder.imageView.setImageResource(item.getIcon());

            //Permets d'appliquer une couleur de sélection et de toucher.
           if (mSelectedPosition == position || mTouchedPosition == position) {
               viewHolder.itemView.setBackgroundColor(viewHolder.itemView.getContext().getResources().getColor(R.color.selectedGray));
            } else {
               viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT); //Transparent parce que le background du drawer est déja de la bonne couleur.
            }
        }
        //Pour le header
        else{
            viewHolder.Avatar.setImageResource(avatar);
            viewHolder.Alias.setText(alias);
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
        return mDrawerItems.size() + 1;
    }


    /**
     *
     * Met à jour la position touchée et fait appel à notifyItemChanged
     * Qui est une méthode qui elle fait appel à l'observer du recycler view
     * afin de faire des demandes d'update.
     *
     * @param position La position touché
     */
    private void touchPosition(int position) {
        int lastPosition = mTouchedPosition;
        mTouchedPosition = position;
        if (lastPosition >= 0) notifyItemChanged(lastPosition);
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
    public void selectPosition(int position) {
        int lastPosition = mSelectedPosition;
        mSelectedPosition = position;
        notifyItemChanged(lastPosition);
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
            return VIEW_HOLDER_TYPE_HEADER;
        return VIEW_HOLDER_TYPE_ITEM;
    }
}