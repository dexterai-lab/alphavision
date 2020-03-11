package com.dexterai.sightable.ui.gallery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dexterai.sightable.DetectorActivity;
import com.dexterai.sightable.R;
import com.mzelzoghbi.zgallery.ZGallery;
import com.mzelzoghbi.zgallery.ZGrid;
import com.mzelzoghbi.zgallery.entities.ZColor;

import java.io.File;
import java.util.ArrayList;

public class GalleryFragment extends Fragment {

    private ArrayList<String> ImageList = new ArrayList<String>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        final TextView textView = root.findViewById(R.id.text_gallery);

        Button buttonGallery = (Button) root.findViewById(R.id.buttonGallery);

        File folder = new File(Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Sightable/");
        for(File f : folder.listFiles()) {
            for (File file : f.listFiles()) {
                Log.d("TrainingFragment", "Image file location: " + Uri.fromFile(file));
                ImageList.add(Uri.fromFile(file).toString());
            }
        }


        buttonGallery.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                ZGallery.with(getActivity(), ImageList)
                        .setToolbarTitleColor(ZColor.WHITE) // toolbar title color
                        .setGalleryBackgroundColor(ZColor.BLACK) // activity background color
                        .setToolbarColorResId(R.color.colorPrimary) // toolbar color
                        .setTitle("Training Gallery") // toolbar title
                        .show();
            }
        });

        return root;
    }

}