package com.audio.effiong.musicplayer.utility;

/**
 * Created by Victor on 6/22/2016.
 */

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.audio.effiong.musicplayer.model.SongModel;

import java.util.ArrayList;

public class LastAddedLoader {

    private static Cursor mCursor;
    public static ArrayList<SongModel> getLastAddedSongs(Context context) {

        ArrayList<SongModel> mSongList = new ArrayList<>();
        mCursor = makeLastAddedCursor(context);

        if (mCursor != null && mCursor.moveToFirst()) {
            do {
                long id = mCursor.getLong(0);
                String title = mCursor.getString(1);
                String artist = mCursor.getString(2);
                String album = mCursor.getString(3);
                int duration = mCursor.getInt(4);
                String path = mCursor.getString(5);
                long dateAdded = mCursor.getLong(6);
                long albumId = mCursor.getLong(7);
                String track = mCursor.getString(8);

                final SongModel song = new SongModel(id, title, artist, path, false, albumId, album, dateAdded,duration,track);
                //String lastplaySong = mGson.toJson(song);
              //Log.e("Recently",lastplaySong);
                mSongList.add(song);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mSongList;
    }

    public static final Cursor makeLastAddedCursor(final Context context) {
        //four weeks ago
        long fourWeeksAgo = (System.currentTimeMillis() / 1000) - (4 * 3600 * 24 * 7);
        long cutoff = 0L;
        // use the most recent of the two timestamps
        if (cutoff < fourWeeksAgo) {
            cutoff = fourWeeksAgo;
        }

        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1");
        selection.append(" AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''");
        selection.append(" AND " + MediaStore.Audio.Media.DATE_ADDED + ">");
        selection.append(cutoff);

        return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{"_id", "title", "artist", "album", "duration", "_data", "date_added", "album_id","track"},
                selection.toString(), null, MediaStore.Audio.Media.DATE_ADDED + " DESC");
    }


    public static final int getSongCountForLastAdded(Context context) {
        Cursor c = makeLastAddedCursor(context);

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


}
