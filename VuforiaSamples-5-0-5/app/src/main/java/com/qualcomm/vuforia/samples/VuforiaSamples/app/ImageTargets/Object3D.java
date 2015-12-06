package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;


import android.graphics.CornerPathEffect;
import android.opengl.Matrix;
import android.util.Log;

import com.qualcomm.vuforia.samples.SampleApplication.utils.CubeObject;

import java.security.Key;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

/**
 * Created by yuesongwang on 11/29/15.
 */
public class Object3D {
    public static int SCALE;

    public CubeObject cube;
    public List<int[]> offsetList;
    public int centerX;
    public int centerY;
    public int centerZ;

    public List<float[]> bottomOffsetList;
    public float bottomCenterX;
    public float bottomCenterY;
    public float bottomCenterZ;
    public float[] onBottomSelfRotationMatrix;

    public List<float[]> boardOffsetList;
    public float boardCenterX;
    public float boardCenterY;
    public float boardCenterZ;
    public float[] onBoardSelfRotationMatrix;

    public float[] selfRotationMatrix;
    public int textureId;

    public boolean isInBoardCoordinate = true;
    public boolean isFalling = false;
    public boolean isMoved = false;
    public int  moveCount = 0;
    public int fallCount = 0;


    private float[] getRotationMatrix(float[] modelViewMatrix)
    {
        float[] temp=new float[16];
        Matrix.setIdentityM(temp, 0);

        temp[0]=modelViewMatrix[0];
        temp[1]=modelViewMatrix[1];
        temp[2]=modelViewMatrix[2];
        temp[4]=modelViewMatrix[4];
        temp[5]=modelViewMatrix[5];
        temp[6]=modelViewMatrix[6];
        temp[8]=modelViewMatrix[8];
        temp[9]=modelViewMatrix[9];
        temp[10]=modelViewMatrix[10];

        return temp;
    }
    //Question
    public void updateBoardXYZ(float[] boardToBottomModelViewMatrix)
    {
        float[] bottomToBoardModelViewMatrix=new float[16];
        android.opengl.Matrix.invertM(bottomToBoardModelViewMatrix, 0, boardToBottomModelViewMatrix, 0);
        float[] temp = convert(bottomToBoardModelViewMatrix,new float[]{bottomCenterX,bottomCenterY,bottomCenterZ});
        boardCenterX = temp[0];
        boardCenterY = temp[1];
        boardCenterZ = temp[2];
        //Offset only rotate
//        for(int i = 0; i < bottomOffsetList.size(); i ++)
//            boardOffsetList.set(i,convert(getRotationMatrix(bottomToBoardModelViewMatrix), bottomOffsetList.get(i)));

    }

//    public void updateBoardOffset(float[] rotationMatrix){
//        for(int i = 0; i < boardOffsetList.size(); i ++)
//            boardOffsetList.set(i,convert(rotationMatrix, bottomOffsetList.get(i)));
//    }


    private float[] convert(float[] modelViewMatrix, float[] oldXYZ) {
        float newX = oldXYZ[0]  * modelViewMatrix[0] + oldXYZ[1]  * modelViewMatrix[4]
                + oldXYZ[2]  * modelViewMatrix[8] + modelViewMatrix[12];
        float newY = oldXYZ[0]   * modelViewMatrix[1] +  oldXYZ[1]  * modelViewMatrix[5]
                +  oldXYZ[2]  * modelViewMatrix[9] + modelViewMatrix[13];
        float newZ =  oldXYZ[0]   * modelViewMatrix[2] +  oldXYZ[1]  * modelViewMatrix[6]
                +  oldXYZ[2]   * modelViewMatrix[10] + modelViewMatrix[14];
        return new float[]{newX,newY,newZ};
    }


    public void updateBottomXYZ(float[] boardToBottomModelViewMatrix)
    {
        float[] temp = convert(boardToBottomModelViewMatrix, new float[]{boardCenterX, boardCenterY, boardCenterZ});
        float[] temp2 = getWithinBorderCoordinate(temp);
        //float[] result=temp.clone();
        float[] result = getInGridCoordinate(temp2);
        bottomCenterX = result[0];
        bottomCenterY = result[1];
        bottomCenterZ = result[2];

        updateBottomOffsetList();

        //Offset only rotate
//        for(int i = 0; i < boardOffsetList.size(); i ++)
//            bottomOffsetList.set(i, convert(getRotationMatrix(boardToBottomModelViewMatrix), boardOffsetList.get(i)));

    }

    public void updateBottomOffsetList()
    {
        for(int i = 0; i < boardOffsetList.size(); i ++)
        bottomOffsetList.set(i, convert(onBottomSelfRotationMatrix, boardOffsetList.get(i)));
    }

