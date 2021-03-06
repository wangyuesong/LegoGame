package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elaine on 12/2/15.
 */
public class DoubleCurveObject extends Object3D {
    public DoubleCurveObject(int x, int y, int z, int t) {
        super(x, y, z, t);

        bottomCenterX=x*Const.cubeSize;
        bottomCenterY=y*Const.cubeSize;
        bottomCenterZ=z*Const.cubeSize;

        List<int[]> doublecurveOffsetList =new ArrayList<int[]>();
        doublecurveOffsetList.add(new int[]{0, 0, 0});
        doublecurveOffsetList.add(new int[]{2, 0, 0});
        doublecurveOffsetList.add(new int[]{1, 0, 0});
        doublecurveOffsetList.add(new int[]{0, 1, 0});
        this.offsetList = doublecurveOffsetList;

        List<float[]> onBoardOffsetList =new ArrayList<float[]>();
        onBoardOffsetList.add(new float[]{0.0f, 0.0f, 0.0f});
        onBoardOffsetList.add(new float[]{2, 0, 0});
        onBoardOffsetList.add(new float[]{1, 0, 0});
        onBoardOffsetList.add(new float[]{0, 1, 0});
        this.boardOffsetList = onBoardOffsetList;


        List<float[]> onBottomOffsetList =new ArrayList<float[]>();
        onBottomOffsetList.add(new float[]{0.0f, 0.0f, 0.0f});
        onBottomOffsetList.add(new float[]{2, 0, 0});
        onBottomOffsetList.add(new float[]{1, 0, 0});
        onBottomOffsetList.add(new float[]{0, 1, 0});
        this.bottomOffsetList =onBottomOffsetList;

    }
}
