package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuesongwang on 11/30/15.
 */
public class ShortStickObject extends Object3D {
    public ShortStickObject(int x, int y, int z, int t) {
        super(x, y, z, t);
        List<int[]> longStickOffsetList =new ArrayList<int[]>();
        longStickOffsetList.add(new int[]{0, 0, 0});
        longStickOffsetList.add(new int[]{0, 0, 1});
        this.offsetList = longStickOffsetList;
    }
}