    private float[] getInGridCoordinate(float[] inputCoordinate)
    {
        float[] outputCoordinate=new float[3];

        outputCoordinate[0] = Math.round(inputCoordinate[0]/20)*20;
        outputCoordinate[1] = Math.round(inputCoordinate[1]/20)*20;
        outputCoordinate[2] = Math.round(inputCoordinate[2]/20)*20;

        return outputCoordinate;
    }

    private float[] getWithinBorderCoordinate(float[] inputCoordinate)
    {
        float[] outputCoordinate=new float[3];
        if(inputCoordinate[0]<-100) outputCoordinate[0]=-100;
        else if(inputCoordinate[0]>100) outputCoordinate[0]=100;
        else outputCoordinate[0]=inputCoordinate[0];

        if(inputCoordinate[1]<-60) outputCoordinate[1]=-60;
        else if(inputCoordinate[1]>60) outputCoordinate[1]=60;
        else outputCoordinate[1]=inputCoordinate[1];

        if(inputCoordinate[2]>180 || inputCoordinate[2]<0) outputCoordinate[2]=180;
        else outputCoordinate[2]=inputCoordinate[2];

        return outputCoordinate;
    }

//
//    public void updateBottomOffset(float[] boardToBottomModelViewMatrix){
//        for(int i = 0; i < bottomOffsetList.size(); i ++)
//            bottomOffsetList.set(i,convert(boardToBottomModelViewMatrix, boardOffsetList.get(i)));
//    }



    public Object3D(int x, int y, int z,int t) {
        this.centerX = x;
        this.centerY = y;
        this.centerZ = z;
        this.textureId =t;
        cube = new CubeObject();

        bottomOffsetList = new ArrayList<>();
        onBottomSelfRotationMatrix = new float[16];

        boardOffsetList = new ArrayList<>();
        onBoardSelfRotationMatrix = new float[16];

        Matrix.setIdentityM(onBoardSelfRotationMatrix,0);
        Matrix.setIdentityM(onBottomSelfRotationMatrix,0);
    }

    public Object3D(int x, int y, int z,int t,List<int[]> offsetList) {
        this.centerX = x;
        this.centerY = y;
        this.centerZ = z;
        this.textureId =t;
        this.offsetList = offsetList;
        cube = new CubeObject();
    }

    public Object3D(int t) {
        this.textureId =t;
        cube = new CubeObject();
        bottomOffsetList = new ArrayList<>();
        onBottomSelfRotationMatrix = new float[16];

        boardOffsetList = new ArrayList<>();
        onBoardSelfRotationMatrix = new float[16];

        Matrix.setIdentityM(onBoardSelfRotationMatrix,0);
        Matrix.setIdentityM(onBottomSelfRotationMatrix,0);
    }

    public void down(Object3D pile){
        if (!detectCollision(pile)&&centerZ!=0)
        {centerZ --; bottomCenterZ-=20;}

    }


    public boolean detectCollision(Object3D pileObject){

//        HashMap<Integer,boolean[][]> pile = new HashMap<>();
//        for(int i =0 ; i < 10; i ++)
//        {
//            boolean[][] level = new boolean[13][9];
//            pile.put(i,level);
//        }
//        for(int[] oneOffset: pileObject.offsetList)
//        {
//            boolean[][] origin = pile.get(oneOffset[2]);
//            origin[oneOffset[0]+6][oneOffset[1]+4]  = true;
//            pile.put(oneOffset[2],origin);
//        }
//
//        for(int[] oneOffset : offsetList)
//        {
//            if(centerZ -1+ oneOffset[2] < 0)
//                continue;
//            if(pile.get(centerZ -1+ oneOffset[2])[centerX + oneOffset[0] + 6][centerY + oneOffset[1] + 4])
//                return true;
//        }
//        return false;


        HashMap<Integer,boolean[][]> pile = new HashMap<>();
        for(int i =0 ; i < 10; i++)
        {
            boolean[][] level = new boolean[13][9];
            pile.put((int)i,level);
        }

        for(int[] oneOffset: pileObject.offsetList)
        {
            boolean[][] origin = pile.get(oneOffset[2]);
            origin[oneOffset[0]+6][oneOffset[1]+4]  = true;
            pile.put(oneOffset[2],origin);
        }

        for(float[] oneOffset : bottomOffsetList)
        {
            if(bottomCenterZ/20 -1+ oneOffset[2] < 0)
                continue;
            if(pile.get((int)(bottomCenterZ/20 -1+ oneOffset[2]))[(int)(bottomCenterX/20 + oneOffset[0] + 6)][(int)(bottomCenterY/20 + oneOffset[1] + 4)])
                return true;
        }
        return false;
    }



}
