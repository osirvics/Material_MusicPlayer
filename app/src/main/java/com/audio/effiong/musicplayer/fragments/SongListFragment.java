package com.audio.effiong.musicplayer.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.afollestad.materialcab.MaterialCab;
import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.adapters.SongsAdapter;
import com.audio.effiong.musicplayer.design.DividerItemDecoration;
import com.audio.effiong.musicplayer.model.SongModel;
import com.audio.effiong.musicplayer.services.MusicService;
import com.audio.effiong.musicplayer.utility.AddPlaylistDialog;
import com.audio.effiong.musicplayer.utility.Helpers;
import com.audio.effiong.musicplayer.utility.ListSongs;
import com.audio.effiong.musicplayer.utility.MusicPreference;
import com.audio.effiong.musicplayer.utility.SortOrder;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class SongListFragment extends Fragment implements SongsAdapter.SongHolder.ClickListener, MaterialCab.Callback  {
    private ArrayList<SongModel> songList;
    private SongsAdapter mAdapter;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private boolean writePermissionAked = false;
    private long tempSongId;
    FastScrollRecyclerView recyclerView;
    String ateKey;
    int accentColor,colorPrimary;
    LinearLayout ll;
    private boolean setList = false;
    int spacing;
    LinearLayout emptyView;
    MenuItem item;
    public String sortOrder = SortOrder.SongSortOrder.TITLE;
    private MaterialCab mCab;




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ateKey = Helpers.getATEKey(getActivity());
        accentColor = Config.accentColor(getActivity(), ateKey);
        colorPrimary = Config.primaryColor(getActivity(), ateKey);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootview = inflater.inflate(R.layout.song_list_fragment, container, false);
       spacing =  getContext().getResources().getDimensionPixelSize(R.dimen.rc_padding_left);
        ll = (LinearLayout) rootview.findViewById(R.id.linear_layout);
        recyclerView = (FastScrollRecyclerView) rootview.findViewById(R.id.recyclervieww);
        emptyView = (LinearLayout)rootview.findViewById(R.id.songs_empty_view);
        //recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        final int columns = getResources().getInteger(R.integer.gallery_columns);
        final GridLayoutManager gridLayoutManager =
                new GridLayoutManager(getActivity(), columns);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAutoHideEnabled(true);
        recyclerView.setAutoHideDelay(1500);
        recyclerView.setThumbColor(accentColor);
        recyclerView.setPopupBgColor(accentColor);
        recyclerView.setPopupTextColor(ContextCompat.getColor(getActivity(),R.color.md_white_1000_75));
        tempSongId = 0;
        songList = new ArrayList<>();
        loadSongs(this);
        return rootview;
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
        if(mCab ==null){
            mCab = new MaterialCab((AppCompatActivity) getActivity(), R.id.cab_stubb).setMenu(R.menu.selected_menu)
                    .setBackgroundColor(colorPrimary).start(this);
        }
        toggleSelection(position);
        return true;
    }

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
            mCab.reset().setBackgroundColor(colorPrimary).setCloseDrawableRes(R.drawable.ic_close_white_24dp).start(this);
            String title = String.valueOf(count);
           // mCab.setTitle( title +" "+ getString(R.string.x_selected));
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
                //mAdapter.removeItems(adapter.getSelectedItems());
               mAdapter.deleteTracks();
                mCab.finish();
                /// mode.finish();
                return true;
            case R.id.action_new_playlist:
                AddPlaylistDialog.newInstance(mAdapter.getSelectedSongs())
                       .show(((FragmentActivity)getActivity()).getSupportFragmentManager(), "ADD_PLAYLIST");
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

    private void loadSongs(final SongsAdapter.SongHolder.ClickListener c) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                sortOrder = MusicPreference.getSortOrder(getActivity());
                songList = ListSongs.getSongListSOrt(getActivity());
                mAdapter = new SongsAdapter(songList,getActivity(), c,ateKey,accentColor,sortOrder);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {

                recyclerView.setAdapter(mAdapter);
                RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), spacing,true);
                recyclerView.addItemDecoration(dividerItemDecoration);
                if(songList.isEmpty())
                    setEmptyView();
            }
        }.execute();
    }

