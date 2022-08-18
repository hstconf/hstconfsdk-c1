package com.infowarelab.conference.face.model;

public class FaceRegisterInfo {
    private byte[] featureData;
    private String name;
    private boolean signIn = false;

    public FaceRegisterInfo(byte[] faceFeature, String name) {
        this.featureData = faceFeature;
        this.name = name;
        this.signIn = false;
    }

    public FaceRegisterInfo(byte[] faceFeature, String name, boolean signIn) {
        this.featureData = faceFeature;
        this.name = name;
        this.signIn = signIn;
    }

    public boolean getSignIn() {
        return signIn;
    }

    public void setSignIn(boolean signIn) {
        this.signIn = signIn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public byte[] getFeatureData() {
        return featureData;
    }

    public void setFeatureData(byte[] featureData) {
        this.featureData = featureData;
    }
}
