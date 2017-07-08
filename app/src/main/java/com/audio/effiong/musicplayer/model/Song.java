package com.audio.effiong.musicplayer.model;

import android.net.Uri;

/**
 * Created by Effiong on 02-Dec-15.
 */
public class Song {
    private long id;
    private String title;
    private String artist;
    private  long songDuration;
   // private String albumPath;
    Uri uri = null;
    long albumID;
     //String data;

    public Song(long songID, String songTitle, String songArtist, long duration, Uri albulmUri, long album_id) {
        id=songID;
        title=songTitle;
        artist=songArtist;
        songDuration = duration;
       // albumPath = albumpath;
        uri = albulmUri;
        albumID = album_id;
        //data =dataS;

    }
   /* public String getData(){
        return data;
    }*/
    public long getAlbumID(){
        return albumID;
    }
    public long getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public long getSongDuration(){
        return songDuration;
    }
//    public String getAlbumSongPath(){
//        return albumPath;
//    }
    public Uri getUri(){
        return uri;
    }
   /* public static void removeSongs(ArrayList<Song> thesongs){
        ArrayList<String> items = new ArrayList<String>();
        for(Song song: thesongs){

            String currentArtist = song.getArtist();
            String currentTitles  = song.getTitle();
             items.add(currentArtist);
            items.add(currentTitles);

        }
          items.clear();
    }*/
}
