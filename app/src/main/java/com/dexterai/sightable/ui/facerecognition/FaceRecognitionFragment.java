package com.dexterai.sightable.ui.facerecognition;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dexterai.sightable.ImageSurfaceView;
import com.dexterai.sightable.R;

public class FaceRecognitionFragment extends Fragment {

    private ImageSurfaceView mImageSurfaceView;
    private Camera camera;
    RelativeLayout rl,cameraLayout;
    LinearLayout saveLayout;
    private FrameLayout cameraPreviewLayout;
    private ImageView capturedImageHolder,saveImage;
    int imageL,imageT,imageW,imageH,width,height,dpsize;


    Bitmap resizedBitmap;
    Camera mCamera = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_facerecognition, container, false);

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);



        cameraPreviewLayout = (FrameLayout)root.findViewById(R.id.camera_preview);
        capturedImageHolder = (ImageView)root.findViewById(R.id.captured_image);

        rl = root.findViewById(R.id.rr2);

        cameraLayout = root.findViewById(R.id.cameraLayout);
        saveLayout = root.findViewById(R.id.saveLayout);

        saveImage = root.findViewById(R.id.saveImage);

        Display display = getActivity().getWindowManager().getDefaultDisplay();


        width= display.getWidth();
        height= display.getHeight();

        camera = checkDeviceCamera();

        mImageSurfaceView = new ImageSurfaceView(getContext(), camera);
        cameraPreviewLayout.addView(mImageSurfaceView);


        dpsize = (int) (getResources().getDimension(R.dimen._150sdp));
        capturedImageHolder.setX((width-dpsize)/2);
        capturedImageHolder.setY((height -dpsize)/2);

        imageL= (int) capturedImageHolder.getX();
        imageT= (int) capturedImageHolder.getY();
        capturedImageHolder.setOnTouchListener(new MoveViewTouchListener(capturedImageHolder));
        Button captureButton = (Button)root.findViewById(R.id.button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(null, null, pictureCallback);
            }
        });

        return root;
    }
    private Camera checkDeviceCamera(){

        try {
            mCamera = Camera.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCamera;
    }
    public void resetCamera()
    {
        if(mCamera!=null) {
            mCamera.release();

        }
    }

    Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            final int REQUIRED_SIZE = 8192; //8MB SIZE
            int scale = 1;
            int wd= b.getWidth();
            while (wd >=( REQUIRED_SIZE)) {
                wd= wd/2;
                scale *= 2;
            }
            options.inSampleSize = scale;
            options.inJustDecodeBounds = false;
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,options);

            if(bitmap==null){
                Toast.makeText(getActivity(), "Captured image is empty", Toast.LENGTH_LONG).show();
                return;
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap= Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,false);
            int bh= bitmap.getHeight();
            int bw= bitmap.getWidth();
            width= rl.getWidth();
            height= rl.getHeight();
            int l = imageL*bw/width;
            int t = imageT*bh/height;
            int w = capturedImageHolder.getWidth()*bw/width;
            int h = capturedImageHolder.getHeight()*bh/height;

            cameraPreviewLayout.setVisibility(View.GONE);
            capturedImageHolder.setVisibility(View.VISIBLE);
            resizedBitmap= Bitmap.createBitmap(bitmap,l,t,w,h);

            if(resizedBitmap!=null) {

                cameraLayout.setVisibility(View.GONE);
                saveLayout.setVisibility(View.VISIBLE);
                saveImage.setImageBitmap(resizedBitmap);
            }
        }
    };

    public class MoveViewTouchListener
            implements View.OnTouchListener
    {
        private GestureDetector mGestureDetector;
        private View mView;


        public MoveViewTouchListener(View view)
        {
            mGestureDetector = new GestureDetector(view.getContext(), mGestureListener);
            mView = view;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            return mGestureDetector.onTouchEvent(event);
        }

        private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener()
        {
            private float mMotionDownX, mMotionDownY;

            @Override
            public boolean onDown(MotionEvent e)
            {
                mMotionDownX = e.getRawX() - mView.getTranslationX();
                mMotionDownY = e.getRawY() - mView.getTranslationY();
                imageL= (int) mView.getX();
                imageT= (int) mView.getY();
                Log.d("imageview"," down");
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
            {
                mView.setTranslationX(e2.getRawX() - mMotionDownX);
                mView.setTranslationY(e2.getRawY() - mMotionDownY);
                imageL= (int) mView.getX();
                imageT= (int) mView.getY();
                if((distanceX==0)&&(distanceY==0))
                {
                    Log.d("imageview"," zoomed");
                }

                return true;
            }
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                Log.d("imageview"," tapped");
                return true;
            }

        };
    }
}