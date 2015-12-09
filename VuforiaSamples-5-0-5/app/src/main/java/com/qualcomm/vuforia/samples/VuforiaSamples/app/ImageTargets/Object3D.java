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

    public boolean leaveBottom = false;
    public boolean isFalling = false;
    public boolean isOnGround = false;
    public boolean ableToFall = false;
    public int fallCount = 0;

    public HashMap<Integer,boolean[][]> pileIsOrNotOccupied;

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
    }



    private float[] convert(float[] modelViewMatrix, float[] oldXYZ) {
        float newX = oldXYZ[0]  * modelViewMatrix[0] + oldXYZ[1]  * modelViewMatrix[4]
                + oldXYZ[2]  * modelViewMatrix[8] + modelViewMatrix[12];
        float newY = oldXYZ[0]   * modelViewMatrix[1] +  oldXYZ[1]  * modelViewMatrix[5]
                +  oldXYZ[2]  * modelViewMatrix[9] + modelViewMatrix[13];
        float newZ =  oldXYZ[0]   * modelViewMatrix[2] +  oldXYZ[1]  * modelViewMatrix[6]
                +  oldXYZ[2]   * modelViewMatrix[10] + modelViewMatrix[14];
        return new float[]{newX,newY,newZ};
    }


    public void moveLeft(Object3D pileObject)
    {
        Log.i("MoveLeft","move left");

        if (isInPileBorder(pileObject, new float[]{bottomCenterX-Const.cubeSize,bottomCenterY,bottomCenterZ}, bottomOffsetList)) {
            bottomCenterX -= Const.cubeSize;
        }
    }

    public void moveRight(Object3D pileObject){
        Log.i("MoveRight","move right");

        if (isInPileBorder(pileObject, new float[]{bottomCenterX+Const.cubeSize,bottomCenterY,bottomCenterZ}, bottomOffsetList)) {
            bottomCenterX += Const.cubeSize;
        }
    }
    public void moveFront(Object3D pileObject){
        Log.i("MoveFront","move front");

        List<float[]> tempOffsetList=new ArrayList<float[]>();

        if (isInPileBorder(pileObject, new float[]{bottomCenterX,bottomCenterY+Const.cubeSize,bottomCenterZ}, bottomOffsetList)) {
            bottomCenterY += Const.cubeSize;
        }

    }

    public void moveBack(Object3D pileObject) {
        Log.i("Move Back", "move Back");

        if (isInPileBorder(pileObject, new float[]{bottomCenterX, bottomCenterY - Const.cubeSize, bottomCenterZ}, bottomOffsetList)) {
            bottomCenterY -= Const.cubeSize;
        }
    }

    public final static int NOT_FALLING = 1;
    public final static int FALLING = 2;

    //when not falling
    public void updateBottomXYZ(float[] boardToBottomModelViewMatrix,Object3D pileObject,int mode)
    {
        float[] temp = convert(boardToBottomModelViewMatrix, new float[]{boardCenterX, boardCenterY, boardCenterZ});

        switch (mode) {
            case NOT_FALLING:

                //转换到格点
                float[] result = getInGridCoordinate(temp);
                bottomCenterX = result[0];
                bottomCenterY = result[1];
                bottomCenterZ = result[2];

                Matrix.multiplyMM(onBottomSelfRotationMatrix, 0
                        , getRotationMatrix(boardToBottomModelViewMatrix), 0
                        , onBoardSelfRotationMatrix, 0);
                onBottomSelfRotationMatrix
                        = getGridRotationMatrix(onBottomSelfRotationMatrix).clone();

                for (int i = 0; i < boardOffsetList.size(); i++)
                    bottomOffsetList.set(i, convert(onBottomSelfRotationMatrix, boardOffsetList.get(i)));

                if (isInPileBorder(pileObject, result, bottomOffsetList)) ableToFall = true;
                else ableToFall=false;

                break;
            case FALLING:

                //转换到格点
                //float[] tempCenter = getInGridCoordinate(temp);

                float[] tempRotationMatrix = new float[16];
                Matrix.multiplyMM(tempRotationMatrix, 0
                        , getRotationMatrix(boardToBottomModelViewMatrix), 0
                        , onBoardSelfRotationMatrix, 0);
                tempRotationMatrix
                        = getGridRotationMatrix(tempRotationMatrix).clone();


                List<float[]> tempOffsetList = new ArrayList<float[]>();

                for (int i = 0; i < boardOffsetList.size(); i++)
                    tempOffsetList.add(i, convert(tempRotationMatrix, boardOffsetList.get(i)));

                if (isInPileBorder(pileObject, new float[]{bottomCenterX,boardCenterY,bottomCenterZ}, tempOffsetList)) {
                    onBottomSelfRotationMatrix = tempRotationMatrix.clone();
                    bottomOffsetList = tempOffsetList;

                    //bottomCenterX = tempCenter[0];
                    //bottomCenterY = tempCenter[1];
                    //bottomCenterZ = tempCenter[2];

                } else {
                    float[] bottomToBoardModelViewMatrix = new float[16];
                    Matrix.invertM(bottomToBoardModelViewMatrix, 0, boardToBottomModelViewMatrix, 0);

                    Matrix.multiplyMM(onBoardSelfRotationMatrix, 0
                            , getRotationMatrix(bottomToBoardModelViewMatrix), 0
                            , onBottomSelfRotationMatrix, 0);
                }
                break;
        }

    }

    public float[] getGridRotationMatrix(float[] rotationMatrix)
    {
        double cosThetaX=rotationMatrix[0]/Math.sqrt(rotationMatrix[0] * rotationMatrix[0]
                +rotationMatrix[1]*rotationMatrix[1]+rotationMatrix[2]*rotationMatrix[2]);
        double cosThetaY=rotationMatrix[5]/Math.sqrt(rotationMatrix[4] * rotationMatrix[4]
                +rotationMatrix[5]*rotationMatrix[5]+rotationMatrix[6]*rotationMatrix[6]);
        double cosThetaZ=rotationMatrix[10]/Math.sqrt(rotationMatrix[8] * rotationMatrix[8]
                +rotationMatrix[9]*rotationMatrix[9]+rotationMatrix[10]*rotationMatrix[10]);

        boolean[] axisIsTaken=new boolean[6];
        float[]  destAxisX= getGridAxisVector(rotationMatrix[0], rotationMatrix[1], rotationMatrix[2],axisIsTaken);
        axisIsTaken = getNewAxisIsTaken(axisIsTaken, destAxisX);
        float[]  destAxisY = getGridAxisVector(rotationMatrix[4],rotationMatrix[5],rotationMatrix[6],axisIsTaken);
        axisIsTaken = getNewAxisIsTaken(axisIsTaken, destAxisY);
        float[]  destAxisZ = getGridAxisVector(rotationMatrix[8],rotationMatrix[9],rotationMatrix[10],axisIsTaken);

        float[] gridRotationMatrix=new float[16];
        Matrix.setIdentityM(gridRotationMatrix,0);

        gridRotationMatrix[0]=destAxisX[0];
        gridRotationMatrix[1]=destAxisX[1];
        gridRotationMatrix[2]=destAxisX[2];

        gridRotationMatrix[4]=destAxisY[0];
        gridRotationMatrix[5]=destAxisY[1];
        gridRotationMatrix[6]=destAxisY[2];

        gridRotationMatrix[8]=destAxisZ[0];
        gridRotationMatrix[9]=destAxisZ[1];
        gridRotationMatrix[10]=destAxisZ[2];

        //Log.i(LOGTAG,gridRotationMatrix[0]+" "+gridRotationMatrix[1]+" "+gridRotationMatrix[2]
          //      +" "+gridRotationMatrix[4]+" "+gridRotationMatrix[5]+" "+gridRotationMatrix[6]
            //    +" "+gridRotationMatrix[8]+" "+gridRotationMatrix[9]+" "+gridRotationMatrix[10]);

        return gridRotationMatrix;
    }

    private boolean[] getNewAxisIsTaken(boolean[] axisIsTaken, float[] destAxis) {
        if(destAxis[0]!=0) {
            axisIsTaken[0] = true;
            axisIsTaken[1] = true;
        }
        else if(destAxis[1]!=0) {
            axisIsTaken[2] = true;
            axisIsTaken[3] = true;
        }
        else
        {
            axisIsTaken[4] = true;
            axisIsTaken[5] = true;
        }
        return axisIsTaken;
    }

    private float[] getGridAxisVector(float x,float y,float z,boolean[] axisIsTaken)
    {
        float max=-2;
        float[] destAxisVector=new float[3];
        double length=Math.sqrt(x*x+y*y+z*z);

        if(!axisIsTaken[0] && !axisIsTaken[1])
        {
            if(x*1.0/length>max) {destAxisVector[0]=1;destAxisVector[1]=0;destAxisVector[2]=0;max=x;}
            if(-x*1.0/length>max) {destAxisVector[0]=-1;destAxisVector[1]=0;destAxisVector[2]=0;max=-x;}
        }
        if(!axisIsTaken[2] && !axisIsTaken[3])
        {
            if(y*1.0/length>max) {destAxisVector[0]=0;destAxisVector[1]=1;destAxisVector[2]=0;max=y;}
            if(-y*1.0/length>max) {destAxisVector[0]=0;destAxisVector[1]=-1;destAxisVector[2]=0;max=-y;}
        }
        if(!axisIsTaken[4] && !axisIsTaken[5])
        {
            if(z*1.0/length>max) {destAxisVector[0]=0;destAxisVector[1]=0;destAxisVector[2]=1;max=z;}
            if(-z*1.0/length>max) {destAxisVector[0]=0;destAxisVector[1]=0;destAxisVector[2]=-1;max=-z;}
        }

        return destAxisVector;
    }

    private boolean isInPileBorder(Object3D pileObject,float[] tempCenter,List<float[]> tempOffsetList)
    {
        for(float[] oneOffset : tempOffsetList) {

            int indexX = (int)(tempCenter[0]/Const.cubeSize)+(int)oneOffset[0]+Const.bottomWidth/2;
            int indexY = (int)(tempCenter[1]/Const.cubeSize)+(int)oneOffset[1]+Const.bottomLength/2;
            int indexZ = (int)(tempCenter[2]/Const.cubeSize)+(int)oneOffset[2];

            if (indexX<0 || indexX>=Const.bottomWidth
                    || indexY<0 || indexY>=Const.bottomLength
                    || indexZ<0 || indexZ>=Const.bottomHeight)
                return false;

            if(pileObject.pileIsOrNotOccupied.get(indexZ)[indexX][indexY])
                return false;
        }
        return true;
    }


    private float[] getInGridCoordinate(float[] inputCoordinate)
    {
        float[] outputCoordinate=new float[3];

        outputCoordinate[0] = Math.round(inputCoordinate[0]/Const.cubeSize)*Const.cubeSize;
        outputCoordinate[1] = Math.round(inputCoordinate[1]/Const.cubeSize)*Const.cubeSize;
        outputCoordinate[2] = Math.round(inputCoordinate[2]/Const.cubeSize)*Const.cubeSize;

        return outputCoordinate;
    }
