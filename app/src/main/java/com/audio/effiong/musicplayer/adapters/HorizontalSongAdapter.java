package com.audio.effiong.musicplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.model.SongModel;
import com.audio.effiong.musicplayer.services.MusicService;
import com.audio.effiong.musicplayer.utility.ListSongs;
import com.audio.effiong.musicplayer.utility.MusicPreference;
import com.bumptech.glide.Glide;

import java.util.ArrayList;


public class HorizontalSongAdapter extends RecyclerView.Adapter<HorizontalSongAdapter.HorizontalSongHolder> {

    private ArrayList<SongModel> songs;
    private Context context;


    public HorizontalSongAdapter(ArrayList<SongModel> songs, Context context){
        this.songs = songs;
        this.context = context;


    }
    @Override
    public HorizontalSongHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.playback_playlist_item, parent, false);
        return  new HorizontalSongHolder(view);

    }

    @Override
    public void onBindViewHolder(HorizontalSongHolder holder, int position) {
        SongModel currentSong = songs.get(position);
        holder.textView.setText(currentSong.getName());
        Glide.with(context).load(ListSongs.getAlbumArtUri(
                currentSong.getAlbumId()).toString()).error(R.drawable.album_art).dontAnimate().into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    public class HorizontalSongHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView imageView;
        TextView textView;
        public HorizontalSongHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.avatar);
            textView = (TextView)itemView.findViewById(R.id.song_title);
            itemView.setOnClickListener(this);         }

        @Override
        public void onClick(View v) {
            Intent i = new Intent();
            int playBackType = MusicPreference.getLastSongListType(context);
            String intentString = MusicService.getPlayedTyped(playBackType);
            i.setAction(intentString);
            i.putExtra("songPos", getLayoutPosition());
            context.sendBroadcast(i);

        }
    }
}
