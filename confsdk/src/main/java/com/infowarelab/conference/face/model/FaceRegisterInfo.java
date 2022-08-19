package com.infowarelab.conference.face.model;

import com.infowarelab.conference.face.dbutil.FaceEntity;

public class FaceRegisterInfo {
    private int id;
    private byte[] featureData;
    private String name;
    private byte[] facePic;
    private boolean signIn = false;

//    public FaceRegisterInfo(byte[] faceFeature, String name) {
//        this.featureData = faceFeature;
//        this.name = name;
//        this.signIn = false;
//    }
//
//    public FaceRegisterInfo(byte[] faceFeature, String name, boolean signIn) {
//        this.featureData = faceFeature;
//        this.name = name;
//        this.signIn = signIn;
//    }

    public FaceRegisterInfo(int id, byte[] faceFeature, String name, byte[] facePic) {
        this.id = id;
        this.featureData = faceFeature;
        this.name = name;
        this.facePic = facePic;
    }

    public FaceRegisterInfo(int id, byte[] faceFeature, String name, byte[] facePic, boolean signIn) {
        this.id = id;
        this.featureData = faceFeature;
        this.name = name;
        this.facePic = facePic;
        this.signIn = signIn;
    }

    public int getId() {
        return id;
    }

    public void setId(boolean signIn) {
        this.id = id;
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

    public byte[] getFacePic() {
        return facePic;
    }

    public void setFacePic(byte[] facePic) {
        this.facePic = facePic;
    }

    public FaceEntity toFaceEntry() { return new FaceEntity(id, featureData, name, facePic); }
}
