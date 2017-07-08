package com.audio.effiong.musicplayer.utility;

/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.audio.effiong.musicplayer.model.Playlist;

import java.util.ArrayList;

public class PlaylistLoader {

    static ArrayList<Playlist> mPlaylistList;
    private static Cursor mCursor;

    public static ArrayList<Playlist> getPlaylists(Context context, boolean defaultIncluded) {

        mPlaylistList = new ArrayList<>();

        if (defaultIncluded)
            makeDefaultPlaylists(context);

        mCursor = makePlaylistCursor(context);

        if (mCursor != null && mCursor.moveToFirst()) {
            do {

                final long id = mCursor.getLong(0);

                final String name = mCursor.getString(1);

                final int songCount = Helpers.getSongCountForPlaylist(context, id);

                final Playlist playlist = new Playlist(id, name, songCount);

                mPlaylistList.add(playlist);
            } while (mCursor.moveToNext());
        }
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        return mPlaylistList;
    }

    private static void makeDefaultPlaylists(Context context) {
        final Resources resources = context.getResources();

        /* Last added list */
        final int songCount =  LastAddedLoader.getSongCountForLastAdded(context);
        final Playlist lastAdded = new Playlist(Helpers.PlaylistType.LastAdded.mId,
                resources.getString(Helpers.PlaylistType.LastAdded.mTitleId), songCount);
        mPlaylistList.add(lastAdded);

        /* Recently Played */
//        final Playlist recentlyPlayed = new Playlist(Helpers.PlaylistType.RecentlyPlayed.mId,
//                resources.getString(Helpers.PlaylistType.RecentlyPlayed.mTitleId), -1);
//        mPlaylistList.add(recentlyPlayed);

        /* Top Tracks */
//        final Playlist topTracks = new Playlist(Helpers.PlaylistType.TopTracks.mId,
//                resources.getString(Helpers.PlaylistType.TopTracks.mTitleId), -1);
//        mPlaylistList.add(topTracks);
    }


    public static final Cursor makePlaylistCursor(final Context context) {
        return context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                new String[]{
                        BaseColumns._ID,
                        MediaStore.Audio.PlaylistsColumns.NAME
                }, null, null, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
    }
}
