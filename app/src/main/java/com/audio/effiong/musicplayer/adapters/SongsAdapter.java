package com.audio.effiong.musicplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
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
import com.audio.effiong.musicplayer.model.SongModel;
import com.audio.effiong.musicplayer.services.MusicService;
import com.audio.effiong.musicplayer.utility.AddPlaylistDialog;
import com.audio.effiong.musicplayer.utility.Helpers;
import com.audio.effiong.musicplayer.utility.ListSongs;
import com.audio.effiong.musicplayer.utility.SortOrder;
import com.bumptech.glide.Glide;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;

/**
 * Created by Victor on 10/9/2016.
 */

public class SongsAdapter extends SelectableAdapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter{
    private ArrayList<SongModel> theSongs;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private Context mContext;
    private String ateKey = "";
    private int accentColor;
    private SongsAdapter.SongHolder.ClickListener clickListener;
    private boolean setList = false;
    private boolean writePermissionAked = false;
    private long tempSongId;
    private String sortOrder;

    public SongsAdapter(ArrayList<SongModel> songs, Context mContext, SongsAdapter.SongHolder.ClickListener clickListener, String ateKey, int accentColor,
                        String sortOrder) {
        super();
        this.clickListener = clickListener;;
        this.theSongs = songs;
        this.mContext = mContext;
        this.ateKey = ateKey;
        this.accentColor = accentColor;
        this.setList = false;
        writePermissionAked = false;
        tempSongId = 0;
        this.sortOrder = sortOrder;


    }

    public void setSortOrder(String order){
        sortOrder = order;
    }

    public long[] getSongIds(){
        long[] songs = new long[getSelectedItems().size()];
        ArrayList<Integer> indices = new ArrayList<>(getSelectedItems());
        for(int i =0; i<indices.size();i++){
            int j = indices.get(i);
            songs[i] = theSongs.get(j-1).getSongId();
        }
        return songs;
    }

    public void deleteTracks(){
        long[] ids = getSongIds();
        ListSongs.deleteSongs(mContext,ids,getSelectedSongs());
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
            playlist.add(theSongs.get(j-1));
           // Log.e("Adpater", "Title of song: " + theSongs.get(j).getName() );
        }
        return playlist;
    }

    public void handleClick(final int position){

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               // final int itemPosition = position;
                if(!setList)
                MusicService.getInstance().setList(theSongs);
                MusicService.getInstance().setPlaylistType(1);
                setList = true;
                Intent i = new Intent();
                i.setAction(MusicService.ACTION_PLAY_ALL_SONGS);
                i.putExtra("songPos", (position-1));
                mContext.sendBroadcast(i);

            }
        }, 50);
    }



    public static class VHHeader extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {
        public final View view;
        TextView shuffle;
        ImageView shuffleImage;
        ImageView listThum;
        String key = "";
        VHHeader(View itemView, String key, Context c) {
            super(itemView);
            view = itemView;
            shuffle = (TextView) itemView.findViewById(R.id.shuffle);
            shuffleImage = (ImageView) itemView.findViewById(R.id.avatar2);
            listThum = (ImageView) itemView.findViewById(R.id.divider);
            this.key = key;

            if(key.contains("dark_theme"))
                listThum.setBackgroundColor(ContextCompat.getColor(c,R.color.progressBackgroundColorDark));

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }

        @Override
        public boolean onLongClick(View v) {
            return false;
        }
    }

    public static class SongHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {
        final View mView;
        TextView songView;
        TextView artistView;
        TextView durationView;
        ImageView avatar,menu;
        private SongHolder.ClickListener listener;
        String key = "";

        public SongHolder(View itemView, ClickListener listener, String key, Context c) {
            super(itemView);
            mView = itemView;
            songView = (TextView) itemView.findViewById(R.id.song_title1);
            artistView = (TextView) itemView.findViewById(R.id.song_artist);
            durationView = (TextView) itemView.findViewById(R.id.songduration);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            menu = (ImageView) itemView.findViewById(R.id.overflow_menu);
            this.key = key;

            if(key.contains("dark_theme"))
            menu.setColorFilter(ContextCompat.getColor(c,R.color.md_grey_100));
            else menu.setColorFilter(ContextCompat.getColor(c,R.color.md_grey_700));

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
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_ITEM) {
            //inflate your layout and pass it to view holder
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater
                    .inflate(R.layout.song, parent, false);
            return new SongHolder(view,clickListener,ateKey,mContext);
        } else if (viewType == TYPE_HEADER) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View view = layoutInflater
                    .inflate(R.layout.header, parent, false);
            return new VHHeader(view,ateKey,mContext);
        }
        throw new RuntimeException("there is no type that matches the type " + viewType + " " +
                "+ make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof SongHolder) {
            final SongHolder mholder = (SongHolder) holder;
            final SongModel currSong = getItem(position);
            Glide.with(mContext).load( ListSongs.getAlbumArtUri(
                    currSong.getAlbumId()).toString()).error(R.drawable.album_art).dontAnimate().into(mholder.avatar);
            mholder.songView.setText(currSong.getName());
            mholder.artistView.setText(currSong.getArtist());
            mholder.mView.setActivated(isSelected(position));
            hideDurView(mholder,currSong);
            mholder.menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        android.support.v7.widget.PopupMenu popup = new PopupMenu(mContext, v, Gravity.END);
                        popup.getMenuInflater().inflate(R.menu.popup_song_menu, popup.getMenu());
                        popup.show();
                        Menu popupMenu = popup.getMenu();
                        popupMenu.setGroupVisible(R.id.user_playlist_group, false);
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                switch (item.getItemId()) {
                                    case R.id.popup_song_add_to_playlist:
                                        AddPlaylistDialog.newInstance(theSongs.get(mholder.getAdapterPosition()-1))
                                                .show(((FragmentActivity)mContext).getSupportFragmentManager(), "ADD_PLAYLIST");
                                        break;
                                    case R.id.popup_song_share:
                                        ListSongs.shareSong(mContext,theSongs.get(mholder.getAdapterPosition()-1)
                                                .getPath());
                                        break;
                                    case R.id.popup_song_details:
                                        try {
                                            ListSongs.showSongDetailDialog(mContext,theSongs
                                                            .get(mholder.getAdapterPosition()-1).getPath()
                                                    ,theSongs.get(mholder.getAdapterPosition()-1)
                                                            .getName(),theSongs.get(mholder.getAdapterPosition()-1)
                                                            .getAlbumName(),theSongs.get(mholder.getAdapterPosition()-1)
                                                            .getArtist());
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(mContext, "Song corrupt or not supported", Toast.LENGTH_SHORT).show();

                                        }
                                        break;
                                    case R.id.popup_set_as_ringtone:
                                        tempSongId = theSongs.get(mholder.getAdapterPosition()-1).getSongId();
                                        ListSongs.requestSetRingtone(mContext,theSongs.get(mholder.getAdapterPosition()-1)
                                                .getSongId(),theSongs.get(mholder.getAdapterPosition()-1).getName());
                                        break;
                                    case R.id.popup_delete_music:
                                        ListSongs.deleteSong(mContext,theSongs.get(mholder.getAdapterPosition()-1)
                                                        .getName(), 0
                                                ,new long[]{theSongs.get(mholder.getAdapterPosition()-1).getSongId()},theSongs.get(mholder.getAdapterPosition()-1));
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


            // String dataItem = getItem(position);
            //cast holder to VHItem and set data
        } else if (holder instanceof VHHeader) {
            VHHeader nholder = (VHHeader) holder;
            nholder.shuffleImage.setColorFilter(accentColor);
            nholder.shuffle.setTextColor(accentColor);
            nholder.shuffle.setText(R.string.action_shuffle_all);
            nholder.shuffle.setTypeface(null, Typeface.BOLD);
            nholder.view.setOnClickListener (new View.OnClickListener () {
                @Override
                public void onClick (View view) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            MusicService.getInstance().setListShuffle(theSongs);
                           //TODO setList = false;
                            mContext.sendBroadcast(new Intent(MusicService.ACTION_SHUFFLE_SONGS ));
                        }
                    }, 50);


                }
            });
        }
    }
