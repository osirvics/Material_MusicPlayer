package com.audio.effiong.musicplayer.utility;

/**
 * Created by Victor on 7/10/2016.
 */

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.widget.RemoteViews;

import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.activities.MainActivity;
import com.audio.effiong.musicplayer.model.SongModel;
import com.audio.effiong.musicplayer.services.MusicService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;


/**
 * Created by architjn on 15/12/15.
 */
public class NotificationHandler {

    private static final int NOTIFICATION_ID = 52;
    private Context context;
    private MusicService service;
    private boolean notify = false;
    private Notification notificationCompat;
    private NotificationManagerCompat notificationManager;
    //private static boolean supportBigNotifications = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    public NotificationHandler(Context context, MusicService service) {
        this.context = context;
        this.service = service;
    }

    public void setNotificationPlayer(boolean removable) {

    }


    public void changeNotificationDetails(String songName, String artistName, String albumName, long albumId, boolean playing, SongModel model) {


        final RemoteViews notiLayoutBig = new RemoteViews(context.getPackageName(),
                R.layout.player_big_notification);
        final RemoteViews notiCollapsedView = new RemoteViews(context.getPackageName(),
                R.layout.player_small_notification);

        notiLayoutBig.setTextViewText(R.id.player_song_name, songName);
        notiLayoutBig.setTextViewText(R.id.player_author_name, artistName);
        notiLayoutBig.setTextViewText(R.id.player_albumname, albumName);
        notiCollapsedView.setTextViewText(R.id.player_song_name, songName);
        notiCollapsedView.setTextViewText(R.id.player_author_name, artistName);
        Intent playClick = new Intent();
        playClick.setAction(MusicService.ACTION_PAUSE_SONG);
        PendingIntent playClickIntent = PendingIntent.getBroadcast(context, 21021, playClick,  PendingIntent.FLAG_UPDATE_CURRENT);
        notiLayoutBig.setOnClickPendingIntent(R.id.player_play, playClickIntent);
        notiCollapsedView.setOnClickPendingIntent(R.id.player_play, playClickIntent);
        Intent prevClick = new Intent();
        prevClick.setAction(MusicService.ACTION_PREV_SONG);
        PendingIntent prevClickIntent = PendingIntent.getBroadcast(context, 21121, prevClick,  PendingIntent.FLAG_UPDATE_CURRENT);
        notiLayoutBig.setOnClickPendingIntent(R.id.player_previous, prevClickIntent);
        notiCollapsedView.setOnClickPendingIntent(R.id.player_previous, prevClickIntent);
        Intent nextClick = new Intent();
        nextClick.setAction(MusicService.ACTION_NEXT_SONG);
        PendingIntent nextClickIntent = PendingIntent.getBroadcast(context, 21221, nextClick,  PendingIntent.FLAG_UPDATE_CURRENT);
        notiLayoutBig.setOnClickPendingIntent(R.id.player_next, nextClickIntent);
        notiCollapsedView.setOnClickPendingIntent(R.id.player_next, nextClickIntent);
        Intent stopClick = new Intent();
        stopClick.setAction(MusicService.ACTION_ADD_QUEUE);
        PendingIntent stopClickIntent = PendingIntent.getBroadcast(context, 21321, stopClick,  PendingIntent.FLAG_UPDATE_CURRENT);
        notiLayoutBig.setOnClickPendingIntent(R.id.player_close, stopClickIntent);
        notiCollapsedView.setOnClickPendingIntent(R.id.player_close, stopClickIntent);

        //path = ListSongs.getAlbumArtUri(albumId).toString();

        int playStateRes;
        if (playing)
            playStateRes = R.drawable.ic_action_pause;
        else
            playStateRes = R.drawable.ic_action_play;
        notiLayoutBig
                .setImageViewResource(R.id.player_play, playStateRes);
        notiCollapsedView
                .setImageViewResource(R.id.player_play, playStateRes);
        String path = ListSongs.getAlbumArt(context,albumId);

        if(path==null){
            Bitmap largeIcon = BitmapFactory.decodeResource(this.context.getResources(), R.drawable.album_art);
            notiLayoutBig.setImageViewBitmap(R.id.player_album_art,largeIcon);
            notiCollapsedView.setImageViewBitmap(R.id.player_album_art,largeIcon);
            notiLayoutBig.setInt(R.id.main_background, "setBackgroundColor", ContextCompat.getColor(context,R.color.coloorPrimary));
            notiCollapsedView.setInt(R.id.main_background, "setBackgroundColor", ContextCompat.getColor(context,R.color.coloorPrimary));

        }







//        Intent notificationIntent = new Intent();
//        notificationIntent.setAction(MusicService.ACTION_NOTI_CLICK);
//        PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);

        Intent deleteIntent = new Intent();
        deleteIntent.setAction(MusicService.ACTION_NOTI_REMOVE);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent clickIntent = PendingIntent.getActivity(this.context, 0, new Intent(this.context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);

        android.support.v4.app.NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_audiotrack_white_24dp)
                .setContentIntent(clickIntent)
                .setDeleteIntent(deletePendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .setCustomContentView(notiCollapsedView)
                .setCustomBigContentView(notiLayoutBig)
                .setWhen(System.currentTimeMillis())
                .setOngoing(true);
        notificationCompat =  builder.build();
        notificationCompat.flags |= Notification.FLAG_ONGOING_EVENT;

        notificationManager = NotificationManagerCompat.from(MusicActivity.applicationContext);
        service.startForeground(NOTIFICATION_ID, notificationCompat);
        notificationManager.notify(NOTIFICATION_ID, notificationCompat);

        //if(notify){
        if(path!=null){
            Glide.with(MusicActivity.applicationContext).load(path).asBitmap().dontAnimate().into(new SimpleTarget<Bitmap>(300,300) {
                @Override
                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                    notiLayoutBig.setImageViewBitmap(R.id.player_album_art,resource);
                    notiCollapsedView.setImageViewBitmap(R.id.player_album_art,resource);
                    //if(!notify){
                    notificationManager.notify(NOTIFICATION_ID, notificationCompat);
                    // }
                    //notify = false;
                    if(Helpers.isNotNougart()){
                        Palette.from(resource)
                                .generate(new Palette.PaletteAsyncListener() {
                                    @Override
                                    public void onGenerated(Palette palette) {
                                        final int[] colors = Helpers.getAvailableColor(context,palette);
                                        int colorTop = colors[0];
                                        notiLayoutBig.setInt(R.id.main_background, "setBackgroundColor", colorTop);
                                        notiCollapsedView.setInt(R.id.main_background, "setBackgroundColor", colorTop);
                                        // if(!notifyPallete){
                                        notificationManager.notify(NOTIFICATION_ID, notificationCompat);
                                        if(!notify)
                                            cancelNotification();

                                    }
                                });
                    }
                }
            });
        }
    }


    public void cancelNotification() {
        service.stopForeground(true);
        notificationManager.cancel(NOTIFICATION_ID);
    }


    public void setNotifyPallete(boolean notificationActive) {
        this.notify = notificationActive;
    }
}
