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


    public void down(Object3D pile){
        if (!detectCollision(pile))
         centerZ --;

    }


//    for(Integer key: pile.keySet())
//    {
//        boolean[][] level = pile.get(key);
//        for(int i = 0; i < level.length; i ++)
//            for(int j = 0; j < level[0].length; j ++)
//            {
//                if(level[i][j])
//                {
//                    pileList.add(new int[]{i - 6, j - 4, key});
//                }
//            }
//    }

    public boolean detectCollision(Object3D pileObject){

        HashMap<Integer,boolean[][]> pile = new HashMap<>();
        for(int i =0 ; i < 10; i ++)
        {
            boolean[][] level = new boolean[13][9];
            pile.put(i,level);
        }
        for(int[] oneOffset: pileObject.offsetList)
        {
            boolean[][] origin = pile.get(oneOffset[2]);
            origin[oneOffset[0]+6][oneOffset[1]+4]  = true;
            pile.put(oneOffset[2],origin);
        }

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