//        public void animateTo(ArrayList<SongModel> models) {
//            applyAndAnimateRemovals(models);
//            applyAndAnimateAdditions(models);
//            applyAndAnimateMovedItems(models);
//        }

    @Override
    public int getItemCount() {
        return theSongs.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;

        return TYPE_ITEM;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    private SongModel getItem(int position) {
        return theSongs.get(position-1);
    }

//        private void applyAndAnimateRemovals(ArrayList<SongModel> newModels) {
//            for (int i = theSongs.size() - 1; i >= 0; i--) {
//                final SongModel model = theSongs.get(i);
//                if (!newModels.contains(model)) {
//                    removeItem(i);
//                }
//            }
//        }
//
//        private void applyAndAnimateAdditions(ArrayList<SongModel> newModels) {
//            for (int i = 0, count = newModels.size(); i < count; i++) {
//                final SongModel model = newModels.get(i);
//                if (!theSongs.contains(model)) {
//                    addItem(i, model);
//                }
//            }
//        }

//        private void applyAndAnimateMovedItems(ArrayList<SongModel> newModels) {
//            for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
//                final SongModel model = newModels.get(toPosition);
//                final int fromPosition = theSongs.indexOf(model);
//                if (fromPosition >= 0 && fromPosition != toPosition) {
//                    moveItem(fromPosition, toPosition);
//                }
//            }
//        }

//        public SongModel removeItem(int position) {
//            final SongModel model = theSongs.remove(position);
//            notifyItemRemoved(position);
//            return model;
//        }
//
//        public void addItem(int position, SongModel model) {
//            theSongs.add(position, model);
//            notifyItemInserted(position);
//        }
//
//        public void moveItem(int fromPosition, int toPosition) {
//            final SongModel model = theSongs.remove(fromPosition);
//            theSongs.add(toPosition, model);
//            notifyItemMoved(fromPosition, toPosition);
//        }

    public void updateDataSet(ArrayList<SongModel> arraylist) {
        this.theSongs = arraylist;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        if(!isPositionHeader(position)){
            final String name = theSongs.get(position-1).getName();
            return name.substring(0,1);
        }
        return "";

    }

    public void hideDurView(final SongHolder holder, final SongModel item){
        if(sortOrder.contains(SortOrder.SongSortOrder.DURATION)){
            holder.durationView.setVisibility(View.VISIBLE);
            holder.durationView.setText(Helpers.getDuration(item.getDurationLong()));
        }
        else{
            holder.durationView.setVisibility(View.INVISIBLE);
        }
    }
}