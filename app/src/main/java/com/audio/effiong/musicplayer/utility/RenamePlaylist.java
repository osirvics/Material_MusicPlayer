
package com.audio.effiong.musicplayer.utility;


import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.audio.effiong.musicplayer.fragments.PlaylistFragment;

public class RenamePlaylist extends DialogFragment {


    public static RenamePlaylist newInstance(long songList) {
        RenamePlaylist dialog = new RenamePlaylist();
        Bundle bundle = new Bundle();
        bundle.putLong("songs", songList);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final long songs = getArguments().getLong("songs");
        String initName = getPlaylistNameFromId(songs);
        return new MaterialDialog.Builder(getActivity()).title("Rename Playlist").positiveText("Rename").negativeText("Cancel").input("Enter playlist name", initName, false, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                String newName = input.toString();
                if ( newName!= null && newName.length() > 0) {
                    if (songs != -1 ){
                        renamePlaylist(getActivity(),newName,songs);
                        Toast.makeText(getActivity(), "Done", Toast.LENGTH_SHORT).show();
                    }
                    if(getParentFragment() instanceof PlaylistFragment) {
                        //update.updateEvent();
                        ((PlaylistFragment) getParentFragment()).updatePlaylists(false);
                    }
                } else {
                    Toast.makeText(getActivity(), "playlist name already exist", Toast.LENGTH_SHORT).show();
                }

            }
        }).build();
    }

    private final Uri uri = MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;

    public void renamePlaylist(Context context, String newplaylist, long playlist_id) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        String where = MediaStore.Audio.Playlists._ID + " =? ";
        String[] whereVal = { Long.toString(playlist_id) };
        values.put(MediaStore.Audio.Playlists.NAME, newplaylist);
        resolver.update(uri, values, where, whereVal);
    }

    private String getPlaylistNameFromId(final long id) {
        Cursor cursor = getActivity().getContentResolver().query(
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

}

