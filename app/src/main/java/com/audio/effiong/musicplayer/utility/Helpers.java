package com.audio.effiong.musicplayer.utility;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.activities.FolderActivity;
import com.audio.effiong.musicplayer.services.MusicService;

import java.util.List;

/**
 * Created by Victor on 7/7/2016.
 */
public class Helpers {

    public static UpdatePlaylistEvent update;

       public Helpers(){
            update = null;
       }

    public static void setUpdateEventListener(UpdatePlaylistEvent listener) {
        update = listener;
    }

    private static ContentValues[] mContentValuesCache = null;
    public static String getATEKey(Context context) {
        if(context!=null){
            return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("dark_theme", false) ?
                    "dark_theme" : "light_theme";
        }
        return "light_theme";
    }

    public static Bitmap getBitmapOfVector(@DrawableRes int id, int height, int width, Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Drawable vectorDrawable = context.getDrawable(id);
            if (vectorDrawable != null)
                vectorDrawable.setBounds(0, 0, width, height);
            Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            if (vectorDrawable != null)
                vectorDrawable.draw(canvas);
            return bm;
        }
        return null;

    }


    public static int dpToPx(int dp, Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }


    public static int getBlackWhiteColor(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        if (darkness >= 0.5) {
            return Color.WHITE;
        } else return Color.BLACK;
    }

    public static int getWindowWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.widthPixels;
    }

    public int getWindowHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.heightPixels;
    }

  public static int getHeight(){
      return Resources.getSystem().getDisplayMetrics().heightPixels;
  }
    public static int getWidth(){
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

//    public synchronized void  loadType1(){
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(final Void... unused) {
//                playList = ListSongs.getSongListSOrt(getApplicationContext()) ;
//                return null;
//            }
//        }.execute();
//    }


    public static final String MUSIC_ONLY_SELECTION = MediaStore.Audio.AudioColumns.IS_MUSIC + "=1"
            + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''";

    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean isJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static boolean isAboveKitkat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean isNotNougart() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.N;
    }

    public static boolean isJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean isKitkat() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT || Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT_WATCH;
    }

    public static Uri getAlbumArtUri(long paramInt) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), paramInt);
    }

    public static final String makeCombinedString(final Context context, final String first,
                                                  final String second) {
        final String formatter = context.getResources().getString(R.string.combine_two_strings);
        return String.format(formatter, first, second);
    }

    public static final String makeLabel(final Context context, final int pluralInt,
                                         final int number) {
        return context.getResources().getQuantityString(pluralInt, number, number);
    }

//    public static final String makeShortTimeString(final Context context, long secs) {
//        long hours, mins;
//
//        hours = secs / 3600;
//        secs %= 3600;
//        mins = secs / 60;
//        secs %= 60;
//
//        final String durationFormat = context.getResources().getString(
//                hours == 0 ? R.string.durationformatshort : R.string.durationformatlong);
//        return String.format(durationFormat, hours, mins, secs);
//    }

    public static int getActionBarHeight(Context context) {
        int mActionBarHeight;
        TypedValue mTypedValue = new TypedValue();

        context.getTheme().resolveAttribute(R.attr.actionBarSize, mTypedValue, true);

        mActionBarHeight = TypedValue.complexToDimensionPixelSize(mTypedValue.data, context.getResources().getDisplayMetrics());

        return mActionBarHeight;
    }

    public static final int getSongCountForPlaylist(final Context context, final long playlistId) {
        Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                new String[]{BaseColumns._ID}, MUSIC_ONLY_SELECTION, null, null);

        if (c != null) {
            int count = 0;
            if (c.moveToFirst()) {
                count = c.getCount();
            }
            c.close();
            c = null;
            return count;
        }

        return 0;
    }

//    public static boolean hasEffectsPanel(final Activity activity) {
//        final PackageManager packageManager = activity.getPackageManager();
//        return packageManager.resolveActivity(createEffectsIntent(),
//                PackageManager.MATCH_DEFAULT_ONLY) != null;
//    }

//    public static Intent createEffectsIntent() {
//        final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
//        effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, MusicPlayer.getAudioSessionId());
//        return effects;
//    }

