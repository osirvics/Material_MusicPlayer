package com.audio.effiong.musicplayer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.StyleRes;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

import static com.audio.effiong.musicplayer.R.id.adView;

public class ArtistDetailsActivity extends  BaseThemedActivity implements ATEActivityThemeCustomizer,
        PlaybackFragment.OnPanelClickListener {

   private RecyclerView artistSongRv;
    private ArrayList<SongModel> artistSongList;
    ArtistDetailsAdapter mAdapter;
   // String artistName;
   // private Intent playIntent;
   // private MusicService musicSrv;
   // private boolean musicBound = false;
   // public PlaybackFragment fragment1;
    private long tempSongId;
    private boolean writePermissionAked = false;
    private boolean panelOpen = false;
    private boolean setList = false;
    private AdView mAdView;
    private boolean adLoaded = false;
    LinearLayout helperLayout;
    AppBarLayout appBarLayout;
    int colorPrimary;


    @StyleRes
    @Override
    public int getActivityTheme() {
        // Overrides what's set in the current ATE Config
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ?
                R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
    }

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist_details);
        panelOpen = MusicPreference.isPanelOpen(this);
        colorPrimary = Config.primaryColor(this,Helpers.getATEKey(this));
        appBarLayout =(AppBarLayout)findViewById(R.id.appbar);
        appBarLayout.setBackgroundColor(colorPrimary);
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
        String title = getIntent().getExtras().getString("name");
        if(toolbar != null)
        {
            toolbar.setTitle(title);
        }
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.panel3_holder);
        if (fragment == null) {
            fragment = PlaybackFragment.newInstance(Helpers.isKitkat());
            fm.beginTransaction()
                    .add(R.id.panel3_holder, fragment)
                    .commit();
        }




        artistSongRv = (RecyclerView)findViewById(R.id.artist_list_rv);
        //artistSongRv.setLayoutManager(new LinearLayoutManager(this));
        final int columns = getResources().getInteger(R.integer.gallery_columns);
        final GridLayoutManager gridLayoutManager =
                new GridLayoutManager(this, columns);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        artistSongRv.setLayoutManager(gridLayoutManager);

        artistSongList = new ArrayList<>();
       new loadArtistList().execute("");

    }


//    private ServiceConnection musicConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
//            //get service
//            musicSrv = binder.getService();
//            //pass list
//            //control = new PlayerControl(getContext(),musicSrv.getMediaPlayer());
//
//            //PlayerHandler.setSongList(songList);
//            //finalTime = musicSrv.getFinalTime();
//            musicBound = true;
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//            musicBound = false;
//        }
//    };

    @Override
    public void isPanelOpen(boolean flag) {
        panelOpen = flag;
    }


    private class loadArtistList extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
           //String artistName =  getIntent().getStringExtra("name");
            artistSongList = ListSongs.getSongsListOfArtist(getApplicationContext(),getIntent().getStringExtra("name"));
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            mAdapter = new ArtistDetailsAdapter(getApplicationContext(),artistSongList);
            artistSongRv.setAdapter(mAdapter);
            int spacing =  getResources().getDimensionPixelSize(R.dimen.rc_padding_left);
            RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(), spacing,false);
            artistSongRv.addItemDecoration(dividerItemDecoration);
           // artistSongRv.setAdapter(new ArtistDetailsAdapter(getApplicationContext(),artistSongList));

            //artistRv.setAdapter(new ArtistListAdapter(getContext(),artists));
        }
    }

    private void reloadArtistAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                artistSongList = ListSongs.getSongsListOfArtist(getApplicationContext(),getIntent().getStringExtra("name"));
                if(MusicService.getInstance().getPlaylistType()==3)
                    MusicService.getInstance().setList(artistSongList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.updateDataSet(artistSongList);
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
//            case R.id.action_settings:
//                Intent ntent = new Intent(getApplication(), SettingsActivity.class);
//                //ntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                // ntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(ntent);
//                //overridePendingTransition(0, 0);
//                //shuffle
//                return true;
            case R.id.action_equalizer:
                Helpers.navigateToEqualizer(ArtistDetailsActivity.this);
                return true;
            case R.id.action_search:
                final Intent intent = new Intent(this, SearchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
               // intent.setAction(ListSongs.NAVIGATE_SEARCH);
                startActivity(intent);
                return true;
//            case R.id.menu_sort_by_title:
//                MusicPreference.setArtistSortOrder(this, SortOrder.ArtistSortOrder.TITLE);
//                reloadArtistAdapter();
//                return true;
//            case R.id.menu_sort_by_artist:
//                MusicPreference.setArtistSortOrder(this,SortOrder.ArtistSortOrder.SONG_ARTIST);
//                reloadArtistAdapter();
//                return true;
//            case R.id.menu_sort_by_date:
//                MusicPreference.setArtistSortOrder(this, SortOrder.ArtistSortOrder.DATE_ADDED);
//                reloadArtistAdapter();
//                return true;
//            case R.id.menu_sort_by_duration:
//                MusicPreference.setArtistSortOrder(this, SortOrder.ArtistSortOrder.DURATION );
//                // iniViews();
//                reloadArtistAdapter();
//                return true;

        }
        return super.onOptionsItemSelected(item);
        //int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //   return true;
        //}

        //return super.onOptionsItemSelected(item);
    }


    public class ArtistDetailsAdapter extends RecyclerView.Adapter<ArtistDetailsAdapter.ArtistListHolder> {

        private ArrayList<SongModel> artistSongs;
        private Context mContext;
       // private String albumPath = "";

        public ArtistDetailsAdapter(Context context, ArrayList<SongModel> givenArtist) {
            this.mContext = context;
            this.artistSongs = givenArtist;
            //setHasStableIds(true);

        }


        @Override
        public ArtistListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.artist_item, parent, false);
            return new ArtistListHolder(view);
        }

        @Override
        public void onBindViewHolder(ArtistListHolder holder, int position) {
           // holder.numberOfSongs.setAlpha(0.6f);
            final SongModel currentArtist = artistSongs.get(position);
            holder.menu.setColorFilter(Color.GRAY);
            // Glide.with(mContext).load(ListSongs.getAlbumArt(mContext, currentArtist.getAlbumId())).placeholder(R.drawable.bg_default_album_art).crossFade().into(holder.thum);
            getAlbumImage(holder, currentArtist);
            holder.artistName.setText(currentArtist.getName());
            holder.numberOfSongs.setText(currentArtist.getAlbumName());


            // Glide.with(mContext).load(ListSongs.getAlbumArt(mContext,currentArtist.getAlbumId())).crossFade().into(holder.thum);

            setOnClicks(holder,position);


        }

        private void setOnClicks(final ArtistListHolder holder, final int position) {
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(MusicService.getInstance()!=null){
                                if(!setList)
                                    MusicService.getInstance().setList(artistSongs);
                                MusicService.getInstance().setPlaylistType(3);
                                setList = true;
                                Intent i = new Intent();
                                i.setAction(MusicService.ACTION_PLAY_ARTIST);
                                i.putExtra("songPos", artistSongRv.getChildAdapterPosition(v));
                                i.putExtra("artistName",getIntent().getStringExtra("name"));
                                mContext.sendBroadcast(i);
                            }
                           else{
                                Toast.makeText(getApplicationContext(), "Service is not running, please restart player", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, 50);
                }
            });

            holder.menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(ArtistDetailsActivity.this,v, Gravity.END);
                    popup.getMenuInflater().inflate(R.menu.popup_song_menu, popup.getMenu());
                    popup.show();
                    Menu popupMenu = popup.getMenu();
                    popupMenu.setGroupVisible(R.id.user_playlist_group, false);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.popup_song_add_to_playlist:
                                    AddPlaylistDialog.newInstance(artistSongs.get(position)).show(getSupportFragmentManager(), "ADD_PLAYLIST");
                                    break;
                                case R.id.popup_song_share:
                                    ListSongs.shareSong(ArtistDetailsActivity.this,artistSongs.get(position).getPath());
                                    break;
                                case R.id.popup_song_details:
                                    ListSongs.showSongDetailDialog(ArtistDetailsActivity.this,artistSongs.get(position).getPath()
                                            ,artistSongs.get(position).getName(),artistSongs.get(position).getAlbumName(),artistSongs.get(position).getArtist());
                                    break;
                                case R.id.popup_set_as_ringtone:
                                    tempSongId = artistSongs.get(position).getSongId();
                                    ListSongs.requestSetRingtone(ArtistDetailsActivity.this,artistSongs.get(position)
                                            .getSongId(),artistSongs.get(position).getName());
                                    break;
                                case R.id.popup_delete_music:
