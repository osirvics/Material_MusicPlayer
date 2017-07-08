package com.audio.effiong.musicplayer.activities;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.StyleRes;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
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
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
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
import com.audio.effiong.musicplayer.utility.MusicActivity;
import com.audio.effiong.musicplayer.utility.MusicPreference;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

import static com.audio.effiong.musicplayer.R.id.adView;

public class AlbumDetailsActivity extends BaseThemedActivity implements ATEActivityThemeCustomizer,PlaybackFragment.OnPanelClickListener {
    public RecyclerView albumView;
    private AlbumDetailAdapter mAdapter;
    ArrayList<SongModel> albumSongs;
    private long albumId;
    private long tempSongId;
    private boolean writePermissionAked = false;
    private boolean panelOpen = false;
    String ateKey;
    private boolean setList = false;
    private AdView mAdView;
    private boolean adLoaded = false;
    LinearLayout helperLayout;
    int spacing;
    public static final String sample = "0";
    ImageView albumArt;
    CollapsingToolbarLayout collapsingToolbar;
    CardView card;
    TextView albumName;
    int contentScrimColor;
    Toolbar toolbar;
    String path2;



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
                    }
                    break;
                case PlaybackFragment.ACTION_HIDE_ADVIEW_NEW:
                        helperLayout.setVisibility(View.GONE);

                    break;
                case PlaybackFragment.ACTION_SHOW_ADVIEW_NEW:
                    if(adLoaded){
                        helperLayout.setVisibility(View.VISIBLE);
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
        ateKey = Helpers.getATEKey(this);





        setContentView(R.layout.activity_album_details_main);

        if(Helpers.isLollipop())
            postponeEnterTransition();
        albumId = getIntent().getLongExtra("albumId", 0);
        card = (CardView)findViewById(R.id.card);
        albumName = (TextView)findViewById(R.id.album_name);
        this.albumName.setText(getIntent().getExtras().getString("albumName", "Music"));
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        card.setAlpha(1f);
        card.setCardBackgroundColor(ContextCompat.getColor(this,R.color.coloorPrimary));
        albumArt = (ImageView)findViewById(R.id.album_art);
        int id = getIntent().getIntExtra("transitionName", 1);
        if(Helpers.isLollipop())
        albumArt.setTransitionName("transition_album_art" + id);
         path2 = ListSongs.getAlbumArtUri(albumId).toString();
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);

        if (collapsingToolbar != null) {
            collapsingToolbar.setTitle(" ");
            collapsingToolbar.setStatusBarScrimColor(ContextCompat.getColor(this,R.color.coloorPrimary));
            collapsingToolbar.setExpandedTitleColor(Config.textColorPrimaryInverse(this, getATEKey()));
        }
        //Glide.with(MusicActivity.applicationContext).load(path2).error(R.drawable.bg_default_album_art).centerCrop().dontAnimate()
                //.into(albumArt);
       getCardColor(path2);
        getImage(path2);







       // getAlbumImage(this,albumId);

        setTranslucentStatusBar(getWindow());

       // handler = new Handler();
        panelOpen = MusicPreference.isPanelOpen(this);
        mAdView = (AdView) findViewById(adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
        helperLayout = (LinearLayout) findViewById(R.id.reavel);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                adLoaded = true;
                if(!panelOpen)
                    mAdView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(int error) {
               // mAdView.setVisibility(View.GONE);
            }
        });

       // String title = getIntent().getExtras().getString("albumName","Music");
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        //final Toolbar toolbarTitle = (Toolbar) findViewById(R.id.toolbarTitle);
        if(toolbar!=null){
            setSupportActionBar(toolbar);
        }
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        spacing =  getResources().getDimensionPixelSize(R.dimen.rc_padding_left);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.panel2_holder);
        if (fragment == null) {
            fragment = PlaybackFragment.newInstance(true);
            fm.beginTransaction()
                    .add(R.id.panel2_holder, fragment)
                    .commit();
        }
        tempSongId = 0;
        albumView = (RecyclerView) findViewById(R.id.album_details_rv);
        //albumView.setLayoutManager(new LinearLayoutManager(this));
        final int columns = getResources().getInteger(R.integer.gallery_columns);
        final GridLayoutManager gridLayoutManager =
                new GridLayoutManager(this, columns);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        albumView.setLayoutManager(gridLayoutManager);

        albumSongs = new ArrayList<>();
        new loadAlbumSongDetails().execute("");

    }

    @Override
    public void isPanelOpen(boolean flag) {
        panelOpen = flag;
    }





    private class loadAlbumSongDetails extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            albumSongs = ListSongs.getAlbumSongList(getApplicationContext(), albumId);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            mAdapter = new AlbumDetailAdapter(albumSongs);
            albumView.setAdapter(mAdapter);
            //RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(thi, spacing,true);
            albumView.addItemDecoration(new DividerItemDecoration(getApplicationContext(),spacing,false));
            //albumView.setAdapter(new AlbumDetailAdapter(albumSongs));

        }
    }

    private void reloadAlbumAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                albumSongs = ListSongs.getAlbumSongList(getApplicationContext(), albumId);
                if(MusicService.getInstance().getPlaylistType()==2)
                    MusicService.getInstance().setList(albumSongs);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.updateDataSet(albumSongs);
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_album_main, menu);

