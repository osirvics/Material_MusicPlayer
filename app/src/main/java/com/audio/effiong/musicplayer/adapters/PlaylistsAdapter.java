package com.audio.effiong.musicplayer.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.fragments.PlaybackFragment;
import com.audio.effiong.musicplayer.model.SongModel;
import com.audio.effiong.musicplayer.services.MusicService;
import com.audio.effiong.musicplayer.utility.AddPlaylistDialog;
import com.audio.effiong.musicplayer.utility.ListSongs;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by Victor on 10/8/2016.
 */

public class PlaylistsAdapter extends SelectableAdapter<PlaylistsAdapter.ViewHolder> {
    private ArrayList<SongModel> theAlbumSongs;
    private Context mContext;
    private long playlistId;
    private boolean setList;
    private boolean writePermissionAked = false;
    private long tempSongId;

    private ViewHolder.ClickListener clickListener;
    public PlaylistsAdapter(ArrayList<SongModel> songs, long playlistId, Context context, ViewHolder.ClickListener clickListener) {
        super();
        this.theAlbumSongs = songs;
        this.mContext = context;
        this.clickListener = clickListener;
        this.playlistId = playlistId;
        this.setList = false;
        writePermissionAked = false;
        tempSongId = 0;
    }

    public void handleClick(final int position){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(MusicService.getInstance()!=null){
                    if(!setList)
                        MusicService.getInstance().setList(theAlbumSongs);
                    MusicService.getInstance().setPlaylistType(4);
                    setList = true;
                    Intent i = new Intent();
                    i.setAction(MusicService.ACTION_PLAY_PLAYLIST);
                    i.putExtra("songPos", position);
                    i.putExtra("playlistID",playlistId);
                    mContext.sendBroadcast(i);
                }
                else{
                    Toast.makeText(mContext.getApplicationContext(), "Service is not running, please restart player", Toast.LENGTH_SHORT).show();
                }

            }
        }, 50);
    }

    public long[] getSongIds(){
        long[] songs = new long[getSelectedItems().size()];
        ArrayList<Integer> indices = new ArrayList<>(getSelectedItems());
        for(int i =0; i<indices.size();i++){
            int j = indices.get(i);
            songs[i] = theAlbumSongs.get(j).getSongId();
        }
        return songs;
    }

    public void deleteTracks(){
        long[] ids = getSongIds();
        ListSongs.deleteSongs(mContext,ids,getSelectedSongs());
    }


    public void removePlaylistSongs(){
        ListSongs.deletePlaylists(mContext,playlistId,getSongIds());
    }

    public void setSetList(boolean flag){
        setList = flag;
    }
    public void updateWritePerms(boolean flag){
        writePermissionAked = flag;
    }
    public void setRingtone(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (writePermissionAked) {
                if (Settings.System.canWrite(mContext))
                    ListSongs.setRingtone(mContext, tempSongId);
            }
            writePermissionAked = false;
        }
    }


    public ArrayList<SongModel> getSelectedSongs(){
        ArrayList<SongModel> playlist = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>(getSelectedItems());
        for(int i = 0; i < indices.size(); i++){
            int j = indices.get(i);
            playlist.add(theAlbumSongs.get(j));
            Log.e("Adpater", "Title of song: " + theAlbumSongs.get(j).getName() );
        }
        return playlist;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {
        TextView titelView;
        TextView artistView;
        ImageView thumb,menu;
        //LinearLayout background;
        // View selectedOverlay;
        final View mView;
        private ClickListener listener;

        public ViewHolder(View itemView, ClickListener listener) {
            super(itemView);
            mView = itemView;
            titelView = (TextView) itemView.findViewById(R.id.album_details_song_title);
            artistView = (TextView) itemView.findViewById(R.id.album_details_song_artist);
            menu = (ImageView) itemView.findViewById(R.id.overflow_menu);
            thumb = (ImageView)itemView.findViewById(R.id.album_details_thum);
            //background = (LinearLayout)itemView.findViewById(R.id.main_background);
            //selectedOverlay = itemView.findViewById(R.id.selected_overlay);

            this.listener = listener;

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null) {
                listener.onItemClicked(getLayoutPosition());
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (listener != null) {
                return listener.onItemLongClicked(getLayoutPosition());
            }
            return false;
        }

        public interface ClickListener {
            void onItemClicked(int position);
            boolean onItemLongClicked(int position);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater
                .inflate(R.layout.artist_item, parent, false);
        return new ViewHolder(view,clickListener);
    }

    @Override
    public void onBindViewHolder(final PlaylistsAdapter.ViewHolder holder, int position) {
        final SongModel currSong = theAlbumSongs.get(position);
        holder.menu.setColorFilter(Color.GRAY);

        //getAlbumImage(holder,currSong);
        final String artUrl = ListSongs.getAlbumArtUri(currSong.getAlbumId()).toString();
        Glide.with(mContext).load(artUrl).placeholder(R.drawable.album_art).crossFade().into(holder.thumb);

        holder.titelView.setText(currSong.getName());
        holder.artistView.setText(currSong.getArtist());
        //holder.selectedOverlay.setVisibility(isSelected(position) ? View.VISIBLE : View.INVISIBLE);
        //holder.background.setActivated(isSelected(position));
        holder.mView.setActivated(isSelected(position));


        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    android.support.v7.widget.PopupMenu popup = new android.support.v7.widget.PopupMenu(mContext, v, Gravity.END);
                    popup.getMenuInflater().inflate(R.menu.popup_song_menu, popup.getMenu());
                    popup.show();
                    Menu popupMenu = popup.getMenu();
                    //int pos = holder.getAdapterPosition();
                    if(playlistId==-11){
                        popupMenu.setGroupVisible(R.id.user_playlist_group, false);
                    }
                    else{
                        popupMenu.setGroupVisible(R.id.user_playlist_group, true);
                    }
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.popup_song_add_to_playlist:
                                    AddPlaylistDialog.newInstance(theAlbumSongs.get(holder.getAdapterPosition())).show(((FragmentActivity)mContext).getSupportFragmentManager(), "ADD_PLAYLIST");
                                    break;
                                case R.id.popup_song_share:
                                    ListSongs.shareSong(mContext,theAlbumSongs.get(holder.getAdapterPosition()).getPath());
                                    break;
                                case R.id.popup_song_details:
                                    ListSongs.showSongDetailDialog(mContext,theAlbumSongs.get(holder.getAdapterPosition()).getPath()
                                            ,theAlbumSongs.get(holder.getAdapterPosition()).getName(),theAlbumSongs.get(holder.getAdapterPosition()).getAlbumName(),theAlbumSongs.get(holder.getAdapterPosition()).getArtist());
                                    break;
                                case R.id.popup_set_as_ringtone:
                                    tempSongId = theAlbumSongs.get(holder.getAdapterPosition()).getSongId();
                                    if(mContext!=null)
                                        ListSongs.requestSetRingtone(mContext,theAlbumSongs.get(holder.getAdapterPosition())
                                                .getSongId(),theAlbumSongs.get(holder.getAdapterPosition()).getName());
                                    break;
                                case R.id.popup_delete_music:
                                    ListSongs.deleteSong(mContext,theAlbumSongs.get(holder.getAdapterPosition())
                                                    .getName(), 0
                                            ,new long[]{theAlbumSongs.get(holder.getAdapterPosition()).getSongId()},theAlbumSongs.get(holder.getAdapterPosition()));
                                    break;
                                case R.id.popup_remove:
                                    removeSongFromPlaylist(mContext,playlistId,theAlbumSongs.get(holder.getAdapterPosition()).getSongId());
                                    break;
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


  /*  private void getAlbumImage(final PlaylistsAdapter.ViewHolder holder, final SongModel currentArtist) {
        final String[] path = {""};
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                    if(mContext!=null)
                path[0] = ListSongs.getAlbumArt(mContext, currentArtist.getAlbumId());
                //List<Album> albumList = AlbumLoader.getAllAlbums(getActivity());
                // mAdapter.updateDataSet(albumList);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                if(mContext!=null)
                Glide.with(mContext).load(path[0]).placeholder(R.drawable.album_art).crossFade().into(holder.thumb);
                // mAdapter.notifyDataSetChanged();
            }
        }.execute();*/



    @Override
    public int getItemCount() {
        return theAlbumSongs.size();
    }

    public void updateDataSet(ArrayList<SongModel> arraylist) {
        this.theAlbumSongs = arraylist;
        //this.songIDs = getSongIds();
    }


    public  void removeSongFromPlaylist(Context ctx, long playlist_id, long song_id) {
        ContentResolver resolver = ctx.getContentResolver();
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist_id);


//        String audioId1 = Long.toString(song_id);
//        String where = "audioId1=? LIMIT 1";
//        String[] args = {audioId1};
//
//        int res =  resolver.delete(uri, where, args);

        int res = resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID+" = "+song_id, null);
        // TODO reloadAdapter();
        Toast.makeText(mContext,"Song removed",Toast.LENGTH_LONG).show();
        ctx.sendBroadcast(new Intent(PlaybackFragment.ACTION_RELOAD));
        // reloadPlaylistList(ctx);
        //return res;
    }
}
