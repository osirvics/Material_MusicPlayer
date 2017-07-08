package com.audio.effiong.musicplayer.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.adapters.PagerAdapter;
import com.audio.effiong.musicplayer.fragments.PlaybackFragment;
import com.audio.effiong.musicplayer.services.MusicService;
import com.audio.effiong.musicplayer.slidinguppanelhelper.SlidingUpPanelLayout;
import com.audio.effiong.musicplayer.utility.Helpers;
import com.audio.effiong.musicplayer.utility.ListSongs;
import com.audio.effiong.musicplayer.utility.MusicActivity;
import com.audio.effiong.musicplayer.utility.MusicPreference;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hotchemi.android.rate.AppRate;
import hotchemi.android.rate.OnClickButtonListener;
import hotchemi.android.rate.StoreType;


public class MainActivity extends BaseThemedActivity implements PlaybackFragment.OnPanelClickListener, ATEActivityThemeCustomizer{
    private final String TAG = MainActivity.class.getSimpleName();
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    public Context context = null;
    TextView songtitle, songartist;
    String path = "";
    ImageView albumart;
    Handler navDrawerRunnable = new Handler();
    NavigationView navigationView;
    private boolean panelIsEpanded = false;
    String ateKey;
    int accentColor,primaryColor;
    /*private AdView mAdView;*/
    public static boolean mainActivityRuning = false;
   /* private boolean adLoaded = false;
    LinearLayout helperLayout;*/
    InterstitialAd mInterstitialAd;
    Handler handler;
    CoordinatorLayout coordinatorLayout;
    AppBarLayout appBarLayout;

    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {

                case PlaybackFragment.ACTION_RECIEVE_SONG:
                    long id= intent.getLongExtra("albumId",0);
                    final String name = intent.getStringExtra("songName");
                    final String artistName = intent.getStringExtra("artistName");
                    if(name!=null && artistName!=null)
                        songtitle.setText(name);
                    songartist.setText(artistName);
                    //if (checkAndRequestPermissions()) {
                        getAlbumImage(context,id);
                    //}

                    break;
                case PlaybackFragment.ACTION_CHANGE_PLAYSTATE:
                    break;
                case PlaybackFragment.ACTION_HIDE_ADVIEW:
//                    if (adLoaded)
//                        helperLayout.setVisibility(View.VISIBLE);
//                    mAdView.setVisibility(View.GONE);
                    break;
                case PlaybackFragment.ACTION_SHOW_ADVIEW:
//                    if(adLoaded){
//                        mAdView.setVisibility(View.VISIBLE);
//                        helperLayout.setVisibility(View.VISIBLE);
//                    }
                    break;
                case PlaybackFragment.ACTION_HIDE_ADVIEW_NEW:
                   // helperLayout.setVisibility(View.GONE);
                    break;
                case PlaybackFragment.ACTION_SHOW_ADVIEW_NEW:
//                    if(adLoaded){
//                        helperLayout.setVisibility(View.VISIBLE);
//                    }
                    break;
            }
        }
    };

    public void configReiever(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackFragment.ACTION_RECIEVE_SONG);
        filter.addAction(PlaybackFragment.ACTION_CHANGE_PLAYSTATE);
        filter.addAction(PlaybackFragment.ACTION_HIDE_ADVIEW);
        filter.addAction(PlaybackFragment.ACTION_SHOW_ADVIEW);
        filter.addAction(PlaybackFragment.ACTION_HIDE_ADVIEW_NEW);
        filter.addAction(PlaybackFragment.ACTION_SHOW_ADVIEW_NEW);
        registerReceiver(br, filter);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {

        if (!ATE.config(this, "light_theme").isConfigured()) {
            ATE.config(this, "light_theme")
                    .activityTheme(R.style.AppThemeLight)
                    .primaryColorRes(R.color.colorPrimary)
                    .accentColorRes(R.color.colorAccent)
                    .coloredNavigationBar(false)
                    .navigationViewThemed(false)
                    .commit();
        }
        if (!ATE.config(this, "dark_theme").isConfigured()) {
            ATE.config(this, "dark_theme")
                    .activityTheme(R.style.AppThemeDark)
                    .primaryColorRes(R.color.colorPrimary)
                    .accentColorRes(R.color.colorAccent)
                    .coloredNavigationBar(false)
                    .navigationViewThemed(false)
                    .commit();
        }
        ateKey = Helpers.getATEKey(this);
        accentColor = Config.accentColor(this, ateKey);
        primaryColor= Config.primaryColor(this,ateKey);
        super.onCreate(savedInstanceState);
        startService(new Intent(this, MusicService.class));

        panelIsEpanded = MusicPreference.isPanelOpen(this);
        mainActivityRuning = true;
        setContentView(R.layout.activity_main);
        appBarLayout =(AppBarLayout)findViewById(R.id.appbar);
        appBarLayout.setBackgroundColor(primaryColor);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .main_content);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarr);
        if(toolbar !=null)
            toolbar.setTitle(R.string.library);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        context = this;
        initDrawer(toolbar);
        initFragment();
        if (checkAndRequestPermissions()) {
            initTabedViewpager();
        }

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int[][] states = new int[][]{
                        new int[]{-android.R.attr.state_checked},  // unchecked
                        new int[]{android.R.attr.state_checked},   // checked
                        new int[]{}                                // default
                };
                if(ateKey.contains("light_theme")){
                    //Fill in color corresponding to state defined in state
                    int[] iconColors = new int[]{
                            Color.parseColor("#747474"),
                            primaryColor,
                            Color.parseColor("#747474"),
                    };

                    int[] textColors = new int[]{
                            Color.parseColor("#212121"),
                            primaryColor,
                            Color.parseColor("#212121"),
                    };
                    ColorStateList navigationViewColorStateList = new ColorStateList(states, iconColors);
                    ColorStateList textColorStateList  = new ColorStateList(states,textColors);
                    navigationView.setItemTextColor(textColorStateList);
                    navigationView.setItemIconTintList(navigationViewColorStateList);
                }
                else if((ateKey.contains("dark_theme"))){
                    int[] darkIconColors = new int[]{
                            Color.parseColor("#c0ffffff"),
                            primaryColor,
                            Color.parseColor("#c0ffffff"),
                    };

                    int[] darkTextColors = new int[]{
                            Color.parseColor("#ffffff"),
                            primaryColor,
                            Color.parseColor("#ffffff"),
                    };
                    ColorStateList navigationViewColorStateList = new ColorStateList(states, darkIconColors);
                    ColorStateList textColorStateList  = new ColorStateList(states,darkTextColors);
                    navigationView.setItemTextColor(textColorStateList);
                    navigationView.setItemIconTintList(navigationViewColorStateList);
                }
            }
        }, 750);
        MobileAds.initialize(this, "ca-app-pub-7549886159702245~1733005610");
        // Initialize the SDK before executing any other operations,
     /*   FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);*/
