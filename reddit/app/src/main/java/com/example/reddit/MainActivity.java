package com.example.reddit;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
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
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
    private Toolbar mToolbar;
    private MenuItem mActionRecherche;
    private MenuItem mActionFiltre;
    private EditText mBoiteRecherche;
    private boolean mIsRechercheActive;

    private LinearLayoutManager linearLayoutManager;
    private LinearLayout linearLayout;

    //Drawer
    private RecyclerView mDrawerRecyclerView;
    private RecyclerView.Adapter mDrawerAdapter;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggleToggle;
    private RecyclerView.LayoutManager mDrawerLayoutManager;


    // DerniereURL utilise
    private String mSubredditCourrant;
    private String mFiltreCourrant;
    private String mProchainePage;

    //GridLayout
    private boolean isGrid;
    private GridLayout gridLayout;

    // Fullscreen image/web
    private ImageView fullImage;
    private boolean contentIsFullScreen;
    private ImageView dimBackground;
    private WebView fullWeb;
    private GoogleApiClient client;

    /**
     * À la création de l'activité (NO SHIT)
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
        mSubredditCourrant = "Front Page";
        mFiltreCourrant = "/hot";


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
     * Après la création de l'activité (NO SHIT)
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
     * � la création des options du menu (Toolbar) (NO SHIT)
     *
     * @param menu
     * @return super.onCreateOptionsMenu(menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // On inflate le menu. Si l'action bar est pr�sente, on va rajouter les items
        getMenuInflater().inflate(R.menu.action_bar_menu, menu);
        mActionRecherche = menu.findItem(R.id.action_search);
        mActionFiltre = menu.findItem(R.id.action_filter);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * À la sélection d'un item du menu (NO SHIT)
     *
     * @param item l'item sélectionné
     * @return super.onOptionsItemSelected(item)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Il faut se charger de traiter les click sur les options du menu ici

        switch (item.getItemId()) {
            case R.id.action_filter:
                return true;
            case R.id.action_filter_hot:
                mFiltreCourrant = "/hot";
                mActionFiltre.getSubMenu().findItem(R.id.action_filter_new).setChecked(false);
                mActionFiltre.getSubMenu().findItem(R.id.action_filter_rising).setChecked(false);
                item.setChecked(true);
                setTitle(mSubredditCourrant + mFiltreCourrant);
                commencerRafraichissement();
                return true;
            case R.id.action_filter_new:
                mFiltreCourrant = "/new";
                mActionFiltre.getSubMenu().findItem(R.id.action_filter_hot).setChecked(false);
                mActionFiltre.getSubMenu().findItem(R.id.action_filter_rising).setChecked(false);
                item.setChecked(true);
                setTitle(mSubredditCourrant + mFiltreCourrant);
                commencerRafraichissement();
                return true;
            case R.id.action_filter_rising:
                mFiltreCourrant = "/rising";
                mActionFiltre.getSubMenu().findItem(R.id.action_filter_new).setChecked(false);
                mActionFiltre.getSubMenu().findItem(R.id.action_filter_hot).setChecked(false);
                item.setChecked(true);
                setTitle(mSubredditCourrant + mFiltreCourrant);
                commencerRafraichissement();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_search:
                if (mIsRechercheActive)
                    desactiverMenuDeRecherche();
                else
                    activerMenuDeRecherche();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialise la toolbar. Elle est ajoutée et attachée au layout
     */
    private void initialiserToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(mToolbar);
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
                    String url = mSubredditCourrant.equals("Front Page") ? "" : mSubredditCourrant;
                    url += mFiltreCourrant;
                    url += ".json";
                    url += "?after=" + mProchainePage;

                    WebServiceClient.get(url, new RequestParams(), new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            FrontPage fp = new GsonBuilder().create().fromJson(response.toString(), FrontPage.class);
                            mProchainePage = fp.data.after;

                            for (FrontPage.Data.Children c : fp.data.children)
                                ((PostAdapter) _recyclelst_post.getAdapter()).addItem(c);
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            Toast.makeText(getApplicationContext(), "Une erreur est survenue!", Toast.LENGTH_LONG);
                        }
                    });
