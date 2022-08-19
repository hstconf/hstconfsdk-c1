package com.infowarelab.conference.face.faceserver;

import android.content.Context;

import com.infowarelab.conference.face.dbutil.FaceEntity;

import java.util.List;

public interface FaceDatabaseAccessObject {
    /**
     * 初始化
     */
    void init(Context context);

    /**
     * 插入一个人脸
     *
     * @param userEntity 人脸信息
     * @return index
     */
    long insert(FaceEntity userEntity);

    /**
     * 获取所有人脸
     *
     * @return 所有人脸
     */
    List<FaceEntity> getAll();

    /**
     * 清空所有人脸
     */
    void clearAll();

    void delete(FaceEntity faceEntity);

    /**
     * 回收资源操作
     */
    void release();
}
