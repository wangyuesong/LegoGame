package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuesongwang on 11/30/15.
 */
public class LongStickObject extends Object3D {
    public LongStickObject(int x, int y, int z, int t) {
        super(x, y, z, t);

        bottomCenterX=x*20.0f;
        bottomCenterY=y*20.0f;
        bottomCenterZ=z*20.0f;

        List<int[]>longStickOffsetList =new ArrayList<int[]>();
        longStickOffsetList.add(new int[]{0, 0, 0});
        longStickOffsetList.add(new int[]{0, 0, 1});
        longStickOffsetList.add(new int[]{0, 0, 2});
        longStickOffsetList.add(new int[]{0, 0, 3});
        longStickOffsetList.add(new int[]{0, 0, 4});
        this.offsetList = longStickOffsetList;


        List<float[]> onBoardOffsetList =new ArrayList<float[]>();
        onBoardOffsetList.add(new float[]{0.0f, 0.0f, 0.0f});
        onBoardOffsetList.add(new float[]{0, 0, 1});
        onBoardOffsetList.add(new float[]{0, 0, 2});
        onBoardOffsetList.add(new float[]{0, 0, 3});
        onBoardOffsetList.add(new float[]{0, 0, 4});
        this.boardOffsetList = onBoardOffsetList;


        List<float[]> onBottomOffsetList =new ArrayList<float[]>();
        onBottomOffsetList.add(new float[]{0.0f, 0.0f, 0.0f});
        onBottomOffsetList.add(new float[]{0, 0, 1});
        onBottomOffsetList.add(new float[]{0, 0, 2});
        onBottomOffsetList.add(new float[]{0, 0, 3});
        onBottomOffsetList.add(new float[]{0, 0, 4});
        this.bottomOffsetList =onBottomOffsetList;

    }

}
