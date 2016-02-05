package com.example.reddit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Build;
import android.provider.CalendarContract;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.style.BackgroundColorSpan;
import android.support.v7.widget.Toolbar;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.reddit.drawer.DrawerAdapter;
import com.example.reddit.drawer.DrawerCallbacks;
import com.example.reddit.drawer.DrawerItem;
import com.example.reddit.utilities.*;
import com.example.reddit.utilities.ImageLoader;
import com.google.gson.Gson;
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
    //Drawer
    private RecyclerView mDrawerRecyclerView;
    private RecyclerView.Adapter mDrawerAdapter;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggleToggle;
    private RecyclerView.LayoutManager mDrawerLayoutManager;

    // DerniereURL utilise
    private String mCurrentSubreddit;
    private String mCurrentFilter;
    private String mProchainePage;

    //GridLayout
    private boolean isGrid;
    private GridLayout gridLayout;

    // Fullscreen image/web
    private ImageView fullImage;
    private boolean contentIsFullScreen;
    private DrawerLayout drawerLayout;
    private ImageView dimBackground;
    private WebView fullWeb;

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
        _recyclelst_post.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(-1)) {
                    //Scroll at Top
                } else if (!recyclerView.canScrollVertically(1)) {
                    //Scroll at bottom
                    String url = mCurrentSubreddit.equals("Front Page") ? "" : mCurrentSubreddit;
                    url += mCurrentFilter;
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
                            Toast.makeText(getApplicationContext(), "Error while parsing server data", Toast.LENGTH_LONG);
                        }
                    });
                } else if (dy < 0) {
                    //Scroll up
                } else if (dy > 0) {
                    //Scroll down
                }
            }
        });// Affiche l'image en centre d'écran.
        _recyclelst_post.addOnItemTouchListener(new RecyclerEventListener(this, _recyclelst_post, new RecyclerEventListener.IEventListener() {
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

            @Override
            public void onDoubleTap(View view, int position) {

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

        //On enleve le focus au text box
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
     *
     */
    private void doSearch() {
        if (mCurrentSubreddit != null) {
            RequestParams requestParams = new RequestParams();
            requestParams.add("q", mSearchBox.getText().toString());
            requestParams.add("restrict_sr", "on");
            requestParams.add("sort", "relevance");
            requestParams.add("t", "all");

            WebServiceClient.get(mCurrentSubreddit + "/search.json", requestParams, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    FrontPage fp = new GsonBuilder().create().fromJson(response.toString(), FrontPage.class);
                    _recyclelst_post.setAdapter(new PostAdapter(fp.data.children, isGrid));
                    _swipe_layout.setRefreshing(false);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(getApplicationContext(), "Error while parsing server data", Toast.LENGTH_LONG);
                    _swipe_layout.setRefreshing(false);
                }
            });
        } else {
            RequestParams requestParams = new RequestParams();
            requestParams.add("q", mSearchBox.getText().toString());
            requestParams.add("restrict_sr", "off");
            requestParams.add("sort", "relevance");
            requestParams.add("t", "all");
            WebServiceClient.get("/search.json", requestParams, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    FrontPage fp = new GsonBuilder().create().fromJson(response.toString(), FrontPage.class);
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
    }

    public void onImageClick(View v) {
        fullImage.setVisibility(View.INVISIBLE);
        fullWeb.setVisibility(View.GONE);
        fullWeb.loadUrl("about:blank");
        dimBackground.setVisibility(View.GONE);
    }
}
