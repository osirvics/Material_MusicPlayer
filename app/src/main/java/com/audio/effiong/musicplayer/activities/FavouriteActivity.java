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
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.StyleRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
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

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.dbhandler.FavoritePlayTableHelper;
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
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

import static com.audio.effiong.musicplayer.R.id.adView;

public class FavouriteActivity extends  BaseThemedActivity implements ATEActivityThemeCustomizer,PlaybackFragment.OnPanelClickListener {
    public RecyclerView rvFavourite;
    ArrayList<SongModel> favouriteSongs;
    public RecentAdapter mAdapter;
    private Cursor cursor = null;
   // private String LOG_TAG = "FavouriteActivity";
    public PlaybackFragment fragment1;
    private long tempSongId;
    private boolean writePermissionAked = false;
    private boolean panelOpen = false;
    private boolean setList = false;
    private AdView mAdView;
    private boolean adLoaded = false;
    LinearLayout helperLayout;
    LinearLayout emptyView;
    int spacing;



    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {

                case PlaybackFragment.ACTION_HIDE_ADVIEW:
                    if (adLoaded)
                        helperLayout.setVisibility(View.VISIBLE);
                    mAdView.setVisibility(View.GONE);
                    break;
                case PlaybackFragment.ACTION_SHOW_ADVIEW:
                    if(adLoaded){
                        mAdView.setVisibility(View.VISIBLE);
                        helperLayout.setVisibility(View.VISIBLE);
                        // helperLayout.requestLayout();
                    }
                    break;
                case PlaybackFragment.ACTION_HIDE_ADVIEW_NEW:
                    //  if(adLoaded) {
                    //mAdView.setVisibility(View.VISIBLE);
                    helperLayout.setVisibility(View.GONE);
                    // mAdView.setVisibility(View.GONE);
                    //}

                    break;
                case PlaybackFragment.ACTION_SHOW_ADVIEW_NEW:
                    if(adLoaded){
                        // mAdView.setVisibility(View.VISIBLE);
                        helperLayout.setVisibility(View.VISIBLE);
                        // helperLayout.requestLayout();
                    }
                    break;
            }
        }
    };

    public void configReiever(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlaybackFragment.ACTION_HIDE_ADVIEW);
        filter.addAction(PlaybackFragment.ACTION_SHOW_ADVIEW);
        filter.addAction(PlaybackFragment.ACTION_HIDE_ADVIEW_NEW);
        filter.addAction(PlaybackFragment.ACTION_SHOW_ADVIEW_NEW);
        registerReceiver(br, filter);
    }


    @StyleRes
    @Override
    public int getActivityTheme() {
        // Overrides what's set in the current ATE Config
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ?
                R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_main);
        panelOpen = MusicPreference.isPanelOpen(this);
        mAdView = (AdView) findViewById(adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
        helperLayout = (LinearLayout)findViewById(R.id.reavel);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adLoaded = true;
                if(!panelOpen)
                    mAdView.setVisibility(View.VISIBLE);
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.panel4_holder);

        if (fragment == null) {
            fragment = PlaybackFragment.newInstance(Helpers.isKitkat());
            fm.beginTransaction()
                    .add(R.id.panel4_holder, fragment)
                    .commit();
        }

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        spacing =  getResources().getDimensionPixelSize(R.dimen.rc_padding_left);
        rvFavourite = (RecyclerView) findViewById(R.id.favourite);
        emptyView = (LinearLayout)findViewById(R.id.songs_empty_view);
        //rvFavourite.setLayoutManager(new LinearLayoutManager(this));
        final int columns = getResources().getInteger(R.integer.gallery_columns);
        final GridLayoutManager gridLayoutManager =
                new GridLayoutManager(this, columns);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvFavourite.setLayoutManager(gridLayoutManager);
        favouriteSongs = new ArrayList<>();
        cursor = FavoritePlayTableHelper.getInstance(this).getFavoriteSongList();

        new LoadFavouriteSongs().execute("");
        // Used to relaod songs when a song is deleted

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fav, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                //shuffle
                return true;
            case R.id.action_equalizer:
                Helpers.navigateToEqualizer(FavouriteActivity.this);
                return true;
            case R.id.action_search:
                final Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void isPanelOpen(boolean flag) {
        panelOpen = flag;
    }

    private class LoadFavouriteSongs extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            favouriteSongs = ListSongs.getSongsFromSQLDBCursor(cursor);
            return "Executed";
        }
        @Override
        protected void onPostExecute(String result) {
            mAdapter = new RecentAdapter(favouriteSongs);
            rvFavourite.setAdapter(mAdapter);
            RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(), spacing,false);
            rvFavourite.addItemDecoration(dividerItemDecoration);
            if(favouriteSongs.isEmpty())
                setEmptyView();
        }
        @Override
        protected void onPreExecute() {
        }
    }
    private void setEmptyView(){
        rvFavourite.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }
    private void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                cursor = FavoritePlayTableHelper.getInstance(FavouriteActivity.this).getFavoriteSongList();
                favouriteSongs = ListSongs.getSongsFromSQLDBCursor(cursor);
                if(MusicService.getInstance().getPlaylistType()==4)
                MusicService.getInstance().setList(favouriteSongs);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.updateDataSet(favouriteSongs);
                mAdapter.notifyDataSetChanged();
                if(favouriteSongs.isEmpty()){
                    setEmptyView();
                }
            }
        }.execute();
    }

    private class AlbumDetailHolder extends RecyclerView.ViewHolder {
        public TextView titelView;
        public TextView artistView;
        public ImageView thumb,menu;
        public final View mView;

        public AlbumDetailHolder(View itemView) {
            super(itemView);
            mView = itemView;
            menu = (ImageView) itemView.findViewById(R.id.overflow_menu);
            titelView = (TextView) itemView.findViewById(R.id.album_details_song_title);
            artistView = (TextView) itemView.findViewById(R.id.album_details_song_artist);
            thumb = (ImageView) itemView.findViewById(R.id.album_details_thum);
        }
    }

    private class RecentAdapter extends RecyclerView.Adapter<AlbumDetailHolder> {
        private ArrayList<SongModel> theAlbumSongs;

        public RecentAdapter(ArrayList<SongModel> songs) {
            theAlbumSongs = songs;
        }

        @Override
        public AlbumDetailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater
                    .inflate(R.layout.song_item, parent, false);
            return new AlbumDetailHolder(view);
        }

        @Override
        public void onBindViewHolder(AlbumDetailHolder holder, int position) {
           // holder.artistView.setAlpha(0.6f);
            holder.menu.setColorFilter(Color.GRAY);
            final SongModel currSong = theAlbumSongs.get(position);
            getAlbumImage(holder, currSong);
            holder.titelView.setText(currSong.getName());
            holder.artistView.setText(currSong.getArtist());
            setOnClicks(holder,position);
        }

            private void setOnClicks(AlbumDetailHolder holder, final int position){
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!setList)
                                MusicService.getInstance().setList(theAlbumSongs);
                            MusicService.getInstance().setPlaylistType(4);
                            setList = true;
                            Intent i = new Intent();
                            i.setAction(MusicService.ACTION_PLAY_FAVOURITE);
                            i.putExtra("songPos", rvFavourite.getChildAdapterPosition(v));
                            sendBroadcast(i);
                        }
                    }, 50);
                }
            });

            holder.menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(FavouriteActivity.this,v, Gravity.END);
                    popup.getMenuInflater().inflate(R.menu.popup_fav_menu, popup.getMenu());
                    popup.show();
                    Menu popupMenu = popup.getMenu();
                    popupMenu.setGroupVisible(R.id.user_playlist_group, false);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.popup_song_add_to_playlist:
                                    AddPlaylistDialog.newInstance(theAlbumSongs.get(position)).show(getSupportFragmentManager(), "ADD_PLAYLIST");
                                    break;
                                case R.id.popup_song_share:
                                    ListSongs.shareSong(FavouriteActivity.this,theAlbumSongs.get(position).getPath());
                                    break;
                                case R.id.popup_song_details:
                                    ListSongs.showSongDetailDialog(FavouriteActivity.this,theAlbumSongs.get(position).getPath()
                                            ,theAlbumSongs.get(position).getName(),theAlbumSongs.get(position).getAlbumName(),theAlbumSongs.get(position).getArtist());
                                    break;
                                case R.id.popup_set_as_ringtone:
                                    tempSongId = theAlbumSongs.get(position).getSongId();
                                    ListSongs.requestSetRingtone(FavouriteActivity.this,theAlbumSongs.get(position)
                                            .getSongId(),theAlbumSongs.get(position).getName());
                                    break;
                                case R.id.popup_delete_music:
                                    ListSongs.deleteSong(FavouriteActivity.this,theAlbumSongs.get(position)
                                                    .getName(), 0
                                            ,new long[]{theAlbumSongs.get(position).getSongId()},theAlbumSongs.get(position));
                                    break;
                                case R.id.popup_remove_music:
                                   ListSongs.storeFavoritePlay(FavouriteActivity.this,theAlbumSongs.get(position),0);
                                    reloadAdapter();

                                       break;
