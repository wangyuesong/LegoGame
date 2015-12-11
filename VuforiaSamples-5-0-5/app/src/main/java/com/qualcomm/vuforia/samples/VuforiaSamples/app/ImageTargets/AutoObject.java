package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elaine on 12/2/15.
 */
public class AutoObject extends Object3D{
    public AutoObject(int x, int y, int z, int t) {
        super(x, y, z, t);

        bottomCenterX=x*Const.cubeSize;
        bottomCenterY=y*Const.cubeSize;
        bottomCenterZ=z*Const.cubeSize;

        List<int[]> autoOffsetList =new ArrayList<int[]>();
        autoOffsetList.add(new int[]{0, 0, 0});
        this.offsetList = autoOffsetList;

        List<float[]> onBoardOffsetList =new ArrayList<float[]>();
        onBoardOffsetList.add(new float[]{0.0f, 0.0f, 0.0f});
        this.boardOffsetList = onBoardOffsetList;


        List<float[]> onBottomOffsetList =new ArrayList<float[]>();
        onBottomOffsetList.add(new float[]{0.0f, 0.0f, 0.0f});
        this.bottomOffsetList =onBottomOffsetList;

    }
}
