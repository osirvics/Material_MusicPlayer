package com.audio.effiong.musicplayer.utility;

/**
 * Created by Victor on 5/15/2016.
 */

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.Spanned;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.dbhandler.FavoritePlayTableHelper;
import com.audio.effiong.musicplayer.model.Album;
import com.audio.effiong.musicplayer.model.Artist;
import com.audio.effiong.musicplayer.model.SongModel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by architjn on 11/12/15.
 */
public class ListSongs {

    public static DeleteEvent ie;
    public static DeleteSearchEvent delete;
    public static final String NAVIGATE_LIBRARY = "navigate_library";
    public static final String NAVIGATE_PLAYLIST = "navigate_playlist";
    public static final String NAVIGATE_QUEUE = "navigate_queue";
    public static final String NAVIGATE_ALBUM = "navigate_album";
    public static final String NAVIGATE_ARTIST = "navigate_artist";
    public static final String NAVIGATE_NOWPLAYING = "navigate_nowplaying";
    public static final String NAVIGATE_SETTINGS = "navigate_settings";
    public static final String NAVIGATE_SEARCH = "navigate_search";
    public static String  path = "";
    public static Cursor cursor = null;
//
//    public ListSongs(DeleteEvent event){
//        ie = event;
//    }

    public ListSongs() {
        // set null or default listener or accept as argument to constructor
        ie = null;
        delete = null;
    }

    // Assign the listener implementing events interface that will receive the events
    public static void setDeleteEventListener(DeleteEvent listener) {
        ie = listener;
    }

    public static void setDeleteSearchListener(DeleteSearchEvent listener){
        delete = listener;
    }

