package com.audio.effiong.musicplayer.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.activities.ArtistDetailsActivity;
import com.audio.effiong.musicplayer.model.Artist;
import com.audio.effiong.musicplayer.model.SongModel;
import com.audio.effiong.musicplayer.utility.AddPlaylistDialog;
import com.audio.effiong.musicplayer.utility.ListSongs;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;


public class ArtistListAdapter extends RecyclerView.Adapter<ArtistListAdapter.ArtistListHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private ArrayList<Artist> artists;
    private Context mContext;
    private ArrayList<SongModel> playListAdd;



    public ArtistListAdapter(Context context, ArrayList<Artist> givenArtist) {
        this.mContext = context;
        artists = givenArtist;
        playListAdd = new ArrayList<>();


    }


    @Override
    public ArtistListHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.artist_item, parent, false);
        return new ArtistListHolder(view);
    }

    @Override
    public void onBindViewHolder(ArtistListHolder holder, int position) {
        holder.menu.setColorFilter(Color.GRAY);
       // holder.numberOfSongs.setAlpha(0.6f);
        final Artist currentArtist = artists.get(position);
       /* Glide.with(mContext).load(current.getUri()).placeholder(R.drawable.bg_default_album_art)
                .error(R.drawable.bg_default_album_art).crossFade().into(holder.avatar);*/
        holder.artistName.setText(currentArtist.getArtistName());
        holder.numberOfSongs.setText(String.valueOf(currentArtist.getNumberOfSongs()) + " " + addSuffix(currentArtist));
        setOnClicks(holder, position);
    }

    private void setOnClicks(final ArtistListHolder holder, final int position) {
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(mContext, ArtistDetailsActivity.class);
                        i.putExtra("name", artists.get(position).getArtistName());
                        mContext.startActivity(i);
                        //((Activity) mContext).overridePendingTransition(0, 0);
                    }
                }, 50);

//                new AsyncTask<Void, Void, Void>() {
//                    @Override
//                    protected Void doInBackground(final Void... unused) {
//                        Intent i = new Intent(mContext, ArtistDetailsActivity.class);
//                        i.putExtra("name", artists.get(position).getArtistName());
//                        mContext.startActivity(i);
//                        ((Activity) mContext).overridePendingTransition(0, 0);
//                        return null;
//                    }
//                    @Override
//                    protected void onPostExecute(Void aVoid) {
//
//                        // mAdapter.notifyDataSetChanged();
//                    }
//                }.execute();
            }
        });

        holder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPlaylist(artists.get(position).getArtistName());
                try {
                    android.support.v7.widget.PopupMenu popup = new android.support.v7.widget.PopupMenu(mContext, v, Gravity.END);
                    popup.getMenuInflater().inflate(R.menu.menu_artist, popup.getMenu());
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.popup_song_add_to_playlist:
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            AddPlaylistDialog.newInstance(playListAdd).show(((FragmentActivity)mContext).getSupportFragmentManager(), "ADD_PLAYLIST");
                                        }
                                    }, 300);

                                    break;
//                                case R.id.popup_delete_music:
////                                    ListSongs.deleteSong(mContext,theAlbumSongs.get(position)
////                                                    .getName(), 0
////                                            ,theAlbumSongs.get(position).getSongId(),theAlbumSongs.get(position));
//
//                                     break;
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
        return artists.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        try {
            final String name = artists.get(position).getArtistName();
            String s = name.substring(0, 1);
            return s;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public String addSuffix(Artist curetAlbum) {
        if (curetAlbum.getNumberOfSongs() == 1)
            return "Song";
        else {
            return "Songs";
        }

    }

    public class ArtistListHolder extends RecyclerView.ViewHolder {
        public TextView artistName;
        public TextView numberOfSongs;
        private View mView;
        private ImageView thum,menu;

        public ArtistListHolder(View itemView) {
            super(itemView);
            mView = itemView;
            thum = (ImageView) itemView.findViewById(R.id.album_details_thum);
            menu = (ImageView) itemView.findViewById(R.id.overflow_menu);
            artistName = (TextView) itemView.findViewById(R.id.album_details_song_title);
            numberOfSongs = (TextView) itemView.findViewById(R.id.album_details_song_artist);

        }
    }

    private class loadArtistList extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            //artistSongList = ListSongs.getSongsListOfArtist(mContext,name);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }
    private void getPlaylist(final String name) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                playListAdd = ListSongs.getSongsListOfArtist(mContext,name);

                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {

            }
        }.execute();

    }


}
