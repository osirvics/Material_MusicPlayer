package com.audio.effiong.musicplayer.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.audio.effiong.musicplayer.fragments.AlbumsFragment;
import com.audio.effiong.musicplayer.fragments.ArtistFragment;
import com.audio.effiong.musicplayer.fragments.PlaylistFragment;
import com.audio.effiong.musicplayer.fragments.SongListFragment;


/**
 * Created by Effiong on 31-Jan-16.
 */
public class PagerAdapter extends FragmentPagerAdapter {
    int mNumOfTabs;


    public PagerAdapter(FragmentManager fm, int numOfTabs) {
        super(fm);
        this.mNumOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                SongListFragment tab1 = new SongListFragment();
                return tab1;
            case 1:
                AlbumsFragment tab2 = new AlbumsFragment();
                return tab2;
            case 2:
                ArtistFragment tab3 = new ArtistFragment();
                return tab3;
            case 3:
                PlaylistFragment tab4 = new PlaylistFragment();
                return tab4;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return  mNumOfTabs;
    }
}