allerProchainePage();
                } else if (dy < 0) {
                    //Scroll up
                } else if (dy > 0) {
                    //Scroll down
                }
            }
        });// Affiche l'image en centre d'écran.
        _recyclelst_post.addOnItemTouchListener(new RecyclerEventListener(this, _recyclelst_post, new RecyclerEventListener.IEventListener(){
            @Override
            public void onClick(View view, int position) {
                view.setDrawingCacheEnabled(true);
                view.buildDrawingCache();

                // Le state sert a decider si le post est une image, gif ou
                int state = 3;
                String url = ((PostAdapter) _recyclelst_post.getAdapter()).getItem(position).url;
                PostAdapter.PostViewHolder poste = new PostAdapter.PostViewHolder(view);

                contentIsFullScreen = true;
                dimBackground.setVisibility(View.VISIBLE);

                fullImage = (ImageView) findViewById(R.id.fullImage);
                fullWeb = (WebView) findViewById(R.id.fullWeb);

                fullWeb.getSettings().setBuiltInZoomControls(true);
                fullWeb.getSettings().setJavaScriptEnabled(true);
                fullWeb.setInitialScale(1);
                fullWeb.getSettings().setLoadWithOverviewMode(true);
                fullWeb.getSettings().setUseWideViewPort(true);
                fullWeb.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
                fullWeb.getSettings().setSupportMultipleWindows(false);
                fullWeb.getSettings().setUserAgentString("Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3");
                fullWeb.setBackgroundColor(Color.TRANSPARENT);
                fullWeb.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        return false;
                    }
                });

                if (((PostAdapter) _recyclelst_post.getAdapter()).getItem(position).url.endsWith(".jpg") ||
                        ((PostAdapter) _recyclelst_post.getAdapter()).getItem(position).url.endsWith(".png")  ) {
                    state = 3;
                }
                else if (((PostAdapter) _recyclelst_post.getAdapter()).getItem(position).url.endsWith(".gif") ||
                        ((PostAdapter) _recyclelst_post.getAdapter()).getItem(position).url.endsWith(".gifv") ) {
                   // Enleve le v de gifv
                    if (url.charAt(url.length()-1)=='v') {
                        url = url.replace(url.substring(url.length() - 1), "");
                        state = 3;
                    }
                }
                else {
                    state = 2;
                }

                switch (state) {
                    // Image View (Pas utilisé)
                    case 1:
                        fullImage.setVisibility(View.VISIBLE);
                        new ImageLoader(fullImage, (ProgressBar) findViewById(R.id.imgProgress), R.drawable.ic_action_alert_warning)
                                .execute((((PostAdapter) _recyclelst_post.getAdapter()).getItem(position).preview.images.get(0).source.url));
                        break;
                    // Page web
                    case 2:
                        fullWeb.setVerticalScrollBarEnabled(true);
                        fullWeb.setHorizontalScrollBarEnabled(true);

                        fullWeb.loadUrl(url);
                        fullWeb.setVisibility(View.VISIBLE);
                        fullWeb.setBackgroundColor(Color.WHITE);
                        break;
                    // Image web
                    case 3:
                        fullWeb.setVerticalScrollBarEnabled(false);
                        fullWeb.setHorizontalScrollBarEnabled(false);

                        String data = "<body><center><img height=\"100%\" src=\"" + url + "\" /></center></body></html>";
                        fullWeb.loadData(data, "text/html", null);
                        fullWeb.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
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
                Intent intent = new Intent(MainActivity.this,CommentsActivity.class);
                intent.putExtra("postId", ((PostAdapter) _recyclelst_post.getAdapter()).getItem(position).id);
                startActivity(intent);
            }
        }));
    }

    /**
     * Initialise le drawer de navigation.
     */
    private void initialiserDrawer() {
        //Ajoute les items par défaut.
        List<DrawerItem> drawerMenuItem = new ArrayList<DrawerItem>();
        drawerMenuItem.add(new DrawerItem("Front Page", "https://www.reddit.com", R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/programming", "https://www.reddit.com/r/programming", R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/gonewild", "https://www.reddit.com/r/gonewild", R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/funny", "https://www.reddit.com/r/funny", R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/aww", "https://www.reddit.com/r/aww", R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/ama", "https://www.reddit.com/r/ama", R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/WTF", "https://www.reddit.com/r/wtf", R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/Random", "https://www.reddit.com/r/random", R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/Randnsfw", "https://www.reddit.com/r/randnsfw", R.drawable.ic_action_trending_up));


        //On assigne le recycler à la vue,
        mDrawerRecyclerView = (RecyclerView) findViewById(R.id.DrawerRecycler);

        //La liste d'objet est de taille fixe. (On va peut-etre changer sa avec les favoris)
        mDrawerRecyclerView.setHasFixedSize(true);

        //Création de l'adapteur.
        mDrawerAdapter = new DrawerAdapter(drawerMenuItem, this);
        mDrawerRecyclerView.setAdapter(mDrawerAdapter);

        //Création du layout manager pour gérer le drawer
        mDrawerLayoutManager = new LinearLayoutManager(this);
        mDrawerRecyclerView.setLayoutManager(mDrawerLayoutManager);

        //On assigne le layout du drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.DrawerLayout);
        mActionBarDrawerToggleToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_open);

        //Listener pour l'ouverture et la fermeture du drawer
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggleToggle);
    }

    /**
     *
     */
    public void commencerRafraichissement() {
        String url = mSubredditCourrant.equals("Front Page") ? "" : mSubredditCourrant;
        url += mFiltreCourrant;
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
                Toast.makeText(getApplicationContext(), "Une erreur est survenue!", Toast.LENGTH_LONG);
                _swipe_layout.setRefreshing(false);
            }
        });
    }

    public void allerProchainePage() {
        String url = mSubredditCourrant.equals("Front Page") ? "" : mSubredditCourrant;
        url += mFiltreCourrant;
        url += ".json";
        url += "?after=" + mProchainePage;

        WebServiceClient.get(url, new RequestParams(), new JsonHttpResponseHandler() {
            /**
             * Override pour définir les actions si la requête est un succès
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
             * Override pour définir les actions si la requête est un échec
             *
             * @param statusCode Le code de status de la requête web.
             * @param headers L'entête de la requête web
             * @param throwable Wrapper l'information de l'erreur
             * @param errorResponse Le message d'erreur
             */
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(getApplicationContext(), "Une erreur est survenue!", Toast.LENGTH_LONG);
            }
        });
    }

    /**
     * Permets de gérer la sélection des items du drawer.
     *
     * @param position La position sélectionnée
     */
    @Override
    public void onDrawerItemSelected(int position) {

        //On ferme le drawer à la sélection d'un item.
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mDrawerRecyclerView);
        }

        //On gère l'item sélectionné
        ((DrawerAdapter) mDrawerAdapter).selectionnerPosition(position);
        mSubredditCourrant = ((DrawerAdapter) mDrawerAdapter).getItem(position).getText();
        mFiltreCourrant = "/hot";
        setTitle(mSubredditCourrant + mFiltreCourrant);
        commencerRafraichissement();
    }

    /**
     * Gère les backPressed
     */
    @Override
    public void onBackPressed() {

        // Si l'image est full screen, on la rends visible
        if (contentIsFullScreen) {
            fullImage.setVisibility(View.GONE);
            fullWeb.setVisibility(View.GONE);
            fullWeb.loadUrl("About::Blank");
            // fullWeb.destroy();
            contentIsFullScreen = false;
            dimBackground.setVisibility(View.GONE);
        }
        //Si le drawer est ouvert on le ferme
        else if (mDrawerLayout.isDrawerOpen(mDrawerRecyclerView))
            mDrawerLayout.closeDrawer(mDrawerRecyclerView);
            //Si la recherche est active on la désactive
        else if (mIsRechercheActive)
            desactiverMenuDeRecherche();
        else
            super.onBackPressed();
    }

    /**
     * Méthode qui active l'action de recherche dans la toolbar
     */

    private void activerMenuDeRecherche() {
        //On retrouve l'action bar
        ActionBar actionBar = getSupportActionBar();

        //On active et affiche la custom view.
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setCustomView(R.layout.search_bar);

        //On masque le titre pour faire plus d'espace
        actionBar.setDisplayShowTitleEnabled(false);

        //On retrouve la vue de recherche.
        mBoiteRecherche = (EditText) actionBar.getCustomView().findViewById(R.id.searchBox);

        //Ajout du listener pour trigger la recherche.
        mBoiteRecherche.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    lancerLaRecherche();
                    desactiverMenuDeRecherche();
                    return true;
                }
                return false;
            }
        });

        //On affiche l'icon de fermeture dans le textbox
        mActionRecherche.setIcon(R.drawable.ic_action_navigation_close);

        //On donne le focus au textbox.
        mBoiteRecherche.requestFocus();

        //On affiche le claver tactil
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(mBoiteRecherche, InputMethodManager.SHOW_IMPLICIT);

        mIsRechercheActive = true;
    }

    /**
     * Méthode qui désactive l'action de recherche dans la toolbar
     */
    private void desactiverMenuDeRecherche() {

        //On retrouve l'action bar
        ActionBar actionBar = getSupportActionBar();

        //On d�sactive la custom view dans la toolbar et on affiche le titre
        actionBar.setDisplayShowCustomEnabled(false);
        actionBar.setDisplayShowTitleEnabled(true);

        //On enl�ve le focus au text box
        mBoiteRecherche.clearFocus();
        View view = this.getCurrentFocus();
        //On masque le clavier tactil
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);


        //On ajoute l'icon (loupe) de recherche
        mActionRecherche.setIcon(getResources().getDrawable(R.drawable.ic_action_search_white));

        mIsRechercheActive = false;
    }

    /**
     * Fait une recherche sur le site de reddit afin d'obtenir les posts répondant au critère de recherche
     */
    private void lancerLaRecherche() {
        String searchQuery = mBoiteRecherche.getText().toString();

        //Permet d'accéder a un subreddit
        if(searchQuery.contains("/r/"))
        {
            mSubredditCourrant = searchQuery;
            setTitle(mSubredditCourrant + mFiltreCourrant);
            commencerRafraichissement();
        }
        else if (mSubredditCourrant != null) {
            RequestParams requestParams = new RequestParams();
            requestParams.add("q", searchQuery);
            requestParams.add("restrict_sr", "on");
            requestParams.add("sort", "relevance");
            requestParams.add("t", "all");

            WebServiceClient.get(mSubredditCourrant + "/search.json", requestParams, new JsonHttpResponseHandler() {

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
                 * Override pour définir les actions si la requête est un échec
                 *
                 * @param statusCode Le code de status de la requête web.
                 * @param headers L'entête de la requête web
                 * @param throwable Wrapper l'information de l'erreur
                 * @param errorResponse Le message d'erreur
                 */
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(getApplicationContext(), "Une erreur est survenue!", Toast.LENGTH_LONG);
                    _swipe_layout.setRefreshing(false);
                }
            });
        } else {
            //Ajoutes les paramètres de recherche.
            RequestParams requestParams = new RequestParams();
            requestParams.add("q", searchQuery);
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
                    Toast.makeText(getApplicationContext(), "Une erreur est survenue!", Toast.LENGTH_LONG);
                    _swipe_layout.setRefreshing(false);
                }
            });
        }
    }

    public void onImageClick(View v) {
        fullImage.setVisibility(View.INVISIBLE);
        fullWeb.setVisibility(View.GONE);
        fullWeb.loadUrl("about:blank");
        dimBackground.setVisibility(View.GONE);
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
