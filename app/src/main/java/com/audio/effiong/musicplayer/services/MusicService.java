package com.audio.effiong.musicplayer.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.audio.effiong.musicplayer.activities.FolderActivity;
import com.audio.effiong.musicplayer.activities.MainActivity;
import com.audio.effiong.musicplayer.dbhandler.FavoritePlayTableHelper;
import com.audio.effiong.musicplayer.fragments.PlaybackFragment;
import com.audio.effiong.musicplayer.interfaces.PlaylistPlayback;
import com.audio.effiong.musicplayer.model.SongModel;
import com.audio.effiong.musicplayer.recievers.RemoteControlReciever;
import com.audio.effiong.musicplayer.utility.LastAddedLoader;
import com.audio.effiong.musicplayer.utility.ListSongs;
import com.audio.effiong.musicplayer.utility.MusicActivity;
import com.audio.effiong.musicplayer.utility.MusicPreference;
import com.audio.effiong.musicplayer.utility.NotificationHandler;
import com.audio.effiong.musicplayer.utility.PlaylistSongLoader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MusicService extends Service implements
        MediaPlayer.OnErrorListener {
    public static final String ACTION_PLAY_SINGLE = "com.audio.effiong.musicplayer.edu.ACTION_PLAY_SINGLE";
    public static final String ACTION_PLAY_ALL_SONGS = "com.audio.effiong.musicplayer.edu.ACTION_PLAY_ALL_SONGS";
    public static final String ACTION_PLAY_ALBUM = "com.audio.effiong.musicplayer.edu.ACTION_PLAY_ALBUM";
    public static final String ACTION_PLAY_PLAYLIST = "com.audio.effiong.musicplayer.edu.ACTION_PLAY_PLAYLIST";
    public static final String ACTION_PLAY_ARTIST = "com.audio.effiong.musicplayer.edu.ACTION_PLAY_ARTIST";
    public static final String ACTION_GET_SONG = "com.audio.effiong.musicplayer.edu.ACTION_GET_SONG";
    public static final String ACTION_NOTI_CLICK = "com.audio.effiong.musicplayer.edu.ACTION_NOTI_CLICK";
    public static final String ACTION_NOTI_REMOVE = "com.audio.effiong.musicplayer.edu.ACTION_NOTI_REMOVE";
    public static final String ACTION_CHANGE_SONG = "com.audio.effiong.musicplayer.edu.ACTION_CHANGE_SONG";
    public static final String ACTION_SEEK_SONG = "com.audio.effiong.musicplayer.edu.ACTION_SEEK_SONG";
    public static final String ACTION_NEXT_SONG = "com.audio.effiong.musicplayer.edu.ACTION_NEXT_SONG";
    public static final String ACTION_PREV_SONG = "com.audio.effiong.musicplayer.edu.ACTION_PREV_SONG";
    public static final String ACTION_PAUSE_SONG = "com.audio.effiong.musicplayer.edu.ACTION_PAUSE_SONG";
    public static final String ACTION_ADD_QUEUE = "com.audio.effiong.musicplayer.edu.ACTION_ADD_QUEUE";
    public static final String ACTION_SHUFFLE_SONGS = "com.audio.effiong.musicplayer.edu.ACTION_SHUFFLE_SONGS";
    public static final String ACTION_PLAY_FAVOURITE = "com.audio.effiong.musicplayer.edu.ACTION_PLAY_FAVOURITE";
    public static final String ACTION_PLAY_RECENT = "com.audio.effiong.musicplayer.edu.ACTION_PLAY_RECENT";
    public static final String ACTION_PLAY_FOLDER = "com.audio.effiong.musicplayer.edu.ACTION_PLAY_FOLDER";
    public static final float VOLUME_DUCK = 0.2f;
    public static final float VOLUME_NORMAL = 1.0f;
    private AudioManager audioManager;
    private boolean pausedTransient = false;
    private int playlistType;
    public MediaPlayer player;
    public ArrayList<SongModel> songs;
    private final String LOG_TAG = "MusicService";
    private int songPosn;
    private int position;
    boolean isReady =false;
    ArrayList<Integer> list;
    ArrayList<SongModel> shuffledPlaylist;
    public int checkplay = 1;
    boolean shufflingPlay = false;
    public  boolean pausedByUser = false;
    public boolean prepared = false;
    private static MusicService mInstance = null;
    private final IBinder musicBind = new MusicBinder();
    public boolean isShuffle = false;
    public  ArrayList<SongModel> playList;
    public boolean isLastSaved = false;
    boolean fromUser;
    private NotificationHandler notificationHandler;
    Context context;
    public static boolean serviceRunning = false;
    int type = 1;
    private ComponentName mMediaButtonReceiverComponent;
    private int repeatMode = 3;
    private boolean isShuffleEnabled = false;
    PlaylistPlayback callback;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        isReady=false;
        return true;
    }
    private BroadcastReceiver playerServiceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                handleBroadcastReceived(context, intent);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY_SINGLE);
        filter.addAction(ACTION_PLAY_ALL_SONGS);
        filter.addAction(ACTION_PLAY_ALBUM);
        filter.addAction(ACTION_GET_SONG);
        filter.addAction(ACTION_NEXT_SONG);
        filter.addAction(ACTION_PREV_SONG);
        filter.addAction(ACTION_PAUSE_SONG);
        filter.addAction(ACTION_SEEK_SONG);
        filter.addAction(ACTION_CHANGE_SONG);
        filter.addAction(ACTION_PLAY_PLAYLIST);
        filter.addAction(ACTION_PLAY_ARTIST);
        filter.addAction(ACTION_NOTI_CLICK);
        filter.addAction(ACTION_NOTI_REMOVE);
        filter.addAction(ACTION_ADD_QUEUE);
        filter.addAction(ACTION_SHUFFLE_SONGS);
        filter.addAction(ACTION_PLAY_FAVOURITE);
        filter.addAction(ACTION_PLAY_RECENT);
        filter.addAction(ACTION_PLAY_FOLDER);
        this.getApplicationContext().registerReceiver(playerServiceBroadcastReceiver, filter);
        notificationHandler = new NotificationHandler(this, this);
        isShuffle = true;
        return START_STICKY;
    }

    private void handleBroadcastReceived(Context context, final Intent intent) throws IOException {
        switch (intent.getAction()) {
            case ACTION_PLAY_SINGLE:
                isLastSaved = false;
                fromUser = true;
                songPosn= 0;
                shufflingPlay = false;
                playSong();
                MusicPreference.saveLastSongListType(this,6);
                long songID = intent.getLongExtra("songPos", 0);
                MusicPreference.setSongId(getApplicationContext(),songID);
                break;
            case ACTION_PLAY_RECENT:
                break;
            case ACTION_PLAY_FAVOURITE:
                isLastSaved = false;
                fromUser = true;
                songPosn= intent.getIntExtra("songPos", 0);
                shufflingPlay = false;
                playSong();
                MusicPreference.saveLastSongListType(this,4);
                Log.e("Playback", "Playing favorite");
                break;
            case ACTION_PLAY_FOLDER:
                isLastSaved = false;
                fromUser = true;
                songPosn= intent.getIntExtra("songPos", 0);
                shufflingPlay = false;
                playSong();
                String path = intent.getStringExtra("file");
                MusicPreference.setFolderPath(this,path);
                MusicPreference.saveLastSongListType(this,7);
                Log.e("Playback", "Playing Folder");
                break;
            case ACTION_PLAY_ALL_SONGS:
                isLastSaved = false;
                fromUser = true;
                songPosn= intent.getIntExtra("songPos", 0);
                shufflingPlay = false;
                playSong();
                MusicPreference.saveLastSongListType(this,1);
                Log.e("Playback", "Playing Songs");
                break;
            case ACTION_PLAY_ALBUM:
                isLastSaved = false;
                fromUser = true;
                songPosn= intent.getIntExtra("songPos", 0);
                shufflingPlay = false;
                playSong();
                MusicPreference.saveLastSongListType(this,2);
                long albumId = intent.getLongExtra("albumId",0);
                MusicPreference.saveLastAlbID(this,albumId);
                Log.e("Playback", "Playing Album");
                break;
            case ACTION_GET_SONG:
                //Toast.makeText(this,"SENDING UPADTE", Toast.LENGTH_SHORT).show();
               if(!MusicPreference.isFirstRun(this))
                updatePlayback();
                updateSeek();
                break;
            case ACTION_NEXT_SONG:
                isLastSaved = false;
                playNext();

                break;
            case ACTION_PREV_SONG:
                isLastSaved = false;
                playPrev();
                break;
            case ACTION_PAUSE_SONG:
                try {
                    togglePlayPause(notificationHandler);
                    updatePlayback();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case ACTION_SEEK_SONG:
                seek(intent.getIntExtra("seek", 0));
                break;
            case ACTION_CHANGE_SONG:
                break;
            case ACTION_PLAY_PLAYLIST:
                isLastSaved = false;
                fromUser = true;
                songPosn= intent.getIntExtra("songPos", 0);
                shufflingPlay = false;
                playSong();
                MusicPreference.saveLastSongListType(this,5);
                long playlistId = intent.getLongExtra("playlistID",-11);
                MusicPreference.saveLastAlbID(this,playlistId);
                Log.e("Playback", "Playing Playlist");
                break;
            case ACTION_PLAY_ARTIST:
                fromUser = true;
                songPosn= intent.getIntExtra("songPos", 0);
                shufflingPlay = false;
                playSong();
                String name = intent.getStringExtra("artistName");
                MusicPreference.saveLastSongListType(this,3);
                MusicPreference.saveLastArtist(this,name);
                Log.e("Playback", "Playing Artist");
                break;
            case ACTION_SHUFFLE_SONGS:
                isLastSaved = false;
                songPosn = 0;
                playSong();
                MusicPreference.saveLastSongListType(this,1);
                break;
            case ACTION_NOTI_CLICK:
                break;
            case ACTION_NOTI_REMOVE:
                break;
            case ACTION_ADD_QUEUE:
                if(MainActivity.mainActivityRuning){
                    if (!isPlaying()){
                        notificationHandler.setNotifyPallete(false);
                        stopForeground(true);
                        notificationHandler.cancelNotification();
                    }
                    else if(isPlaying()){
                        tempStopNoti(notificationHandler);
                        notificationHandler.setNotifyPallete(false);
                        stopForeground(true);
                        notificationHandler.cancelNotification();
                    }
                }else {
                    notificationHandler.setNotifyPallete(false);
                    stopForeground(true);
                    notificationHandler.cancelNotification();
                    shutDown();
                }
                break;
        }
    }


    public static String getPlayedTyped(int type){

        switch (type){
            case 1:
                return  ACTION_PLAY_ALL_SONGS;
            case 2:
               return   ACTION_PLAY_ALBUM;
            case 3:
                return  ACTION_PLAY_ARTIST;
            case 4:
                return  ACTION_PLAY_FAVOURITE;
            case 5:
                return  ACTION_PLAY_PLAYLIST;
            case 6:
                return  ACTION_PLAY_SINGLE;
            case 7:
                return   ACTION_PLAY_FOLDER;
            default:
                break;
        }
        return MusicService.ACTION_PLAY_ALL_SONGS;
    }

    public void getAudioFocus(){
        int result = audioManager.requestAudioFocus(afChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);
    }
    public void updatePlayback() {
        Intent i = new Intent();
        i.setAction(PlaybackFragment.ACTION_RECIEVE_SONG);
        if(songs!=null && getCurrentPlayingSong()!=null){
            if(!songs.isEmpty() && getCurrentPlayingSong()!=null){
                i.putExtra("artistName", getCurrentPlayingSong().getArtist());
                i.putExtra("songName", getCurrentPlayingSong().getName());
                i.putExtra("albumId", getCurrentPlayingSong().getAlbumId());
                i.putExtra("albumName", getCurrentPlayingSong().getAlbumName());
            }
        }
        sendBroadcast(i);
    }
    private void updateNotificationPlayer() {
        notificationHandler.setNotifyPallete(true);
        if(getCurrentPlayingSong()!=null){
            notificationHandler.changeNotificationDetails(
                    getCurrentPlayingSong().getName(),
                    getCurrentPlayingSong().getArtist(),getCurrentPlayingSong().getAlbumName(),
                    getCurrentPlayingSong().getAlbumId(),
                    getMediaPlayer().isPlaying(), getCurrentPlayingSong());
        }
    }


    private void updateNotiPlayerLossTrans() {
        notificationHandler.setNotifyPallete(true);
        notificationHandler.changeNotificationDetails(
                getCurrentPlayingSong().getName(),
                getCurrentPlayingSong().getArtist(),getCurrentPlayingSong().getAlbumName(),
                getCurrentPlayingSong().getAlbumId(),
                false, getCurrentPlayingSong());
    }

    public void updateSeek(){
        if(!songs.isEmpty()){
            Intent update = new Intent();
            update.setAction(PlaybackFragment.ACTION_UPDATE_SEEK);
            sendBroadcast(update);
        }
    }
    public void setPlayState(){
        Intent state = new Intent();
        state.setAction(PlaybackFragment.ACTION_CHANGE_PLAYSTATE);
        sendBroadcast(state);
    }
    public void togglePlayPause(NotificationHandler notificationHandler){
        boolean state;
        if(player.isPlaying() && player!=null ){
            player.pause();
            pausedByUser = true;
            state = false;
            sendBroadcast(new Intent(PlaybackFragment.ACTION_SET_PAUSE));
        }
        else{
            getAudioFocus();
            player.start();
            state = true;
            notificationHandler.setNotificationPlayer(false);
            sendBroadcast(new Intent(PlaybackFragment.ACTION_SET_PLAY));
            updateSeek();
            updateNotificationPlayer();
            registerAudioReciever();
            checkplay = 2;
            pausedByUser = false;
            getAudioEffects();
        }
        notificationHandler.setNotifyPallete(true);
        notificationHandler.changeNotificationDetails(getCurrentPlayingSong().getName(),
                getCurrentPlayingSong().getArtist(),getCurrentPlayingSong().getAlbumName(),
                getCurrentPlayingSong().getAlbumId(), state, getCurrentPlayingSong());
    }

    public void tempStopNoti(NotificationHandler notificationHandler) {
        boolean state;
        player.pause();
        pausedByUser = true;
        state = false;
        sendBroadcast(new Intent(PlaybackFragment.ACTION_SET_PAUSE));
        notificationHandler.setNotifyPallete(true);
        if(songs!=null && getCurrentPlayingSong()!=null){
            notificationHandler.changeNotificationDetails(getCurrentPlayingSong().getName(),
                    getCurrentPlayingSong().getArtist(), getCurrentPlayingSong().getAlbumName(),
                    getCurrentPlayingSong().getAlbumId(), state, getCurrentPlayingSong());
        }

    }



    public void tempStopPlayback(NotificationHandler notificationHandler) {
        boolean state;
        pausedByUser = true;
        state = false;
        sendBroadcast(new Intent(PlaybackFragment.ACTION_SET_PAUSE));
        notificationHandler.setNotifyPallete(true);
        if(songs!=null && getCurrentPlayingSong()!=null){
            notificationHandler.changeNotificationDetails(getCurrentPlayingSong().getName(),
                    getCurrentPlayingSong().getArtist(), getCurrentPlayingSong().getAlbumName(),
                    getCurrentPlayingSong().getAlbumId(), state, getCurrentPlayingSong());
        }
    }


    public void tempStopAudioNoisy() {
        player.pause();
        pausedByUser = true;
        sendBroadcast(new Intent(PlaybackFragment.ACTION_SET_PAUSE));
        notificationHandler.setNotifyPallete(true);
        notificationHandler.changeNotificationDetails(getCurrentPlayingSong().getName(),
                getCurrentPlayingSong().getArtist(), getCurrentPlayingSong().getAlbumName(),
                getCurrentPlayingSong().getAlbumId(), false, getCurrentPlayingSong());
    }


    public class MusicBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mInstance = this;
        serviceRunning = true;
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        mMediaButtonReceiverComponent = new ComponentName(getPackageName(),
                RemoteControlReciever.class.getName());
        registerAudioReciever();
        playlistType = 0;
        fromUser = MusicPreference.getFromUser(this);
        songPosn=0;
        shuffledPlaylist = new ArrayList<>();
        playList = new ArrayList<>();
        player = new MediaPlayer();
        initMusicPlayer();
        if(!MusicPreference.isFirstRun(this)){
            if(playList==null || playList.isEmpty()){
              // Toast.makeText(this,"Loading saved songs", Toast.LENGTH_SHORT).show();
                loadLastSaved();
            }
        }
        updateRepeat();
        updateShuffle();
    }

    public void updateRepeat() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                if(MusicPreference.isRepeatAllEnabled(getApplicationContext())){
                    repeatMode =2;
                }
                else if(MusicPreference.isRepeatOneEnabled(getApplicationContext())){
                    repeatMode = 1;
                }
                else {
                    repeatMode =3;
                }
                return null;
            }

        }.execute();
    }

    public void updateShuffle(){
        if(MusicPreference.isShuffleEnabled(this))
            isShuffleEnabled = true;
        else isShuffleEnabled = false;

    }

    @Override
    public void onDestroy() {
        shutDown();
    }
    private final AudioManager.OnAudioFocusChangeListener afChangeListener =  new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(final int focusChange) {
            try {
                if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    if(player!=null){
                        if(player.isPlaying()){
                            player.pause();
                            pausedTransient = true;
                            sendBroadcast(new Intent(PlaybackFragment.ACTION_SET_PAUSE));
                            updateNotiPlayerLossTrans();
                        }
                    }
                }
                else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                    // Lower the volume
                    if(player!=null){
                        if(player.isPlaying())
                            player.setVolume(VOLUME_DUCK,VOLUME_DUCK);
                    }

                } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    // Raise volume back to normal
                    if(player!=null){
                        player.setVolume(VOLUME_NORMAL,VOLUME_NORMAL);
                        if(!player.isPlaying() && pausedTransient &&!pausedByUser){
                            player.start();
                            sendBroadcast(new Intent(PlaybackFragment.ACTION_SET_PLAY));
                            updateNotificationPlayer();
                            registerAudioReciever();
                            pausedTransient = false;
                        }
                    }

                }
                else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                    // Stop playback

                    try {
                        if(player!=null){
                            audioManager.abandonAudioFocus(afChangeListener);
                            player.pause();
                            sendBroadcast(new Intent(PlaybackFragment.ACTION_SET_PAUSE));
                            updateNotiPlayerLossTrans();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }


        }
    };

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnErrorListener(this);
    }
    public void setList(ArrayList<SongModel> theSongs){
        songs = theSongs;
        PlaybackFragment.setPlaylist(theSongs);
        shuffleIndex(theSongs);

        Intent intent = new Intent(PlaybackFragment.ACTION_SHOW_PLAYBACK_PLAYLIST);
        sendBroadcast(intent);
    }
    public void setListShuffle(ArrayList<SongModel> theSongs){
        shufflingPlay = true;
        songs = theSongs;
        shuffleIndex(theSongs);
    }
    public void shuffleIndex(ArrayList<SongModel> playlist){
        list = new ArrayList<>(playlist.size());
        for(int i =0; i < playlist.size();i++){
            list.add(i);
        }

        //,new Random(System.nanoTime())
        Collections.shuffle(list,new Random(System.nanoTime()));

    }
    public void playSongLast(){
        prepared = true;
        player.reset();
        if ( songs.size()>0){
            long currSong = 0;
            SongModel playSong = songs.get(songPosn);
            currSong = playSong.getSongId();
            Uri base = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Uri trackUri = ContentUris.withAppendedId(base, currSong);
            try{
                // player.setAudioSessionId(getAudioSessionId());
                player.setDataSource(getApplicationContext(), trackUri);
            }
            catch(Exception e){
                e.printStackTrace();
                ////Log.e("MUSIC SERVICE", "Error setting data source", e);
            }
            try{
                player.prepare();
            }
            catch(Exception ee)
            {
                ////Log.e("MUSIC SEkRVICE", "Error setting data source", ee);
                Toast.makeText(getApplicationContext(), "Song corrupt or not supported", Toast.LENGTH_SHORT).show();
                ee.printStackTrace();
                isReady = false;
            }
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    isReady = true;
                    setPlayState();
                    tempStopPlayback(notificationHandler);
                }
            });
        }
    }
    public void playSong(){
        prepared = true;
        player.reset();
        //Log.e(LOG_TAG,"songsPsn>> index of play: "+ songPosn);
        if ( songs.size()>=0){
            long currSong = 0;
            if(shufflingPlay){
                position = list.get(songPosn);
                currSong = songs.get(position).getSongId();
                //Log.e(LOG_TAG,"SHUFFLE PLAY SONGS>> Position of play: "+ position);
            }
            else if(fromUser){
                currSong = songs.get(songPosn).getSongId();
                position = songPosn;
               // Log.e(LOG_TAG,"USING FROM USER>> Position of play: "+ position);
            }
            else{
                if(isShuffleEnabled)
                {
                    if(isLastSaved){
                        position = songPosn;
                        currSong = songs.get(position).getSongId();
                    }
                    else {
                        position = list.get(songPosn);
                        currSong = songs.get(position).getSongId();
                       // Log.e(LOG_TAG,"SHUFFLE PLAY>> Position of play: "+ position);
                    }
                }
                else {
                    position = songPosn;
                    currSong = songs.get(position).getSongId();
                   // Log.e(LOG_TAG,"NORMAL PLAY>> Position of play: "+ position);
                }
            }
            configuirePlayback(currSong);
        }
    }
    public void configuirePlayback(long id){
        // Append the external URI with our songs'
        Uri base = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri trackUri = ContentUris.withAppendedId(base, id);
        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            player.prepare();
        }
        catch(Exception ee)
        {
            ee.printStackTrace();
            ////Log.e("MUSIC SEkRVICE", "Error setting data source", ee);
            Toast.makeText(getApplicationContext(), "Song corrupt or not supported", Toast.LENGTH_SHORT).show();
            ee.printStackTrace();
            isReady = false;
        }
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isReady = true;
                if(isLastSaved) {
                    int seekPos = MusicPreference.getLastProgress(getApplicationContext());
                    if (player != null) {
                        if (seekPos < 0) {
                            seekPos = 0;
                        }
                        else if (isReady){
                            if(!songs.isEmpty()){
                                if (seekPos > player.getDuration()) {
                                    seekPos = player.getDuration();
                                }
                                player.seekTo(seekPos);
                                updateSeek();
                            }
                        }
                        isLastSaved =false;
                    }
                }
                else{
                    setPlayState();
                    getAudioFocus();
                    mp.start();
                    registerAudioReciever();
                    updateNotificationPlayer();
                    updatePlayback();
                    updateSeek();
                    openPanel();
                    getAudioEffects();
                }
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        isReady = false;
                        fromUser = false;
                        mp.reset();
                        if(repeatMode==2){
                            playNext();
                        }
                        else if(repeatMode==1){
                            playNextOne();
                        }
                        else if(repeatMode==3){
                            playNextNoRepeat();
                        }
                    }
                });
            }
        });
    }

    public void loadLastSaved(){
        new loadSavedSongs().execute("");
    }

    private class loadSavedSongs extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            type = MusicPreference.getLastSongListType(getApplicationContext());
            long albumId = MusicPreference.getLastAlbID(getApplicationContext());
            String directory = MusicPreference.getFolderPath(getApplicationContext());
            File file = new File(directory);
            String artName = MusicPreference.getLastArtist(getApplicationContext());
            long songId = MusicPreference.getSongId(getApplicationContext());
            setList(getLastPlaylist(type,albumId,artName,songId,file));

            updatePlayback();
            updateSeek();
            return "Executed";
        }
        @Override
        protected void onPostExecute(String result) {
            int pos = MusicPreference.getLastPosition(getApplicationContext());
            setPosition(pos);
            isLastSaved = true;
            try {
                playSong();
                updatePlayback();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        @Override
        protected void onPreExecute() {
        }
    }

    public ArrayList<SongModel> getLastPlaylist(int type, long albumid, String artistName, long songId,File file){
        switch (type){
            case 1:
                playList = ListSongs.getSongListSOrt(getApplicationContext());
                break;
            case 2:
                playList = ListSongs.getAlbumSongList(getApplicationContext(),albumid);
                break;
            case 3:
                playList =  ListSongs.getSongsListOfArtist(getApplicationContext(),artistName);
                break;
            case 4:
                Cursor cursor =  FavoritePlayTableHelper.getInstance(this).getFavoriteSongList();
                playList =  ListSongs.getSongsFromSQLDBCursor(cursor);
                break;
            case 5:
                if(albumid == -11){
                    playList = LastAddedLoader.getLastAddedSongs(getApplicationContext());
                }
                else
                    playList = PlaylistSongLoader.getSongsInPlaylist(getApplicationContext(), albumid);
                break;
            case 6:
                SongModel song = ListSongs.getSong(getApplicationContext(),songId);
                playList.add(song);
                break;
            case 7:
                if(file!=null && file.canRead())
                    playList = FolderActivity.getSongsInDirectory(getApplicationContext(),file,"f");
                else playList = ListSongs.getSongListSOrt(getApplicationContext());
                break;
            default:
                break;
        }
        return playList;
    }
    public void playPrev(){
        if((player.getCurrentPosition() / 1000) <= 2){
            songPosn--;
            if (songPosn < 0) songPosn = songs.size() - 1;
            playSong();
        }
        else{
            int position;
            position = songPosn;
            songPosn = position;
            playSong();
        }
    }
    public void  playNextNoRepeat(){
        songPosn++;
        if(songPosn >= songs.size()){
            songPosn = songs.size()-1;
            playSongLast();
        }
        else{
            fromUser = false;
            playSong();
        }
    }

    public void playNextOne(){
        int pos = songPosn;
        songPosn = pos;
        fromUser = false;
        playSong();
    }

    public void playNext() {
        songPosn++;
        if (songPosn >= songs.size()) {
            songPosn = 0;
        }
        fromUser = false;
        playSong();
        //list.remove(4);

    }

    public int getCurrentSongIndex() {
        //returns the last index of shuffled index play
        if(MusicPreference.isShuffleEnabled(this)&&!fromUser){
            return position;
        }
        // returns shuffling all play position when service gets destroyed
        else if(shufflingPlay){
            return position;
        }
        // returns normal play index
        return songPosn;
    }

    public void setSong(int songIndex){
        songPosn=songIndex;
    }
    public SongModel getCurrentPlayingSong() {
        if(!songs.isEmpty() && songs !=null ){
            if(position >=songs.size()){
                return null;
            }
            else
                return songs.get(position);
        }
        else  return null;
    }
    public void setPosition(int pos){
        songPosn = pos;
    }
    public boolean isPlaying() {
        if (player != null) {
            try {
                return player.isPlaying();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }
        return false;
    }
    public static MusicService getInstance() {
        return mInstance;
    }
    public int getPosition(){
        return player.getCurrentPosition();
    }
    public int getDur(){
        if(isReady){
            return player.getDuration();
        }
        return -1;
    }
    public void seek(int pos){
        player.seekTo(pos);
    }
    public boolean ismMediaPlayerPrepared(){
        return isReady;
    }
    public MediaPlayer getMediaPlayer(){
        return this.player;
    }
    public void shutDown(){
        try {
            int pos = getCurrentSongIndex();
            MusicPreference.saveLastPosition(this, pos);
            int progress = getPosition();
            MusicPreference.setFromUser(this,fromUser);
            MusicPreference.saveLastProgress(this, progress);
            this.getApplicationContext().unregisterReceiver(playerServiceBroadcastReceiver);
            releaseAudioEffects();
        } catch (Exception e) {
            e.printStackTrace();
        }
        serviceRunning = false;
        checkplay =1;
        if (player != null) {
            try {
                player.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                player.reset();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                player.release();
                player = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Intent i = new Intent(MusicActivity.applicationContext,MusicService.class);
        stopService(i);
        stopSelf();
    }

    public void openPanel(){
        boolean firstRun = MusicPreference.isHidden(getApplicationContext());
        if(firstRun){
            sendBroadcast(new Intent(PlaybackFragment.ACTION_SHOW_PANEL));
            MusicPreference.setHidden(getApplicationContext(),false);
            MusicPreference.iSFirstRun(getApplicationContext(),false);
        }
    }


    public int getAudioSessionId() {
        synchronized (this) {
            if(player!=null)
                try {
                    return player.getAudioSessionId();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return -1;
    }

    private void getAudioEffects() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
                try {
                    intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
                sendBroadcast(intent);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
            }
        }.execute();
    }

    private void releaseAudioEffects() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final Intent audioEffectsIntent = new Intent(
                        AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
                try {
                    audioEffectsIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
                audioEffectsIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
                sendBroadcast(audioEffectsIntent);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
            }
        }.execute();
    }

    public void setPlaylistType(int type){
        playlistType = type;
    }

    public int getPlaylistType(){
        return playlistType;
    }



    public void registerAudioReciever(){
        audioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);
    }
    public void unregisterAudioReciever(){
        audioManager.unregisterMediaButtonEventReceiver
                (new ComponentName(getApplicationContext(), RemoteControlReciever.class));
    }

    public void setPlaylistListener( PlaylistPlayback playback) {
        callback = playback;
    }

}
