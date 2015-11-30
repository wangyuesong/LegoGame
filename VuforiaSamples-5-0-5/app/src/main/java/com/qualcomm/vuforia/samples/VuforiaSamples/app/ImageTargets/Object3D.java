package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;


import android.opengl.Matrix;

import com.qualcomm.vuforia.samples.SampleApplication.utils.CubeObject;
import com.qualcomm.vuforia.samples.SampleApplication.utils.MeshObject;

/**
 * Created by yuesongwang on 11/29/15.
 */
public class Object3D {
    public CubeObject cube;
    private double length, width, height;
    public float X;
    public float Y;
    public float Z;
    public boolean isSticked;
    public boolean lock;
    public float[] selfRotationMatrix;
    public int textureId;
    public int interval;

    public Object3D(double length, double width, double height, float x, float y, float z) {
        selfRotationMatrix = new float[16];
        Matrix.setIdentityM(selfRotationMatrix,0);
        interval = 50;
        this.length = length;
        this.width = width;
        this.height = height;
        this.X = x;
        this.Y = y;
        this.Z = z;
        cube = new CubeObject(length,width,height);
    }
}
