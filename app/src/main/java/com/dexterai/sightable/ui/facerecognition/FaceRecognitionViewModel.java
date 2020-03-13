package com.dexterai.sightable.ui.facerecognition;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FaceRecognitionViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public FaceRecognitionViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}