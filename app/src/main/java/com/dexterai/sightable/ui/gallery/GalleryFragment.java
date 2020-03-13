package com.dexterai.sightable.ui.gallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.dexterai.sightable.DetectorActivity;
import com.dexterai.sightable.ImageAdapter;
import com.dexterai.sightable.R;
import com.mzelzoghbi.zgallery.ZGallery;
import com.mzelzoghbi.zgallery.ZGrid;
import com.mzelzoghbi.zgallery.entities.ZColor;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

public class GalleryFragment extends Fragment {

    private ArrayList<String> ImageList = new ArrayList<String>();
    private ArrayList<Bitmap> bitmapList = new ArrayList<Bitmap>();
    GridView gridView;
    ImageView imageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        gridView = (GridView)root.findViewById(R.id.imageGridView);
        imageView = (ImageView) root.findViewById(R.id.imageView);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                imageView.setImageBitmap(bitmapList.get(position));
            }
            });
        return root;
    }
    private Bitmap urlImageToBitmap(String imageUrl) throws Exception {
        Bitmap result = null;
        URL url = new URL(imageUrl);
        if(url != null) {
            result = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(url.openConnection().getInputStream()),300,300,false );
        }
        return result;
    }
    void setupAdapter() {
        if (getActivity() == null || gridView == null) return;
        if (ImageList != null) {
//            gridView.setAdapter(new ArrayAdapter<String>(getActivity(),
//                    android.R.layout.simple_list_item_multiple_choice, ImageList));
            gridView.setAdapter(new ImageAdapter(getContext(), this.bitmapList));

        } else {
            gridView.setAdapter(null);
        }
    }
    private class FetchItemsTask extends AsyncTask<Void,Void,ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(Void... params) {
            File folder = new File(Environment
                    .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "Sightable/");
            try {
                for(File f : folder.listFiles()) {
                    int i=0;
                    for (File file : f.listFiles()) {
                        Log.d("TrainingFragment", "Image file location: " + Uri.fromFile(file));
                        ImageList.add(Uri.fromFile(file).toString());
                        bitmapList.add(urlImageToBitmap(ImageList.get(i)));
                        i++;
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return ImageList;
        }

        @Override
        protected void onPostExecute(ArrayList<String> items) {
            ImageList = items;
            setupAdapter();
        }
    }
}