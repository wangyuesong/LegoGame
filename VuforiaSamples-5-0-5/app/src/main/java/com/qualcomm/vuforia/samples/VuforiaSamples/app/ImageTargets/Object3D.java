package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;


import android.opengl.Matrix;

import com.qualcomm.vuforia.samples.SampleApplication.utils.CubeObject;
import com.qualcomm.vuforia.samples.SampleApplication.utils.MeshObject;

import java.util.List;

/**
 * Created by yuesongwang on 11/29/15.
 */
public class Object3D {
    public CubeObject cube;
    public List<float[]>CenterList;
    private double length, width, height;
    public float Center_X;
    public float Center_Y;
    public float Center_Z;
    public boolean isSticked;
    public boolean lock;
    public float[] selfRotationMatrix;
    public int textureId;
    public int interval;

    public Object3D(float x, float y, float z,int t,List<float[]>c) {
//        selfRotationMatrix = new float[16];
//        Matrix.setIdentityM(selfRotationMatrix,0);
//        interval = 50;
        this.length = length;
        this.width = width;
        this.height = height;
        this.Center_X = x;
        this.Center_Y = y;
        this.Center_Z = z;
        this.CenterList = c;
        this.textureId =t;
        cube = new CubeObject();
    }
}
