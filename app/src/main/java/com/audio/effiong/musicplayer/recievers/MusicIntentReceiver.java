package com.audio.effiong.musicplayer.recievers;

import android.content.Context;
import android.content.Intent;

import com.audio.effiong.musicplayer.services.MusicService;

/**
 * Created by Victor on 9/25/2016.
 */

public class MusicIntentReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (intent.getAction().equals(
                android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            try {
                if(MusicService.serviceRunning)
                MusicService.getInstance().tempStopAudioNoisy();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            // signal your service to stop playback
            // (via an Intent, for instance)
        }
    }
}
