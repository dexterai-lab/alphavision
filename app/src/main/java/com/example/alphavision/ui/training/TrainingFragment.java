package com.example.alphavision.ui.training;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.alphavision.CameraActivity;
import com.example.alphavision.CameraConnectionFragment;
import com.example.alphavision.CameraHelper;
import com.example.alphavision.LegacyCameraConnectionFragment;
import com.example.alphavision.R;
import com.example.alphavision.env.Logger;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class TrainingFragment extends Fragment {

    private static final int REQUEST_CAMERA = 0;
    private int STORAGE_PERMISSION_CODE = 1;
    private static final String TAG = "TrainingFragment";
    private Button buttonTakepic;
    private Camera mCamera;
    private View mLayout;
    private CameraHelper.CameraPreview mCameraPreview;
    CoordinatorLayout coordinatorLayout;
    protected int TIMER_INTERVAL = 0;
    //Set this to set minimum number of images taken at pre-defined interval
    private int TIMER_INTERVAL_MIN = 1000;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.i(TAG, "Show camera button pressed. Checking permission.");

        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        // Check if the Camera permission is already available.
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.

            requestCameraPermission();

        } else {

            // Camera permissions is already available, show the camera preview.
            Log.i(TAG,
                    "CAMERA permission has already been granted. Displaying camera preview.");

            mCamera = getCameraInstance();
            mCameraPreview = new CameraHelper.CameraPreview(getParentFragment().getContext(), mCamera);
            FrameLayout preview = (FrameLayout) root.findViewById(R.id.camera_preview);
            preview.addView(mCameraPreview);

            buttonTakepic = (Button) root.findViewById(R.id.buttonTakepic);
            buttonTakepic.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getActivity(), "You have already granted this permission!",
                                Toast.LENGTH_SHORT).show();

                        SeekBar s = (SeekBar) root.findViewById(R.id.seekBar);
                        s.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                            public void onProgressChanged(SeekBar seekBar,
                                                          int progress, boolean fromUser) {
                                if(progress == 1){
                                    TIMER_INTERVAL = 1000;
                                    setTimer(getContext(),int TIMER_INTERVAL);
                                }
                                else if(progress == 2){
                                    TIMER_INTERVAL = 2000;
                                    setTimer(getContext(),int timeInterval);
                                }
                                else if(progress == 3){
                                    TIMER_INTERVAL = 3000;
                                    setTimer(getContext(),int timeInterval);
                                }
                                else if(progress == 4){
                                    TIMER_INTERVAL = 4000;
                                    setTimer(getContext(),int timeInterval);
                                }
                                else if(progress == 5){
                                    TIMER_INTERVAL = 5000;
                                    setTimer(getContext(),int timeInterval);
                                }
                                else{
                                    setTimer(getContext(),int timeInterval);
                                }

                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        };
                        new CountDownTimer(5000, 1000) {

                            @Override
                            public void onFinish() {

                            }

                            @Override
                            public void onTick(long millisUntilFinished) {
                                mCamera.startPreview();
                                mCamera.takePicture(null, null, mPicture);
                            }

                        }.start();
                    } else {
                        requestStoragePermission();
                    }
                }
            }); // END_INCLUDE(camera_permission)
        }
        return root;
    }

    /**
     * Helper method to access the camera returns null if it cannot get the
     * camera or does not exist
     *
     * @return
     */
    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
        }

    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "AlphaVision");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MainActivity", "Media storage dir is: " + mediaStorageDir.getPath());
                Log.d("AlphaVision", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    private void requestCameraPermission() {
        Log.i(TAG, "CAMERA permission has NOT been granted. Requesting permission.");

        // BEGIN_INCLUDE(camera_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG,
                    "Displaying camera permission rationale to provide additional context.");
            FrameLayout preview = (FrameLayout)  mCameraPreview.findViewById(R.id.camera_preview);

            Snackbar.make(preview , R.string.permission_camera_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{Manifest.permission.CAMERA},
                                    REQUEST_CAMERA);
                        }
                    })
                    .show();
        } else {

            // Camera permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
        }
        // END_INCLUDE(camera_permission_request)
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(getContext())
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to store the captured images to the device!")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Permission GRANTED", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}