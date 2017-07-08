package com.audio.effiong.musicplayer.utility;

/**
 * Created by Victor on 6/13/2016.
 */

import android.content.Context;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.audio.effiong.musicplayer.model.SongModel;

import java.util.ArrayList;

public class MusicPreference {


    private static final String PREF_NAME = "com.architjn";
    private static final String REPEAT_ALL = "repeat_all";
    private static final String REPEAT_ONE = "repeat_one";
    private static final String SHUFFLE = "shuffle";
    private static final String isFirstRun = "false";
    private static final String position = "position";
    private static final String isPausedByUser = "paused";
    private static final String SORT_ORDER = "SORT_ORDER";
    private static final String ALBUM_SORT_ORDER = "ALBUM_SORT_ORDER";
    private static final String ARTIST_SORT_ORDER = "ARTIST_SORT_ORDER";
    private static final String THEME_PREFERNCE = "theme_preference";
    private static String root_sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();
   // SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(con);


    public static SongModel playingSongDetail;
    public static ArrayList<SongModel> playlist = new ArrayList<SongModel>();


    public static String getTheme(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).
                getString(THEME_PREFERNCE, "light");
    }



//    public static void saveLastSong(Context context, SongModel mDetail) {
//        //SharedPreferences.Editor editor = getPreferanse(context).edit();
//        Gson mGson = new Gson();
//        String lastplaySong = mGson.toJson(mDetail);
//        PreferenceManager.getDefaultSharedPreferences(context)
//                .edit().putString("lastplaysong", lastplaySong).commit();
//    }

//    public static SongModel getLastSong(Context context) {
//        if (playingSongDetail == null) {
//          //  SharedPreferences mSharedPreferences = getPreferanse(context);
//            String lastplaySong = PreferenceManager.getDefaultSharedPreferences(context).getString("lastplaysong", "");
//            Gson mGson = new Gson();
//            playingSongDetail = mGson.fromJson(lastplaySong, SongModel.class);
//        }
//        return playingSongDetail;
//    }

    public static void saveLastArtist(Context context, String type) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("nameOfArtist", type)
                .apply();
    }

    public static String getLastArtist(Context context) {
        return  PreferenceManager.getDefaultSharedPreferences(context)
                .getString("nameOfArtist", "");
    }


    public static void saveLastSongListType(Context context, int type) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt("songlisttype", type)
                .apply();
    }

    public static int getLastSongListType(Context context) {
        return  PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("songlisttype", 0);
    }

    public static void saveLastAlbID(Context context, long id) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
        .putLong("lastalbid", id)
                .apply();
    }

    public static long getLastAlbID(Context context) {
        return  PreferenceManager.getDefaultSharedPreferences(context)
                .getLong("lastalbid", 1);
    }

    public static void saveLastPosition(Context context, int positon) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt("lastposition", positon)
                .apply();
    }

    public static int getLastPosition(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("lastposition", 0);
    }

    public static void saveLastProgress(Context context, int progress) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt("duration", progress)
                .apply();
    }

    public static int getLastProgress(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt("duration",0);
    }

    public static String getFolderPath(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString("folder_pathh",root_sd);
    }
    public static void setFolderPath(Context context, String input) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString("folder_pathh", input)
                .apply();
    }


    public static boolean getFromUser(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("restart",false);
    }
    public static void setFromUser(Context context, boolean input) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean("restart", input)
                .apply();
    }


    public static boolean isFirstRun(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(isFirstRun,true);
    }
    public static void iSFirstRun(Context context, boolean input) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(isFirstRun, input)
                .apply();
    }

    public static void setSongId(Context context, long input) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong(position, input)
                .apply();
    }

    public static long getSongId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getLong(position,0);
    }


    public static void setShuffle(Context context) {
        if (isShuffleEnabled(context))
            setShuffleEnabled(context,false);
        else
            setShuffleEnabled(context,true);
    }

    public static boolean isShuffleEnabled(Context context) {
        return  PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(SHUFFLE, false);
    }

    public static void setShuffleEnabled(Context context, boolean input) {
         PreferenceManager.getDefaultSharedPreferences(context)
        .edit().putBoolean(SHUFFLE, input).apply();
    }


    public static boolean isHidden(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("showPanel",true);
    }
    public static void setHidden(Context context, boolean input) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean("showPanel", input)
                .apply();
    }

    public static void setRepeatEnable(Context context) {
        if (isRepeatAllEnabled(context)) {
            setRepeatAllEnable(context,false);
            setRepeatOneEnable(context,true);
        } else if (isRepeatOneEnabled(context)) {
            setRepeatOneEnable(context,false);
        } else {
            setRepeatAllEnable(context,true);
        }
    }

    public static void setRepeatAllEnable(Context context,boolean enable) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(REPEAT_ALL, enable).apply();
    }

    public static void setRepeatOneEnable(Context context,boolean enable) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(REPEAT_ONE, enable).apply();
    }

    public static boolean isRepeatAllEnabled(Context context) {
        return  PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(REPEAT_ALL, false);
    }

    public static boolean isRepeatOneEnabled(Context context) {
        return  PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(REPEAT_ONE, false);
    }


    public static boolean isPanelOpen(Context context) {
        return  PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(isPausedByUser, false);
    }

    public static void setPanelState(Context context, boolean input) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(isPausedByUser, input).apply();
    }


    public static String getSortOrder(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SORT_ORDER, SortOrder.SongSortOrder.TITLE);
    }
    public static void setSortOrder(Context context, String input) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(SORT_ORDER, input)
                .apply();
    }

    public static String getAlbumSortOrder(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(ALBUM_SORT_ORDER, SortOrder.AlbumSortOrder.TRACK_NUMBER);
    }
    public static void setAlbumSortOrder(Context context, String input) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(ALBUM_SORT_ORDER, input)
                .apply();
    }

    public static String getArtistSortOrder(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(ARTIST_SORT_ORDER, SortOrder.ArtistSortOrder.TITLE);
    }
    public static void setArtistSortOrder(Context context, String input) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(ARTIST_SORT_ORDER, input)
                .apply();
    }
}