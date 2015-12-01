package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;


import android.graphics.CornerPathEffect;

import com.qualcomm.vuforia.samples.SampleApplication.utils.CubeObject;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yuesongwang on 11/29/15.
 */
public class Object3D {
    public static int SCALE;

    public CubeObject cube;
    public List<int[]> offsetList;
    private double length, width, height;
    public int centerX;
    public int centerY;
    public int centerZ;
    public boolean isSticked;
    public boolean lock;
    public float[] selfRotationMatrix;
    public int textureId;
    public int interval;

    public Object3D(int x, int y, int z,int t) {
        this.centerX = x;
        this.centerY = y;
        this.centerZ = z;
        this.textureId =t;
        cube = new CubeObject();
    }

    public Object3D(int x, int y, int z,int t,List<int[]> offsetList) {
        this.centerX = x;
        this.centerY = y;
        this.centerZ = z;
        this.textureId =t;
        this.offsetList = offsetList;
        cube = new CubeObject();
    }


    public void down(HashMap<Integer,boolean[][]> pile){
        if (!detectCollision(pile))
         centerZ --;

    }

    public boolean detectCollision(HashMap<Integer,boolean[][]> pile){
        List<int[]> newOffsetList = new ArrayList<>(offsetList);
        newOffsetList.add(new int[]{0,0,0});
        for(int[] oneOffset : offsetList)
        {
            if(centerZ -1+ oneOffset[2] < 0)
                continue;
            if(pile.get(centerZ -1+ oneOffset[2])[centerX + oneOffset[0] + 6][centerY + oneOffset[1] + 4])
                return true;
        }
        return false;
    }



}
