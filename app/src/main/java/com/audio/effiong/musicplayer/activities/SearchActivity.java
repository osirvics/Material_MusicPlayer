package com.audio.effiong.musicplayer.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.model.SongModel;
import com.audio.effiong.musicplayer.services.MusicService;
import com.audio.effiong.musicplayer.utility.AddPlaylistDialog;
import com.audio.effiong.musicplayer.utility.ListSongs;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SearchActivity extends BaseThemedActivity implements SearchView.OnQueryTextListener, View.OnTouchListener,ATEActivityThemeCustomizer {

    private SearchView mSearchView;
    private InputMethodManager mImm;
    private String queryString;
    private View mainView, emptyView;
    private int itemPosition;

    private RecyclerView mCrimeRecyclerView;
    private ReSongAdapter mAdapter;
    private ArrayList<SongModel> songList;
    String path = "";
    private long tempSongId;
    private boolean writePermissionAked = false;
    CardView searchCard;
    CoordinatorLayout coordinatorLayout;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    @StyleRes
    @Override
    public int getActivityTheme() {
        // Overrides what's set in the current ATE Config
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ?
                R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
    }
    @Override
    protected void onStart(){
        super.onStart();
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
                mAdapter.reload(position);
            }
            @Override
            public void setBoolean(boolean flag) {
                writePermissionAked = flag;
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mImm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id
                .main_content);
        emptyView = findViewById(R.id.search_empty_view);
        searchCard = (CardView) findViewById(R.id.card_view);
        mCrimeRecyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemPosition = 0;
        songList = new ArrayList<>();
        if (checkAndRequestPermissions()) {
            initLoad();
        }

        //Fetch songs from device

        //getMySongs();
        //Sorting
//        Collections.sort(songList, new Comparator<SongModel>() {
//            public int compare(SongModel a, SongModel b) {
//                return a.getName().compareTo(b.getName());
//            }
//        });



    }
     public void initLoad(){
         songList = ListSongs.getSongList(this);
         mAdapter = new ReSongAdapter(songList);
         mCrimeRecyclerView.setAdapter(mAdapter);
         emptyView.setVisibility(View.VISIBLE);
         searchCard.setVisibility(View.GONE);
         mCrimeRecyclerView.setVisibility(View.GONE);
         mAdapter.notifyDataSetChanged();
     }