//                                    ListSongs.deleteTracks(ArtistDetailsActivity.this, new long[]{
//                                           artistSongs.get(position).getSongId()
//                                    });
                                   ListSongs.deleteSong(ArtistDetailsActivity.this,artistSongs.get(position)
                                                    .getName(), 0
                                            ,new long[]{artistSongs.get(position).getSongId()},artistSongs.get(position));
                                    break;
//                                    case R.id.gotoalbum:
//                                        break;
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

        private void getAlbumImage(final ArtistListHolder holder, final SongModel currentArtist) {
            final String[] albumPath = {""};

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... unused) {

                    albumPath[0] = ListSongs.getAlbumArt(mContext, currentArtist.getAlbumId());
                    //List<Album> albumList = AlbumLoader.getAllAlbums(getActivity());
                    // mAdapter.updateDataSet(albumList);
                    return null;
                }
                @Override
                protected void onPostExecute(Void aVoid) {
                    Glide.with(mContext).load(albumPath[0]).placeholder(R.drawable.album_art).crossFade().into(holder.thum);
                    //mAdapter.notifyDataSetChanged();
                }
            }.execute();
        }



        @Override
        public int getItemCount() {
            return artistSongs.size();
        }

        public void updateDataSet(ArrayList<SongModel> arraylist) {
            this.artistSongs = arraylist;
            //this.songIDs = getSongIds();
        }

        public class ArtistListHolder extends RecyclerView.ViewHolder {
            public TextView artistName;
            public TextView numberOfSongs;
            private View mView;
            private ImageView thum,menu;
            public ArtistListHolder(View itemView) {
                super(itemView);
                mView = itemView;
                menu = (ImageView) itemView.findViewById(R.id.overflow_menu);
                thum = (ImageView) itemView.findViewById(R.id.album_details_thum);
                artistName = (TextView) itemView.findViewById(R.id.album_details_song_title);
                numberOfSongs = (TextView) itemView.findViewById(R.id.album_details_song_artist);

            }
        }
    }


    @Override
    public void onBackPressed() {
        PlaybackFragment fragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.panel3_holder);
        if(panelOpen){
            fragment.collapsePanel();
        }
        else {
            super.onBackPressed();
           // overridePendingTransition(0, 0);
        }

        //finish();
        // }
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
        // Used to relaod songs when a song is deleted
        ListSongs.setDeleteEventListener(new ListSongs.DeleteEvent() {
            @Override
            public void deleteEvent() {
                reloadArtistAdapter();
                Log.e("ARTSITACTIVITY","HAPILY CRAETED CALLBACK");
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
    protected void onDestroy() {
        super.onDestroy();
        MusicPreference.setPanelState(this,false);

    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        setList = false;
        unregisterReceiver(br);
        Log.e("Artist_Details_Activity","ON STOP CALLED");
    }
}
