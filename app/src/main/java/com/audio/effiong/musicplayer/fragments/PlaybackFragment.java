package com.audio.effiong.musicplayer.fragments;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.adapters.HorizontalSongAdapter;
import com.audio.effiong.musicplayer.dbhandler.FavoritePlayTableHelper;
import com.audio.effiong.musicplayer.model.SongModel;
import com.audio.effiong.musicplayer.services.MusicService;
import com.audio.effiong.musicplayer.slidinguppanelhelper.SlidingUpPanelLayout;
import com.audio.effiong.musicplayer.utility.AddPlaylistDialog;
import com.audio.effiong.musicplayer.utility.Helpers;
import com.audio.effiong.musicplayer.utility.ListSongs;
import com.audio.effiong.musicplayer.utility.MusicActivity;
import com.audio.effiong.musicplayer.utility.MusicPreference;
import com.audio.effiong.musicplayer.widgets.PlayPauseDrawable;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;

import static com.audio.effiong.musicplayer.utility.Helpers.getDarkColor;


public class PlaybackFragment extends Fragment {
    public static final String ACTION_RECIEVE_SONG = "com.audio.effiong.musicplayer.edu.ACTION_RECIEVE_SONG";
    public static final String ACTION_CHANGE_PLAYSTATE = "com.audio.effiong.musicplayer.edu.ACTION_CHANGE_PLAY";
    public static final String ACTION_SET_PLAY = "com.audio.effiong.musicplayer.edu.ACTION_SET_PLAY";
    public static final String ACTION_SET_PAUSE = "com.audio.effiong.musicplayer.edu.ACTION_SET_PAUSE";
    public static final String ACTION_UPDATE_SEEK = "com.audio.effiong.musicplayer.edu.ACTION_UPDATE_SEEK";
    public static final String ONSTART_UPDATE_SEEK = "com.audio.effiong.musicplayer.edu.ONSTART_UPDATE_SEEK";
    public static final String ACTION_CLOSE_PANEL = "com.audio.effiong.musicplayer.edu.ACTION_CLOSE_PANEL";
    public static final String ACTION_SHOW_PANEL = "com.audio.effiong.musicplayer.edu.ACTION_SHOW_PANEL";
    public static final String ACTION_HIDE_ADVIEW = "com.audio.effiong.musicplayer.edu.ACTION_HIDE_ADVIEW";
    public static final String  ACTION_SHOW_ADVIEW =  "com.audio.effiong.musicplayer.edu.ACTION_SHOW_ADVIEW";
    public static final String ACTION_HIDE_ADVIEW_NEW = "com.audio.effiong.musicplayer.edu.ACTION_HIDE_ADVIEW_NEW";
    public static final String  ACTION_SHOW_ADVIEW_NEW =  "com.audio.effiong.musicplayer.edu.ACTION_SHOW_ADVIEW_NEW";
    public static final String  ACTION_RELOAD =  "com.audio.effiong.musicplayer.edu.ACTION_RELOAD";
    public static final String  ACTION_SHOW_PLAYBACK_PLAYLIST =  "com.audio.effiong.musicplayer.edu.ACTION_SHOW_PLAYBACK_PLAYLIST";


    private String TAG = "PlaybackFragment";
    private SlidingUpPanelLayout mLayout;
    private RelativeLayout slidepanelchildtwo_topviewone;
    private RelativeLayout slidepanelchildtwo_topviewtwo;
    private boolean isExpand = false;
    private ImageView songAlbumbg;
    private ImageView img_bottom_slideone;
    private ImageView img_bottom_slidetwo;
    private TextView txt_playesongname;
    private TextView txt_playesongname_slidetoptwo;
    private TextView txt_songartistname_slidetoptwo;
    private TextView txt_timeprogress;
    private TextView txt_timetotal;
    private ImageView next;
    private ImageView prev;
    private ImageView repeat;
    private ImageView imgbtn_suffel;
    private ImageView img_Favorite;
    private ImageView more;
    private SeekBar seekBar;
    private long timeElapsed;
    private Handler durationHandler = new Handler();
    String path = "";
    private static final int MAX_ALPHA = 255, TRANS_ALPHA = 90;
    private int colorLight, colorTo = 0xffffffff;
    PlayPauseDrawable playPauseDrawable = new PlayPauseDrawable();
    PlayPauseDrawable playPauseDrawable2 = new PlayPauseDrawable();
    FloatingActionButton playPauseFloating;
    ImageView playPauseWrapper;
    private ProgressBar mProgress;
    String ateKey;
    int accentColor,width,height,colorTop,colorPrimary;
    LinearLayout backgroundGroup;
    int size = 0;
    boolean useDefault = false;
    LinearLayout seekBackground;
    private boolean  panelExpanded = false;
    Window window;
    private boolean writePermissionAked = false;
    private long tempSongId;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private RecyclerView recyclerView;
    private HorizontalSongAdapter adapter;
    private static ArrayList<SongModel> playlists = new ArrayList<>();

