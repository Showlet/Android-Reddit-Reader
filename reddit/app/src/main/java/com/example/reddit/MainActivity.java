package com.example.reddit;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.support.v7.app.ActionBarDrawerToggle;

import android.support.v7.widget.Toolbar;

import com.example.reddit.drawer.DrawerAdapter;
import com.example.reddit.drawer.DrawerCallbacks;
import com.example.reddit.drawer.DrawerItem;
import com.example.reddit.utilities.CacheManager;
import com.example.reddit.utilities.FetchCompleted;
import com.example.reddit.utilities.Fetcher;
import com.example.reddit.utilities.PreferencesManager;
import com.example.reddit.utilities.WebServiceClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.view.Menu;
import android.view.MenuItem;

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
    private EditText mSearchBox;
    private boolean mIsSearchActive;

    private  LinearLayoutManager linearLayoutManager;
    private LinearLayout linearLayout;
    //Drawer
    private RecyclerView mDrawerRecyclerView;
    private RecyclerView.Adapter mDrawerAdapter;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggleToggle;
    private RecyclerView.LayoutManager mDrawerLayoutManager;

    // DerniereURL utilise
    private String mCurrentURL;
    private String mCurrentSubreddit;

    //GridLayout
    private boolean isGrid;
    private GridLayout gridLayout;


    /**
     *
     * � la cr�ation de l'activit� (NO SHIT)
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialisation des helpers
        PreferencesManager.initializeInstance(getApplicationContext());
        CacheManager.initializeInstance(getApplicationContext());

        // isGrid est la variable qui mets l'affichage en gridview ou en list.
        String sadf = PreferencesManager.getInstance().getPreference("AFFICHAGE_SETTING");
        isGrid = (sadf.equals("Grid"));
        mCurrentURL = "https://www.reddit.com/hot.json";


        //Active le http caching
        CacheManager.getInstance().enableHttpCaching();


        //Initialise les composantes de l'interface graphique.
        initialiserRecyclerView();
        initialiserSwipeLayout();
        initialiserToolbar();
        initialiserDrawer();

        if (isGrid) {
            InitialiserGridLayout();
        }

        commencerRafraichissement(mCurrentURL);
    }

    /**
     *
     * Apr�s la cr�ation de l'activit� (NO SHIT)
     *
     * @param savedInstanceState
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);

        //On synchronise l'�tat (ouvert/ferm�) du drawer
        mActionBarDrawerToggleToggle.syncState();
    }

    /**
     *
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
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * � la s�lection d'un item du menu (NO SHIT)
     * @param item l'item s�lectionn�
     * @return super.onOptionsItemSelected(item)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Il faut se charger de traiter les click sur les options du menu ici
        //TODO: Filtrer les post, etc.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_filter:
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_search:
                if(mIsSearchActive)
                    disableSearchMenu();
                else
                    enableSearchMenu();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     *
     * Initialise la toolbar. Elle est ajout�e et attach�e au layout
     *
     */
    private void initialiserToolbar() {
        toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
    }

    /**
     * Initialise le SwipeLayout et definit le OnRefresh
     */
    private void initialiserSwipeLayout()
    {
        if (!isGrid) {
            _swipe_layout = (SwipeRefreshLayout) findViewById(R.id.swipe_user_linear);
        }
        if(isGrid) {
            _swipe_layout = (SwipeRefreshLayout) findViewById(R.id.swipe_user_grid);
        }

        _swipe_layout.setColorSchemeResources(R.color.colorPrimary);
        _swipe_layout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        MainActivity.this.commencerRafraichissement(mCurrentURL);
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
    private void initialiserRecyclerView()
    {
        if (!isGrid) {
            linearLayout = (LinearLayout)findViewById(R.id.linearLayout);
            linearLayout.setVisibility(View.VISIBLE);

            _recyclelst_post = (RecyclerView) findViewById(R.id.recyclelist_post_linear);
            _recyclelst_post.setHasFixedSize(true);
           linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            _recyclelst_post.setLayoutManager(linearLayoutManager);
        }

        else if (isGrid) {
            _recyclelst_post = (RecyclerView) findViewById(R.id.recyclelist_post_grid);
            _recyclelst_post.setHasFixedSize(true);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
            _recyclelst_post.setLayoutManager(gridLayoutManager);
        }
    }

    /**
     *
     * Initialise le drawer de navigation.
     *
     */
    private void initialiserDrawer()  {

        //On va g�rer sa diff�rament c'est juste pour tester.
        List<DrawerItem> drawerMenuItem = new ArrayList<DrawerItem>();
        drawerMenuItem.add(new DrawerItem("Front","https://www.reddit.com/hot.json",R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/programming","https://www.reddit.com/r/programming.json",R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/gonewild","https://www.reddit.com/r/gonewild.json",R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/funny","https://www.reddit.com/r/funny.json",R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/aww","https://www.reddit.com/r/aww.json",R.drawable.ic_action_trending_up));
        drawerMenuItem.add(new DrawerItem("/r/ama","https://www.reddit.com/r/ama.json",R.drawable.ic_action_trending_up));


        //On assigne le recycler � la vue,
        mDrawerRecyclerView = (RecyclerView) findViewById(R.id.DrawerRecycler);
        //La liste d'objet est de taille fixe. (On va peut-etre changer sa avec les favoris)
        mDrawerRecyclerView.setHasFixedSize(true);

        //Cr�ation de l'adapteur.
        mDrawerAdapter = new DrawerAdapter("MaxVerro",R.drawable.avatar,drawerMenuItem,this);
        mDrawerRecyclerView.setAdapter(mDrawerAdapter);

        //Cr�ation du layout manager pour g�rer le drawer
        mDrawerLayoutManager = new LinearLayoutManager(this);
        mDrawerRecyclerView.setLayoutManager(mDrawerLayoutManager);

        //On assigne le layout du drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.DrawerLayout);
        mActionBarDrawerToggleToggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.drawer_open,R.string.drawer_open);

        //Listener pour l'ouverture et la fermeture du drawer
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggleToggle);
    }

    /**
     *
     * @param url ou on vas chercher les post
     */
    public void commencerRafraichissement(String url)
    {
        mCurrentURL = url;
        String[] params = {
                url, ""
        };
        Fetcher fetcher = new Fetcher();
        fetcher.addListener(new FetchCompleted() {
            @Override
            public void run() {
                Gson gson = new GsonBuilder().create();
                try {

                    FrontPage fp = gson.fromJson(m_result, FrontPage.class);
                    _recyclelst_post.setAdapter(new PostAdapter(fp.data.children, isGrid));
                    _swipe_layout.setRefreshing(false);

                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), "Error while parsing server data", Toast.LENGTH_LONG);
                    _swipe_layout.setRefreshing(false);
                }
            }
        });
        fetcher.execute(params);
    }


    /**
     *
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
        mCurrentSubreddit = mCurrentSubreddit == "Front Page" ?  null : mCurrentSubreddit;
        commencerRafraichissement(((DrawerAdapter) mDrawerAdapter).getItem(position).getmUrl());
    }

    /**
     * G�re les backPressed
     */
    @Override
    public void onBackPressed() {

        //Si le drawer est ouvert on le ferme
        if (mDrawerLayout.isDrawerOpen(mDrawerRecyclerView))
            mDrawerLayout.closeDrawer(mDrawerRecyclerView);
        else if(mIsSearchActive)
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

        //On masque le clavier tactil
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);

        //On ajoute l'icon (loupe) de recherche
        mSearchAction.setIcon(getResources().getDrawable(R.drawable.ic_action_search_white));

        mIsSearchActive = false;
    }

    /**
     *
     */
    private void doSearch()
    {
        if(mCurrentSubreddit != null) {
            RequestParams requestParams = new RequestParams();
            requestParams.add("q",mSearchBox.getText().toString());
            requestParams.add("restrict_sr","on");
            requestParams.add("sort","relevance");
            requestParams.add("t","all");

            WebServiceClient.get(mCurrentSubreddit + "/search.json", requestParams, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    FrontPage fp = new GsonBuilder().create().fromJson(response.toString(), FrontPage.class);
                    _recyclelst_post.setAdapter(new PostAdapter(fp.data.children));
                    _swipe_layout.setRefreshing(false);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    //TODO Handle le onFailure
                }
            });
        }
        else{
            RequestParams requestParams = new RequestParams();
            requestParams.add("q",mSearchBox.getText().toString());
            requestParams.add("restrict_sr","off");
            requestParams.add("sort","relevance");
            requestParams.add("t","all");
            WebServiceClient.get("/search.json", requestParams, new JsonHttpResponseHandler() {

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    FrontPage fp = new GsonBuilder().create().fromJson(response.toString(), FrontPage.class);
                    _recyclelst_post.setAdapter(new PostAdapter(fp.data.children));
                    _swipe_layout.setRefreshing(false);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    //TODO Handle le onFailure
                }
            });
        }
    }

}
