package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuesongwang on 11/30/15.
 */
public class CurveObject extends  Object3D {
    public CurveObject(int x, int y, int z, int t) {
        super(x, y, z, t);

        bottomCenterX=x*Const.cubeSize;
        bottomCenterY=y*Const.cubeSize;
        bottomCenterZ=z*Const.cubeSize;


        List<int[]> longStickOffsetList =new ArrayList<int[]>();
        longStickOffsetList.add(new int[]{0, 0, 0});
        longStickOffsetList.add(new int[]{0, 0, 1});
        longStickOffsetList.add(new int[]{1, 0, 0});
        this.offsetList = longStickOffsetList;


        List<float[]> onBoardOffsetList =new ArrayList<float[]>();
        onBoardOffsetList.add(new float[]{0.0f, 0.0f, 0.0f});
        onBoardOffsetList.add(new float[]{0, 0, 1});
        onBoardOffsetList.add(new float[]{1, 0, 0});
        this.boardOffsetList = onBoardOffsetList;

        List<float[]> onBottomOffsetList =new ArrayList<float[]>();
        onBottomOffsetList.add(new float[]{0.0f, 0.0f, 0.0f});
        onBottomOffsetList.add(new float[]{0, 0, 1});
        onBottomOffsetList.add(new float[]{1, 0, 0});
        this.bottomOffsetList =onBottomOffsetList;

    }

}
