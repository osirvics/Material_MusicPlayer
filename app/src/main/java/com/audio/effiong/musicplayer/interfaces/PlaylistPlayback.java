package com.audio.effiong.musicplayer.interfaces;

import com.audio.effiong.musicplayer.model.SongModel;

import java.util.ArrayList;

/**
 * Created by Victor on 04/07/2017.
 */

public interface PlaylistPlayback {
    void onPlaylistStarted(ArrayList<SongModel> songs);
}
