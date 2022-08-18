package com.infowarelab.conference.face.faceserver;



public class CompareResult {
    private String userName;
    private float similar;
    private int trackId;
    private int signInResult = 0;

    public CompareResult(String userName, float similar) {
        this.userName = userName;
        this.similar = similar;
    }

    public CompareResult(String userName, float similar, int signInResult) {
        this.userName = userName;
        this.similar = similar;
        this.signInResult = signInResult;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public float getSimilar() {
        return similar;
    }

    public void setSimilar(float similar) {
        this.similar = similar;
    }

    public int getTrackId() {
        return trackId;
    }

    public int getSignInResult() {
        return signInResult;
    }
    public void setTrackId(int trackId) {
        this.trackId = trackId;
    }
}
