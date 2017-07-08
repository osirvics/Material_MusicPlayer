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
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.appthemeengine.Config;
import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.adapters.ArtistListAdapter;
import com.audio.effiong.musicplayer.design.DividerItemDecoration;
import com.audio.effiong.musicplayer.model.Artist;
import com.audio.effiong.musicplayer.utility.Helpers;
import com.audio.effiong.musicplayer.utility.ListSongs;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ArtistFragment extends Fragment {

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private ArrayList<Artist> artists;
    private FastScrollRecyclerView artistRv;
    String ateKey;
    int accentColor,colorPrimary;
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
        View rootView = inflater.inflate(R.layout.artist_fragment, container, false);

        artistRv = (FastScrollRecyclerView) rootView.findViewById(R.id.artist_list_rv);
        emptyView = (LinearLayout)rootView.findViewById(R.id.songs_empty_view);
       // artistRv.setLayoutManager(new LinearLayoutManager(getActivity()));
        final int columns = getResources().getInteger(R.integer.gallery_columns);
        final GridLayoutManager gridLayoutManager =
                new GridLayoutManager(getActivity(), columns);
        gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        artistRv.setLayoutManager(gridLayoutManager);
        artistRv.setAutoHideEnabled(true);
        artistRv.setAutoHideDelay(1500);
        artistRv.setThumbColor(accentColor);
        artistRv.setPopupBgColor(accentColor);
        artistRv.setPopupTextColor(ContextCompat.getColor(getActivity(),R.color.md_white_1000_75));
        artists = new ArrayList<>();
            new loadArtistList().execute("");
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
        String TAG = "ArtistFragment";
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
                       new loadArtistList().execute("");
                        //else any one or both the permissions are not granted
                    } else {
                        //Log.d(TAG, "Some permissions are not granted ask again ");
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


    private class loadArtistList extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            artists = ListSongs.getArtistList(getContext());
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            artistRv.setAdapter(new ArtistListAdapter(getContext(),artists));
            int spacing =  getContext().getResources().getDimensionPixelSize(R.dimen.rc_padding_left);
          RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), spacing,false);
            artistRv.addItemDecoration(dividerItemDecoration);
            if(artists.isEmpty()){
                setEmptyView();
            }
        }
    }

    private void setEmptyView(){
        artistRv.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);
    }
}
