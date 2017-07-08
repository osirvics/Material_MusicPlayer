package com.audio.effiong.musicplayer.dbhandler;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import com.audio.effiong.musicplayer.model.SongModel;
import com.audio.effiong.musicplayer.utility.MusicActivity;

/**
 * Created by Victor on 6/22/2016.
 */
public class FavoritePlayTableHelper {
    public static final String TABLENAME = "ResentPlay";

    public static final String ID = "_id";
    public static final String ALBUM_ID = "album_id";
    public static final String ARTIST = "artist";
    public static final String TITLE = "title";
    public static final String DISPLAY_NAME = "display_name";
    public static final String DURATION = "duration";
    public static final String PATH = "path";
    public static final String AUDIOPROGRESS = "audioProgress";
    public static final String AUDIOPROGRESSSEC = "audioProgressSec";
    public static final String LastPlayTime = "lastplaytime";
    public static final String ISFAVORITE = "isfavorite";

    private static MusicPlayerDBHelper dbHelper = null;
    private static FavoritePlayTableHelper mInstance;
    private SQLiteDatabase sampleDB;


    public static synchronized FavoritePlayTableHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new FavoritePlayTableHelper(context);
        }
        return mInstance;
    }

    public Context context;

    public FavoritePlayTableHelper(Context context_) {
        this.context = context_;
        if (dbHelper == null) {
            dbHelper = ((MusicActivity) context.getApplicationContext()).DB_HELPER;
        }
    }

    public void inserSong(SongModel songDetail, int isFav) {
        try {

            sampleDB = dbHelper.getDB();
            sampleDB.beginTransaction();

            String sql = "Insert or Replace into " + TABLENAME + " values(?,?,?,?,?,?,?,?,?,?,?);";
            SQLiteStatement insert = sampleDB.compileStatement(sql);

            try {
                if (songDetail != null) {
                    insert.clearBindings();
                    insert.bindLong(1, songDetail.getSongId());
                    insert.bindLong(2, songDetail.getAlbumId());
                    insert.bindString(3, songDetail.getArtist());
                    //following two
                    insert.bindString(4, songDetail.getName());
                    insert.bindString(5, songDetail.getAlbumName());
                    insert.bindString(6, songDetail.getDuration());
                    insert.bindString(7, songDetail.getPath());
                    insert.bindString(8, songDetail.audioProgress + "");
                    insert.bindString(9, songDetail.audioProgressSec + "");
                    insert.bindString(10, System.currentTimeMillis() + "");
                    insert.bindLong(11, isFav);
                   // (int _id, int aLBUM_ID, String _artist, String _title, String _path, String _display_name, String _duration)
                    insert.execute();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            sampleDB.setTransactionSuccessful();

        } catch (Exception e) {
        } finally {
            sampleDB.endTransaction();
        }
    }

    private void closeCurcor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
            cursor = null;
        }
    }

    public Cursor getFavoriteSongList() {
        Cursor mCursor = null;
        try {
            String sqlQuery = "Select * from " + TABLENAME + " where " + ISFAVORITE + "=1";
            sampleDB = dbHelper.getDB();
            mCursor = sampleDB.rawQuery(sqlQuery, null);
        } catch (Exception e) {
            closeCurcor(mCursor);
            e.printStackTrace();
        }
        return mCursor;
    }

    public boolean getIsFavorite(SongModel mDetail) {
        Cursor mCursor = null;
        try {
            String sqlQuery = "Select * from " + TABLENAME + " where " + ID + "=" + mDetail.getSongId() + " and " + ISFAVORITE + "=1";
            sampleDB = dbHelper.getDB();
            mCursor = sampleDB.rawQuery(sqlQuery, null);
            if (mCursor != null && mCursor.getCount() >= 1) {
                closeCurcor(mCursor);
                return true;
            }
        } catch (Exception e) {
            closeCurcor(mCursor);
            e.printStackTrace();
        }
        return false;
    }
}
