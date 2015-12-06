package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yuesongwang on 11/30/15.
 */
public class PileObject extends Object3D {

    public void mergeAnObject(Object3D other) {
//        for (int i =0; i<other.offsetList.size();i++)
//        {
//         this.offsetList.add(new int[]{other.centerX+other.offsetList.get(i)[0],
//                other.centerY + other.offsetList.get(i)[1],
//                other.centerZ + other.offsetList.get(i)[2]});
//        }
        for (int i = 0; i < other.bottomOffsetList.size(); i++) {
            this.offsetList.add(new int[]{(int) (other.bottomCenterX/20 + other.bottomOffsetList.get(i)[0]),
                    (int) (other.bottomCenterY/20 + other.bottomOffsetList.get(i)[1]),
                    (int) (other.bottomCenterZ/20 + other.bottomOffsetList.get(i)[2])});
        }
    }
    public void elimate(){
        int []countlevel = new int[10];
    for(int[] oneOffset: this.offsetList)
    {
        countlevel[oneOffset[2]]++;
    }

        for (int i = 1;i <10 ;i++){
            if (countlevel[i] == 13*9){
                for (int j = 0; j < this.offsetList.size(); j++) {
                    if (offsetList.get(j)[2] == i) {
                        this.offsetList.remove(j);
                        j --;
                    }
                }
            for (int k =0; k<offsetList.size();k++){
                if(offsetList.get(k)[2]> i ) offsetList.get(k)[2]--;
            }
            }
        }

    }


    public PileObject(int x, int y, int z, int t, List<int[]> offsetList) {
        super(x, y, z, t,offsetList);

    }
}
