package com.dexterai.sightable.ui.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.dexterai.sightable.BuildConfig;
import com.dexterai.sightable.DetectorActivity;
import com.dexterai.sightable.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;


public class CameraFragment extends Fragment {

    private CameraViewModel cameraViewModel;
    private AdView adView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_camera, container, false);

        Button buttonDetect = (Button) root.findViewById(R.id.buttonDetect);
        adView = (AdView) root.findViewById(R.id.adView);

        //Launch Ads - Initialize one time
        MobileAds.initialize(getContext(), "ca-app-pub-3940256099942544~3347511713");
        AdRequest adRequest = new AdRequest.Builder().build();
        // Start loading the ad in the background.
        adView.loadAd(adRequest);

        buttonDetect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DetectorActivity.class);
                intent.putExtra("some","some data");
                startActivity(intent);
            }
        });

        return root;
    }
}