//        mAdView = (AdView) findViewById(adView);
//        AdRequest adRequest = new AdRequest.Builder()
//                .build();
//        mAdView.loadAd(adRequest);
//        helperLayout = (LinearLayout) findViewById(R.id.reavel);
//        mAdView.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//                super.onAdLoaded();
//                adLoaded = true;
//                if(!panelIsEpanded)
//                    mAdView.setVisibility(View.VISIBLE);
//            }
//        });
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7549886159702245/9768499611");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent ntent = new Intent(getApplication(), SettingsActivity.class);
                        startActivity(ntent);
                    }
                }, 100);
            }
        });

        requestNewInterstitial();
       askRateApp();

    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(adRequest);
    }
    private void initTabedViewpager() {
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(R.string.list_of_songs));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.list_of_albums));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.list_of_artists));
        tabLayout.addTab(tabLayout.newTab().setText(R.string.list_of_playlists));
        tabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setBackgroundColor(primaryColor);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());
        assert viewPager != null;
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        viewPager.setOffscreenPageLimit(4);
        //viewPager.setBackgroundColor(primaryColor);
    }

    private void initFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.panel_holder);
        if (fragment == null) {
            fragment = PlaybackFragment.newInstance(Helpers.isKitkat());
            fm.beginTransaction()
                    .add(R.id.panel_holder, fragment)
                    .commit();
        }

    }

    private void initDrawer(Toolbar toolbar) {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View header = null;
        if (navigationView != null) {
            header = navigationView.inflateHeaderView(R.layout.nav_header);
            albumart = (ImageView) header.findViewById(R.id.album_art);
            songtitle = (TextView) header.findViewById(R.id.song_title);
            songartist = (TextView) header.findViewById(R.id.song_artist);
        }


        if (navigationView != null) {
            navDrawerRunnable.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setupDrawerContent(navigationView);
                }
            }, 750);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(final MenuItem menuItem) {
                        mDrawerLayout.closeDrawers();
                        menuItem.setChecked(true);
                        mDrawerLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                switch (menuItem.getItemId()) {
                                    case R.id.nav_library:

                                        mDrawerLayout.closeDrawers();
                                        break;
                                    case R.id.fav:
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                final Intent i = new Intent(context, FavouriteActivity.class);
                                                startActivity(i);
                                            }
                                        },100);
                                        break;
                                    case R.id.nav_settings:
                                        if (mInterstitialAd.isLoaded()) {
                                            mInterstitialAd.show();
                                        } else {
                                            Handler handler1 = new Handler();
                                            handler1.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent ntent = new Intent(getApplication(), SettingsActivity.class);
                                                    startActivity(ntent);
                                                }
                                            },100);
                                        }
                                        break;
                                    case R.id.nav_about:
                                        Handler handler2 = new Handler();
                                        handler2.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                final Intent about = new Intent(context, AboutActivity.class);
                                                startActivity(about);
                                            }
                                        },100);
                                        break;
                                    case R.id.nav_help:
                                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                                        intent.setData(Uri.parse("mailto:victor46539@gmail.com")); // only email apps should handle this
                                        intent.putExtra(Intent.EXTRA_EMAIL, "addresses");
                                        intent.putExtra(Intent.EXTRA_SUBJECT, "subject");
                                        if (intent.resolveActivity(getPackageManager()) != null) {
                                            startActivity(intent);
                                        }
                                        break;
                                    case R.id.folder:
                                        Handler handler3 = new Handler();
                                        handler3.postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                final Intent folder = new Intent(context, FolderActivity.class);
                                                startActivity(folder);
                                            }
                                        },100);
                                        break;
                                }
                            }
                        }, 75);
                        return true;
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_settings:
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent ntent = new Intent(getApplication(), SettingsActivity.class);
                            startActivity(ntent);
                        }
                    },80);
                }
                return true;
            case R.id.action_equalizer:
                Helpers.navigateToEqualizer(MainActivity.this);
                return true;
            case R.id.action_search:
                Handler handler1 = new Handler();
                handler1.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        final Intent intent = new Intent(getApplicationContext(), SearchActivity.class);
                        startActivity(intent);
                    }
                },80);

                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkAndRequestPermissions() {
        int permissionReadStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionEriteStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionEriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionReadStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        //Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        //Log.d(TAG, "read & write services permission granted");
                        // process the normal flow
                        initTabedViewpager();
                        //else any one or both the permissions are not granted
                    } else {
                        //Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                            Snackbar snackbar = Snackbar
                                    .make(coordinatorLayout, "External storage permission is required for this app", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Grant", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    checkAndRequestPermissions();
                                                }
                                            });
                            snackbar.setActionTextColor(ContextCompat.getColor(this,R.color
                            .colorAccent));
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.DKGRAY);
                            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
                        }
                        else {
                            Snackbar snackbar = Snackbar
                                    .make(coordinatorLayout, "Go to settings and enable permissions", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Settings", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                                            intent.setData(uri);
                                            context.startActivity(intent);
                                        }
                                    });
                            snackbar.setActionTextColor(ContextCompat.getColor(this,R.color
                                    .colorAccent));
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.DKGRAY);
                            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
