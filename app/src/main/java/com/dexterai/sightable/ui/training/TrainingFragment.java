package com.dexterai.sightable.ui.training;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.dexterai.sightable.CameraHelper;
import com.dexterai.sightable.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrainingFragment extends Fragment {

    private static final int REQUEST_CAMERA = 0;
    private int STORAGE_PERMISSION_CODE = 1;
    private static final String TAG = "TrainingFragment";
    private Button buttonTakepic,  buttonTrain;
    private SeekBar seekBar;
    private TextView textViewInterval,textViewCountdown;
    private static TextInputEditText textInputName, textInputSamples;
    private int NUM_OF_SAMPLES = 0;
    protected int TIMER_INTERVAL = 1000;
    private int sum_progress = 1;
    private Camera mCamera;
    private CameraHelper.CameraPreview mCameraPreview;

    private ArrayList<Uri> ImageList = new ArrayList<Uri>();

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Log.i(TAG, "Show camera button pressed. Checking permission.");

        View root = inflater.inflate(R.layout.fragment_training, container, false);

        // Check if the Camera permission is already available.
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Camera permission has not been granted.

            requestCameraPermission();

        } else {

            // Camera permissions is already available, show the camera preview.
            Log.i(TAG,
                    "CAMERA permission has already been granted. Displaying camera preview.");

            FrameLayout preview = (FrameLayout) root.findViewById(R.id.camera_preview);
            seekBar = (SeekBar) root.findViewById(R.id.seekBar);
            textViewInterval = (TextView) root.findViewById(R.id.textViewInterval);
            textViewCountdown = (TextView) root.findViewById(R.id.textViewCountdown);
            textInputName =  (TextInputEditText) root.findViewById(R.id.TextInputName);
            textInputSamples =  (TextInputEditText) root.findViewById(R.id.TextInputSamples);
            buttonTakepic = (Button) root.findViewById(R.id.buttonTakepic);
            buttonTrain = (Button) root.findViewById(R.id.buttonTrain);

            buttonTrain.setEnabled(false);

            //Get Firebase References
            storage = FirebaseStorage.getInstance();
            storageReference = storage.getReference();

            mCamera = getCameraInstance();
            mCameraPreview = new CameraHelper.CameraPreview(getParentFragment().getContext(), mCamera);
            preview.addView(mCameraPreview);

//            textViewInterval.setText(seekBar.getProgress());
            textViewInterval.setText(" " + (seekBar.getProgress()+1) + "s" );

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    sum_progress = (progress + 1);
                    //Read the progress input and use it for purpose
                    textViewInterval.setText(" "+ sum_progress + "s");
                    //BUG:Check why it starts with zero!
                    TIMER_INTERVAL = sum_progress * 1000;
                    Log.d("TrainingFragment", "Timer interval: " + TIMER_INTERVAL);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            buttonTakepic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ActivityCompat.checkSelfPermission(getContext(),
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getActivity(), "You have already granted this permission!",
                                Toast.LENGTH_SHORT).show();

                        final String name = textInputName.getText().toString();
                        if(name.matches("")){
                            textInputName.setError("This field cannot be empty!");
                            return;
                        }
                        else{
                            if(!isValidName(name)){
                                textInputName.setError("Invalid name, remove special characters!");
                                return;
                            }
                            else{
                                int factor = (seekBar.getProgress()+1)*1000;
                                NUM_OF_SAMPLES = (Integer.parseInt(textInputSamples.getText().toString().trim()))*factor;
                                Log.d("TrainingFragment", "Number of samples: " + NUM_OF_SAMPLES);
                                Log.d("TrainingFragment", "Timer int inside timers: " + TIMER_INTERVAL);
                                CountDownTimer start = new CountDownTimer(NUM_OF_SAMPLES, TIMER_INTERVAL) {

                                    private Long COUNTDOWN;

                                    @Override
                                    public void onFinish() {
                                        mCamera.startPreview();
                                        textViewCountdown.setVisibility(View.INVISIBLE);
                                    }

                                    @Override
                                    public void onTick(long millisUntilFinished) {
                                        mCamera.startPreview();
                                        textViewCountdown.setVisibility(View.VISIBLE);
                                        /* TODO: Handle divide by zero */
                                        COUNTDOWN = (millisUntilFinished / TIMER_INTERVAL) + 1;
                                        textViewCountdown.setText(String.valueOf(COUNTDOWN));
                                        mCamera.takePicture(null, null, mPicture);
                                        COUNTDOWN -= 1;
                                    }

                                }.start();
                            }
                        }
                    } else {
                        requestStoragePermission();
                    }
                }
            }); // END_INCLUDE(camera_permission)

            textInputName.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(s.toString().trim().length()==0){
                        buttonTrain.setEnabled(false);
                    } else {
                        buttonTrain.setEnabled(true);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(s.toString().trim().length()==0){
                        buttonTrain.setEnabled(false);
                    } else {
                        buttonTrain.setEnabled(true);
                    }

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            buttonTrain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uploadTrainingData();
                }
            });
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


    // validating name
    private boolean isValidName(String name) {
        String name_pattern = "^[a-zA-Z\\\\s]+";
        Pattern pattern = Pattern.compile(name_pattern);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Sightable/" + textInputName.getText());
        if (!mediaStorageDir.exists()) {
            Log.d("TrainingFragment", "Directory does not exist.");
            if (!mediaStorageDir.mkdirs()) {
                Log.d("TrainingFragment", "Media storage dir is: " + mediaStorageDir.getPath());
                Log.d("TrainingFragment", "failed to create directory");
                return null;
            }

        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + textInputName.getText() + "_" + timeStamp + ".jpg");
        Log.d("TrainingFragment", "Media storage filename is: " + mediaFile);

        return mediaFile;
    }

    private void uploadTrainingData() {

        File folder = new File(Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Sightable/" + textInputName.getText());

        for (File file : folder.listFiles()) {
            Log.d("TrainingFragment", "Image file location: " + Uri.fromFile(file));
            ImageList.add(Uri.fromFile(file));
        }

        if(ImageList != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            int upload_count;

            for(upload_count=0; upload_count < ImageList.size(); upload_count++){
                Uri IndividualImage = ImageList.get(upload_count);
                StorageReference ImageName = storageReference.child("images/"+ textInputName.getText() + "/" +IndividualImage.getLastPathSegment());
                ImageName.putFile(IndividualImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    }) ;
        }}
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
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(getContext())
                    .setTitle("Permission needed")
                    .setMessage("This permission is needed to store the captured images to the device!")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
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
                    new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
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