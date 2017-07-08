package com.audio.effiong.musicplayer.model;

/**
 * Created by Victor on 5/15/2016.
 */
public class SongModel {

    private long songId, albumId, dateAdded;
    private String name;
    private String artist;
    private String path;
    private String albumName;
    private long duration;
    private String trackNumber;
    private boolean fav;
    public float audioProgress = 0.0f;
    public int audioProgressSec = 0;

    public SongModel() {
        super();
    }

    public SongModel(long songId, String name, String artist,
                String path, boolean fav, long albumId,
                String albumName, long dateAdded, long duration, String trackNo) {
        this.songId = songId;
        this.name = name;
        this.artist = artist;
        this.path = path;
        this.fav = fav;
        this.dateAdded = dateAdded;
        this.albumId = albumId;
        this.albumName = albumName;
        this.duration = duration;
        this.trackNumber = trackNo;
    }

    public long getAlbumId() {
        return albumId;
    }

    public long getSongId() {
        return songId;
    }

    public String getArtist() {
        return artist;
    }

    public String getName() {
        return name;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getPath() {
        return path;
    }
    public String getTrackNumber(){
        return trackNumber;
    }

    public boolean isFav() {
        return fav;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public long getDurationLong() {
        return duration;
    }

    public long gettDurationLong() {
        return 97;
    }

    public String getDuration() {
        try {
            Long time = duration;
            long seconds = time / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;

            if (seconds < 10) {
                return String.valueOf(minutes) + ":0" + String.valueOf(seconds);
            } else {
                return String.valueOf(minutes) + ":" + String.valueOf(seconds);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return String.valueOf(0);
        }
    }

    public String getFormatedTime(long duration) {
        try {
            Long time = duration;
            long seconds = time / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;

            if (seconds < 10) {
                return String.valueOf(minutes) + ":0" + String.valueOf(seconds);

            } else {
                return String.valueOf(minutes) + ":" + String.valueOf(seconds);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return String.valueOf(0);
        }
    }

//    public Bitmap getSmallCover(Context context) {
//
//        // ImageLoader.getInstance().getDiskCache().g
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inSampleSize = 1;
//        Bitmap curThumb = null;
//        try {
//            Uri uri = Uri.parse("content://media/external/audio/media/" + getSongId() + "/albumart");
//            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
//            if (pfd != null) {
//                FileDescriptor fd = pfd.getFileDescriptor();
//                curThumb = BitmapFactory.decodeFileDescriptor(fd);
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return curThumb;
//    }
}