//
//    private float[] getWithinBorderCoordinate(float[] inputCoordinate)
//    {
//        float const1 = (Const.bottomWidth -2)/2* Const.cubeSize;
//        float const2 = (Const.bottomLength -2)/2 * Const.cubeSize;
//        float[] outputCoordinate=new float[3];
//        if(inputCoordinate[0]<-const1) outputCoordinate[0]=-const1;
//        else if(inputCoordinate[0]>const1) outputCoordinate[0]=const1;
//        else outputCoordinate[0]=inputCoordinate[0];
//
//        if(inputCoordinate[1]<-const2) outputCoordinate[1]=-const2;
//        else if(inputCoordinate[1]>const2) outputCoordinate[1]=const2;
//        else outputCoordinate[1]=inputCoordinate[1];
//
//        if(inputCoordinate[2]>9*Const.cubeSize || inputCoordinate[2]<0) outputCoordinate[2]=9*Const.cubeSize;
//        else outputCoordinate[2]=inputCoordinate[2];
//
//        return outputCoordinate;
//    }

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
        {centerZ --; bottomCenterZ-=Const.cubeSize;}

    }

    public void drop(Object3D pileObject){
        while(!detectCollision(pileObject) &&  !isOnGround){

                bottomCenterZ =bottomCenterZ - Const.cubeSize;

        }
            isOnGround = true;
            ((PileObject) pileObject).mergeAnObject(this);

    }
    public boolean detectCollision(Object3D pileObject){
        HashMap<Integer,boolean[][]> pile = new HashMap<>();
        for(int i =0 ; i < Const.bottomHeight; i++)
        {
            boolean[][] level = new boolean[Const.bottomWidth][Const.bottomLength];
            pile.put((int)i,level);
        }

        Log.i("Test","Object: x="  + bottomCenterX +", y=" + bottomCenterY + ", z=" + bottomCenterZ);

        for(int[] oneOffset: pileObject.offsetList)
        {
            boolean[][] origin = pile.get(oneOffset[2]);
            origin[oneOffset[0]+Const.bottomWidth/2][oneOffset[1]+Const.bottomLength/2]  = true;
            pile.put(oneOffset[2],origin);
        }

        for(float[] oneOffset : bottomOffsetList)
        {
            if(bottomCenterZ/Const.cubeSize -1+ oneOffset[2] < 0)
                continue;
            Log.i("Test","x="  + bottomCenterX +", y=" + bottomCenterY + ", z=" + bottomCenterZ);
            int level = (int)(bottomCenterZ/Const.cubeSize -1+ oneOffset[2]);
            int x =(int)(bottomCenterX/Const.cubeSize + oneOffset[0]
                    + Const.bottomWidth/2);
            int y = (int)(bottomCenterY/Const.cubeSize + oneOffset[1] + Const.bottomLength/2);
            Log.i("Levelxy","Level x y:"+ level + ","  + x + "," + y);
            if(x<0 || y<0 || x>=Const.bottomWidth || y>=Const.bottomLength) return false;

            if(pile.get(level)[x][y])
                return true;
        }
        return false;
    }



}
