package com.infowarelab.conference.face.faceserver;

import android.content.Context;
import android.util.Log;

import com.infowarelab.conference.face.dbutil.FaceDao;
import com.infowarelab.conference.face.dbutil.FaceEntity;
import com.infowarelab.conference.face.dbutil.FaceRoomDatabase;

import java.util.List;

public class RoomFaceDao implements FaceDatabaseAccessObject {
    private static FaceDao dao;
    private FaceRoomDatabase faceRoomDatabase;
    private final String TAG = "InfowareLab.Face";

    @Override
    public void init(Context context) {
        faceRoomDatabase = FaceRoomDatabase.getInstance(context);
        dao = faceRoomDatabase.userDao();
    }

    @Override
    public long insert(FaceEntity userEntity) {
        return dao == null ? -1 : dao.addFace(userEntity);
    }

    @Override
    public List<FaceEntity> getAll() {
        return dao == null ? null : dao.getAllFace();
    }

    @Override
    public void clearAll() {
        if (dao == null) {
            return;
        }
        dao.deleteAll();
    }

    @Override
    public void delete(FaceEntity faceEntity) {
        if (dao == null) {
            return;
        }
        dao.delete(faceEntity);
    }

    @Override
    public void release() {
        if (faceRoomDatabase != null) {
            faceRoomDatabase.close();
            faceRoomDatabase.release();
            faceRoomDatabase = null;
        }
    }
}
