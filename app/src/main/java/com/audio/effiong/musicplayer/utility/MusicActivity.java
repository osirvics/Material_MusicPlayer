package com.audio.effiong.musicplayer.utility;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Handler;
import android.view.Display;
import android.view.WindowManager;

import com.audio.effiong.musicplayer.dbhandler.MusicPlayerDBHelper;

/**
 * Created by Victor on 6/22/2016.
 */
public class MusicActivity extends Application {
    public static Context applicationContext = null;
    public static volatile Handler applicationHandler = null;
    public static Point displaySize = new Point();
    public static float density = 1;
    public static int themeId;
    public static String themeSetting = "";


//    @Override
//    protected void attachBaseContext(Context base) {
//        super.attachBaseContext(base);
//        MultiDex.install(this);
//    }

    @Override
    public void onCreate() {
        super.onCreate();

        applicationContext = getApplicationContext();
        applicationHandler = new Handler(applicationContext.getMainLooper());





        /**
         * Data base initialize
         */
        initilizeDB();
        /*
         * Display Density Calculation so that Application not problem with All
		 * resolution.
		 */
        checkDisplaySize();
        density = applicationContext.getResources().getDisplayMetrics().density;



//
//        if (!ATE.config(this, "light_theme").isConfigured()) {
//            ATE.config(this, "light_theme")
//                    .activityTheme(R.style.AppThemeLight)
//                    .primaryColorRes(R.color.colorPrimary2)
//                    .accentColorRes(R.color.colorAccent)
//                    .coloredNavigationBar(false)
//                    .navigationViewSelectedIconRes(R.color.colorAccentLightDefault)
//                    .navigationViewSelectedTextRes(R.color.colorAccentLightDefault)
//                   // .usingMaterialDialogs(true)
//                    .commit();
//        }
//        if (!ATE.config(this, "dark_theme").isConfigured()) {
//            ATE.config(this, "dark_theme")
//                    .activityTheme(R.style.AppThemeDark)
//                    .primaryColorRes(R.color.colorPrimaryDarkDefault)
//                    .accentColorRes(R.color.colorAccentDarkDefault)
//                    .coloredNavigationBar(false)
//                    .navigationViewSelectedIconRes(R.color.colorAccentDarkDefault)
//                     .navigationViewSelectedTextRes(R.color.colorAccentDarkDefault)
//                    //.usingMaterialDialogs(true)
//                    .commit();
//        }
//        if (!ATE.config(this, "light_theme_notoolbar").isConfigured()) {
//            ATE.config(this, "light_theme_notoolbar")
//                    .activityTheme(R.style.AppThemeLight)
//                    .coloredActionBar(false)
//                    .primaryColorRes(R.color.colorPrimaryLightDefault)
//                    .accentColorRes(R.color.colorAccentLightDefault)
//                    .navigationViewSelectedIconRes(R.color.colorAccent)
//                    .navigationViewSelectedTextRes(R.color.colorAccent)
//                    .coloredNavigationBar(false)
//                    //.usingMaterialDialogs(true)
//                    .commit();
//        }
//        if (!ATE.config(this, "dark_theme_notoolbar").isConfigured()) {
//            ATE.config(this, "dark_theme_notoolbar")
//                    .activityTheme(R.style.AppThemeDark)
//                    .coloredActionBar(false)
//                    .primaryColorRes(R.color.colorPrimaryDarkDefault)
//                    .accentColorRes(R.color.colorAccentDarkDefault)
//                    .coloredNavigationBar(true)
//                    .navigationViewSelectedIconRes(R.color.colorAccentLightDefault)
//                    .navigationViewSelectedTextRes(R.color.colorAccentLightDefault)
//                    //.usingMaterialDialogs(true)
//                    .commit();
//        }

		/*
         * Imageloader initialize
		 */
       // initImageLoader(applicationContext);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    public static int getThemeId()
    {
        return themeId;
    }

//    public static void reloadTheme()
//    {
//        themeSetting = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString("defaultTheme", "0");
//        if(themeSetting.equals("0"))
//            themeId = R.style.AppTheme;
//        else
//            themeId = R.style.AppTheme3;
//    }

//
//    public static void reloadTheme()
//    {
//
//    boolean theme = MusicPreference.getTheme(applicationContext);
//      if(theme){
//          themeId = R.style.AppTheme;
//       }
//       else
//          themeId = R.style.AppTheme3;
//    }


    public static int dp(float value) {
        return (int) Math.ceil(density * value);
    }

    public static void checkDisplaySize() {
        try {
            WindowManager manager = (WindowManager) applicationContext.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    if (android.os.Build.VERSION.SDK_INT < 13) {
                        displaySize.set(display.getWidth(), display.getHeight());
                    } else {
                        display.getSize(displaySize);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    /**
     * Related to Data Base.
     */
    public MusicPlayerDBHelper DB_HELPER;

    private void initilizeDB() {
        if (DB_HELPER == null) {
            DB_HELPER = new MusicPlayerDBHelper(MusicActivity.this);
        }
        try {
            DB_HELPER.getWritableDatabase();
            DB_HELPER.openDataBase();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void closeDB() {
        try {
            DB_HELPER.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