    public static ArrayList<Album> getAlbumList(Context context) {
        final ArrayList<Album> albumList = new ArrayList<>();
        System.gc();
        final String orderBy = MediaStore.Audio.Albums.ALBUM;
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, null, null, orderBy);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.ALBUM);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.ARTIST);
            int numOfSongsColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.NUMBER_OF_SONGS);
            int albumArtColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.ALBUM_ART);
            //add albums to list
            do {
                albumList.add(new Album(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        false, musicCursor.getString(albumArtColumn),
                        musicCursor.getInt(numOfSongsColumn)));
            }
            while (musicCursor.moveToNext());
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return albumList;
    }

    public static Album getLastAddedAlbum(Context context) {
        System.gc();
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        final String orderBy = MediaStore.Audio.Media.DATE_ADDED + " DESC LIMIT 1";
        Cursor musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            long albumId = musicCursor.getLong(albumColumn);
            return getAlbumFromId(context, albumId);
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return null;
    }

    public static Album getAlbumFromId(Context context, long albumId) {
        System.gc();
        final String where = MediaStore.Audio.Albums._ID + "='" + albumId + "'";
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, where, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.ALBUM);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.ARTIST);
            int numOfSongsColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.NUMBER_OF_SONGS);
            int albumArtColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.ALBUM_ART);
            //add albums to list
            return new Album(musicCursor.getLong(idColumn),
                    musicCursor.getString(titleColumn),
                    musicCursor.getString(artistColumn),
                    false, musicCursor.getString(albumArtColumn),
                    musicCursor.getInt(numOfSongsColumn));
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return null;

    }

    public static ArrayList<Artist> getArtistList(Context context) {
        ArrayList<Artist> albumList = new ArrayList<>();
        System.gc();
        final String orderBy = MediaStore.Audio.Artists.ARTIST;
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, null, null, orderBy);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.ARTIST);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Artists._ID);
            int numOfAlbumsColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
            int numOfTracksColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
            //add albums to list
            do {
                albumList.add(new Artist(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getInt(numOfAlbumsColumn),
                        musicCursor.getInt(numOfTracksColumn)));
            }
            while (musicCursor.moveToNext());
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return albumList;
    }

    public static long getArtistIdFromName(Context context, String name) {
        System.gc();
        String where = MediaStore.Audio.Artists.ARTIST + "='" + name + "'";
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, where, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Artists._ID);
            return musicCursor.getLong(idColumn);
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return 0;
    }


    public static ArrayList<SongModel> getSongList(Context context) {
        ArrayList<SongModel> songList = new ArrayList<>();
        System.gc();
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        final String orderBy = MediaStore.Audio.Media.DATE_ADDED + " DESC";
        Cursor musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int addedDateColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);
            int songDurationColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.DURATION);
            int songTrackColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TRACK);
            do {
                songList.add(new SongModel(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        musicCursor.getString(pathColumn), false,
                        musicCursor.getLong(albumIdColumn),
                        musicCursor.getString(albumColumn),
                        musicCursor.getLong(addedDateColumn),
                        musicCursor.getLong(songDurationColumn),
                        musicCursor.getString(songTrackColumn)));
            }
            while (musicCursor.moveToNext());


        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return songList;
    }

    public static ArrayList<SongModel> getSongListSOrt(Context context) {
        ArrayList<SongModel> songList = new ArrayList<>();
        System.gc();
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";
        final String orderBy = MusicPreference.getSortOrder(context);
                // MediaStore.Audio.Media.DATE_ADDED + " DESC";
        Cursor musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int addedDateColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);
            int songDurationColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.DURATION);
            int songTrackColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TRACK);
            do {
                songList.add(new SongModel(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        musicCursor.getString(pathColumn), false,
                        musicCursor.getLong(albumIdColumn),
                        musicCursor.getString(albumColumn),
                        musicCursor.getLong(addedDateColumn),
                        musicCursor.getLong(songDurationColumn),
                        musicCursor.getString(songTrackColumn)));
            }
            while (musicCursor.moveToNext());


        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return songList;
    }



    public static SongModel getSongFromId(Context context,long id) {
        System.gc();
        final String where = MediaStore.Audio.Media._ID + "=" + id;
        Cursor musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int addedDateColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);
            int songDurationColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);
            int songTrackColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TRACK);
            return new SongModel(musicCursor.getLong(idColumn),
                    musicCursor.getString(titleColumn),
                    musicCursor.getString(artistColumn),
                    musicCursor.getString(pathColumn), false,
                    musicCursor.getLong(albumIdColumn),
                    musicCursor.getString(albumColumn),
                    musicCursor.getLong(addedDateColumn),
                    musicCursor.getLong(songDurationColumn),
                    musicCursor.getString(songTrackColumn));
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return null;
    }

    public static SongModel getSong(Context context, long songId) {
        System.gc();
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
                + MediaStore.Audio.Media._ID + "='" + songId + "'";
        Cursor musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int addedDateColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);
            int songDurationColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);
            int songTrackColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TRACK);
            return new SongModel(musicCursor.getLong(idColumn),
                    musicCursor.getString(titleColumn),
                    musicCursor.getString(artistColumn),
                    musicCursor.getString(pathColumn), false,
                    musicCursor.getLong(albumIdColumn),
                    musicCursor.getString(albumColumn),
                    musicCursor.getLong(addedDateColumn),
                    musicCursor.getLong(songDurationColumn),
                    musicCursor.getString(songTrackColumn));
        }
        return null;
    }

    public static ArrayList<Album> getAlbumListOfArtist(Context context, long artistId) {
        final ArrayList<Album> albumList = new ArrayList<>();
        System.gc();
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.Albums.getContentUri("external", artistId),
                        null, null, null, MediaStore.Audio.Albums.DEFAULT_SORT_ORDER);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Artists.Albums.ALBUM);
            int idColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Artists.Albums.ARTIST);
            int numOfSongsColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Albums.NUMBER_OF_SONGS_FOR_ARTIST);
            int albumArtColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.ALBUM_ART);
            //add albums to list
            do {
                albumList.add(new Album(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        false, musicCursor.getString(albumArtColumn),
                        musicCursor.getInt(numOfSongsColumn)));
            }
            while (musicCursor.moveToNext());
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return albumList;
    }
    public static ArrayList<SongModel> getSongsListOfArtist(Context context, String artistName) {
        ArrayList<SongModel> songList = new ArrayList<>();
        System.gc();
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
                + MediaStore.Audio.Media.ARTIST + "='" + artistName.replace("'", "''") + "'";
        final String orderBy = MusicPreference.getArtistSortOrder(context);
                // MediaStore.Audio.Media.TITLE;
        Cursor musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int addedDateColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);
            int songDurationColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);
            int songTrackColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TRACK);
            do {
                songList.add(new SongModel(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        musicCursor.getString(pathColumn), false,
                        musicCursor.getLong(albumIdColumn),
                        musicCursor.getString(albumColumn),
                        musicCursor.getLong(addedDateColumn),
                        musicCursor.getLong(songDurationColumn),
                        musicCursor.getString(songTrackColumn)));
            }
            while (musicCursor.moveToNext());
        }
        musicCursor.close();
        return songList;
    }

    /*public static Search getSearchResults(Context context, String sQuery) {
        System.gc();
        ArrayList<Song> songList = searchSong(context, sQuery);
        ArrayList<Album> albumList = searchAlbum(context, sQuery);
        ArrayList<Artist> artistList = searchArtist(context, sQuery);
        return new Search(songList, albumList, artistList);
    }
*/
    /*private static ArrayList<Song> searchSong(Context context, String sQuery) {
        ArrayList<Song> songList = new ArrayList<>();
        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
                + MediaStore.Audio.Media.TITLE + " LIKE '%" + sQuery.replace("'", "''") + "%'";
        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int addedDateColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);
            int songDurationColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);
            do {
                songList.add(new Song(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        musicCursor.getString(pathColumn), false,
                        musicCursor.getLong(albumIdColumn),
                        musicCursor.getString(albumColumn),
                        musicCursor.getLong(addedDateColumn),
                        musicCursor.getLong(songDurationColumn)));
            }
            while (musicCursor.moveToNext());
        }
        return songList;
    }*/

    private static ArrayList<Album> searchAlbum(Context context, String sQuery) {
        ArrayList<Album> albumList = new ArrayList<>();
        final String where = MediaStore.Audio.Albums.ALBUM + " LIKE '%" + sQuery.replace("'", "''") + "%'";
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, null, where, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.ALBUM);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.ARTIST);
            int numOfSongsColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.NUMBER_OF_SONGS);
            int albumArtColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Albums.ALBUM_ART);
            //add albums to list
            do {
                albumList.add(new Album(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        false, musicCursor.getString(albumArtColumn),
                        musicCursor.getInt(numOfSongsColumn)));
            }
            while (musicCursor.moveToNext());
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return albumList;
    }


   /* private static ArrayList<Artist> searchArtist(Context context, String sQuery) {
        ArrayList<Artist> artistList = new ArrayList<>();
        final String where = MediaStore.Audio.Artists.ARTIST + " LIKE '%" + sQuery.replace("'", "''") + "%'";
        Cursor musicCursor = context.getContentResolver().
                query(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, null, where, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            //get columns
            int titleColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.ARTIST);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Artists._ID);
            int numOfAlbumsColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.NUMBER_OF_ALBUMS);
            int numOfTracksColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Artists.NUMBER_OF_TRACKS);
            //add albums to list
            do {
                artistList.add(new Artist(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getInt(numOfAlbumsColumn),
                        musicCursor.getInt(numOfTracksColumn)));
            }
            while (musicCursor.moveToNext());
        }
        return artistList;
    }*/

    public static ArrayList<SongModel> getAlbumSongList(Context context, long albumId) {
        System.gc();
        Cursor musicCursor;
        ArrayList<SongModel> songs = new ArrayList<>();
        String where = MediaStore.Audio.Media.ALBUM_ID + "=?";
        String whereVal[] = {String.valueOf(albumId)};
        String orderBy = MusicPreference.getAlbumSortOrder(context);
                //MediaStore.Audio.Media.TITLE;

        musicCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, whereVal, orderBy);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex
                    (android.provider.MediaStore.Audio.Media.ARTIST);
            int pathColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);
            int albumIdColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);
            int albumColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);
            int addedDateColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);
            int albumSongDuration = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);
            int songTrackColumn = musicCursor.getColumnIndex
                    (MediaStore.Audio.Media.TRACK);
            do {
                songs.add(new SongModel(musicCursor.getLong(idColumn),
                        musicCursor.getString(titleColumn),
                        musicCursor.getString(artistColumn),
                        musicCursor.getString(pathColumn), false,
                        musicCursor.getLong(albumIdColumn),
                        musicCursor.getString(albumColumn),
                        musicCursor.getLong(addedDateColumn),
                        musicCursor.getLong(albumSongDuration),
                        musicCursor.getString(songTrackColumn)));
            }
            while (musicCursor.moveToNext());
        }
        if (musicCursor != null) {
            musicCursor.close();
        }
        return songs;
    }

    public static String getAlbumArt(Context context, long albumdId) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[]{String.valueOf(albumdId)},
                null);
        String imagePath = "";
        if (cursor != null && cursor.moveToFirst()) {
            imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
        }
        if (cursor != null) {
            cursor.close();
        }
        return imagePath;
    }


    public static ArrayList<SongModel> getSongsFromSQLDBCursor(Cursor cursor) {
        ArrayList<SongModel> generassongsList = new ArrayList<SongModel>();
        long hehe =2016;
        long not = 122233;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {

                    long ID = cursor.getLong(cursor.getColumnIndex(FavoritePlayTableHelper.ID));
                    long album_id = cursor.getLong(cursor.getColumnIndex(FavoritePlayTableHelper.ALBUM_ID));
                    String ARTIST = cursor.getString(cursor.getColumnIndex(FavoritePlayTableHelper.ARTIST));
                    String TITLE = cursor.getString(cursor.getColumnIndex(FavoritePlayTableHelper.TITLE));
                    String DISPLAY_NAME = cursor.getString(cursor.getColumnIndex(FavoritePlayTableHelper.DISPLAY_NAME));
                    String DURATION = cursor.getString(cursor.getColumnIndex(FavoritePlayTableHelper.DURATION));
                    String Path = cursor.getString(cursor.getColumnIndex(FavoritePlayTableHelper.PATH));

                    final SongModel song = new SongModel(ID, TITLE, ARTIST, Path, false, album_id, DISPLAY_NAME, hehe,not,"5");
                    generassongsList.add(song);

                } while (cursor.moveToNext());
            }
            closeCrs();
        } catch (Exception e) {
            closeCrs();
            e.printStackTrace();
        }
        return generassongsList;
    }
    private static void closeCrs() {
        if (cursor != null) {
            try {
                cursor.close();
            } catch (Exception e) {

            }
        }
    }

    public static void setRingtone(final Context context, final long id) {
        final ContentResolver resolver = context.getContentResolver();
        final Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
        try {
            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Audio.AudioColumns.IS_RINGTONE, "1");
            values.put(MediaStore.Audio.AudioColumns.IS_ALARM, "1");
            resolver.update(uri, values, null, null);
        } catch (final UnsupportedOperationException ingored) {
            return;
        }

        final String[] projection = new String[] {
                BaseColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.TITLE
        };

        final String selection = BaseColumns._ID + "=" + id;
        Cursor cursor = resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                selection, null, null);
        try {
            if (cursor != null && cursor.getCount() == 1) {
                cursor.moveToFirst();
                Settings.System.putString(resolver, Settings.System.RINGTONE, uri.toString());
                final String message = "added as rigntone";
                Toast.makeText((Activity)context, message, Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
    }


    public static void shareSong(Context context,final String path){
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("audio/*");
        share.putExtra(Intent.EXTRA_STREAM,
                Uri.parse("file:///" + path));
        context.startActivity(Intent.createChooser(share,
                context.getString(R.string.share_music)));
    }

    public static void deleteSong(final Context context, final String songName, final int position, final long[] list, final SongModel song){
        showMaterialDialog(context,"Delete Song", "Delete " +songName +" from device?","DELETE", new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                switch (which) {
                    // final long songId
                    case POSITIVE:
                        deleteTracks(context,list);
                        storeFavoritePlay(context,song,0);
                           ie.deleteEvent();
                        ie.deleteSongsEvent(position);
                        Toast.makeText(context, "Deleted",Toast.LENGTH_SHORT).show();
                        //delete.deleteSong(position);
                        //reloadAdapter();
                        break;
                    case NEGATIVE:
                        // proceed with logic by disabling the related features or quit the app.
                        break;
                }
            }
        });

    }

    public static void deleteSongs(final Context context, final long[] list, final ArrayList<SongModel> songs){
        final String content = context.getResources().getQuantityString(
                R.plurals.multiple_cab_delete_popup, list.length, list.length);
        showMaterialDialog(context,"Delete Song", content,"DELETE", new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                switch (which) {
                    // final long songId
                    case POSITIVE:
                        deleteTracks(context,list);
                        ie.deleteEvent();
                        ie.deleteSongsEvent(0);
                        Toast.makeText(context, "Deleted",Toast.LENGTH_SHORT).show();

                        new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(final Void... unused) {
                               for (int i =0; i < songs.size(); i++){
                                    SongModel song = songs.get(i);

                                   storeFavoritePlay(context,song,0);
                                }
                                return null;
                            }
                        }.execute();

                        //delete.deleteSong(position);
                        //reloadAdapter();
                        break;
                    case NEGATIVE:
                        // proceed with logic by disabling the related features or quit the app.
                        break;
                }
            }
        });

    }

    public static void deletePlaylists(final Context context, final long playlist_id, final long[] id) {
        //String playlistNmae = getPlaylistNameFromId(context, id);
        final String content = context.getResources().getQuantityString(
                R.plurals.playlist_cab_remove_popup, id.length, id.length);

        ListSongs.showMaterialDialog(context, "Remove Songs", content, "OK", new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                switch (which) {
                    case POSITIVE:
                        removeSongFromPlaylist(context,playlist_id,id);
                        ie.deleteEvent();

                        break;
                    case NEGATIVE:
                        // proceed with logic by disabling the related features or quit the app.
                        break;

                }
            }


        });
    }


    public static void removeSongFromPlaylist(Context ctx, long playlist_id, long[] ids) {
        ContentResolver resolver = ctx.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist_id);


