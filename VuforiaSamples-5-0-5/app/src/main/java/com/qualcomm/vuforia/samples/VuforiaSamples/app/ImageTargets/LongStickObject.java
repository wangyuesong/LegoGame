package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuesongwang on 11/30/15.
 */
public class LongStickObject extends Object3D {
    public LongStickObject(int x, int y, int z, int t) {
        super(x, y, z, t);
        List<int[]>longStickOffsetList =new ArrayList<int[]>();
        longStickOffsetList.add(new int[]{0, 0, 0});
        longStickOffsetList.add(new int[]{0, 0, 1});
        longStickOffsetList.add(new int[]{0, 0, 2});
        longStickOffsetList.add(new int[]{0, 0, 3});
        longStickOffsetList.add(new int[]{0, 0, 4});
        this.offsetList = longStickOffsetList;
    }

}
