package com.infowarelab.conference.face.faceserver;


import android.content.Context;

import com.infowarelab.conference.face.dbutil.FaceDatabaseManager;
import com.infowarelab.conference.face.dbutil.FaceEntity;

import java.util.List;

public class SQLiteFaceDao implements FaceDatabaseAccessObject {
    private static FaceDatabaseManager faceDataBaseManager;

    @Override
    public void init(Context context) {
        faceDataBaseManager = new FaceDatabaseManager(context);
    }

    @Override
    public long insert(FaceEntity userEntity) {
        if (faceDataBaseManager == null) {
            return -1;
        }
        return faceDataBaseManager.addFace(userEntity.getFaceName(), userEntity.getFaceFeature(), userEntity.getFacePic());
    }

    @Override
    public List<FaceEntity> getAll() {
        if (faceDataBaseManager == null) {
            return null;
        }
        return faceDataBaseManager.selectAllFaces();
    }

    @Override
    public void clearAll() {
        if (faceDataBaseManager == null) {
            return;
        }
        faceDataBaseManager.deleteAllFace();
    }

    @Override
    public void delete(FaceEntity faceEntity) {
        if (faceDataBaseManager == null || faceEntity == null) {
            return;
        }
        faceDataBaseManager.deleteFaceById(faceEntity.getId());
    }

    @Override
    public void release() {
        if (faceDataBaseManager == null) {
            return;
        }
        faceDataBaseManager.release();
    }
}