//    private void reloadAdapter() {
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(final Void... unused) {
//                songList = ListSongs.getSongList(SearchActivity.this);
//                return null;
//            }
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                mAdapter.updateDataSet(songList);
//                mAdapter.notifyDataSetChanged();
//            }
//        }.execute();
//    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_search, menu);

        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.menu_search));

        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint(getString(R.string.search_library));

        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.menu_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return false;
            }
        });

        menu.findItem(R.id.menu_search).expandActionView();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextChange(String query) {
        // Here is where we are going to implement our filter logic
//        final ArrayList<SongModel> filteredModelList = filter(songList, query);
//        mAdapter.animateTo(filteredModelList);
//        mCrimeRecyclerView.scrollToPosition(0);

        if(query!=null){
            if (query.matches("")) {
                if (emptyView != null) {
                    emptyView.setVisibility(View.VISIBLE);
                    mCrimeRecyclerView.setVisibility(View.GONE);
                    searchCard.setVisibility(View.GONE);
                }
                return false;
            }
        }
        if((query!=null)){
            emptyView.setVisibility(View.GONE);
            searchCard.setVisibility(View.VISIBLE);
            mCrimeRecyclerView.setVisibility(View.VISIBLE);
            if(mAdapter!=null){
                mAdapter.filter2(query);
                return true;
            }

            else{
                return false;
            }
        }
        else{
            return false;
        }

    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideInputManager();
        return false;
    }


    private class SongHolder extends RecyclerView.ViewHolder {
         final View mView;
         TextView songView;
         TextView artistView;
         TextView durationView;
         ImageView avatar,menu;

         SongHolder(View itemView) {
            super(itemView);
            mView = itemView;
            songView = (TextView) itemView.findViewById(R.id.song_title1);
            artistView = (TextView) itemView.findViewById(R.id.song_artist);
            durationView = (TextView) itemView.findViewById(R.id.songduration);
            menu = (ImageView) itemView.findViewById(R.id.overflow_menu);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
        }


    }


    private class ReSongAdapter extends RecyclerView.Adapter<SongHolder> {
        private ArrayList<SongModel> theSongs;
        private ArrayList<SongModel> itemsCopy;

        ReSongAdapter(ArrayList<SongModel> songs) {
            this.theSongs = new ArrayList<>(songs);
            itemsCopy = new  ArrayList<>(theSongs);
        }

        @Override
        public SongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater
                    .inflate(R.layout.search_songs, parent, false);
            return new SongHolder(view);
        }

        @Override
        public void onBindViewHolder(SongHolder holder, final int position) {
           // holder.artistView.setAlpha(0.6f);
            holder.menu.setColorFilter(Color.GRAY);
            final SongModel currSong = theSongs.get(position);
            getAlbumImage(holder, position);
            holder.songView.setText(currSong.getName());
            holder.artistView.setText(currSong.getArtist());
            setOnclick(holder,position);
        }
            private void setOnclick(final SongHolder holder, final int position){

            holder.mView.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   // int position = getAdapterPosition();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<SongModel> singleSong = new ArrayList<>();
                            final SongModel currSong = theSongs.get(position);
                            singleSong.add(currSong);
                            MusicService.getInstance().setList(singleSong);
                            Intent i = new Intent();
                            i.setAction(MusicService.ACTION_PLAY_SINGLE);
                            i.putExtra("songPos", currSong.getSongId());
                            sendBroadcast(i);
                        }
                    }, 50);

                }
            });

                holder.menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        itemPosition = holder.getAdapterPosition();
                        PopupMenu popup = new PopupMenu(SearchActivity.this,v, Gravity.END);
                        popup.getMenuInflater().inflate(R.menu.popup_song_menu, popup.getMenu());
                        popup.show();
                        Menu popupMenu = popup.getMenu();
                        popupMenu.setGroupVisible(R.id.user_playlist_group, false);
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                switch (item.getItemId()) {
                                    case R.id.popup_song_add_to_playlist:
                                        AddPlaylistDialog.newInstance(theSongs.get(position)).show(getSupportFragmentManager(), "ADD_PLAYLIST");
                                        break;
                                    case R.id.popup_song_share:
                                        ListSongs.shareSong(SearchActivity.this,theSongs.get(position).getPath());
                                        break;
                                    case R.id.popup_song_details:
                                        ListSongs.showSongDetailDialog(SearchActivity.this,theSongs.get(position).getPath()
                                                ,theSongs.get(position).getName(),theSongs.get(position).getAlbumName(),theSongs.get(position).getArtist());
                                        break;
                                    case R.id.popup_set_as_ringtone:
                                        tempSongId = theSongs.get(position).getSongId();
                                        ListSongs.requestSetRingtone(SearchActivity.this,theSongs.get(position)
                                                .getSongId(),theSongs.get(position).getName());
                                        break;
                                    case R.id.popup_delete_music:
                                        ListSongs.deleteSong(SearchActivity.this,theSongs.get(position)
                                                        .getName(), itemPosition
                                                ,new long[]{theSongs.get(position).getSongId()},theSongs.get(position));
                                       // mAdapter.reload(itemPosition );
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

        @Override
        public int getItemCount() {
            return theSongs.size();
        }

//        private void updateDataSet(ArrayList<SongModel> arraylist) {
//            this.theSongs = arraylist;
//        }

        private void getAlbumImage(final SongHolder holder, final int position) {

            final String[] albumPath = {""};
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... unused) {

                    try {
                        albumPath[0] = ListSongs.getAlbumArt(getApplicationContext(),
                                theSongs.get(position).getAlbumId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
                @Override
                protected void onPostExecute(Void aVoid) {
                    Glide.with(getApplicationContext()).load(albumPath[0]).placeholder(R.drawable.album_art).crossFade().into(holder.avatar);
                }
            }.execute();
        }


//        public SongModel removeItem(int position) {
//            final SongModel model = theSongs.remove(position);
//            notifyItemRemoved(position);
//            return model;
//        }

        private void reload(int position){
            theSongs.remove(position);
            notifyDataSetChanged();
            notifyItemRemoved(position);
        }



        private ArrayList<SongModel> filter(ArrayList<SongModel> models, String query) {

            query = query.toLowerCase();

            final ArrayList<SongModel> filteredModelList = new ArrayList<>();
            for (SongModel model : models) {
                final String text = model.getName().toLowerCase();
                final String text2 = model.getArtist().toLowerCase();
                if (text.contains(query)||text2.contains(query)) {
                    filteredModelList.add(model);
                }
            }
            return filteredModelList;
        }


        private void filter2(String text) {

                if(text.isEmpty()){
                    theSongs.clear();
                    theSongs.addAll(itemsCopy);
                } else{
                    ArrayList<SongModel> result = new ArrayList<>();
                        text = text.toLowerCase();
                    //final String text = item.getName().toLowerCase();
                   // final String text2 = model.getArtist().toLowerCase();
                    for(SongModel item: itemsCopy){
                        if(item!=null){
                            try {
                                if(item.getName().toLowerCase().contains(text) || item.getArtist().toLowerCase().contains(text)){
                                    result.add(item);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    theSongs.clear();
                    theSongs.addAll(result);
                }
                notifyDataSetChanged();
        }

    }



    public void hideInputManager() {
        if (mSearchView != null) {
            if (mImm != null) {
                mImm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
            }
            mSearchView.clearFocus();
        }
    }

    private boolean checkAndRequestPermissions() {
        int permissionReadStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionEriteStorage = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionEriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionReadStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        //Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        //Log.d(TAG, "read & write services permission granted");
                        // process the normal flow
                        initLoad();
                        //else any one or both the permissions are not granted
                    } else {
                        //Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                            Snackbar snackbar = Snackbar
                                    .make(coordinatorLayout, "External storage permission required for this app to acess and play music", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Grant", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            checkAndRequestPermissions();
                                        }
                                    });
                            snackbar.setActionTextColor(ContextCompat.getColor(this,R.color
                                    .colorAccent));
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.DKGRAY);
                            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
                        }
                        else {
                            Snackbar snackbar = Snackbar
                                    .make(coordinatorLayout, "Go to settings and enable permissions", Snackbar.LENGTH_INDEFINITE)
                                    .setAction("Settings", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                                            intent.setData(uri);
                                            startActivity(intent);
                                        }
                                    });
                            snackbar.setActionTextColor(ContextCompat.getColor(this,R.color
                                    .colorAccent));
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.DKGRAY);
                            TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                            textView.setTextColor(Color.WHITE);
                            snackbar.show();
//                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
//                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }


}
