package com.audio.effiong.musicplayer.utility;

/**
 * Created by Victor on 8/4/2016.
 */

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.audio.effiong.musicplayer.model.Playlist;
import com.audio.effiong.musicplayer.model.SongModel;

import java.util.ArrayList;
import java.util.List;

public class AddPlaylistDialog extends DialogFragment {

    public static AddPlaylistDialog newInstance(SongModel song) {
        long[] songs = new long[1];
        songs[0] = song.getSongId();
        return newInstance(songs);
    }

    public static AddPlaylistDialog newInstance(ArrayList<SongModel> song) {
        long[] songs = new long[song.size()];
        for(int i = 0; i < song.size(); i++)
        {
            SongModel thesong = song.get(i);
            songs[i] = thesong.getSongId();
        }
        //songs[0] = song.getSongId();
        return newInstance(songs);
    }

    public static AddPlaylistDialog newInstance(long[] songList) {
        AddPlaylistDialog dialog = new AddPlaylistDialog();
        Bundle bundle = new Bundle();
        bundle.putLongArray("songs", songList);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final List<Playlist> playlists = PlaylistLoader.getPlaylists(getActivity(), false);
        CharSequence[] chars = new CharSequence[playlists.size() + 1];
        chars[0] = "Create new playlist";

        for (int i = 0; i < playlists.size(); i++) {
            chars[i + 1] = playlists.get(i).name;
        }
        return new MaterialDialog.Builder(getActivity()).title("Add to playlist").items(chars).itemsCallback(new MaterialDialog.ListCallback() {
            @Override
            public void onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                long[] songs = getArguments().getLongArray("songs");
                if (which == 0) {
                    CreatePlaylistDialog.newInstance(songs).show(getActivity().getSupportFragmentManager(), "CREATE_PLAYLIST");
                    return;
                }

                Helpers.addToPlaylist(getActivity(), songs, playlists.get(which - 1).id);
                dialog.dismiss();

            }
        }).build();
    }
}
