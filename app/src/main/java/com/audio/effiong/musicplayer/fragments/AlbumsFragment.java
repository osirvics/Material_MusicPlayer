package com.audio.effiong.musicplayer.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.activities.AlbumDetailsActivity;
import com.audio.effiong.musicplayer.design.PaletteBitmap;
import com.audio.effiong.musicplayer.design.PaletteBitmapTranscoder;
import com.audio.effiong.musicplayer.model.Album;
import com.audio.effiong.musicplayer.utility.Helpers;
import com.audio.effiong.musicplayer.utility.ListSongs;
import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by Effiong on 04-Feb-16.
 */
public class AlbumsFragment extends Fragment {
    private FastScrollRecyclerView gv;
    private AlbumsListAdapter adapter;
    private ArrayList<Album> albumList;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private int itemPosition;
    String ateKey;
    int accentColor,colorPrimary;
    private final String TAG = "AlbumsFragment";
    LinearLayout emptyView;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ateKey = Helpers.getATEKey(getActivity());
        accentColor = Config.accentColor(getActivity(), ateKey);
        colorPrimary = Config.primaryColor(getActivity(), ateKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.albums_fragment, container, false);
        gv = (FastScrollRecyclerView) rootView.findViewById(R.id.recyclerview_grid);
        emptyView = (LinearLayout)rootView.findViewById(R.id.songs_empty_view);
        gv.setLayoutManager(new LinearLayoutManager(getActivity()));
        gv.setAutoHideEnabled(true);
        gv.setAutoHideDelay(1500);
        gv.setThumbColor(accentColor);
        gv.setPopupBgColor(accentColor);
        gv.setPopupTextColor(ContextCompat.getColor(getActivity(),R.color.md_white_1000_75));
            setList();
        return rootView;

    }
    private  boolean checkAndRequestPermissions() {
        int permissionReadStorage = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionEriteStorage = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionEriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionReadStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            requestPermissions(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        //Log.e(TAG, "Permission callback called-------");
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
                        //Log.e(TAG, "read & write services permission granted");
                        // process the normal flow
                        setList();
                        //else any one or both the permissions are not granted
                    } else {
                        //Log.e(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showDialogOK("Read and Write Services Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(getActivity(), "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


    private void setList() {
        new Thread(new Runnable() {
            public void run() {
                    // carry on the normal flow, as the case of  permissions  granted.
                    albumList = ListSongs.getAlbumList(getContext());
//                final GridLayoutManager gridLayoutManager =
//                        new GridLayoutManager(getContext(), 2);
                final int columns = getResources().getInteger(R.integer.album_columns);
                final GridLayoutManager gridLayoutManager =
                        new GridLayoutManager(getActivity(), columns);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
//                        gridLayoutManager.scrollToPosition(0);
//                        gv.setLayoutManager(gridLayoutManager);
                        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                        gv.setLayoutManager(gridLayoutManager);
                        adapter = new AlbumsListAdapter(albumList,Glide.with(getActivity()));
                        gv.setAdapter(adapter);
                    }
                });
                if (albumList.isEmpty()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setEmptyView();
                        }
                    });
                }
            }
        }).start();
    }

    private void setEmptyView(){
        gv.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }
    public class AlbumsListAdapter extends RecyclerView.Adapter<AlbumsListAdapter.AlbumHolder> implements FastScrollRecyclerView.SectionedAdapter {
        private ArrayList<Album> albums;
        private final BitmapRequestBuilder<String, PaletteBitmap> glideRequest;
        private final int defaultColor;

        public AlbumsListAdapter(ArrayList<Album> theAlbums,RequestManager glide) {
            albums = theAlbums;
            this.defaultColor = ContextCompat.getColor(getContext(), R.color.coloorPrimary);;
            this.glideRequest = glide
                    .fromString()
                    .asBitmap()
                    .transcode(new PaletteBitmapTranscoder(getActivity()), PaletteBitmap.class)
                    .placeholder(R.drawable.album_art)
                    .fitCenter()
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE);
        }

        @Override
        public AlbumHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater
                    .inflate(R.layout.album_list_items, parent, false);
            return new AlbumHolder(view);
        }

        @Override
        public void onBindViewHolder(final AlbumHolder holder, int position) {
            final Album currentAlbum = albums.get(position);
            holder.albumName.setText(currentAlbum.getAlbumTitle());
            holder.artistName.setText(String.valueOf(currentAlbum.getSongNumber())+ " "+  addSuffix(currentAlbum));
            //String path = ListSongs.getAlbumArtUri(currentAlbum.getAlbumId()).toString();
            String path = ListSongs.getAlbumArtUri(currentAlbum.getAlbumId()).toString();
            holder.card.setBackgroundColor(this.defaultColor);
            if (path != null) { // simulate an optional url from the data item
                holder.icon.setVisibility(View.VISIBLE);
                glideRequest
                        .load(path)
                        .into(holder.target);
            } else {
                Glide.clear(holder.icon);
               // holder.icon.setVisibility(View.GONE);
            }
            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    itemPosition = gv.getChildAdapterPosition(v);
                    Album playSong = albumList.get(itemPosition);
                    final Intent i = new Intent(getContext(), AlbumDetailsActivity.class);
                    i.putExtra("albumName", playSong.getAlbumTitle());
                    i.putExtra("albumId", playSong.getAlbumId());
                    if(Helpers.isLollipop())
                        holder.icon.setTransitionName("transition_album_art"  + itemPosition);
                    i.putExtra("transitionName",itemPosition);
                    final List<Pair<View, String>> pairs = new ArrayList<>();
                    pairs.add(Pair.create(((View)holder.icon), "transition_album_art"  + itemPosition));
                    android.os.Handler handler = new android.os.Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(Helpers.isLollipop()){
                                Bundle options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(),
                                        pairs.toArray(new Pair[pairs.size()])).toBundle();
                                startActivity(i,options);
                            }
                            else{
                                startActivity(i);
                            }
                        }
                    }, 50);
                }
            });


        }

        @Override
        public int getItemCount() {
            return albums.size();
        }

        @Override
        public void onViewRecycled(AlbumHolder holder) {
            super.onViewRecycled(holder);
            Glide.clear(holder.icon);
        }

        class AlbumHolder extends RecyclerView.ViewHolder {
            TextView albumName;
            public TextView artistName;
            public ImageView icon;
            final View mView;
            RelativeLayout card;
            final Target<PaletteBitmap> target;

            public AlbumHolder(View itemView) {
                super(itemView);
                mView = itemView;
                albumName = (TextView) itemView.findViewById(R.id.album_name);
                artistName = (TextView) itemView.findViewById(R.id.line2);
                icon = (ImageView) itemView.findViewById(R.id.album_icon);
                card = (RelativeLayout)itemView.findViewById(R.id.rel_title_layout);
                target = new PaletteBitmapImageViewTarget(this, AlbumsListAdapter.this.defaultColor);

            }
        }

        private  class PaletteBitmapImageViewTarget extends ImageViewTarget<PaletteBitmap> {
            private final AlbumsFragment.AlbumsListAdapter.AlbumHolder holder;
            PaletteBitmapImageViewTarget(AlbumsFragment.AlbumsListAdapter.AlbumHolder holder, int defaultColor) {
                super(holder.icon);
                this.holder = holder;
            }
            @Override protected void setResource(PaletteBitmap resource) {
                super.view.setImageBitmap(resource.bitmap);
            }
            @Override public void onResourceReady(PaletteBitmap resource,
                                                  GlideAnimation<? super PaletteBitmap> glideAnimation) {
                if (glideAnimation == null || !glideAnimation.animate(resource, this)) {
                    setResource(resource);
                    setColors(resource.palette);
                    return;
                }
                setColors(resource.palette);
            }

            private void setColors(Palette palette) {
                final int[] colors = Helpers.getAvailableColor(getActivity(),palette);
                Palette.Swatch swatch = palette.getMutedSwatch();

                holder.card.setBackgroundColor(colors[0]);
                if (swatch != null) {
                    int textColor = Helpers.getBlackWhiteColor(swatch.getTitleTextColor());
                    holder.albumName.setTextColor(textColor);
                    holder.artistName.setTextColor(textColor);
                }
            }
        }

        private String addSuffix( Album curetAlbum){
            if(curetAlbum.getSongNumber()==1)
                return "Music";
            else {
                return "Musics";
            }

        }


        @NonNull
        @Override
        public String getSectionName(int position) {
            try {
                final String name = albums.get(position).getAlbumTitle();
                String s=name.substring(0,1);
                return s;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

}