//    public static int getBlackWhiteColor(int color) {
//        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
//        if (darkness >= 0.5) {
//            return Color.WHITE;
//        } else return Color.BLACK;
//    }

    public enum IdType {
        NA(0),
        Artist(1),
        Album(2),
        Playlist(3);

        public final int mId;

        IdType(final int id) {
            mId = id;
        }

        public static IdType getTypeById(int id) {
            for (IdType type : values()) {
                if (type.mId == id) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Unrecognized id: " + id);
        }
    }

    public enum PlaylistType {
        LastAdded(-11, R.string.playlist_last_added),
        RecentlyPlayed(-22, R.string.playlist_recently_played),
        TopTracks(-33, R.string.playlist_top_tracks);

        public long mId;
        public int mTitleId;

        PlaylistType(long id, int titleId) {
            mId = id;
            mTitleId = titleId;
        }

        public static PlaylistType getTypeById(long id) {
            for (PlaylistType type : PlaylistType.values()) {
                if (type.mId == id) {
                    return type;
                }
            }

            return null;
        }
    }




    public static void addToPlaylist(final Context context, final long[] ids, final long playlistid) {
        final int size = ids.length;
        final ContentResolver resolver = context.getContentResolver();
        final String[] projection = new String[]{
                "max(" + "play_order" + ")",
        };
        final Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistid);
        Cursor cursor = null;
        int base = 0;

        try {
            cursor = resolver.query(uri, projection, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                base = cursor.getInt(0) + 1;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }

        int numinserted = 0;
        for (int offSet = 0; offSet < size; offSet += 1000) {
            makeInsertItems(ids, offSet, 1000, base);
            numinserted += resolver.bulkInsert(uri, mContentValuesCache);
        }
        final String message = context.getResources().getQuantityString(
                R.plurals.number_of_playlist, numinserted, numinserted);
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        context.sendBroadcast(new Intent(FolderActivity.ACTION_CLEAR_PLAYLIST));
    }

    public static void makeInsertItems(final long[] ids, final int offset, int len, final int base) {
        if (offset + len > ids.length) {
            len = ids.length - offset;
        }

        if (mContentValuesCache == null || mContentValuesCache.length != len) {
            mContentValuesCache = new ContentValues[len];
        }
        for (int i = 0; i < len; i++) {
            if (mContentValuesCache[i] == null) {
                mContentValuesCache[i] = new ContentValues();
            }
            mContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, base + offset + i);
            mContentValuesCache[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, ids[offset + i]);
        }
    }


    public static final long createPlaylist(final Context context, final String name) {
        if (name != null && name.length() > 0) {
            final ContentResolver resolver = context.getContentResolver();
            final String[] projection = new String[]{
                    MediaStore.Audio.PlaylistsColumns.NAME
            };
            final String selection = MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'";
            Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    projection, selection, null, null);
            if (cursor.getCount() <= 0) {
                final ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                final Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                        values);
                return Long.parseLong(uri.getLastPathSegment());
            }
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            return -1;
        }
        return -1;
    }

    public static void deletePlaylist(final Context context, final String selectedplaylist, long id) {
        String playlistNmae = getPlaylistNameFromId(context, id);

        ListSongs.showMaterialDialog(context, "Delete Playlist", "Do you want to delete " + playlistNmae, "OK", new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                switch (which) {
                    case POSITIVE:
                        String playlistid = selectedplaylist;
                        ContentResolver resolver = context.getContentResolver();
                        String where = MediaStore.Audio.Playlists._ID + "=?";
                        String[] whereVal = {playlistid};
                        resolver.delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, where, whereVal);
                        Toast.makeText(context, R.string.playlist_success, Toast.LENGTH_SHORT).show();
                       // Toast toast = Toast.makeText(context," Deleted", Toast.LENGTH_SHORT);
                      //  toast.show();
                        update.updateEvent();

                        break;
                    case NEGATIVE:
                        // proceed with logic by disabling the related features or quit the app.
                        break;

                }
            }
            // String playlistid = getPlayListId(context, selectedplaylist);

        });
    }












    public String getPlayListId(Context context,String playlist )
    {

        //  read this record and get playlistid

        Uri newuri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;

        final String playlistid = MediaStore.Audio.Playlists._ID;

        final String playlistname = MediaStore.Audio.Playlists.NAME;

        String where = MediaStore.Audio.Playlists.NAME + "=?";

        String[] whereVal = {playlist};

        String[] projection = {playlistid, playlistname};

        ContentResolver resolver = context.getContentResolver();

        Cursor record = resolver.query(newuri , projection, where, whereVal, null);

        int recordcount = record.getCount();

        String foundplaylistid = "";

        if (recordcount > 0)

        {
            record.moveToFirst();

            int idColumn = record.getColumnIndex(playlistid);

            foundplaylistid = record.getString(idColumn);

            record.close();
        }

        return foundplaylistid;
    }

    public static String getPlaylistNameFromId(Context context, final long id) {
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, new String[] {
                        MediaStore.Audio.Playlists.NAME
                }, MediaStore.Audio.Playlists._ID + "=?", new String[] {
                        String.valueOf(id)
                }, MediaStore.Audio.Playlists.NAME);
        String playlistName = null;
        if (cursor != null) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                playlistName = cursor.getString(0);
            }
        }
        cursor.close();
        cursor = null;
        return playlistName;
    }

    public static void initEmailHandle(Context context){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.parse("mailto:victor46539@gmail.com");
        intent.setData(data);
        context.startActivity(intent);
    }
    public interface UpdatePlaylistEvent
    {
        void updateEvent ();
    }
    public static void navigateToEqualizer(Activity context) {
        try {
            // The google MusicFX apps need to be started using startActivityForResult
            context.startActivityForResult(Helpers.createEffectsIntent(), 934);
        } catch (final ActivityNotFoundException notFound) {
            Toast.makeText(context, "Equalizer not found", Toast.LENGTH_SHORT).show();
        }
    }

    public static Intent createEffectsIntent() {
        final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, MusicService.getInstance().getAudioSessionId());
        return effects;
    }

    public static int getDarkColor(int baseColor) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[2] *= 0.78f;
        return Color.HSVToColor(hsv);
    }


    public static int[] getAvailableColor(Context context, Palette palette) {
        int[] temp = new int[1];


        if (palette.getVibrantSwatch() != null) {
            temp[0] = palette.getVibrantSwatch().getRgb();


            //temp[1] = palette.getVibrantSwatch().getBodyTextColor();
            // temp[2] = palette.getVibrantSwatch().getTitleTextColor();
        }
        else if (palette.getDarkVibrantSwatch() != null) {
            temp[0] = palette.getDarkVibrantSwatch().getRgb();
            // Log.e(TAG,"DarkVibarntSwatch Used");
            //// temp[1] = palette.getDarkVibrantSwatch().getBodyTextColor();
            // temp[2] = palette.getDarkVibrantSwatch().getTitleTextColor();
        }
        else if (palette.getMutedSwatch() != null) {
            temp[0] = palette.getMutedSwatch().getRgb();
            // Log.e(TAG,"MutedSwatch Used");
            //// temp[1] = palette.getDarkMutedSwatch().getBodyTextColor();
            // temp[2] = palette.getDarkMutedSwatch().getTitleTextColor();
        }else if (palette.getLightMutedSwatch() != null) {
            temp[0] = palette.getLightMutedSwatch().getRgb();
            //Log.e(TAG,"LightMutedSwatch Used");
            //// temp[1] = palette.getDarkMutedSwatch().getBodyTextColor();
            // temp[2] = palette.getDarkMutedSwatch().getTitleTextColor();
        }
        else if (palette.getDarkMutedSwatch() != null) {
            temp[0] = palette.getDarkMutedSwatch().getRgb();
            // Log.e(TAG,"DarkMutedSwatch Used");

            //// temp[1] = palette.getDarkMutedSwatch().getBodyTextColor();
            // temp[2] = palette.getDarkMutedSwatch().getTitleTextColor();
        }

        else{
            // useDefault =true;
            // Log.e(TAG,"DefaultSwatch Used");
            if (context!=null)
            temp[0] = ContextCompat.getColor(context, R.color.coloorPrimary);
            else temp[0] = 0xffffffff;
        }
        //animateColorChangeView(backgroundGroup,temp[0]);
        // temp[1] = ContextCompat.getColor(getActivity(), android.R.color.white);
        // temp[2] = 0xffe5e5e5;

        return temp;
    }


    public int deletePlaylistTracks(Context context, long playlistId,
                                    long audioId) {
        ContentResolver resolver = context.getContentResolver();
        int countDel = 0;
        try {
            Uri uri = MediaStore.Audio.Playlists.Members.getContentUri(
                    "external", playlistId);
            String where = MediaStore.Audio.Playlists.Members._ID + "=?" ; // my mistake was I used .AUDIO_ID here

            String audioId1 = Long.toString(audioId);
            String[] whereVal = { audioId1 };
            countDel=resolver.delete(uri, where,whereVal);
            //Log.d("TAG", "tracks deleted=" + countDel);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return countDel;

    }

    public void delete(final List<Long> songsDelete, //audioId
                       final List<Integer> position, long playlistId, Context context) {
        if (playlistId != -1) {
            for (int i = 0; i < songsDelete.size(); i++) {
                //Log.d("song going to del", "" + songsDelete.get(i)); //audio Id 1214
                int countDel = deletePlaylistTracks(context, playlistId, songsDelete.get(i));

            }
        }
    }

    public static String getDuration(long milliseconds) {
        long sec = (milliseconds / 1000) % 60;
        long min = (milliseconds / (60 * 1000)) % 60;
        long hour = milliseconds / (60 * 60 * 1000);

        String s = (sec < 10) ? "0" + sec : "" + sec;
        String m = (min < 10) ? "0" + min : "" + min;
        String h = "" + hour;

        String time = "";
        if (hour > 0) {
            time = h + ":" + m + ":" + s;
        } else {
            time = m + ":" + s;
        }
        return time;
    }

//    public static void removeFromPlaylist(@NonNull Context context, @NonNull ArrayList<Playlist> list) {
//        Uri contentUri = MediaStore.Audio.Playlists.Members.getContentUri("external", (long) ((Playlist) list.get(0)).playlistId);
//        String[] strArr = new String[list.size()];
//        for (int i = 0; i < strArr.length; i++) {
//            strArr[i] = String.valueOf(((PlaylistSong) list.get(i)).idInPlayList);
//        }
//        String str = "_id in (";
//        for (String str2 : strArr) {
//            str = str + "?, ";
//        }
//        try {
//            context.getContentResolver().delete(contentUri, str.substring(0, str.length() - 2) + ")", strArr);
//        } catch (SecurityException e) {
//        }
//    }

    public static void openPlayStore(Context c, String appPackName){
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=" + appPackName
            ));
            c.startActivity(intent);
        } catch (android.content.ActivityNotFoundException e) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("http://details?id=" + appPackName));
            c.startActivity(intent);
        }
    }
}