//    private class loadSongs extends AsyncTask<String, Void, String> {
//
//        @Override
//        protected String doInBackground(String... params) {
//            sortOrder = MusicPreference.getSortOrder(getActivity());
//            songList = ListSongs.getSongListSOrt(getActivity());
//            mAdapter = new SongsAdapter(songList,getActivity(), this,ateKey,accentColor);
//            return "Executed";
//        }
//        @Override
//        protected void onPostExecute(String result) {
//            recyclerView.setAdapter(mAdapter);
//            RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), spacing,true);
//            recyclerView.addItemDecoration(dividerItemDecoration);
//                if(songList.isEmpty())
//                    setEmptyView();
//        }
//
//    }

    private void setEmptyView(){
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
    }

    @Override
      public void onActivityCreated(final Bundle savedInstanceState) {
           super.onActivityCreated(savedInstanceState);
          setHasOptionsMenu(true);
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
        String TAG = "SongListFragment";
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
                        loadSongs(this);
                        //iniViews();
                        //else any one or both the permissions are not granted
                    } else {
                        //Log.e(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showDialogOK("Read and Write Services Permission required for this app", "Permission Request",
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

    private void showDialogOK(String message,String title, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }

    private void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                sortOrder = MusicPreference.getSortOrder(getActivity());
                songList = ListSongs.getSongListSOrt(getActivity());
                if(MusicService.getInstance().getPlaylistType()==1)
                  MusicService.getInstance().setList(songList);
                return null;
            }
            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.setSortOrder(sortOrder);
                mAdapter.updateDataSet(songList);
               mAdapter.notifyDataSetChanged();
                if(songList.isEmpty()){
                    setEmptyView();
                }
            }
        }.execute();
    }


    @Override
    public void onStart() {
        super.onStart();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (writePermissionAked) {
//                if (Settings.System.canWrite(getActivity()))
//                    ListSongs.setRingtone(getActivity(), tempSongId);
//            }
//            writePermissionAked = false;
//        }
        if(mAdapter!=null)
            mAdapter.setRingtone();
        ListSongs.setDeleteEventListener(new ListSongs.DeleteEvent() {
            @Override
            public void deleteEvent() {
                reloadAdapter();
            }

            @Override
            public void deleteSongsEvent(int position) {

            }
            @Override
            public void setBoolean(boolean flag) {
                mAdapter.updateWritePerms(true);
            }

        });
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();
        super.onResume();

    }
    @Override
    public void onStop() {
        super.onStop();
        setList = false;
        if(mAdapter!=null)
        mAdapter.setSetList(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_sort, menu);
        //MenuItem item;

        final String orderBy = MusicPreference.getSortOrder(getActivity());
        if(orderBy.contains(SortOrder.SongSortOrder.TITLE)){
            item = menu.findItem(R.id.menu_sort_by_title);
            item.setChecked(true);
        }
        else if(orderBy.contains(SortOrder.SongSortOrder.SONG_ARTIST)){
           // menu.getItem( R.id.menu_sort_by_artist).setChecked(true);
            item = menu.findItem( R.id.menu_sort_by_artist);
            item.setChecked(true);
        }
        else if(orderBy.contains(SortOrder.SongSortOrder.DATE_ADDED)){
           // menu.getItem( R.id.menu_sort_by_date).setChecked(true);
            item = menu.findItem(R.id.menu_sort_by_date);
            item.setChecked(true);
        }
        else if(orderBy.contains(SortOrder.SongSortOrder.DURATION)){
           // menu.getItem( R.id.menu_sort_by_duration).setChecked(true);
            item = menu.findItem(R.id.menu_sort_by_duration);
            item.setChecked(true);
        }
//        ImageButton mButton = (ImageButton) findViewById(R.id.button);
//        final Drawable buttonIcon = context.getResources().getDrawable(R.mipmap.your_icon);
//        buttonIcon.setAlpha(138); //this is the value of opacity 1~255
//        mButton.setBackground(buttonIcon);
//     Drawable drawable = menu.getItem(R.id.menu_sort_by).getIcon();
//    if(drawable != null) {
//           drawable.mutate();
//           drawable.setAlpha(138);
//      }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_title:
                MusicPreference.setSortOrder(getActivity(), SortOrder.SongSortOrder.TITLE);
                sortOrder = SortOrder.SongSortOrder.TITLE;
               reloadAdapter();
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                return true;
            case R.id.menu_sort_by_artist:
                MusicPreference.setSortOrder(getActivity(),SortOrder.SongSortOrder.SONG_ARTIST);
                sortOrder = SortOrder.SongSortOrder.SONG_ARTIST;
                reloadAdapter();
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                return true;
            case R.id.menu_sort_by_date:
                MusicPreference.setSortOrder(getActivity(), SortOrder.SongSortOrder.DATE_ADDED);
                sortOrder = SortOrder.SongSortOrder.DATE_ADDED;
               reloadAdapter();
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                return true;
            case R.id.menu_sort_by_duration:
                MusicPreference.setSortOrder(getActivity(), SortOrder.SongSortOrder.DURATION );
                sortOrder = SortOrder.SongSortOrder.DURATION;
                reloadAdapter();
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                return true;
//            case R.id.menu_sort_by_year:
//               // mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_YEAR);
//                reloadAdapter();
//                return true;
//            break;
//            case R.id.menu_sort_by_duration:
//                //mPreferences.setSongSortOrder(SortOrder.SongSortOrder.SONG_DURATION);
//                reloadAdapter();
//                return true;
//            break;
        }
        return super.onOptionsItemSelected(item);
    }

}