//        for(int i = 0; i < menu.size(); i++){
//            Drawable drawable = menu.getItem(i).getIcon();
//            if(drawable != null) {
//                drawable.mutate();
//                drawable.setColorFilter((ContextCompat.getColor(MusicActivity.applicationContext, R.color.md_white_1000)), PorterDuff.Mode.MULTIPLY);
//            }
//        }
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
            case R.id.action_search:
                final Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                return true;

           // if (item.getItemId() == android.R.id.home)


            case android.R.id.home:
            onBackPressed();
                return true;
//            case R.id.menu_sort_by_artist:
//                MusicPreference.setAlbumSortOrder(this,SortOrder.AlbumSortOrder.SONG_ARTIST);
//                reloadAlbumAdapter();
//                return true;
//            case R.id.menu_sort_by_date:
//                MusicPreference.setAlbumSortOrder(this, SortOrder.AlbumSortOrder.DATE_ADDED);
//                reloadAlbumAdapter();
//                return true;
//            case R.id.menu_sort_by_duration:
//                MusicPreference.setAlbumSortOrder(this, SortOrder.AlbumSortOrder.DURATION );
//                reloadAlbumAdapter();
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onStart() {
        super.onStart();
        configReiever();
       toolbar.setBackgroundColor(Color.TRANSPARENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (writePermissionAked) {
                if (Settings.System.canWrite(this))
                    ListSongs.setRingtone(this, tempSongId);
            }
            writePermissionAked = false;
            //.e(LOG_TAG, " songlistfrag ONSTART CALLED");
        }
        ListSongs.setDeleteEventListener(new ListSongs.DeleteEvent() {
            @Override
            public void deleteEvent() {
                reloadAlbumAdapter();
              //  Log.e(LOG_TAG,"HAPILY CRAETED CALLBACK");
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
    protected void onStop() {
        super.onStop();
        setList = false;
        unregisterReceiver(br);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        collapsingToolbar.setContentScrimColor(Color.TRANSPARENT);
        if(path2==null || path2.contains(""))
        collapsingToolbar.setStatusBarScrimColor(contentScrimColor);

    }

        @Override
    protected void onPause(){
        super.onPause();
    }
    private class AlbumDetailHolder extends RecyclerView.ViewHolder {
        TextView titelView;
        TextView artistView;
        TextView thumb;
        ImageView menu;
        final View mView;

        public AlbumDetailHolder(View itemView) {
            super(itemView);
            mView = itemView;
            titelView = (TextView) itemView.findViewById(R.id.album_details_song_title);
            artistView = (TextView) itemView.findViewById(R.id.album_details_song_artist);
            menu = (ImageView) itemView.findViewById(R.id.overflow_menu);
            thumb = (TextView) itemView.findViewById(R.id.album_details_thum);
        }
    }
    private class AlbumDetailAdapter extends RecyclerView.Adapter<AlbumDetailHolder> {
        private ArrayList<SongModel> theAlbumSongs;

        public AlbumDetailAdapter(ArrayList<SongModel> songs) {
            this.theAlbumSongs = songs;
        }

        @Override
        public AlbumDetailHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater
                    .inflate(R.layout.item_album_song, parent, false);
            return new AlbumDetailHolder(view);
        }

        @Override
        public void onBindViewHolder(AlbumDetailHolder holder,final int position) {
           // holder.titelView.setAlpha(0.87f);
           // holder.artistView.setAlpha(0.6f);
            final SongModel currSong = theAlbumSongs.get(position);
            holder.menu.setColorFilter(Color.GRAY);
            //getAlbumImage(holder,currSong);
            final String trackNo = currSong.getTrackNumber();
            if(trackNo!=null){
                if(trackNo.equals(sample)){
                    holder.thumb.setText("-");
                }
                else
                holder.thumb.setText(trackNo);
            }
            else{
                holder.thumb.setText("-");
            }
            holder.titelView.setText(currSong.getName());
            holder.artistView.setText(currSong.getArtist());

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(MusicService.getInstance()!=null){
                                if (!setList)
                                    MusicService.getInstance().setList(albumSongs);
                                MusicService.getInstance().setPlaylistType(2);
                                setList = true;
                                Intent i = new Intent();
                                i.setAction(MusicService.ACTION_PLAY_ALBUM);
                                i.putExtra("songPos", albumView.getChildAdapterPosition(v));
                                i.putExtra("albumId",albumId);
                                sendBroadcast(i);
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
                   try {
                    android.support.v7.widget.PopupMenu popup = new android.support.v7.widget.PopupMenu(AlbumDetailsActivity.this, v, Gravity.RIGHT);
                        popup.getMenuInflater().inflate(R.menu.popup_song_menu, popup.getMenu());
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
                                        ListSongs.shareSong(AlbumDetailsActivity.this,theAlbumSongs.get(position).getPath());
                                        break;
                                    case R.id.popup_song_details:
                                        ListSongs.showSongDetailDialog(AlbumDetailsActivity.this,theAlbumSongs.get(position).getPath()
                                                ,theAlbumSongs.get(position).getName(),theAlbumSongs.get(position).getAlbumName(),theAlbumSongs.get(position).getArtist());
                                        break;
                                    case R.id.popup_set_as_ringtone:
                                        tempSongId = theAlbumSongs.get(position).getSongId();
                                        ListSongs.requestSetRingtone(AlbumDetailsActivity.this,theAlbumSongs.get(position)
                                                .getSongId(),theAlbumSongs.get(position).getName());
                                        break;
                                    case R.id.popup_delete_music:
                                        ListSongs.deleteSong(AlbumDetailsActivity.this,theAlbumSongs.get(position)
                                                        .getName(), 0
                                                ,new long[]{theAlbumSongs.get(position).getSongId()},theAlbumSongs.get(position));
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
                    } catch (Exception e) {
                       e.printStackTrace();
                   }

                }
            });

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
    public void onBackPressed() {
        PlaybackFragment fragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.panel2_holder);
        if(panelOpen){
            fragment.collapsePanel();
        }
        else {
            super.onBackPressed();
        }
        //setResult(Activity.RESULT_OK);
    }

    public void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        if(Helpers.isLollipop())
                        startPostponedEnterTransition();
                        return true;
                    }
                });
    }


    public void getImage(String path){
        Glide.with(MusicActivity.applicationContext)
                .load(path)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.album_art)
                .error(R.drawable.album_art)
                .dontAnimate()
                .into(new GlideDrawableImageViewTarget(albumArt) {
                    @Override
                    public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                        super.onResourceReady(resource, animation);
                        scheduleStartPostponedTransition(albumArt);
                        //never called
                    }

                    @Override
                    public void onLoadFailed(Exception e, Drawable errorDrawable) {
                        super.onLoadFailed(e, errorDrawable);
                        albumArt.setImageDrawable(errorDrawable);
//                        Glide.with(MusicActivity.applicationContext).load(R.drawable.bg_default_album_art)
//                                .centerCrop().dontAnimate().into(albumArt);
                        scheduleStartPostponedTransition(albumArt);
                        //never called
                    }

                });
    }

    public void getCardColor(String pahh){
        Glide.with(MusicActivity.applicationContext).load(pahh).asBitmap()
                .error(R.drawable.album_art).fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE).into(new SimpleTarget<Bitmap>() {

            @Override
            public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                Palette.from(resource)
                        .generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                final int[] colors = Helpers.getAvailableColor(getApplicationContext(),palette);
                               int colorTopp = colors[0];
                               card.setCardBackgroundColor(colorTopp);
                                collapsingToolbar.setContentScrimColor(Color.TRANSPARENT);
                                collapsingToolbar.setStatusBarScrimColor(colorTopp);
                                Palette.Swatch  swatch = palette.getMutedSwatch();
                                if(swatch != null){
                                    albumName.setTextColor(Helpers.getBlackWhiteColor(swatch.getTitleTextColor()));
                                }
                                contentScrimColor = colors[0];

                            }
                        });
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                albumArt.setImageDrawable(errorDrawable);
                collapsingToolbar.setStatusBarScrimColor(ContextCompat.getColor(getApplicationContext(),R.color.coloorPrimary));

            }
        });
    }

    public static void setTranslucentStatusBar(Window window) {
        if (window == null) return;
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= Build.VERSION_CODES.LOLLIPOP) {
            setTranslucentStatusBarLollipop(window);
        }
        else if (sdkInt >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatusBarKiKat(window);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setTranslucentStatusBarLollipop(Window window) {
        window.setStatusBarColor(Color.TRANSPARENT);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void setTranslucentStatusBarKiKat(Window window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }
}