//                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
//                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    @Override
    public void onBackPressed() {
        PlaybackFragment fragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.panel_holder);
        if(panelIsEpanded){
            fragment.collapsePanel();
        }
        else {
            moveTaskToBack(true);
        }
    }

    @Override
    protected void onDestroy() {
        mainActivityRuning = false;
        try {
            int pos = MusicService.getInstance().getCurrentSongIndex();
            MusicPreference.saveLastPosition(this, pos);
            int progress = MusicService.getInstance().getPosition();
            MusicPreference.saveLastProgress(this, progress);
            MusicPreference.setPanelState(this,false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mainActivityRuning = true;
        unregisterReceiver(br);

    }

    @Override
    protected void onStart() {
        configReiever();
        super.onStart();
        SlidingUpPanelLayout.setFlagResume(Helpers.isKitkat());
        mainActivityRuning = true;

    }

    @Override
    public void onResume() {
        mainActivityRuning = true;
        super.onResume();
    }

    @Override
    public int getActivityTheme() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ?
                R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
    }
    @Override
    public void isPanelOpen(boolean flag) {
        panelIsEpanded = flag;
    }

    public void getAlbumImage(Context context, final long id) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                path = ListSongs.getAlbumArt(getApplicationContext(), id);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MusicActivity.applicationContext).load(path).placeholder(R.drawable.album_art).crossFade().centerCrop().into(albumart);
                    }
                }, 250);
            }
        }.execute();


    }
    public void askRateApp(){
        AppRate.with(this)
                .setStoreType(StoreType.GOOGLEPLAY) //default is Google, other option is Amazon
                .setInstallDays(3) // default 10, 0 means install day.
                .setLaunchTimes(5) // default 10 times.
                .setRemindInterval(2) // default 1 day.
                .setShowLaterButton(true) // default true.
                .setDebug(false) // default false.
                .setCancelable(false) // default false.
                .setOnClickButtonListener(new OnClickButtonListener() { // callback listener.
                    @Override
                    public void onClickButton(int which) {

                       // Log.e(MainActivity.class.getName(), Integer.toString(which));
                    }
                })
                .setTitle(R.string.rate_dialog_title)
                .setTextLater(R.string.new_rate_dialog_later)
                .setTextNever(R.string.new_rate_dialog_never)
                .setTextRateNow(R.string.rate_dialog_ok)
                .monitor();

        AppRate.showRateDialogIfMeetsConditions(this);
    }


}