//        String audioId1 = Long.toString(song_id);
//        String where = "audioId1=? LIMIT 1";
//        String[] args = {audioId1};
//
//        int res =  resolver.delete(uri, where, args);
        int count = 0;

        for(int i = 0; i<ids.length; i++){
            long song_id =  ids[i];
            count +=   resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID+" = "+song_id, null);
        }


        // TODO reloadAdapter();
       // String detial = ctx.getString(R.string.remove_songs_playlists, String.valueOf(count));
        final String message = ctx.getResources().getQuantityString(
                R.plurals.playlist_cab_removed, count, count);

        Toast.makeText(ctx,message,Toast.LENGTH_LONG).show();
        // reloadPlaylistList(ctx);
        //return res;
    }


    public static void deleteSongSearch(final Context context, String songName, String Path, final long songId, final SongModel song, final int position){
        showMaterialDialog(context,"Delete Song", "Delete " +songName +" from device?","DELETE", new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                switch (which) {
                    case POSITIVE:
                        deleteTracks(context,new long[]{ songId });
                        storeFavoritePlay(context,song,0);
//                        File songFile = new File(path);
//                        if (songFile.delete()) {
                        Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show();
//                        }
//                        context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
//                                MediaStore.MediaColumns._ID + "='" + songId + "'", null);

                        ie.deleteEvent();
                        ie.deleteSongsEvent(position);

                        //reloadAdapter();
                        break;
                    case NEGATIVE:
                        // proceed with logic by disabling the related features or quit the app.
                        break;
                }
            }
        });

    }




    public static void showMaterialDialog(Context context,String title,String content,String positiveText, MaterialDialog.SingleButtonCallback kk) {
        new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .positiveText(positiveText)
                .negativeText("CANCEL")
                .onPositive(kk)
                .onNegative(kk)
                .show();
    }


    public static void setAsRingtone(Context context, String path) {

            String s = path; // getPath
            File k = new File(s);  // set File from path
            if (s != null) {      // file.exists

                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DATA, k.getAbsolutePath());
                values.put(MediaStore.MediaColumns.TITLE, "ring");
                values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
                values.put(MediaStore.MediaColumns.SIZE, k.length());
                values.put(MediaStore.Audio.Media.ARTIST, R.string.app_name);
                values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
                values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
                values.put(MediaStore.Audio.Media.IS_ALARM, true);
                values.put(MediaStore.Audio.Media.IS_MUSIC, false);

                Uri uri = MediaStore.Audio.Media.getContentUriForPath(k
                        .getAbsolutePath());
                context.getContentResolver().delete(
                        uri,
                        MediaStore.MediaColumns.DATA + "=\""
                                + k.getAbsolutePath() + "\"", null);
                Uri newUri = context.getContentResolver().insert(uri, values);

                try {
                    RingtoneManager.setActualDefaultRingtoneUri(
                            context, RingtoneManager.TYPE_RINGTONE,
                            newUri);
                } catch (Throwable t) {

                }
            }
        }

    public static void requestSetRingtone(final Context context,long songId, String songName ){
        //tempSongId = songList.get(position).getSongId();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(Settings.System.canWrite(context)) {
                // Do stuff here
                ListSongs.setRingtone(context,songId);
            }
            else {

                showMaterialDialog(context,"Permission request","Please grant permission to enable " + songName + " to be set as ringtone", "OK", new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE:
                                ie.setBoolean(true);
                                Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
                                intent.setData(Uri.parse("package:" + context.getPackageName()));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                                break;
                            case NEGATIVE:
                                // proceed with logic by disabling the related features or quit the app.
                                break;
                        }
                    }
                });
            }
        }
        else{
            ListSongs.setRingtone(context,songId);
        }
    }


    public static void showSongDetailDialog(Context c, String path, String songName, String albumName,String artistName) {
        String bitrate = null;
        int kbps = 320;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(path);
             bitrate = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            mmr.release();
        }

        if(bitrate!=null) {
            try {
                kbps = Integer.parseInt(bitrate)/1000;
               // info.add(new Information(bitrate, kbps+" kbps"));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        if (path != null && fileExist(path)){
            MediaExtractor mex = new MediaExtractor();
            try {
                mex.setDataSource(path);
            } catch (IOException e) {
                e.printStackTrace();
            }

            MediaFormat mf = null;
            try {
                mf = mex.getTrackFormat(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //int bitRate = mf.getInteger(MediaFormat.KEY_BIT_RATE);
            int sampleRate = 0;
            if (mf != null) {
                sampleRate = mf.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            }
            String mime = null;
            if (mf != null) {
                mime = mf.getString(MediaFormat.KEY_MIME);
            }
            File songFile = new File(path);
            float file_size = (songFile.length() / 1024);
            StringBuilder content = new StringBuilder();
            content.append(getString(c,R.string.song_name));
            content.append(songName);
            content.append("\n\n");
            content.append(getString(c,R.string.album_name));
            content.append(albumName);
            content.append("\n\n");
            content.append(getString(c,R.string.artist_name));
            content.append(artistName);
            content.append("\n\n");
            content.append(getString(c,R.string.file_size));
            content.append(String.valueOf(String.format("%.2f", file_size / 1024)));
            content.append(" MB");
            content.append("\n\n");
            content.append(getString(c,R.string.file_path));
            content.append(path);
            content.append("\n\n");
//        content.append(getString(c,R.string.file_name));
//        content.append(songFile.getName());
//        content.append("\n\n");
            content.append(getString(c,R.string.format));
            content.append(mime);
            content.append("\n\n");
 content.append(getString(c,R.string.bitrate));
     content.append(String.valueOf(kbps));
      content.append(" kb/s");
       content.append("\n\n");
            content.append(getString(c,R.string.samplingrate));
            content.append(sampleRate);
            content.append(" Hz");
            new MaterialDialog.Builder(c)
                    .title(R.string.details)
                    .content(content.toString())
                    .positiveText("OK")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog,
                                            @NonNull DialogAction dialogAction) {
                            materialDialog.dismiss();
                        }
                    })
                    .show();
        }
        else{

            Toast.makeText(c, "Song corrupt or not supported", Toast.LENGTH_SHORT).show();
        }

    }

    public static Spanned getString(Context c,@StringRes int string) {
        return Html.fromHtml(c.getResources().getString(string));
    }


    public interface DeleteEvent
    {
        // This is just a regular method so it can return something or
        // take arguments if you like.
         void deleteEvent ();
        void deleteSongsEvent(int position);
         void setBoolean(boolean flag);
    }

    public interface DeleteSearchEvent{
        void deleteSong(int position);
    }


    public static void deleteTracks(final Context context, final long[] list) {
        final String[] projection = new String[] {
                BaseColumns._ID, MediaStore.MediaColumns.DATA, MediaStore.Audio.AudioColumns.ALBUM_ID
        };
        final StringBuilder selection = new StringBuilder();
        selection.append(BaseColumns._ID + " IN (");
        for (int i = 0; i < list.length; i++) {
            selection.append(list[i]);
            if (i < list.length - 1) {
                selection.append(",");
            }
        }
        selection.append(")");
        final Cursor c = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(),
                null, null);
        if (c != null) {


            // Step 1: Remove selected tracks from the current playlist, as well
            // as from the album art cache
//            c.moveToFirst();
//            while (!c.isAfterLast()) {
//                // Remove from current playlist
//                final long id = c.getLong(0);
//                removeTrack(id);
//                // Remove the track from the play count
//                SongPlayCount.getInstance(context).removeItem(id);
//                // Remove any items in the recents database
//                RecentStore.getInstance(context).removeItem(id);
//                c.moveToNext();
//            }

            // Step 2: Remove selected tracks from the database
            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    selection.toString(), null);

            // Step 3: Remove files from card
            c.moveToFirst();
            while (!c.isAfterLast()) {
                final String name = c.getString(1);
                final File f = new File(name);
                try { // File.delete can throw a security exception
                    if (!f.delete()) {
                        // I'm not sure if we'd ever get here (deletion would
                        // have to fail, but no exception thrown)
                        //Log.e("MusicUtils", "Failed to delete file " + name);
                    }
                    c.moveToNext();
                } catch (final SecurityException ex) {
                    c.moveToNext();
                }
            }
            c.close();
        }

       // final String message = makeLabel(context, R.plurals.NNNtracksdeleted, list.length);

       // CustomToast.makeText((Activity)context, message, CustomToast.LENGTH_SHORT).show();
        // We deleted a number of tracks, which could affect any number of
        // things
        // in the media content domain, so update everything.
        context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
        // Notify the lists to update
       // refresh();
    }

    /**
     * Store Favorite Play Data
     */
    public static synchronized void storeFavoritePlay(final Context context, final SongModel mDetail, final int isFav) {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    FavoritePlayTableHelper.getInstance(context).inserSong(mDetail, isFav);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }
    private static boolean fileExist(String albumArtPath) {
        File imgFile = new File(albumArtPath);
        return imgFile.exists();
    }

    public static Uri getAlbumArtUri(long paramInt) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), paramInt);
    }
}