    public static PlaybackFragment newInstance(boolean flag) {
        PlaybackFragment fragment = new PlaybackFragment();
        Bundle args = new Bundle();
        args.putBoolean("isAlbum", flag);
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ateKey = Helpers.getATEKey(getActivity());
        accentColor = Config.accentColor(getActivity(), ateKey);
        colorPrimary = Config.primaryColorDark(getActivity(), ateKey);
        window = getActivity().getWindow();
    }

    public void populatePlaylist(){
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        adapter = new HorizontalSongAdapter(playlists, getActivity());
        recyclerView.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (adapter.getItemCount() == 0) getActivity().finish();
            }
        });
    }



    private Runnable updateSeekBarTime = new Runnable() {
        public void run() {
            //get current position
            try {
                timeElapsed = MusicService.getInstance().getPosition();
            } catch
                    (Exception ee) {
                ee.printStackTrace();
            }
            try {
                if (MusicService.getInstance().ismMediaPlayerPrepared()) {
                    final int dur = MusicService.getInstance().getDur();
                    txt_timetotal.setText(getDuration(dur));
                    txt_timeprogress.setText(getDuration(timeElapsed));
                    seekBar.setMax(dur);
                    seekBar.setProgress((int) timeElapsed);
                    mProgress.setMax(dur);
                    mProgress.setProgress((int) timeElapsed);
                    durationHandler.postDelayed(this, 1000);
                }
            }
            catch (Exception e)
            {e.printStackTrace();
            }
        }
    };

    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            switch (intent.getAction()) {
                case ACTION_RECIEVE_SONG:
                    try {
                        updatePlayback(getActivity(),intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case ACTION_CHANGE_PLAYSTATE:
                    updatePlayPauseFloatingButton();
                    break;
                case ACTION_SET_PAUSE:
                    //updatePause();
                    updatePlayPauseFloatingButton();
                    //setPause();
                    // MusicPreference.setPausedByUser(getActivity(),true);
                  /*  MusicPreference.setPlayBackFlag(getActivity(),true);*/
                    //rstate = MusicPreference.getPlayBackFlag(getActivity());
                    break;
                case ACTION_SET_PLAY:
                    updatePlayPauseFloatingButton();
                    break;
                case ACTION_UPDATE_SEEK:
                    updateSeekBarTime();
                    break;
                case ONSTART_UPDATE_SEEK:
                    seekBar.setProgress(intent.getIntExtra("position",0));
                    break;
                case ACTION_CLOSE_PANEL:
                    collapsePanel();
                    break;
                case ACTION_SHOW_PANEL:
                    showPanel();
                    break;
                case ACTION_SHOW_PLAYBACK_PLAYLIST:
                    populatePlaylist();
                    break;
            }
        }
    };

    public void configReiever(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RECIEVE_SONG);
        filter.addAction(ACTION_SET_PAUSE);
        filter.addAction(ACTION_SET_PLAY);
        filter.addAction(ACTION_CHANGE_PLAYSTATE);
        filter.addAction(ACTION_UPDATE_SEEK);
        filter.addAction(ONSTART_UPDATE_SEEK);
        filter.addAction(ACTION_CLOSE_PANEL);
        filter.addAction(ACTION_SHOW_PANEL);
        filter.addAction(ACTION_SHOW_PLAYBACK_PLAYLIST);
        getActivity().registerReceiver(br, filter);
    }
    OnPanelClickListener mCallback;


    // Container Activity must implement this interface
    public interface OnPanelClickListener {
        void isPanelOpen(boolean flag);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnPanelClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnPanelClickListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.playback_fragment, container, false);
        //context = this;
        size = Helpers.getWindowWidth(getContext());
        width = Helpers.getWidth();
        height= Helpers.getHeight();
        initiSlidingUpPanel(rootView);
        tempSongId = 0;
        initOnclicks();
        updateShuffle();
        updateRepeat();
       // populatePlaylist();
        return rootView;
    }
    private void initiSlidingUpPanel(View view) {
        mLayout = (SlidingUpPanelLayout)getActivity().findViewById(R.id.sliding_layout);
        final boolean flag =  getArguments().getBoolean("isAlbum");
        mLayout.setFlag(flag);
        boolean firstRun = MusicPreference.isFirstRun(getActivity());
        if(firstRun){
            if (mLayout != null) {
                if (mLayout.getPanelState() != SlidingUpPanelLayout.PanelState.HIDDEN) {
                    mLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                }
            }
        }

        //Album art image
        songAlbumbg = (ImageView)view.findViewById(R.id.image_songAlbumbg_mid);
        //bottom sliding up panel
        img_bottom_slideone = (ImageView) view.findViewById(R.id.img_bottom_slideone);
        //top sliding up panel
        img_bottom_slidetwo = (ImageView) view.findViewById(R.id.img_bottom_slidetwo);
        txt_timeprogress = (TextView) view.findViewById(R.id.slidepanel_time_progress);
        txt_timetotal = (TextView) view.findViewById(R.id.slidepanel_time_total);
        prev = (ImageView) view.findViewById(R.id.btn_backward);
        next = (ImageView) view.findViewById(R.id.btn_forward);
        repeat = (ImageView) view.findViewById(R.id.btn_toggle);
        imgbtn_suffel = (ImageView) view.findViewById(R.id.btn_suffel);
        playPauseFloating = (FloatingActionButton) view.findViewById(R.id.playpausefloating);
        playPauseWrapper = (ImageView) view.findViewById(R.id.play_pause_wrapper);
        seekBar = (SeekBar) view.findViewById(R.id.song_progress);
        mProgress = (ProgressBar) view.findViewById(R.id.song_progress_normal);
        img_Favorite = (ImageView) view.findViewById(R.id.bottombar_img_Favorite);
        more = (ImageView) view.findViewById(R.id.bottombar_moreicon);
        seekBar.getProgressDrawable().setColorFilter(0xffffffff,PorterDuff.Mode.MULTIPLY);
        mProgress.getProgressDrawable().setColorFilter(accentColor,PorterDuff.Mode.SRC_IN);
        TypedValue typedvaluecoloraccent = new TypedValue();
        getActivity().getTheme().resolveAttribute(R.attr.colorAccent, typedvaluecoloraccent, true);

        if (playPauseFloating != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                playPauseFloating.setBackgroundTintMode(PorterDuff.Mode.LIGHTEN);
                playPauseDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                seekBar.setThumbTintList(ColorStateList.valueOf(Color.WHITE));
            }
            if(ateKey.contains("dark_theme")){
                playPauseDrawable2.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
            else  {
                playPauseDrawable2.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            }
            playPauseFloating.setImageDrawable(playPauseDrawable);
            playPauseWrapper.setImageDrawable(playPauseDrawable2);
            playPauseWrapper.setAlpha(0.5f);
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH )
            {
                playPauseFloating.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
                playPauseDrawable.setColorFilter(Color.BLACK, PorterDuff.Mode.MULTIPLY);
                seekBar.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
            }

            playPauseDrawable.transformToPlay(false);
            playPauseDrawable2.transformToPlay(false);
            if (MusicService.serviceRunning) {
                if (MusicService.getInstance().isPlaying())
                {
                    playPauseDrawable.transformToPause(false);
                    playPauseDrawable2.transformToPause(false);
                }
                else {
                    playPauseDrawable.transformToPlay(false);
                    playPauseDrawable2.transformToPlay(false);
                }


            }
        }
        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);
        backgroundGroup = (LinearLayout) view.findViewById(R.id.main_background);
        seekBackground = (LinearLayout) view.findViewById(R.id.control_seek_bar_holder);
        txt_playesongname = (TextView) view.findViewById(R.id.txt_playesongname);
        txt_playesongname_slidetoptwo = (TextView) view.findViewById(R.id.txt_playesongname_slidetoptwo);
        txt_playesongname_slidetoptwo.setSelected(true);
        txt_songartistname_slidetoptwo = (TextView) view.findViewById(R.id.txt_songartistname_slidetoptwo);
        txt_songartistname_slidetoptwo.setSelected(true);
        //bottom now playing card
        slidepanelchildtwo_topviewone = (RelativeLayout) view.findViewById(R.id.slidepanelchildtwo_topviewone);
        //top changed now playing card
        slidepanelchildtwo_topviewtwo = (RelativeLayout) view.findViewById(R.id.slidepanelchildtwo_topviewtwo);
        slidepanelchildtwo_topviewone.setVisibility(View.VISIBLE);
        slidepanelchildtwo_topviewtwo.setVisibility(View.INVISIBLE);
        slidepanelchildtwo_topviewone.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().sendBroadcast(new Intent(PlaybackFragment.ACTION_HIDE_ADVIEW));
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);

            }
        });

        slidepanelchildtwo_topviewtwo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                getActivity().sendBroadcast(new Intent(PlaybackFragment.ACTION_SHOW_ADVIEW_NEW));


            }
        });


        mLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);

                if (slideOffset == 0.0f) {
                    isExpand = false;
                    mCallback.isPanelOpen(false);
                    slidepanelchildtwo_topviewone.setVisibility(View.VISIBLE);
                    slidepanelchildtwo_topviewtwo.setVisibility(View.INVISIBLE);

                    if(!flag){
                        if(Helpers.isLollipop())
                            window.setStatusBarColor(colorPrimary);
                    }
                } else if (slideOffset > 0.0f && slideOffset < 1.0f) {
                    getActivity().sendBroadcast(new Intent(PlaybackFragment.ACTION_HIDE_ADVIEW_NEW));
                    if(!flag){
                        if(Helpers.isLollipop())
                            window.setStatusBarColor(colorPrimary);
                    }
                    if(flag){
                        if(Helpers.isLollipop())
                            window.setStatusBarColor(Color.TRANSPARENT);
                    }
                }
                else {
                    isExpand = true;
                    mCallback.isPanelOpen(true);
                    slidepanelchildtwo_topviewone.setVisibility(View.INVISIBLE);
                    slidepanelchildtwo_topviewtwo.setVisibility(View.VISIBLE);
                    playPauseFloating.show();

                }

            }

            @Override
            public void onPanelExpanded(View panel) {
                panelExpanded = true;
               // MusicService.getInstance().setPanelState(true);

                if(playPauseFloating != null)
                    playPauseFloating.show();
                getActivity().sendBroadcast(new Intent(PlaybackFragment.ACTION_HIDE_ADVIEW));
                getActivity().sendBroadcast(new Intent(PlaybackFragment.ACTION_SHOW_ADVIEW_NEW));
                Log.i(TAG, "onPanelExpanded");
                isExpand = true;
                if(Helpers.isLollipop())
                    animateColorChangeViewR(window,getDarkColor(colorTop));
                if(useDefault){
                    if(Helpers.isLollipop())
                        animateColorChangeViewR(window,getDarkColor(colorLight));
                }
                mCallback.isPanelOpen(true);
                setPanelStatusQuo(true);


            }

            @Override
            public void onPanelCollapsed(View panel) {
                if(panelExpanded)
                    getActivity().sendBroadcast(new Intent(PlaybackFragment.ACTION_SHOW_ADVIEW));
                panelExpanded = false;
                slidepanelchildtwo_topviewtwo.setVisibility(View.INVISIBLE);
                slidepanelchildtwo_topviewone.setVisibility(View.VISIBLE);
                Log.i(TAG, "onPanelCollapsed");
                if(playPauseFloating != null)
                    playPauseFloating.hide();
                panelExpanded = false;
                isExpand = false;
                mCallback.isPanelOpen(false);
                if(!flag){
                    if(Helpers.isLollipop())
                        animateColorChangeViewR(window,colorPrimary);
                }
                if(flag){
                    if(Helpers.isLollipop())
                        animateColorChangeViewR(window,Color.TRANSPARENT);
                }
                setPanelStatusQuo(false);
               // MusicService.getInstance().setPanelState(false);
            }

            @Override
            public void onPanelAnchored(View panel) {
                Log.i(TAG, "onPanelAnchored");
            }
            @Override
            public void onPanelHidden(View panel) {
                Log.i(TAG, "onPanelHidden");
            }
        });
    }


    public void initOnclicks(){

        imgbtn_suffel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPreference.setShuffle(getActivity());
                updateShuffle();
                MusicService.getInstance().updateShuffle();
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().sendBroadcast(new Intent(MusicService.ACTION_NEXT_SONG));
                    }
                }, 200);
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().sendBroadcast(new Intent(MusicService.ACTION_PREV_SONG));
                    }
                }, 200);
            }
        });

        playPauseFloating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPauseDrawable.transformToPlay(true);
                playPauseDrawable.transformToPause(true);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().sendBroadcast(new Intent(MusicService.ACTION_PAUSE_SONG));
                    }
                }, 200);

            }
        });
        playPauseWrapper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                playPauseDrawable2.transformToPlay(true);
                playPauseDrawable2.transformToPause(true);

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getActivity().sendBroadcast(new Intent(MusicService.ACTION_PAUSE_SONG));
                    }
                }, 250);

            }
        });




        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                /*if (fromUser)
                    MusicService.getInstance().getMediaPlayer().seekTo(progress);*/

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                MusicService.getInstance().getMediaPlayer().seekTo(seekBar.getProgress());
//                Intent i = new Intent(MusicService.ACTION_SEEK_SONG);
//                i.putExtra("seek", seekBar.getProgress());
//                getActivity().sendBroadcast(i);
            }
        });

        repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MusicPreference.setRepeatEnable(getActivity());
                updateRepeat();
                MusicService.getInstance().updateRepeat();


            }
        });

        img_Favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!MusicService.getInstance().songs.isEmpty()){
                    if (MusicService.getInstance().getCurrentPlayingSong() != null ){
                        storeFavoritePlay(getActivity(),MusicService.getInstance().getCurrentPlayingSong(), v.isSelected() ? 0 : 1);
                        v.setSelected(v.isSelected() ? false : true);
                        animateHeartButton(v);
                    }
                }
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    android.support.v7.widget.PopupMenu popup = new android.support.v7.widget.PopupMenu(getActivity(), v, Gravity.END);
                    popup.getMenuInflater().inflate(R.menu.popup_playback_menu, popup.getMenu());
                    popup.show();
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            switch (item.getItemId()) {
                                case R.id.popup_song_add_to_playlist:
                                    if(!MusicService.getInstance().songs.isEmpty()){
                                        if (MusicService.getInstance().getCurrentPlayingSong() != null ){
                                            AddPlaylistDialog.newInstance(MusicService.getInstance().getCurrentPlayingSong()).show(getActivity().getSupportFragmentManager(), "ADD_PLAYLIST");
                                        }
                                    }
                                    break;
                                case R.id.popup_song_share:
                                    if(!MusicService.getInstance().songs.isEmpty()){
                                        if (MusicService.getInstance().getCurrentPlayingSong() != null ){
                                            ListSongs.shareSong(getActivity(),MusicService.getInstance().getCurrentPlayingSong().getPath());
                                        }
                                    }
                                    break;
                                case R.id.popup_set_as_ringtone:
                                    if(!MusicService.getInstance().songs.isEmpty()){
                                        if(MusicService.getInstance().getCurrentPlayingSong()!=null){
                                            tempSongId = MusicService.getInstance().getCurrentPlayingSong().getSongId();
                                            String name = MusicService.getInstance().getCurrentPlayingSong().getName();
                                            ListSongs.requestSetRingtone(getActivity(),MusicService.getInstance()
                                                    .getCurrentPlayingSong()
                                                    .getSongId(),name);

                                        }
                                    }
                                    break;
                                case R.id.action_equalizer:
                                    Helpers.navigateToEqualizer(getActivity());
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
    }

    public void updatePlayPauseFloatingButton() {
        if (MusicService.getInstance().isPlaying()) {
            playPauseDrawable.transformToPause(false);
            playPauseDrawable2.transformToPause(false);

        } else {
            playPauseDrawable.transformToPlay(false);
            playPauseDrawable2.transformToPlay(false);
        }
    }
    public void collapsePanel(){
        if (isExpand) {
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        configReiever();
        askUpdate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (writePermissionAked) {
                if (Settings.System.canWrite(getActivity()))
                    ListSongs.setRingtone(getActivity(), tempSongId);
            }
            writePermissionAked = false;
        }

        ListSongs.setDeleteEventListener(new ListSongs.DeleteEvent() {
            @Override
            public void deleteEvent() {
            }

            @Override
            public void deleteSongsEvent(int position) {

            }
            @Override
            public void setBoolean(boolean flag) {
                writePermissionAked = flag;
            }



        });
//        if(MusicService.serviceRunning){
//           // if(MusicService.getInstance().isPanelExpanded()){
//                // mLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
//                slidepanelchildtwo_topviewone.setVisibility(View.INVISIBLE);
//                slidepanelchildtwo_topviewtwo.setVisibility(View.VISIBLE);
//            }
//        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(br);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        askUpdate();
//        if(MusicService.serviceRunning){
//            if(MusicService.getInstance().isPanelExpanded()==true){
//                slidepanelchildtwo_topviewone.setVisibility(View.INVISIBLE);
//                slidepanelchildtwo_topviewtwo.setVisibility(View.VISIBLE);
//            }
//        }
    }

    public void updateShuffle() {
        if (MusicPreference.isShuffleEnabled(getActivity())) {
            imgbtn_suffel.getDrawable().setAlpha(MAX_ALPHA);
        } else
            imgbtn_suffel.getDrawable().setAlpha(TRANS_ALPHA);
    }

    private void updateRepeat() {
        if (MusicPreference.isRepeatAllEnabled(getActivity())) {
            repeat.getDrawable().setAlpha(MAX_ALPHA);
            repeat.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                    R.drawable.ic_repeat_white_36dp));
            DrawableCompat.setTint(repeat.getDrawable(), colorTo);
        } else if (MusicPreference.isRepeatOneEnabled(getActivity())) {
            repeat.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                    R.drawable.ic_repeat_one_white_36dp));
            repeat.getDrawable().setAlpha(MAX_ALPHA);
            DrawableCompat.setTint(repeat.getDrawable(), colorTo);
        } else {
            repeat.setImageDrawable(ContextCompat
                    .getDrawable(getActivity(), R.drawable.ic_repeat_white_36dp));
            repeat.getDrawable().setAlpha(TRANS_ALPHA);
            DrawableCompat.setTint(repeat.getDrawable(), colorTo);
        }
    }

    public void updatePlayback( Context context, Intent intent){
        long id= intent.getLongExtra("albumId",0);
        getAlbumImage(id);
        final String name = intent.getStringExtra("songName");
        final String albumName = intent.getStringExtra("albumName");
        final String artistName = intent.getStringExtra("artistName");
        txt_playesongname_slidetoptwo.setText(name +" "+albumName);
        txt_songartistname_slidetoptwo.setText(artistName);
        if(ateKey.contains("dark_theme")){
            txt_playesongname_slidetoptwo.setTextColor(Helpers.getBlackWhiteColor(colorTo));
            txt_songartistname_slidetoptwo.setTextColor(Helpers.getBlackWhiteColor(colorTo));
        }
        txt_playesongname.setText(name);
        if(MusicService.getInstance().songs!=null){
            if(!MusicService.getInstance().songs.isEmpty())
                checkIsFavorite(context,MusicService.getInstance().getCurrentPlayingSong(),img_Favorite);
        }
    }

    private void animateColorChangeView(final LinearLayout ll, int color) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                ((ColorDrawable) ll.getBackground()).getColor(), color);
        colorAnimation.setDuration(2000);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                ll.setBackgroundColor((Integer) animator.getAnimatedValue());
            }

        });
        colorAnimation.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void animateColorChangeViewR(final Window ll, int color) {
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(),
                ll.getStatusBarColor(), color);
        colorAnimation.setDuration(2000);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                ll.setStatusBarColor((Integer) animator.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }


    public void updateSeekBarTime(){
        durationHandler.postDelayed(updateSeekBarTime, 1000);
    }

    private void askUpdate() {
        Intent i = new Intent();
        i.setAction(MusicService.ACTION_GET_SONG);
        getActivity().sendBroadcast(i);
    }

    public static String getDuration(long milliseconds) {
        long sec = (milliseconds / 1000) % 60;
        long min = (milliseconds / (60 * 1000)) % 60;
        long hour = milliseconds / (60 * 60 * 1000);

        String s = (sec < 10) ? "0" + sec : "" + sec;
        String m = (min < 10) ? "0" + min : "" + min;
        String h = "" + hour;

        String time = "";
        if (hour > 0) {
            time = h + ":" + m + ":" + s;
        } else {
            time = m + ":" + s;
        }
        return time;
    }



    public void getAlbumImage(final long id) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                path = ListSongs.getAlbumArt(MusicActivity.applicationContext, id);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(MusicActivity.applicationContext).load(path).error(R.drawable.album_art).animate(R.anim.slide_left).override(size,size).into(songAlbumbg);
                        Glide.with(MusicActivity.applicationContext).load(path).placeholder(R.drawable.album_art).crossFade().into(img_bottom_slideone);
                        Glide.with(MusicActivity.applicationContext).load(path).placeholder(R.drawable.album_art).crossFade().into(img_bottom_slidetwo);
                        getPallete(path);
                    }
                }, 5);

            }
        }.execute();
    }


    public void getPallete(String pahh){
        useDefault = true;
        Glide.with(MusicActivity.applicationContext).load(pahh).asBitmap()
                .error(R.drawable.bg_default_album_art).fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL).into(new SimpleTarget<Bitmap>(width,height/2) {

            @Override
            public void onResourceReady(final Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                useDefault = false;
                Palette.from(resource)
                        .generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {


                                final int[] colors = Helpers.getAvailableColor(getActivity(),palette);
                                colorTop = colors[0];
                                animateColorChangeView(backgroundGroup,colors[0]);
                                animateColorChangeView(seekBackground,getDarkColor(colors[0]));
                                if(isExpand){
                                    if(Helpers.isLollipop())
                                        animateColorChangeViewR(window,getDarkColor(colorTop));
                                }
                            }
                        });
            }


        });
        colorLight = (ContextCompat.getColor(MusicActivity.applicationContext, R.color.coloorPrimary));
        if(useDefault){
            if(isExpand){
                if(Helpers.isLollipop())
                    animateColorChangeViewR(window, getDarkColor(colorLight));
            }

            // animateColorChangeViewR(window,getDarkColor(colorLight));
            animateColorChangeView(backgroundGroup,colorLight);
            animateColorChangeView(seekBackground,getDarkColor(colorLight));
        }


    }
    /**
     * Store Favorite Play Data
     */
    public synchronized void storeFavoritePlay(final Context context, final SongModel mDetail, final int isFav) {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    FavoritePlayTableHelper.getInstance(context).inserSong(mDetail, isFav);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    public synchronized void checkIsFavorite(final Context context, final SongModel song, final View v) {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            boolean isFavorite = false;

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    isFavorite = FavoritePlayTableHelper.getInstance(context).getIsFavorite(song);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                v.setSelected(isFavorite);
            }
        };

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            task.execute();
        }
    }

    //  private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    public static void animateHeartButton(final View v) {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator rotationAnim = ObjectAnimator.ofFloat(v, "rotation", 0f, 360f);
        rotationAnim.setDuration(300);
        rotationAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(v, "scaleX", 0.2f, 1f);
        bounceAnimX.setDuration(300);
        bounceAnimX.setInterpolator(OVERSHOOT_INTERPOLATOR);

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(v, "scaleY", 0.2f, 1f);
        bounceAnimY.setDuration(300);
        bounceAnimY.setInterpolator(OVERSHOOT_INTERPOLATOR);
        bounceAnimY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });

        animatorSet.play(bounceAnimX).with(bounceAnimY).after(rotationAnim);
        animatorSet.start();
    }

    public void showPanel(){
        if(mLayout != null)
            mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    private  void setPanelStatusQuo(final Boolean state) {
        new Thread(new Runnable() {
            public void run() {
                MusicPreference.setPanelState(getActivity(),state);
            }
        }).start();
    }

    public static void setPlaylist(ArrayList<SongModel> songs){
        playlists = songs;
    }


}







