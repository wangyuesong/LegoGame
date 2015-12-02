package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elaine on 12/2/15.
 */
public class DoubleCurveObject extends Object3D {
    public DoubleCurveObject(int x, int y, int z, int t) {
        super(x, y, z, t);
        List<int[]> doublecurveOffsetList =new ArrayList<int[]>();
        doublecurveOffsetList.add(new int[]{0, 0, 0});
        doublecurveOffsetList.add(new int[]{0, 0, 1});
        doublecurveOffsetList.add(new int[]{1, 0, 0});
        doublecurveOffsetList.add(new int[]{0, 1, 0});
        this.offsetList = doublecurveOffsetList;
    }
}
