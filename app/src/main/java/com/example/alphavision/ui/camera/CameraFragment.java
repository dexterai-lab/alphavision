package com.example.alphavision.ui.camera;

import android.content.Intent;
import android.os.Bundle;
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

import com.example.alphavision.DetectorActivity;
import com.example.alphavision.R;

public class CameraFragment extends Fragment {

    private CameraViewModel cameraViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cameraViewModel =
                ViewModelProviders.of(this).get(CameraViewModel.class);
        View root = inflater.inflate(R.layout.fragment_camera, container, false);

        Button buttonDetect = (Button) root.findViewById(R.id.buttonDetect);
        buttonDetect.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DetectorActivity.class);
                intent.putExtra("some","some data");
                startActivity(intent);
            }
        });

//        final TextView textView = root.findViewById(R.id.text_home);
//        cameraViewModel.getText().observe(this, new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return root;
    }
}