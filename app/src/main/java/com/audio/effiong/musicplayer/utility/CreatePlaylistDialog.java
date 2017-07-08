package com.audio.effiong.musicplayer.utility;

/**
 * Created by Victor on 8/4/2016.
 */

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.audio.effiong.musicplayer.fragments.PlaylistFragment;
import com.audio.effiong.musicplayer.model.SongModel;


/**
 * Created by naman on 20/12/15.
 */
public class CreatePlaylistDialog extends DialogFragment {
    public static UpdatePlaylistEvent update;
//
//    public CreatePlaylistDialog(){
//        update = null;
//    }

    public static void setUpdateEventListener(UpdatePlaylistEvent listener) {
        update = listener;
    }

    public static CreatePlaylistDialog newInstance() {
        return newInstance((SongModel) null);
    }

    public static CreatePlaylistDialog newInstance(SongModel song) {
        long[] songs;
        if (song == null) {
            songs = new long[0];
        } else {
            songs = new long[1];
            songs[0] = song.getSongId();
        }
        return newInstance(songs);
    }



    public static CreatePlaylistDialog newInstance(long[] songList) {
        CreatePlaylistDialog dialog = new CreatePlaylistDialog();
        Bundle bundle = new Bundle();
        bundle.putLongArray("songs", songList);
        dialog.setArguments(bundle);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity()).title("New Playlist").positiveText("Create").negativeText("Cancel").input("Enter playlist name", "", false, new MaterialDialog.InputCallback() {
            @Override
            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {

                long[] songs = getArguments().getLongArray("songs");
                long playistId = Helpers.createPlaylist(getActivity(), input.toString());

                if (playistId != -1) {
                    if (songs != null && songs.length != 0)
                        Helpers.addToPlaylist(getActivity(), songs, playistId);
                    else
                        Toast.makeText(getActivity(), "Created playlist", Toast.LENGTH_SHORT).show();

                        getActivity().sendBroadcast(new Intent(PlaylistFragment.ACTION_UPDATE_PLAYLIST));
//                    if(getParentFragment() instanceof PlaylistFragment) {
//                    //update.updateEvent();
//                      ((PlaylistFragment) getParentFragment()).updatePlaylists(true);
//                   }
                } else {
                    Toast.makeText(getActivity(), "playlist name already exist", Toast.LENGTH_SHORT).show();
                }

            }
        }).build();
    }

    public interface UpdatePlaylistEvent
    {
        void updateEvent ();
    }
}
