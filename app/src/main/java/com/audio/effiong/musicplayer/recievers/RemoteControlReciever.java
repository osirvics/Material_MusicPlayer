package com.audio.effiong.musicplayer.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

import com.audio.effiong.musicplayer.services.MusicService;

/**
 * Created by Victor on 10/1/2016.
 */

public class RemoteControlReciever extends BroadcastReceiver {
    static final long CLICK_DELAY = 600;
    static long lastClick = 0; // oldValue
    static long currentClick = System.currentTimeMillis();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {

            KeyEvent event = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);

            if (event == null)
                return;

            final int keycode = event.getKeyCode();
           // final int action = event.getAction();
           // final long eventtime = event.getEventTime();

           String command = null;
            switch (keycode) {
                case KeyEvent.KEYCODE_MEDIA_STOP:
                   // Log.e("RemoteControl", "KEYCODE_MEDIA_STOP" );
                    command = "yes";

                    break;
                case KeyEvent.KEYCODE_HEADSETHOOK:
                    command = "yes";
                    //Log.e("RemoteControl", "KEYCODE_MEDIA_PLAY_HEADSETHOOK" );
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    //Log.e("RemoteControl", "KEYCODE_MEDIA_PLAY_PAUSE" );
                    command = "yes";
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    //Log.e("RemoteControl", "KEYCODE_MEDIA_NEXT" );
                    command = "yes";
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    Log.e("RemoteControl", "KEYCODE_MEDIA_PREVIOUS" );
                    command = "yes";
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                   // Log.e("RemoteControl", "KEYCODE_MEDIA_PAUSE" );
                    command = "yes";
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    //Log.e("RemoteControl", "KEYCODE_MEDIA_PLAY" );
                    command = "yes";
                    break;
//                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
//                        lastClick = currentClick;
//                        currentClick = System.currentTimeMillis();
//                        if (currentClick - lastClick < CLICK_DELAY) {
//                            //This is double click
//                            context.sendBroadcast(new Intent(MusicService.ACTION_NEXT_SONG));
//                        } else {
//                            //This is single click
//                            context.sendBroadcast(new Intent(MusicService.ACTION_PAUSE_SONG));
//
//                        }
//                    }

            }
            if(command!=null){
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    lastClick = currentClick;
                    currentClick = System.currentTimeMillis();
                    if (currentClick - lastClick < CLICK_DELAY) {
                        //This is double click
                        context.sendBroadcast(new Intent(MusicService.ACTION_NEXT_SONG));
                    } else {
                        //This is single click
                        context.sendBroadcast(new Intent(MusicService.ACTION_PAUSE_SONG));

                    }
                }
            }




        }
    }
}
