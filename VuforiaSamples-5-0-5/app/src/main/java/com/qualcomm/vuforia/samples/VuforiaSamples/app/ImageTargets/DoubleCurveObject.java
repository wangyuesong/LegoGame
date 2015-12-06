package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elaine on 12/2/15.
 */
public class DoubleCurveObject extends Object3D {
    public DoubleCurveObject(int x, int y, int z, int t) {
        super(x, y, z, t);

        bottomCenterX=x*20.0f;
        bottomCenterY=y*20.0f;
        bottomCenterZ=z*20.0f;

        List<int[]> doublecurveOffsetList =new ArrayList<int[]>();
        doublecurveOffsetList.add(new int[]{0, 0, 0});
        doublecurveOffsetList.add(new int[]{0, 0, 1});
        doublecurveOffsetList.add(new int[]{1, 0, 0});
        doublecurveOffsetList.add(new int[]{0, 1, 0});
        this.offsetList = doublecurveOffsetList;

        List<float[]> onBoardOffsetList =new ArrayList<float[]>();
        onBoardOffsetList.add(new float[]{0.0f, 0.0f, 0.0f});
        onBoardOffsetList.add(new float[]{0, 0, 1});
        onBoardOffsetList.add(new float[]{1, 0, 0});
        onBoardOffsetList.add(new float[]{0, 1, 0});
        this.boardOffsetList = onBoardOffsetList;


        List<float[]> onBottomOffsetList =new ArrayList<float[]>();
        onBottomOffsetList.add(new float[]{0.0f, 0.0f, 0.0f});
        onBottomOffsetList.add(new float[]{0, 0, 1});
        onBottomOffsetList.add(new float[]{1, 0, 0});
        onBottomOffsetList.add(new float[]{0, 1, 0});
        this.bottomOffsetList =onBottomOffsetList;

    }
}