//                                    case R.id.delete:
//                                        break;
                                default:
                                    break;
                            }
                            return true;
                        }
                    });
                }
            });

        }

        private void getAlbumImage(final AlbumDetailHolder holder, final SongModel currentArtist) {
            final String[] path = {""};
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... unused) {
                    path[0] = ListSongs.getAlbumArt(getApplicationContext(), currentArtist.getAlbumId());
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    Glide.with(getApplicationContext()).load(path[0]).placeholder(R.drawable.album_art).crossFade().into(holder.thumb);
                }
            }.execute();
        }

        @Override
        public int getItemCount() {
            return theAlbumSongs.size();
        }
        public void updateDataSet(ArrayList<SongModel> arraylist) {
            this.theAlbumSongs = arraylist;
            //this.songIDs = getSongIds();
        }
    }

    @Override
    protected void onResume(){
//        fragment1 = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.panel4_holder);
//        if(MusicPreference.getPlayBackFlag(this)){
//            //fragment1.setPlay();
//           // fragment1.updatePlayPauseFloatingButton();
//            // fragment1.getAlbumImage(MusicPreference.getAlbumId(this));
//            // fragment1.updateSeekBarTime();
//        }
        super.onResume();
    }

    @Override
    protected void onStop(){
        super.onStop();
        setList = false;
        unregisterReceiver(br);
    }
    @Override
    protected void onStart(){
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
                reloadAdapter();
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

    @Override
    protected void onDestroy(){
        super.onDestroy();
        MusicPreference.setPanelState(this,false);

    }
       @Override
    public void onBackPressed(){

           PlaybackFragment fragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.panel4_holder);
           if(panelOpen){
               fragment.collapsePanel();
           }
           else {
               super.onBackPressed();
               //overridePendingTransition(0, 0);
           }
       }
}
