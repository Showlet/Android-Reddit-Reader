package com.example.reddit;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reddit.drawer.DrawerAdapter;
import com.example.reddit.drawer.DrawerCallbacks;
import com.example.reddit.drawer.DrawerItem;
import com.example.reddit.utilities.*;
import com.example.reddit.utilities.ImageLoader;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity implements DrawerCallbacks {
    private RecyclerView _recyclelst_post;
    private SwipeRefreshLayout _swipe_layout;

    //Toolbar
    private Toolbar toolbar;
    private MenuItem mSearchAction;
    private MenuItem mFilterAction;
    private EditText mSearchBox;
    private boolean mIsSearchActive;

    private LinearLayoutManager linearLayoutManager;
    private LinearLayout linearLayout;
    private RecyclerView.LayoutManager mDrawerLayoutManager;

    //Drawer
    private RecyclerView mDrawerRecyclerView;
    private RecyclerView.Adapter mDrawerAdapter;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggleToggle;


    // DerniereURL utilise
    private String mCurrentSubreddit;
    private String mCurrentFilter;
    private String mProchainePage;

    //GridLayout
    private boolean isGrid;
    private GridLayout gridLayout;

    // Fullscreen image
    private ImageView fullImage;
    private boolean imageIsFullscreen;
    private ImageView dimBackground;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * � la cr�ation de l'activit� (NO SHIT)
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // isGrid est la variable qui mets l'affichage en gridview ou en list.
        isGrid = false;

        dimBackground = (ImageView) findViewById(R.id.dimBackground);

        //Initialisation des helpers
        PreferencesManager.initializeInstance(getApplicationContext());
        CacheManager.initializeInstance(getApplicationContext());

        // isGrid est la variable qui mets l'affichage en gridview ou en list.
        String interfaceType = PreferencesManager.getInstance().getPreference(Settings.INTERFACE_KEY);
        isGrid = (interfaceType.equals("Grid"));
        mCurrentSubreddit = "";
        mCurrentFilter = "/hot";


        //Active le http caching
        CacheManager.getInstance().enableHttpCaching();


        //Initialise les composantes de l'interface graphique.
        initialiserRecyclerView();
        initialiserSwipeLayout();
        initialiserToolbar();
        initialiserDrawer();
        if (isGrid)
            InitialiserGridLayout();
        commencerRafraichissement();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    /**
     * Apr�s la cr�ation de l'activit� (NO SHIT)
     *
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //On synchronise l'�tat (ouvert/ferm�) du drawer
        mActionBarDrawerToggleToggle.syncState();
    }

    /**
     * � la cr�ation des options du menu (Toolbar) (NO SHIT)
     *
     * @param menu
     * @return super.onCreateOptionsMenu(menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // On inflate le menu. Si l'action bar est pr�sente, on va rajouter les items
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        mSearchAction = menu.findItem(R.id.action_search);
        mFilterAction = menu.findItem(R.id.action_filter);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * � la s�lection d'un item du menu (NO SHIT)
     *
     * @param item l'item s�lectionn�
     * @return super.onOptionsItemSelected(item)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Il faut se charger de traiter les click sur les options du menu ici

        switch (item.getItemId()) {
            case R.id.action_filter:
                return true;
            case R.id.action_filter_hot:
                mCurrentFilter = "/hot";
                mFilterAction.getSubMenu().findItem(R.id.action_filter_new).setChecked(false);
                mFilterAction.getSubMenu().findItem(R.id.action_filter_rising).setChecked(false);
                item.setChecked(true);
                commencerRafraichissement();
                return true;
            case R.id.action_filter_new:
                mCurrentFilter = "/new";
                mFilterAction.getSubMenu().findItem(R.id.action_filter_hot).setChecked(false);
                mFilterAction.getSubMenu().findItem(R.id.action_filter_rising).setChecked(false);
                item.setChecked(true);
                commencerRafraichissement();
                return true;
            case R.id.action_filter_rising:
                mCurrentFilter = "/rising";
                mFilterAction.getSubMenu().findItem(R.id.action_filter_new).setChecked(false);
                mFilterAction.getSubMenu().findItem(R.id.action_filter_hot).setChecked(false);
                item.setChecked(true);
                commencerRafraichissement();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_search:
                if (mIsSearchActive)
                    disableSearchMenu();
                else
                    enableSearchMenu();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialise la toolbar. Elle est ajout�e et attach�e au layout
     */
    private void initialiserToolbar() {
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
    }

    /**
     * Initialise le SwipeLayout et definit le OnRefresh
     */
    private void initialiserSwipeLayout() {
        if (!isGrid) {
            _swipe_layout = (SwipeRefreshLayout) findViewById(R.id.swipe_user_linear);
        }
        if (isGrid) {
            _swipe_layout = (SwipeRefreshLayout) findViewById(R.id.swipe_user_grid);
        }

        _swipe_layout.setColorSchemeResources(R.color.colorPrimary);
        _swipe_layout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        MainActivity.this.commencerRafraichissement();
                    }
                }
        );
    }

    private void InitialiserGridLayout() {
        gridLayout = (GridLayout) findViewById(R.id.gridLayout);
        gridLayout.setUseDefaultMargins(false);
        gridLayout.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        gridLayout.setRowOrderPreserved(false);
    }

    /**
     * Initialise le RecyclerView
     */
    private void initialiserRecyclerView() {

        if (!isGrid) {
            linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
            linearLayout.setVisibility(View.VISIBLE);

            _recyclelst_post = (RecyclerView) findViewById(R.id.recyclelist_post_linear);
            _recyclelst_post.setHasFixedSize(true);
            linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            _recyclelst_post.setLayoutManager(linearLayoutManager);
        } else if (isGrid) {
            _recyclelst_post = (RecyclerView) findViewById(R.id.recyclelist_post_grid);
            _recyclelst_post.setHasFixedSize(true);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
            _recyclelst_post.setLayoutManager(gridLayoutManager);
        }
        _recyclelst_post.setHasFixedSize(true);

        _recyclelst_post.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(-1)) {
                    //Scroll at Top
                } else if (!recyclerView.canScrollVertically(1)) {
                    //Scroll at bottom
                    allerProchainePage();
                } else if (dy < 0) {
                    //Scroll up
                } else if (dy > 0) {
                    //Scroll down
                }
            }
        });

        // Affiche l'image en centre d'écran.
        _recyclelst_post.addOnItemTouchListener(new RecyclerEventListener(this, _recyclelst_post, new RecyclerEventListener.IEventListener() {
            @Override
            public void onClick(View view, int position) {
                view.setDrawingCacheEnabled(true);
                view.buildDrawingCache();


                fullImage = (ImageView) findViewById(R.id.fullImage);
                PostAdapter.PostViewHolder poste = new PostAdapter.PostViewHolder(view);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    poste.progressBar.setTranslationZ(20);
                }

                fullImage.setVisibility(View.VISIBLE);
                imageIsFullscreen = true;

                new ImageLoader(fullImage, poste.progressBar, R.drawable.ic_action_alert_warning).execute((((PostAdapter) _recyclelst_post.getAdapter()).getItem(position).preview.images.get(0).source.url));

                dimBackground.setBackgroundColor(Color.parseColor("#bf000000"));
            }

            @Override
            public void onLongClick(View view, int position) {

            }

            /**
             *
             * Sur le double tap on affiche les commentaires
             *
             * @param view La vue
             * @param position La position
             */
            @Override
            public void onDoubleTap(View view, int position) {
                Intent intent = new Intent(getApplicationContext(), CommentsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                intent.putExtra("postId", ((PostAdapter) _recyclelst_post.getAdapter()).getItem(position).id);
                startActivity(intent);
            }
        }));
    }

    /**
     * Initialise le drawer de navigation.
     */
    private void initialiserDrawer() {

        //On va g�rer sa diff�rament c'est juste pour tester.
        List<DrawerItem> drawerMenuItem = new ArrayList<DrawerItem>();
        drawerMenuItem.add(new DrawerItem("Front Page", "https://www.reddit.com", R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/programming", "https://www.reddit.com/r/programming", R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/gonewild", "https://www.reddit.com/r/gonewild", R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/funny", "https://www.reddit.com/r/funny", R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/aww", "https://www.reddit.com/r/aww", R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/ama", "https://www.reddit.com/r/ama", R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/WTF", "https://www.reddit.com/r/wtf", R.drawable.ic_action_trending_up));


        //On assigne le recycler � la vue,
        mDrawerRecyclerView = (RecyclerView) findViewById(R.id.DrawerRecycler);
        //La liste d'objet est de taille fixe. (On va peut-etre changer sa avec les favoris)
        mDrawerRecyclerView.setHasFixedSize(true);

        //Cr�ation de l'adapteur.
        mDrawerAdapter = new DrawerAdapter(drawerMenuItem, this);
        mDrawerRecyclerView.setAdapter(mDrawerAdapter);

        //Cr�ation du layout manager pour g�rer le drawer
        mDrawerLayoutManager = new LinearLayoutManager(this);
        mDrawerRecyclerView.setLayoutManager(mDrawerLayoutManager);

        //On assigne le layout du drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.DrawerLayout);
        mActionBarDrawerToggleToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_open);

        //Listener pour l'ouverture et la fermeture du drawer
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggleToggle);
    }

    /**
     *
     */
    public void commencerRafraichissement() {
        String url = mCurrentSubreddit.equals("Front Page") ? "" : mCurrentSubreddit;
        url += mCurrentFilter;
        url += ".json";

        WebServiceClient.get(url, new RequestParams(), new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                FrontPage fp = new GsonBuilder().create().fromJson(response.toString(), FrontPage.class);
                mProchainePage = fp.data.after;

                // Si on a desactiver les post NSFW
                if (PreferencesManager.getInstance().getPreference(Settings.NSFW_key).equals("Off")) {
                    ArrayList<FrontPage.Data.Children> lstToRemove = new ArrayList<>();

                    for (int i = 0; i < fp.data.children.size(); i++)
                        if (fp.data.children.get(i).data.over_18)
                            lstToRemove.add(fp.data.children.get(i));

                    fp.data.children.removeAll(lstToRemove);
                }

                _recyclelst_post.setAdapter(new PostAdapter(fp.data.children, isGrid));
                _swipe_layout.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), "Error while parsing server data", Toast.LENGTH_LONG);
                _swipe_layout.setRefreshing(false);
            }
        });
    }

    public void allerProchainePage() {
        String url = mCurrentSubreddit.equals("Front Page") ? "" : mCurrentSubreddit;
        url += mCurrentFilter;
        url += ".json";
        url += "?after=" + mProchainePage;

        WebServiceClient.get(url, new RequestParams(), new JsonHttpResponseHandler() {
            /**
             * Override pour défénir les actions si la requête est un échec
             *
             * @param statusCode Le code de status de la requête web.
             * @param headers L'entête de la requête web
             * @param response La réponse
             */
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                FrontPage fp = new GsonBuilder().create().fromJson(response.toString(), FrontPage.class);
                mProchainePage = fp.data.after;
                for (FrontPage.Data.Children c : fp.data.children) {
                    if (PreferencesManager.getInstance().getPreference(Settings.NSFW_key).equals("Off")) {
                        if (!c.data.over_18)
                            ((PostAdapter) _recyclelst_post.getAdapter()).addItem(c);
                    } else
                        ((PostAdapter) _recyclelst_post.getAdapter()).addItem(c);
                }
            }

            /**
             * Override pour défénir les actions si la requête est un échec
             *
             * @param statusCode Le code de status de la requête web.
             * @param headers L'entête de la requête web
             * @param throwable Wrapper l'information de l'erreur
             * @param errorResponse Le message d'erreur
             */
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), "Error while parsing server data", Toast.LENGTH_LONG);
            }
        });
    }

    /**
     * Permets de g�rer la sélection des items du drawer.
     *
     * @param position La position s�lectionn�
     */
    @Override
    public void onDrawerItemSelected(int position) {

        //On ferme le drawer � la s�lection d'un item.
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mDrawerRecyclerView);
        }

        //On g�re l'item s�lectionn�
        ((DrawerAdapter) mDrawerAdapter).selectPosition(position);
        mCurrentSubreddit = ((DrawerAdapter) mDrawerAdapter).getItem(position).getText();
        mCurrentFilter = "/hot";
        setTitle(mCurrentSubreddit + mCurrentFilter);
        commencerRafraichissement();
    }

    /**
     * G�re les backPressed
     */
    @Override
    public void onBackPressed() {

        // Si l'image est full screen, on la rends visible
        if (imageIsFullscreen) {
            fullImage.setVisibility(View.INVISIBLE);
            imageIsFullscreen = false;
        }
        //Si le drawer est ouvert on le ferme
        else if (mDrawerLayout.isDrawerOpen(mDrawerRecyclerView))
            mDrawerLayout.closeDrawer(mDrawerRecyclerView);
            //Si la recherche est active on la désactive
        else if (mIsSearchActive)
            disableSearchMenu();
        else
            super.onBackPressed();
    }

    /**
     * M�thode qui active l'action de recherche dans la toolbar
     */

    private void enableSearchMenu() {
        //On retrouve l'action bar
        ActionBar actionBar = getSupportActionBar();

        //On active et affiche la custom view.
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.search_bar);
        //On masque le titre pour faire plus d'espace
        actionBar.setDisplayShowTitleEnabled(false);

        //On retrouve la vue de recherche.
        mSearchBox = (EditText) actionBar.getCustomView().findViewById(R.id.searchBox);

        //Ajout du listener pour trigger la recherche.
        mSearchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doSearch();
                    disableSearchMenu();
                    return true;
                }
                return false;
            }
        });

        //On affiche l'icon de fermeture dans le textbox
        mSearchAction.setIcon(R.drawable.ic_action_navigation_close);

        //On donne le focus au textbox.
        mSearchBox.requestFocus();

        //On affiche le claver tactil
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(mSearchBox, InputMethodManager.SHOW_IMPLICIT);

        mIsSearchActive = true;
    }

    /**
     * M�thode qui d�sactive l'action de recherche dans la toolbar
     */
    private void disableSearchMenu() {

        //On retrouve l'action bar
        ActionBar actionBar = getSupportActionBar();

        //On d�sactive la custom view dans la toolbar et on affiche le titre
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);

        //On enl�ve le focus au text box
        mSearchBox.clearFocus();
        View view = this.getCurrentFocus();
        //On masque le clavier tactil
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);


        //On ajoute l'icon (loupe) de recherche
        mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_action_search_white));

        mIsSearchActive = false;
    }

    /**
     * Fait une recherche sur le site de reddit afin d'obtenir les posts répondant au critère de recherche
     */
    private void doSearch() {
        if (mCurrentSubreddit != null) {
            RequestParams requestParams = new RequestParams();
            requestParams.add("q", mSearchBox.getText().toString());
            requestParams.add("restrict_sr", "on");
            requestParams.add("sort", "relevance");
            requestParams.add("t", "all");

            WebServiceClient.get(mCurrentSubreddit + "/search.json", requestParams, new JsonHttpResponseHandler() {

                /**
                 *
                 * Override pour définir les actions quand la requête est ok
                 *
                 * @param statusCode Le code de status de la requête web.
                 * @param headers L'entête de la requête web
                 * @param response La réponson
                 */
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    FrontPage fp = new GsonBuilder().create().fromJson(response.toString(), FrontPage.class);

                    // Si on a desactiver les post NSFW
                    if(PreferencesManager.getInstance().getPreference(Settings.NSFW_key).equals("Off")) {
                        ArrayList<FrontPage.Data.Children> lstToRemove = new ArrayList<>();

                        for (int i = 0; i < fp.data.children.size(); i++)
                            if (fp.data.children.get(i).data.over_18)
                                lstToRemove.add(fp.data.children.get(i));

                        fp.data.children.removeAll(lstToRemove);
                    }

                    _recyclelst_post.setAdapter(new PostAdapter(fp.data.children, isGrid));
                    _swipe_layout.setRefreshing(false);
                }

                /**
                 *
                 * Override pour défénir les actions si la requête est un échec
                 *
                 * @param statusCode Le code de status de la requête web.
                 * @param headers L'entête de la requête web
                 * @param throwable Wrapper l'information de l'erreur
                 * @param errorResponse Le message d'erreur
                 */
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(getApplicationContext(), "Error while parsing server data", Toast.LENGTH_LONG);
                    _swipe_layout.setRefreshing(false);
                }
            });
        } else {
            //Ajoutes les paramètres de recherche.
            RequestParams requestParams = new RequestParams();
            requestParams.add("q", mSearchBox.getText().toString());
            requestParams.add("restrict_sr", "off");
            requestParams.add("sort", "relevance");
            requestParams.add("t", "all");
            WebServiceClient.get("/search.json", requestParams, new JsonHttpResponseHandler() {

                /**
                 *
                 * Override pour définir les actions quand la requête est ok
                 *
                 * @param statusCode Le code de status de la requête web.
                 * @param headers L'entête de la requête web
                 * @param response La réponson
                 */
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    FrontPage fp = new GsonBuilder().create().fromJson(response.toString(), FrontPage.class);
                    _recyclelst_post.setAdapter(new PostAdapter(fp.data.children, isGrid));
                    _swipe_layout.setRefreshing(false);
                }

                /**
                 *
                 * Override pour défénir les actions si la requête est un échec
                 *
                 * @param statusCode Le code de status de la requête web.
                 * @param headers L'entête de la requête web
                 * @param throwable Wrapper l'information de l'erreur
                 * @param errorResponse Le message d'erreur
                 */
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(getApplicationContext(), "Error while parsing server data", Toast.LENGTH_LONG);
                    _swipe_layout.setRefreshing(false);
                }
            });
        }
    }

    public void onImageClick(View v) {
        fullImage.setVisibility(View.INVISIBLE);
        dimBackground.setBackgroundColor(Color.parseColor("#00FFFFFF"));

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.reddit/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://com.example.reddit/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
