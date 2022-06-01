package com.infowarelab.conference.ui.activity.inconf.view.video;

import java.io.Serializable;

/**
 * Created by Always on 2017/11/20.
 */

public class LocalCameraPosition implements Serializable {
    private int left;
    private int top;
    private int bottom;
    private int right;
    private int width;
    private int height;

    private boolean isShowName = true;

    public LocalCameraPosition() {
        this.left = 0;
        this.top = 0;
        this.bottom = 0;
        this.right = 0;
        this.width = 1;
        this.height = 1;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isShowName() {
        return isShowName;
    }

    public void setShowName(boolean showName) {
        isShowName = showName;
    }

    @Override
    public String toString() {
        return "LocalCameraPosition{" +
                "left=" + left +
                ", top=" + top +
                ", bottom=" + bottom +
                ", right=" + right +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
