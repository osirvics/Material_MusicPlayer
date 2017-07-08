package com.audio.effiong.musicplayer.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.afollestad.materialcab.MaterialCab;
import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.adapters.PlaylistsAdapter;
import com.audio.effiong.musicplayer.design.DividerItemDecoration;
import com.audio.effiong.musicplayer.fragments.PlaybackFragment;
import com.audio.effiong.musicplayer.model.SongModel;
import com.audio.effiong.musicplayer.services.MusicService;
import com.audio.effiong.musicplayer.utility.AddPlaylistDialog;
import com.audio.effiong.musicplayer.utility.Helpers;
import com.audio.effiong.musicplayer.utility.LastAddedLoader;
import com.audio.effiong.musicplayer.utility.ListSongs;
import com.audio.effiong.musicplayer.utility.MusicPreference;
import com.audio.effiong.musicplayer.utility.PlaylistSongLoader;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

import static com.audio.effiong.musicplayer.R.id.adView;

public class PlaylistDetailsActivity extends BaseThemedActivity implements ATEActivityThemeCustomizer,
        PlaybackFragment.OnPanelClickListener,PlaylistsAdapter.ViewHolder.ClickListener,MaterialCab.Callback {
    private long playlistId;
    private PlaylistsAdapter mAdapter;
    private RecyclerView playlistRv;
    private boolean panelOpen = false;
    private long tempSongId;
    private boolean writePermissionAked = false;
    private AdView mAdView;
    private boolean adLoaded = false;
    //private boolean setList = false;
    LinearLayout helperLayout;
    ArrayList<SongModel> playlistsongs;
    LinearLayout emptyView;
    ArrayList<SongModel> lastadded;
    private ActionMode actionMode;
    //private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private MaterialCab mCab;
    AppBarLayout appBarLayout;
    int primaryColor;

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
                case PlaybackFragment.ACTION_RELOAD:
                    reloadAdapter();
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
        filter.addAction(PlaybackFragment.ACTION_RELOAD);
        registerReceiver(br, filter);
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        MusicPreference.setPanelState(getApplicationContext(),false);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_playlist_details);
        appBarLayout =(AppBarLayout)findViewById(R.id.appbar);

        panelOpen = MusicPreference.isPanelOpen(this);
        primaryColor = Config.primaryColor(this, Helpers.getATEKey(this));
        appBarLayout.setBackgroundColor(primaryColor);
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
        });
        String title = getIntent().getExtras().getString("playlistName");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar!=null){
            toolbar.setTitle(title);
        }
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        initPanelFragment();
        playlistsongs = new ArrayList<>();
        lastadded = new ArrayList<>();
        playlistRv = (RecyclerView) findViewById(R.id.artist_list_rv);
        emptyView = (LinearLayout)findViewById(R.id.songs_empty_view);
        final int columns = getResources().getInteger(R.integer.gallery_columns);
        final GridLayoutManager gridLayoutManager =
                new GridLayoutManager(this, columns);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        playlistRv.setLayoutManager(gridLayoutManager);
        //playlistRv.setLayoutManager(new LinearLayoutManager(this));
        playlistId = getIntent().getExtras().getLong("playlistId");
        if(playlistId == -11)
            new loadLastAdded().execute("");
        else
            new loadUserCreatedPlaylist().execute("");
    }

    private void initPanelFragment() {
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.panel_holder);
        if (fragment == null) {
            fragment = PlaybackFragment.newInstance(Helpers.isKitkat());
            fm.beginTransaction()
                    .replace(R.id.panelPlaylist_holder, fragment)
                    .commit();
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



    @Override
    public void onItemClicked(int position) {
        if (mCab != null && mCab.isActive()) {
            toggleSelection(position);
        } else {
            mAdapter.handleClick(position);
        }
    }

    @Override
    public boolean onItemLongClicked(int position) {
        // if (actionMode == null) {
        // actionMode = startSupportActionMode(actionModeCallback);
        //}

        if(mCab ==null){
            mCab = new MaterialCab(this, R.id.cab_stub).setMenu(R.menu.selected_menu)
                    .setPopupMenuTheme(R.style.ThemeOverlay_AppCompat_Dark)
                    .setBackgroundColor(primaryColor).start(this);
        }
        toggleSelection(position);
        return true;
    }

    /**
     * Toggle the selection state of an item.
     *
     * If the item was the last one in the selection and is unselected, the selection is stopped.
     * Note that the selection must already be started (actionMode must not be null).
     *
     * @param position Position of the item to toggle the selection state
     */
    private void toggleSelection(int position) {
        mAdapter.toggleSelection(position);
        int count = mAdapter.getSelectedItemCount();

        if (count == 0) {
            mCab.finish();
        }
        //else {
        // actionMode.setTitle(String.valueOf(count));
        // actionMode.invalidate();
        // }
        else{
//            if (mCab == null)
//                mCab = new MaterialCab(this, R.id.cab_stub).setMenu(R.menu.selected_menu)
//                        .setBackgroundColorRes(R.color.md_black_1000).start(this);
//            else if (!mCab.isActive())
            mCab.reset().setBackgroundColor(primaryColor).setCloseDrawableRes(R.drawable.ic_close_white_24dp).start(this);
            String title = String.valueOf(count);
            mCab.setTitle(getString(R.string.x_selected,title));
        }


    }

    @Override
    public boolean onCabCreated(MaterialCab cab, Menu menu) {
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_remove:
                if(playlistId==-11){
                    mAdapter.deleteTracks();
                }
                else{
                    mAdapter.removePlaylistSongs();
                }
                //mAdapter.removeItems(adapter.getSelectedItems());
                mCab.finish();
                /// mode.finish();
                return true;
            case R.id.action_new_playlist:
                AddPlaylistDialog.newInstance(mAdapter.getSelectedSongs())
                        .show(getSupportFragmentManager(), "ADD_PLAYLIST");
                // mode.finish();
                mCab.finish();
                return true;

            default:
                return false;
        }
    }

    @Override
    public boolean onCabFinished(MaterialCab cab) {
        mAdapter.clearSelection();
        mCab = null;
        return true;
    }


