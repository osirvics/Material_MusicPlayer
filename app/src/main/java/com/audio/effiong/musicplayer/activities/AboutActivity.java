package com.audio.effiong.musicplayer.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.utility.Helpers;

import de.psdev.licensesdialog.LicensesDialog;

import static com.audio.effiong.musicplayer.R.id.linked;
import static com.audio.effiong.musicplayer.utility.Helpers.openPlayStore;

public class AboutActivity extends BaseThemedActivity implements ATEActivityThemeCustomizer {
    String googlePlusUrl = "https://plus.google.com/communities/117368641216065921347";
    String linkedUrl = "https://ng.linkedin.com/in/edu-victor-ba489945";
    //String playStoreUrl ="https://play.google.com/store/apps/details?id=com.audio.effiong.musicplayer";
    String packageName = "com.audio.effiong.musicplayer";
    String packageNamePro = "com.audioplayer.best.musicplayer";
    LinearLayout view;
    LinearLayout community;
    LinearLayout bugs;
    LinearLayout linkedin;
    LinearLayout rate;
    LinearLayout share;
    LinearLayout remove_app_ads;
    //Group one
    TextView cardOneHeading, cardOneVersion,cardOneVersion2, cardOneLicenses;
    LinearLayout first;
    AppCompatImageView version, licences;
    //Group two
    TextView cardTwoHeading, cardTwoJoin, cardTwoBugs,cardTwoAds,cardTwoRate;
    AppCompatImageView communityView, report_bugs,remove_ads, rate_app,share_app;
    //Group three
    TextView location;
    AppCompatImageView author_icon, linkedIn_icon;
    String ateKey;
    int accentColor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar!=null)
            toolbar.setTitle(R.string.about);
        setSupportActionBar(toolbar);
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

         view = (LinearLayout) LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_licenses, null);

        ateKey = Helpers.getATEKey(this);
        accentColor = Config.accentColor(this, ateKey);
        setCardOne();
        setCardTwo();
        setCardThree();
        applyColorFilter();
    }

    public void setCardOne(){
        cardOneHeading = (TextView)findViewById(R.id.group_one_heading);
        cardOneVersion = (TextView)findViewById(R.id.version_text_one);
        cardOneVersion2 = (TextView)findViewById(R.id.version_text_two);
        cardOneLicenses  = (TextView)findViewById(R.id.license_text);
        licences = (AppCompatImageView)findViewById(R.id.license_icon);
        version = (AppCompatImageView)findViewById(R.id.version_icon);
        first = (LinearLayout)findViewById(R.id.first);
        cardOneVersion2.setText(getCurrentVersionName(AboutActivity.this));

        first.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onMultipleClick(view);
            }
        });
    }

    public void setCardTwo(){
        cardTwoHeading = (TextView)findViewById(R.id.group_two_heading);
        community = (LinearLayout)findViewById(R.id.second);
        bugs = (LinearLayout) findViewById(R.id.layout_bug);
        remove_app_ads = (LinearLayout) findViewById(R.id.ads);
        rate = (LinearLayout) findViewById(R.id.rate);
        share = (LinearLayout)findViewById(R.id.share);
        cardTwoJoin = (TextView)findViewById(R.id.join_community_text_two);
        cardTwoBugs = (TextView)findViewById(R.id.report_bugs_text_two);
        cardTwoAds = (TextView) findViewById(R.id.remove_ads_text_two);
        cardTwoRate =(TextView) findViewById(R.id.rate_app_text_two);
        communityView = (AppCompatImageView) findViewById(R.id.join_community_icon);
        rate_app = (AppCompatImageView) findViewById(R.id.rate_app_icon);
        remove_ads = (AppCompatImageView) findViewById(R.id.remove_ads_icon);
        report_bugs = (AppCompatImageView) findViewById(R.id.report_bugs_icon);
        share_app =(AppCompatImageView) findViewById(R.id.share_app_icon);


        community.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(googlePlusUrl));
                startActivity(i);
            }
        });
        bugs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:victor46539@gmail.com"));
                intent.putExtra(Intent.EXTRA_EMAIL, "addresses");
                intent.putExtra(Intent.EXTRA_SUBJECT, "subject");
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(Intent.createChooser(intent, getResources().getText(R.string.send_via)));
                }


//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                Uri data = Uri.parse("mailto:victor46539@gmail.com");
//                intent.setData(data);
//                startActivity(intent);



//                Intent intent = new Intent(Intent.ACTION_SENDTO);
//                intent.setType("*/*");
//                intent.setData(Uri.parse("mailto:victor46539@gmail.com"));
//                intent.putExtra(Intent.EXTRA_EMAIL, "victor46539@gmail.com");
//                intent.putExtra(Intent.EXTRA_SUBJECT, "subject");
//                intent.putExtra(Intent.EXTRA_STREAM, "attachment");
//                if (intent.resolveActivity(getPackageManager()) != null) {
//                    startActivity(intent);
//                }
            }
        });

        remove_app_ads.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayStore(AboutActivity.this,packageNamePro);
            }
        });

        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPlayStore(AboutActivity.this,packageName);
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,  "Hey, check out this awesome app at: https://play.google.com/store/apps/details?id=com.audio.effiong.musicplayer");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.send_to)));
            }
        });
    }

    public void setCardThree(){
        linkedin = (LinearLayout)findViewById(linked);
        author_icon = (AppCompatImageView) findViewById(R.id.author_icon);
        linkedIn_icon = (AppCompatImageView)findViewById(R.id.linkedin_icon);
        location = (TextView)findViewById(R.id.author_location);


        linkedin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(linkedUrl));
                startActivity(i);
            }
        });
    }

    private void applyColorFilter(){
        if(ateKey.contains("dark_theme")){
            licences.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_100));
            version.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_100));
            author_icon.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_100));
            linkedIn_icon.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_100));
            communityView.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_100));
            rate_app.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_100));
            remove_ads.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_100));
            report_bugs.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_100));
            share_app.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_100));
        }
        else{
            licences.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_600));
            version.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_600));
            communityView.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_600));
            rate_app.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_600));
            remove_ads.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_600));
            report_bugs.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_600));
            share_app.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_600));
            author_icon.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_600));
            linkedIn_icon.setColorFilter(ContextCompat.getColor(this,R.color.md_grey_600));

        }
    }

    private void onMultipleClick(final View view) {
        new LicensesDialog.Builder(this)
                .setNotices(R.raw.licenses)
                .setTitle("Licenses")
                .build()
                .show();
    }

    @Override
    public int getActivityTheme() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false) ?
                R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
    }

    private static String getCurrentVersionName(@NonNull Context paramContext)
    {
        try
        {
            return   paramContext.getPackageManager().getPackageInfo(paramContext.getPackageName(), 0).versionName;

        }
        catch (PackageManager.NameNotFoundException ee)
        {
            ee.printStackTrace();
        }
        return "0.0.0";
    }


}
