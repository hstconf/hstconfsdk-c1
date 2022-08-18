package com.infowarelab.conference.face.util;

import android.graphics.Rect;
import android.util.Log;

import com.arcsoft.face.FaceInfo;
import com.infowarelabsdk.conference.domain.Point;

import java.util.List;

public class TrackUtil {

    public static boolean isSameFace(FaceInfo faceInfo1,FaceInfo faceInfo2) {
        return faceInfo1.getFaceId() == faceInfo2.getFaceId();
    }

    public static void keepMaxFace(List<FaceInfo> ftFaceList) {
        if (ftFaceList == null || ftFaceList.size() <= 1) {
            return;
        }
        FaceInfo maxFaceInfo = ftFaceList.get(0);
        for (FaceInfo faceInfo : ftFaceList) {
            if (faceInfo.getRect().width() > maxFaceInfo.getRect().width()) {
                maxFaceInfo = faceInfo;
            }
        }
        ftFaceList.clear();
        ftFaceList.add(maxFaceInfo);
    }

    public static void keepMaxFaceWithDetectArea(List<FaceInfo> ftFaceList, Rect detectRect) {
        if (ftFaceList == null || ftFaceList.size() <= 0) {
            return;
        }
        FaceInfo maxFaceInfo = null;

        for (FaceInfo faceInfo : ftFaceList) {

            if (detectRect != null) {

                boolean inDetectArea = detectRect.contains(faceInfo.getRect());

                if (!inDetectArea) {
                    //Log.d("InfowareLab.Face", "NOT visible for the face rectangle!");
                    continue;
                }
            }

            if (maxFaceInfo == null) maxFaceInfo = faceInfo;

            if (faceInfo.getRect().width() > maxFaceInfo.getRect().width()) {
                maxFaceInfo = faceInfo;
            }
        }
        ftFaceList.clear();
        if (maxFaceInfo != null)
            ftFaceList.add(maxFaceInfo);
    }
}