//    private class ActionModeCallback implements ActionMode.Callback {
//        @SuppressWarnings("unused")
//        private final String TAG = ActionModeCallback.class.getSimpleName();
//
//        @Override
//        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
//            mode.getMenuInflater().inflate (R.menu.selected_menu, menu);
//            return true;
//        }

//        @Override
//        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//            return false;
//        }

//        @Override
//        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
//            switch (item.getItemId()) {
//                case R.id.menu_remove:
//                    //mAdapter.removeItems(adapter.getSelectedItems());
//                    mAdapter.deleteTracks();
//
//                    mode.finish();
//                    return true;
//                case R.id.action_new_playlist:
//                    AddPlaylistDialog.newInstance(mAdapter.getSelectedSongs())
//                            .show(getSupportFragmentManager(), "ADD_PLAYLIST");
//                    mode.finish();
//                    return true;
//
//                default:
//                    return false;
//            }
//        }

//        @Override
//        public void onDestroyActionMode(ActionMode mode) {
//            mAdapter.clearSelection();
//            actionMode = null;
//        }
    // }

//    @Override
//    public void onBackPressed() {
//        if (mCab != null && mCab.isActive()) {
//            mCab.finish();
//            mCab = null;
//        } else {
//            super.onBackPressed();
//        }
//    }



    private class loadLastAdded extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            lastadded =  LastAddedLoader.getLastAddedSongs(getApplicationContext());
            mAdapter = new PlaylistsAdapter(lastadded,playlistId,PlaylistDetailsActivity.this,PlaylistDetailsActivity.this);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            playlistRv.setAdapter(mAdapter);
            int spacing =  getResources().getDimensionPixelSize(R.dimen.rc_padding_left);
            RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(), spacing,false);
            playlistRv.addItemDecoration(dividerItemDecoration);
            if(lastadded.isEmpty()){
                setEmptyView();
            }
        }
        @Override
        protected void onPreExecute() {
        }
    }

    private void reloadLastAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                ArrayList<SongModel> lastadded =  LastAddedLoader.getLastAddedSongs(getApplicationContext());
                if(MusicService.getInstance().getPlaylistType()==4)
                    MusicService.getInstance().setList(lastadded);
                mAdapter.updateDataSet(lastadded);
                //mAdapter.notifyDataSetChanged();
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }


    private void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                final  ArrayList<SongModel> playlistsongs = PlaylistSongLoader.getSongsInPlaylist(getApplicationContext(), playlistId);
                if(MusicService.getInstance().getPlaylistType()==4)
                    MusicService.getInstance().setList(playlistsongs);
                mAdapter.updateDataSet(playlistsongs);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private class loadUserCreatedPlaylist extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            playlistsongs = PlaylistSongLoader.getSongsInPlaylist(getApplicationContext(), playlistId);
            mAdapter = new PlaylistsAdapter(playlistsongs,playlistId,PlaylistDetailsActivity.this,PlaylistDetailsActivity.this);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            playlistRv.setAdapter(mAdapter);
            int spacing =  getResources().getDimensionPixelSize(R.dimen.rc_padding_left);
            RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(), spacing,false);
            playlistRv.addItemDecoration(dividerItemDecoration);
            if(playlistsongs.isEmpty()){
                setEmptyView();
            }
            //setRecyclerViewAapter();
        }

        @Override
        protected void onPreExecute() {
        }
    }

    private void setEmptyView(){
        playlistRv.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }



    @Override
    public void onStop(){
        super.onStop();
        if(mAdapter!=null)
            mAdapter.setSetList(false);
        unregisterReceiver(br);
    }

    @Override
    public void onStart() {
        super.onStart();
        configReiever();
        if(mAdapter!=null)
            mAdapter.setRingtone();
        ListSongs.setDeleteEventListener(new ListSongs.DeleteEvent() {
            @Override
            public void deleteEvent() {
                //new loadUserCreatedPlaylist().execute("");
                if(playlistId == -11)
                {
                    reloadLastAdapter();
                }
                else
                    reloadAdapter();
            }
            @Override
            public void deleteSongsEvent(int position) {
            }
            @Override
            public void setBoolean(boolean flag) {
                //writePermissionAked = flag;
                mAdapter.updateWritePerms(true);

            }

        });




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // super.onCreateOptionsMenu(menu);
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fav, menu);
//
//        return super.onCreateOptionsMenu(menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
//           case android.R.id.home:
//               onBackPressed();
//                mDrawerLayout.openDrawer(GravityCompat.START);
            //return true;
            case R.id.action_equalizer:
                Helpers.navigateToEqualizer(PlaylistDetailsActivity.this);
                return true;
            case R.id.action_search:
                final Intent intent = new Intent(this, SearchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                intent.setAction(ListSongs.NAVIGATE_SEARCH);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
       /* if (isExpand) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {*/
        PlaybackFragment fragment = (PlaybackFragment) getSupportFragmentManager().findFragmentById(R.id.panelPlaylist_holder);
        if(panelOpen){
            fragment.collapsePanel();
        }
        else if (mCab != null && mCab.isActive()) {
            mCab.finish();
            mCab = null;
        }
        else {
            //overridePendingTransition(0, 0);
            super.onBackPressed();
        }
    }

}

