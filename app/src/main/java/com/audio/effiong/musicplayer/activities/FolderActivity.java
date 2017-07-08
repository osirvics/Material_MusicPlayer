package com.audio.effiong.musicplayer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.design.DividerItemDecoration;
import com.audio.effiong.musicplayer.fragments.PlaybackFragment;
import com.audio.effiong.musicplayer.model.SongModel;
import com.audio.effiong.musicplayer.services.MusicService;
import com.audio.effiong.musicplayer.utility.AddPlaylistDialog;
import com.audio.effiong.musicplayer.utility.Helpers;
import com.audio.effiong.musicplayer.utility.ListSongs;
import com.audio.effiong.musicplayer.utility.MusicPreference;
import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;


public class FolderActivity extends BaseThemedActivity implements ATEActivityThemeCustomizer, PlaybackFragment.OnPanelClickListener {
    public static final String  ACTION_CLEAR_PLAYLIST =  "com.audio.effiong.musicplayer.edu.ACTION_CLEAR_PLAYLIST";
    private final static String[] projection = {
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK
    };
    public RecentAdapter mAdapter;
    public boolean scan = true;
    ArrayList<Object> generics;
    FastScrollRecyclerView recyclerview;
    int spacing;
    Toolbar toolbar;
    String ateKey = "";
    ArrayList<SongModel> playList;

