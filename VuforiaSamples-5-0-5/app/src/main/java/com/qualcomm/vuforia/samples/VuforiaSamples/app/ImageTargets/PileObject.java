package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.util.List;

/**
 * Created by yuesongwang on 11/30/15.
 */
public class PileObject extends Object3D {

    public void mergeAnObject(Object3D other)
    {
        for (int i =0; i<other.offsetList.size();i++)
        {
         this.offsetList.add(new int[]{other.centerX+other.offsetList.get(i)[0],
                other.centerY + other.offsetList.get(i)[1],
                other.centerZ + other.offsetList.get(i)[2]});
        }
    }

    public PileObject(int x, int y, int z, int t, List<int[]> offsetList) {
        super(x, y, z, t,offsetList);

    }
}
