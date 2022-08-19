package com.infowarelab.conference.face.dbutil;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

// entity声明定义，并且指定了映射数据表明
@Entity(tableName = "Face")
public class FaceEntity {
    // 设置主键，并且定义自增增
    @PrimaryKey(autoGenerate = true)
    public int id;
    // 字段映射具体的数据表字段名
    @ColumnInfo(name = "faceName")
    private String faceName;

    @ColumnInfo(name = "faceFeature")
    private byte[] faceFeature;

    @ColumnInfo(name = "facePic")
    private byte[] facePic;

    public FaceEntity() {
        this.id = 0;
        this.faceFeature = null;
        this.faceName = null;
        this.facePic = null;
    }

    @Ignore
    public FaceEntity(int id, byte[] faceFeature, String faceName, byte[] facePic) {
        this.id = id;
        this.faceFeature = faceFeature;
        this.faceName = faceName;
        this.facePic = facePic;
    }

    public String getFaceName() {
        return faceName;
    }

    public void setFaceName(String faceName) {
        this.faceName = faceName;
    }

    public byte[] getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeature(byte[] faceFeature) {
        this.faceFeature = faceFeature;
    }

    public byte[] getFacePic() {
        return facePic;
    }

    public void setFacePic(byte[] facePic) {
        this.facePic = facePic;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
