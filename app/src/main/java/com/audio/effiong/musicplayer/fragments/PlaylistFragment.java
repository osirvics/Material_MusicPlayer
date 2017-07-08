package com.audio.effiong.musicplayer.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.activities.PlaylistDetailsActivity;
import com.audio.effiong.musicplayer.activities.SearchActivity;
import com.audio.effiong.musicplayer.activities.SettingsActivity;
import com.audio.effiong.musicplayer.design.DividerItemDecoration;
import com.audio.effiong.musicplayer.model.Playlist;
import com.audio.effiong.musicplayer.utility.CreatePlaylistDialog;
import com.audio.effiong.musicplayer.utility.Helpers;
import com.audio.effiong.musicplayer.utility.LetterTileProvider;
import com.audio.effiong.musicplayer.utility.ListSongs;
import com.audio.effiong.musicplayer.utility.PlaylistLoader;
import com.audio.effiong.musicplayer.utility.RenamePlaylist;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

public class PlaylistFragment extends Fragment {

    private final String TAG = "PlaylistActivity";
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private ArrayList<Playlist> playlists;
    private FastScrollRecyclerView playlistRv;
    ArtistListAdapter mAdapter;
    int playlistCount;
    int spacing;
    String ateKey;
    int accentColor;
    public static final String  ACTION_UPDATE_PLAYLIST =  "ACTION_UPDATE_PLAYLIST";

    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            switch (intent.getAction()) {
                case ACTION_UPDATE_PLAYLIST:
                    updatePlaylists(false);
                    break;

            }
        }
    };


    public void configReiever(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPDATE_PLAYLIST);
        getActivity().registerReceiver(br, filter);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ateKey = Helpers.getATEKey(getActivity());
        accentColor = Config.accentColor(getActivity(), ateKey);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getActivity().unregisterReceiver(br);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        spacing =  getContext().getResources().getDimensionPixelSize(R.dimen.rc_padding_left);
        playlistRv = (FastScrollRecyclerView) view.findViewById(R.id.artist_list_rv);
        playlistRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        playlistRv.setAutoHideEnabled(true);
        playlistRv.setAutoHideDelay(1500);
        playlistRv.setThumbColor(accentColor);
        playlists = new ArrayList<>();
        new loadArtistList().execute("");
        configReiever();
        return view;
    }


    private class loadArtistList extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            playlists = PlaylistLoader.getPlaylists(getActivity(), true);
            playlistCount = playlists.size();
            mAdapter = new ArtistListAdapter(getActivity(),playlists);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            playlistRv.setAdapter(mAdapter);
            RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), spacing,false);
            playlistRv.addItemDecoration(dividerItemDecoration);
        }
    }





    public class ArtistListAdapter extends RecyclerView.Adapter<ArtistListAdapter.ArtistListHolder> {
        private ArrayList<Playlist> playlists1;
        private Context mContext;

         ArtistListAdapter(Context context, ArrayList<Playlist> givenArtist){
            this.mContext = context;
            playlists1 = givenArtist;

        }
        public void add(Playlist item, int position) {
            playlists1.add(position, item);
            //setEmptyViewVisibility();
            notifyItemInserted(position);
        }

        public void reloadAdapter() {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(final Void... unused) {
                    playlists = PlaylistLoader.getPlaylists(getActivity(), true);
                     playlistCount = playlists.size();
                     mAdapter = new ArtistListAdapter(getActivity(),playlists);
                    return null;
                }
                @Override
                protected void onPostExecute(Void aVoid) {
                    playlistRv.setAdapter(mAdapter);
                }
            }.execute();
        }



        @Override
        public ArtistListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.playlist_song_item, parent, false);
            return new ArtistListHolder(view);
        }

        @Override
        public void onBindViewHolder(ArtistListHolder holder, int position) {
            holder.menu.setColorFilter(Color.GRAY);
            final Playlist currentPlaylist = playlists1.get(position);
            final Resources res = getResources();
            final int tileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size);
            String playlistName = currentPlaylist.name;
            final LetterTileProvider tileProvider = new LetterTileProvider(getActivity());
            final Bitmap letterTile = tileProvider.getLetterTile(playlistName, playlistName, tileSize, tileSize);
            holder.thum.setImageBitmap(letterTile);
            holder.playlistName.setText(playlistName);
            setOnClicks(holder,position);
        }
        private void setOnClicks(final ArtistListHolder holder, final int position){
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Playlist item = playlists1.get(position);
                            Intent i = new Intent(getContext(), PlaylistDetailsActivity.class);
                            i.putExtra("playlistId", item.id);
                            i.putExtra("playlistName",item.name);
                            startActivity(i);
                        }
                    }, 50);

                }
            });

            holder.menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupMenu popup = new PopupMenu(getActivity(),v, Gravity.END);
                    popup.getMenuInflater().inflate(R.menu.menu_playlist, popup.getMenu());
                    popup.show();
                    Menu popupMenu = popup.getMenu();
                    int pos = holder.getAdapterPosition();
                    if(pos==0){
                        popupMenu.setGroupVisible(R.id.user_playlist_group, false);
                    }
                    else{
                        popupMenu.setGroupVisible(R.id.user_playlist_group, true);
                    }

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            Playlist item2 = playlists1.get(position);
                            switch (item.getItemId()) {
                                case R.id.action_new_playlist:
                                    CreatePlaylistDialog.newInstance().show(getChildFragmentManager(), "CREATE_PLAYLIST");
                                    break;
                                case R.id.action_rename_list:
                                    RenamePlaylist.newInstance(item2.id).show(getChildFragmentManager(), "RENAME_PLAYLIST");
                                    break;
                                case R.id.action_delete_list:
                                    String playlistId =  String.valueOf(item2.id);
                                    Helpers.deletePlaylist(getActivity(), playlistId, item2.id);
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

        @Override
        public int getItemCount() {
            return playlistCount;
        }

        public class ArtistListHolder extends RecyclerView.ViewHolder{
             TextView playlistName;
            private View mView;
            private ImageView thum,menu;
            public ArtistListHolder(View itemView) {
                super(itemView);
                mView = itemView;
                menu = (ImageView)itemView.findViewById(R.id.overflow_menu);
                thum = (ImageView)itemView.findViewById(R.id.album_details_thum);
                playlistName = (TextView)itemView.findViewById(R.id.album_details_song_title);
            }
        }
    }

    public static void update(){

    }

    public void updatePlaylists(Boolean state) {
        mAdapter.reloadAdapter();
    }
    @Override
    public void onStart(){
        super.onStart();
        Helpers.setUpdateEventListener(new Helpers.UpdatePlaylistEvent() {
            @Override
            public void updateEvent() {
                updatePlaylists(false);
            }

        });
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_playlist_action, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_playlist:
                CreatePlaylistDialog.newInstance().show(getChildFragmentManager(), "CREATE_PLAYLIST");
                return true;
            case R.id.action_search:
                final Intent intent = new Intent(getActivity(), SearchActivity.class);
                intent.setAction(ListSongs.NAVIGATE_SEARCH);
                startActivity(intent);
                return true;
            case R.id.action_settings:
                Intent  ntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(ntent);
                getActivity().overridePendingTransition(0, 0);

        }
        return super.onOptionsItemSelected(item);
    }
}