    LinearLayout emptyView;
    private File file;
    private ArrayList<String> myList;
    private ArrayList<SongModel> folderSongs;
    private boolean panelOpen = false;
    private long tempSongId;
    private boolean writePermissionAked = false;
    int accentColor;
    InterstitialAd mInterstitialAd;


    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {

                case FolderActivity.ACTION_CLEAR_PLAYLIST:
                    playList.clear();
                    break;
            }
        }
    };

    public static ArrayList<SongModel> getSongsInDirectory(Context c,File directory, String sortingMethod) {
        ArrayList<SongModel> songs = new ArrayList<>();

        String path = directory.getAbsolutePath();
        if(!path.endsWith("/")) path+="/";
        MusicPreference.setFolderPath(c,path);
        String pa = MusicPreference.getFolderPath(c);
        //String where = MediaStore.MediaColumns.DATA + " REGEXP \"" + path + "[^/]+\"";
        String where = MediaStore.MediaColumns.DATA + " LIKE \"" + path + "%\" AND " + MediaStore.MediaColumns.DATA + " NOT LIKE \"" + path + "%/%\"";
        Cursor musicCursor = c.getContentResolver().
                query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, where, null, getSortOrder(sortingMethod));
        int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
        int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int dataColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int albumIdColum = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
        int albumColum = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
        int dateColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);
        int duratiomColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
        int trackColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
        if(musicCursor!=null && musicCursor.moveToFirst()) {
            do {
                String title = musicCursor.getString(titleColumn);
                long songId  = musicCursor.getLong(idColumn);
                String artist = musicCursor.getString(artistColumn);
                String pathUrl = musicCursor.getString(dataColumn);
                long albumId = musicCursor.getLong(albumIdColum);
                String album = musicCursor.getString(albumColum);
                long dateAdded = musicCursor.getLong(dateColumn);
                long duration = musicCursor.getLong(duratiomColumn);
                String trackNumber = musicCursor.getString(trackColumn);
                songs.add(new SongModel(songId,title,artist,pathUrl,false,albumId,album,dateAdded,duration,trackNumber));
            } while (musicCursor.moveToNext());
        }
        musicCursor.close();

        return songs;
    }

    private static String getSortOrder(String sortingMethod) {
        switch (sortingMethod) {
            case "nat":
                return MediaStore.Audio.Media.TRACK+","+MediaStore.Audio.Media.ARTIST+","+MediaStore.Audio.Media.TITLE;
            case "at":
                return MediaStore.Audio.Media.ARTIST+","+MediaStore.Audio.Media.TITLE;
            case "ta":
                return MediaStore.Audio.Media.TITLE+","+MediaStore.Audio.Media.ARTIST;
            case "f":
                return MediaStore.Audio.Media.DATA;
        }
        return null;
    }

    public void configReiever(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(FolderActivity.ACTION_CLEAR_PLAYLIST);
        registerReceiver(br, filter);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            String root_sd = Environment.getExternalStorageDirectory().toString();
            file = new File(root_sd);
            File list[];
            myList.clear();
            generics.clear();
            if(file.canRead() && file!=null){
                list = file.listFiles(new AudioFilter());
                for(File newList: list){
                    myList.add(newList.getName());
                }
                toolbar.setTitle(root_sd);
                folderSongs = getSongsInDirectory(FolderActivity.this,file,"f");
                generics.addAll(myList);
                generics.addAll(folderSongs);
                mAdapter = new RecentAdapter(myList,folderSongs,generics);
                recyclerview.setAdapter(mAdapter);
                setEmptyView(mAdapter);
            }
            mAdapter = new RecentAdapter(myList,folderSongs,generics);
            recyclerview.setAdapter(mAdapter);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        //mInterstitialAd = new InterstitialAd(this);

        // set the ad unit ID
        mInterstitialAd = new InterstitialAd(this);

        // set the ad unit ID
        mInterstitialAd.setAdUnitId("ca-app-pub-7549886159702245/6263303213");

        AdRequest adRequest = new AdRequest.Builder()
                .build();

        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded() {
                showInterstitial();
            }
        });

      /*  mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-7549886159702245/9768499611");

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent ntent = new Intent(getApplication(), SettingsActivity.class);
//                        startActivity(ntent);
//                    }
//                }, 100);
            }
        });*/
        String root_sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(root_sd);
        setSupportActionBar(toolbar);
        emptyView = (LinearLayout)findViewById(R.id.songs_empty_view);
        toolbar.setNavigationIcon(R.drawable.ic_home_black_24dp);
        panelOpen = MusicPreference.isPanelOpen(this);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.panel5_holder);

        if (fragment == null) {
            fragment = PlaybackFragment.newInstance(Helpers.isKitkat());
            fm.beginTransaction()
                    .add(R.id.panel5_holder, fragment)
                    .commit();
        }
        ateKey = Helpers.getATEKey(this);
        accentColor = Config.accentColor(this,ateKey);
        recyclerview = (FastScrollRecyclerView)findViewById(R.id.recyclerview);

        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        recyclerview.setAutoHideEnabled(true);
        recyclerview.setAutoHideDelay(1500);
        recyclerview.setThumbColor(accentColor);
        recyclerview.setPopupBgColor(accentColor);
        recyclerview.setPopupTextColor(ContextCompat.getColor(this,R.color.md_white_1000_75));
        recyclerview.setLayoutManager(new LinearLayoutManager(this));
        myList = new ArrayList<>();
        generics = new ArrayList<>();
        folderSongs = new ArrayList<>();
        playList = new ArrayList<>();
        file = new File( root_sd ) ;
        spacing =  getResources().getDimensionPixelSize(R.dimen.rc_padding_left);
        new StartDirTask(file,"haha").execute();
    }

    /*private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(adRequest);
    }*/

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    @Override
    public int getActivityTheme() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ?
                R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
    }

    @Override
    public void isPanelOpen(boolean flag) {
        panelOpen = flag;
    }

    private void setEmptyView(RecentAdapter adapter){
        if(adapter.getItemCount()==0){
            recyclerview.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        }
        else{
            recyclerview.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void GoToParrentDirTask(String parent, File parentDir) {

        file = new File(parent) ;
        File list[] = file.listFiles(new AudioFilter());
        myList.clear();
        generics.clear();
        for(File file: list)
        {
            myList.add(file.getName());
        }
        folderSongs = getSongsInDirectory(FolderActivity.this,parentDir,"f");
        generics.addAll(myList);
        generics.addAll(folderSongs);
        toolbar.setTitle(parent);
        mAdapter = new RecentAdapter(myList,folderSongs,generics);
        mAdapter.notifyDataSetChanged();
        recyclerview.getRecycledViewPool().clear();
        recyclerview.setAdapter(mAdapter);
        setEmptyView(mAdapter);
    }

    private void setMenuClick(SongHolder holder, final int position){
        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(FolderActivity.this,v, Gravity.END);
                popup.getMenuInflater().inflate(R.menu.menu_popup_folder, popup.getMenu());
                popup.show();
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()) {
                            case R.id.popup_song_add_to_playlist:
                                AddPlaylistDialog.newInstance(folderSongs.get(position)).show(getSupportFragmentManager(), "ADD_PLAYLIST");
                                break;
                            case R.id.popup_song_share:
                                ListSongs.shareSong(FolderActivity.this,folderSongs.get(position).getPath());
                                break;
                            case R.id.popup_song_details:
                                ListSongs.showSongDetailDialog(FolderActivity.this,folderSongs.get(position).getPath()
                                        ,folderSongs.get(position).getName(),folderSongs.get(position).getAlbumName(),folderSongs.get(position).getArtist());
                                break;
                            case R.id.popup_set_as_ringtone:
                                tempSongId = folderSongs.get(position).getSongId();
                                ListSongs.requestSetRingtone(FolderActivity.this,folderSongs.get(position)
                                        .getSongId(),folderSongs.get(position).getName());
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
            }
        });
    }

    private void setFolderClick(AlbumDetailHolder holder, final String root){

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PopupMenu popup = new PopupMenu(FolderActivity.this, v, Gravity.END);
                    popup.getMenuInflater().inflate(R.menu.menu_artist, popup.getMenu());
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.popup_song_add_to_playlist:
                                    File jj = new File(file,root);
                                    new PlayListTask(jj).execute();
                                    break;
                                default:
                                    break;
                            }
                            return true;
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }
    @Override
    public void onBackPressed() {
        PlaybackFragment fragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.panel5_holder);
        if(panelOpen){
            fragment.collapsePanel();
        }
        else {
            try {
                String parent = file.getParent();
                final File parentDir = file.getParentFile();
                if(parent.equals("/mnt") || parent.equals("/storage") ){
                    finish();
                }else {
                    GoToParrentDirTask(parent,parentDir);
                }
            } catch (Exception e) {
                finish();
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        MusicPreference.setPanelState(this,false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(br);
    }

    @Override
    public void onStart() {
        super.onStart();
        configReiever();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (writePermissionAked) {
                if (Settings.System.canWrite(this))
                    ListSongs.setRingtone(this, tempSongId);
            }
            writePermissionAked = false;
        }
        ListSongs.setDeleteEventListener(new ListSongs.DeleteEvent() {
            @Override
            public void deleteEvent() {
            }
            @Override
            public void deleteSongsEvent(int position) {
            }
            @Override
            public void setBoolean(boolean flag) {
                writePermissionAked = flag;
            }
        });
    }

    private class StartDirTask extends AsyncTask<Void, Void, Boolean> {
        File list[] = null ;
        private File newDirectory;
        public StartDirTask(File newDirectory, String path) {
            this.newDirectory = newDirectory;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            if (newDirectory!=null && newDirectory.canRead()){
                list = newDirectory.listFiles(new AudioFilter());
                for(File newList: list){
                    myList.add(newList.getName());
                }
                folderSongs = getSongsInDirectory(FolderActivity.this,newDirectory,"f");
                generics.addAll(myList);
                generics.addAll(folderSongs);
                mAdapter = new RecentAdapter(myList,folderSongs,generics);
                return true;
            } else {
                return false;
            }
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                recyclerview.setAdapter(mAdapter);
                RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(), spacing,false);
                recyclerview.addItemDecoration(dividerItemDecoration);
                setEmptyView(mAdapter);

            } else {
                Toast.makeText(FolderActivity.this, R.string.dirError, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ChangeDirTask extends AsyncTask<Void, Void, Boolean> {
        File list[] = null ;
        int pos;
        private File newDirectory;
        public ChangeDirTask(File newDirectory, String path, int position) {
            this.newDirectory = newDirectory;
            this.pos = position;
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            if (newDirectory!=null && newDirectory.canRead()){
                file = new File(file, myList.get(pos));
                File list[] = file.listFiles(new AudioFilter());
                myList.clear();
                generics.clear();
                //folderSongs.clear();
                for (File newDir: list) {
                    myList.add(newDir.getName());
                }
                folderSongs = getSongsInDirectory(FolderActivity.this,newDirectory,"f");
                generics.addAll(myList);
                generics.addAll(folderSongs);
                mAdapter = new RecentAdapter(myList,folderSongs,generics);
                return true;
            }
            else{
                return false;
            }
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            if(success) {
                toolbar.setTitle(file.toString());
                recyclerview.setAdapter(mAdapter);
                setEmptyView(mAdapter);

            } else {
                Toast.makeText(FolderActivity.this, R.string.dirError, Toast.LENGTH_SHORT).show();
            }

        }
    }

    public class SongHolder extends RecyclerView.ViewHolder {
        final View mView;
        TextView titelView;
        TextView artistView;
        ImageView thumb,menu;

        public SongHolder(View itemView) {
            super(itemView);
            mView = itemView;
            titelView = (TextView) itemView.findViewById(R.id.album_details_song_title);
            artistView = (TextView) itemView.findViewById(R.id.album_details_song_artist);
            menu = (ImageView) itemView.findViewById(R.id.overflow_menu);
            thumb = (ImageView)itemView.findViewById(R.id.album_details_thum);
        }
        public void update(SongModel item, int pos){
            titelView.setText(item.getName());
            artistView.setText(item.getAlbumName());
            Glide.with(FolderActivity.this).load( ListSongs.getAlbumArtUri(
                    item.getAlbumId()).toString()).error(R.drawable.album_art).into(thumb);

        }
    }

    public class AlbumDetailHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public TextView artistView;
        TextView titelView;
        AppCompatImageView thumb;
        ImageView menu;
        private TextView name;

        public AlbumDetailHolder(View itemView) {
            super(itemView);
            mView = itemView;
            menu = (ImageView) itemView.findViewById(R.id.overflow_menu);
            titelView = (TextView) itemView.findViewById(R.id.album_details_song_title);
            thumb = (AppCompatImageView) itemView.findViewById(R.id.album_details_thum);
        }
        private void update(String name) {
            titelView.setText(name);
        }

    }

    private class RecentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter{
        private final static int  TYPE_DIRECTORY=1, TYPE_SONG=2;
        private ArrayList<String> directories;
        private ArrayList<SongModel> folderSongs;
        private ArrayList<Object> items;
        public RecentAdapter(ArrayList<String > path, ArrayList<SongModel> songs, ArrayList<Object> item) {
            directories = path;
            folderSongs = songs;
            this.items = item;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            switch (viewType) {
                case TYPE_DIRECTORY:
                    LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                    View view = layoutInflater
                            .inflate(R.layout.folder_item, parent, false);
                    return new AlbumDetailHolder(view);
                case TYPE_SONG:
                    LayoutInflater layoutInflater2 = LayoutInflater.from(parent.getContext());
                    View view2 = layoutInflater2
                            .inflate(R.layout.artist_item, parent, false);
                    return new SongHolder(view2);
            }
            return null;
        }
        @Override
        public int getItemViewType(int position) {
            Object item = items.get(position);
            if(item instanceof String) return TYPE_DIRECTORY;
            else if(item instanceof SongModel) return TYPE_SONG;
            return -1;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            Object item = items.get(position);
            if(holder instanceof AlbumDetailHolder) {
                ((AlbumDetailHolder) holder).update((String)item);
                setFolderClick(((AlbumDetailHolder) holder),(String)item);
                ((AlbumDetailHolder) holder).menu.setColorFilter(Color.GRAY);
                if(ateKey.contains("dark_theme")){
                    ((AlbumDetailHolder)holder).thumb.setColorFilter(ContextCompat.getColor(FolderActivity.this,R.color.md_grey_400));;
                }

                setOnClicks(((AlbumDetailHolder) holder),position);
            } else if(holder instanceof SongHolder) {
                ((SongHolder) holder).menu.setColorFilter(Color.GRAY);
                ((SongHolder) holder).update((SongModel) item,position);
                setSongClick(((SongHolder) holder),(position-myList.size()));
                setMenuClick(((SongHolder)holder),(position-myList.size()));
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private void setOnClicks(final AlbumDetailHolder holder, final int position) {
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            File temp_file = new File(file, myList.get(position));
                            new ChangeDirTask(temp_file,"haha",position).execute();
                           // MusicService.getInstance().showPlayListSize();
                        }
                    }, 150);

                }
            });
        }

        private void setSongClick(SongHolder holder, final int position){
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!folderSongs.isEmpty()){
                                if(MusicService.getInstance()!=null){
                                    MusicService.getInstance().setList(folderSongs);
                                    String currentPath = file.getAbsolutePath();
                                    Intent i = new Intent();
                                    i.setAction(MusicService.ACTION_PLAY_FOLDER);
                                    i.putExtra("songPos", position);
                                    i.putExtra("file",currentPath);
                                    sendBroadcast(i);
                                }
                            }else{
                                Toast.makeText(getApplicationContext(), "can't play the file",Toast.LENGTH_LONG).show();
                            }
                        }
                    },50);
                }
            });
        }

        @NonNull
        @Override
        public String getSectionName(int position) {

                Object item = items.get(position);
                if(item instanceof String) {
                    final String name = (String)item;
                    return name.substring(0,1);
                }
                else if(item instanceof SongModel) {

                    final String namee = ((SongModel) item).getName();
                    return namee.substring(0,1);

                }
            return "";
        }
    }

    public class AudioFilter implements FileFilter {
        @Override
        public boolean accept(File pathname) {
            if (pathname.isDirectory()  && !pathname.isFile() && !pathname.isHidden() ){
                return true;
            }
            return false;
        }
    }

    public class Filewalker {
        boolean added = false;
        void walk(File root) {
            if(scan){
                File[] list = root.listFiles(new AudioFilter());
                for (File f : list) {
                    if (f.isDirectory()&& f.canRead()) {
                        ArrayList<SongModel> tempList  = getSongsInDirectory(FolderActivity.this,f,"f");
                        playList.addAll(tempList);
                        if(!added){
                            ArrayList<SongModel> temp = getSongsInDirectory(FolderActivity.this,root,"f");
                            playList.addAll(temp);
                            added = true;
                        }
                        walk(f);
                    }
                }
            }
            else {
                sendBroadcast(new Intent(FolderActivity.ACTION_CLEAR_PLAYLIST));
            }
        }
    }

    public class PlayListTask  extends  AsyncTask<Void, Void, Boolean>{
        final MaterialDialog.Builder builder=  new MaterialDialog.Builder(FolderActivity.this);
        MaterialDialog dialog;
        boolean show = true;
        File jj;
        public PlayListTask(File newDirectory) {
            this.jj = newDirectory;
        }
        public PlayListTask() {
        }

        @Override
        protected void onPreExecute() {

            builder.title("Getting files")
                    .content("")
                    .progress(true, 0)
                    .negativeText("CANCEL")
                    .progressIndeterminateStyle(true)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            new PlayListTask().cancel(true);
                            show= false;
                            scan = false;
                            dialog.dismiss();

                        }
                    });
            dialog = builder.build();
            dialog.show();
        }
        @Override
        protected Boolean doInBackground(Void... params) {
            if (jj.isDirectory()){
                File[] tempList = jj.listFiles(new AudioFilter());{
                    if(tempList.length==0){
                        playList = getSongsInDirectory(FolderActivity.this,jj,"f");
                    }
                    else if (tempList.length>0){
                        scan = true;
                        new Filewalker().walk(jj);
                    }
                }
                return true;
            }
            else {
                return false;
            }
        }
        @Override
        protected void onPostExecute(final Boolean success) {
            dialog.dismiss();
            if(success) {
                if(playList.size()==0){
                    if(show)
                        Toast.makeText(FolderActivity.this, "No music found", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(show)
                        AddPlaylistDialog.newInstance(playList).show(getSupportFragmentManager(), "ADD_PLAYLIST");
                }
            } else {
                Toast.makeText(FolderActivity.this,R.string.dirError , Toast.LENGTH_SHORT).show();
            }
        }
    }
}
