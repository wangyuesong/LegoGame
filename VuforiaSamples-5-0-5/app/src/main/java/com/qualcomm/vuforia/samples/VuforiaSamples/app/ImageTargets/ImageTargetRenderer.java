/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.State;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.Trackable;
import com.qualcomm.vuforia.TrackableResult;
import com.qualcomm.vuforia.VIDEO_BACKGROUND_REFLECTION;
import com.qualcomm.vuforia.Vec2F;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.qualcomm.vuforia.samples.SampleApplication.utils.CubeShaders;
import com.qualcomm.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleApplication3DModel;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.qualcomm.vuforia.samples.SampleApplication.utils.Teapot;
import com.qualcomm.vuforia.samples.SampleApplication.utils.Texture;
import com.qualcomm.vuforia.samples.VuforiaSamples.R;

// The renderer class for the ImageTargets sample.
public class ImageTargetRenderer implements GLSurfaceView.Renderer, View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
    private static final String LOGTAG = "ImageTargetRenderer";

    private SampleApplicationSession vuforiaAppSession;
    private ImageTargets mActivity;

    private Vector<Texture> mTextures;

    private int shaderProgramID;

    private int vertexHandle;

    private int normalHandle;

    private int textureCoordHandle;

    private int mvpMatrixHandle;

    private int texSampler2DHandle;

    private float kBuildingScale = 12.0f;
    private SampleApplication3DModel mBuildingsModel;

    private Renderer mRenderer;

    boolean mIsActive = false;

    private static final float OBJECT_SCALE_FLOAT = 1.0f;

    private static final float CUBE_SIDE = 50.0f;

    private float autoRotateAngle=0.0f;

    //    public static final int Const.bottomWidth = 5;
//    public static final int Const.bottomLength = 3;
//    public static final int Const.cubeSize = 50;

    public ImageTargetRenderer(ImageTargets activity,
                               SampleApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;



    }

    // Called to draw the current frame.
    @Override
    public void onDrawFrame(GL10 gl)
    {
        if (!mIsActive)
            return;

        // Call our function to render content
        renderFrame();
    }


    // Called when the surface is created or recreated.
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceCreated");

        initRendering();

        // Call Vuforia function to (re)initialize rendering after first use
        // or after OpenGL ES context was lost (e.g. after onPause/onResume):
        vuforiaAppSession.onSurfaceCreated();
    }


    // Called when the surface changed size.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height)
    {
        Log.d(LOGTAG, "GLRenderer.onSurfaceChanged");

        // Call Vuforia function to handle render surface size changes:
        vuforiaAppSession.onSurfaceChanged(width, height);
    }


    // Function for initializing the renderer.
    private void initRendering()
    {
        mRenderer = Renderer.getInstance();

        HashMap<Integer,boolean[][]> pile= new HashMap<Integer,boolean[][]>();
        for(int i =0 ; i < Const.bottomHeight; i ++)
        {
            boolean[][] level = new boolean[Const.bottomWidth][Const.bottomLength];
            if(i == 0)
            {
                level = new boolean[Const.bottomWidth][Const.bottomLength];
                for(int k = 0; k < level.length; k ++)
                    for(int h =0; h < level[0].length; h++)
                    {
                        level[k][h] = true;
                    }
            }
            if(i == 1)
            {
                level = new boolean[Const.bottomWidth][Const.bottomLength];
                for(int k = 0; k < level.length; k ++)
                    for(int h =0; h < level[0].length; h++)
                    {
                        if (k ==Const.bottomWidth/2
                                //&& h ==Const.bottomLength/2
                                )level[k][h] = false;
                        else level[k][h] = true;
                    }
            }
            pile.put(i,level);
        }
        pileObject = pileToPileObject(pile);

        GLES20.glClearColor(0.0f, 0.0f, 0.0f, Vuforia.requiresAlpha() ? 0.0f
                : 1.0f);

        for (Texture t : mTextures)
        {
            GLES20.glGenTextures(1, t.mTextureID, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, t.mTextureID[0]);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA,
                    t.mWidth, t.mHeight, 0, GLES20.GL_RGBA,
                    GLES20.GL_UNSIGNED_BYTE, t.mData);
        }

        shaderProgramID = SampleUtils.createProgramFromShaderSrc(
                CubeShaders.CUBE_MESH_VERTEX_SHADER,
                CubeShaders.CUBE_MESH_FRAGMENT_SHADER);

        vertexHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexPosition");
        normalHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexNormal");
        textureCoordHandle = GLES20.glGetAttribLocation(shaderProgramID,
                "vertexTexCoord");
        mvpMatrixHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "modelViewProjectionMatrix");
        texSampler2DHandle = GLES20.glGetUniformLocation(shaderProgramID,
                "texSampler2D");

        try
        {
            mBuildingsModel = new SampleApplication3DModel();
            mBuildingsModel.loadModel(mActivity.getResources().getAssets(),
                    "ImageTargets/Buildings.txt");
        } catch (IOException e)
        {
            Log.e(LOGTAG, "Unable to load buildings");
        }

        // Hide the Loading Dialog
        mActivity.loadingDialogHandler
                .sendEmptyMessage(LoadingDialogHandler.HIDE_LOADING_DIALOG);

    }

    private Object3D pileToPileObject(HashMap<Integer,boolean[][]> pile)
    {
        List<int[]>pileList =new ArrayList<int[]>();

        for(Integer key: pile.keySet())
        {
            boolean[][] level = pile.get(key);
            for(int i = 0; i < level.length; i ++)
                for(int j = 0; j < level[0].length; j ++)
                {
                    if(level[i][j])
                    {
                        pileList.add(new int[]{i - Const.bottomWidth/2, j - Const.bottomLength/2, key});
                    }
                }
        }

        Object3D tempObject;
        tempObject=new PileObject(0,0,0,5,pileList);

        tempObject.pileIsOrNotOccupied=pile;
        return tempObject;
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        manualSwitch = !manualSwitch;
    }
    @Override
    public void onClick(View v) {
        if(fallingObject.isFalling) {
            switch (v.getId()) {
                case R.id.up:
                    fallingObject.moveFront(pileObject);
                    break;
                case R.id.down:
                    fallingObject.moveBack(pileObject);
                    break;
                case R.id.left:
                    fallingObject.moveLeft(pileObject);
                    break;
                case R.id.right:
                    fallingObject.moveRight(pileObject);
                    break;
            }
        }

    }

    Object3D pileObject;
    Object3D fallingObject = new CurveObject(0,0,0,1);
    private boolean manualSwitch=true;
    int score = 0;
    private void renderFrame() {
        if(!fallingObject.ableToFall && !fallingObject.isFalling){
            final TextView tv = (TextView) mActivity.findViewById(R.id.indicator);
            tv.post(new Runnable() {//另外一种更简洁的发送消息给ui线程的方法。
                @Override
                public void run() {//run()方法会在ui线程执行
                    tv.setTextColor(Color.YELLOW);
                    tv.setText("Please move closer");
                }
            });
        }
        else{
            final TextView tv = (TextView) mActivity.findViewById(R.id.indicator);
            tv.post(new Runnable() {//另外一种更简洁的发送消息给ui线程的方法。
                @Override
                public void run() {//run()方法会在ui线程执行
                    tv.setText("");
                }
            });
        }
        autoRotateAngle = (autoRotateAngle + 1) % 360;

        Object3D[] randomObjectList = new Object3D[]{new CurveObject(0, 0, 0, 0), new AutoObject(0, 0, 0, 2),
                //new DoubleCurveObject(0, 0, 0, 3),
                new LongStickObject(0, 0, 0, 4), new ShortStickObject(0, 0, 0, 5)};
        if (fallingObject.isOnGround) {
            fallingObject = randomObjectList[new Random().nextInt(randomObjectList.length)];
        }
        boolean result = ((PileObject) pileObject).elimate();
        if(result){
            score += 100;
            final TextView tv = (TextView) mActivity.findViewById(R.id.score);
            tv.post(new Runnable() {//另外一种更简洁的发送消息给ui线程的方法。
                @Override
                public void run() {//run()方法会在ui线程执行
                    tv.setText("Score:" + score);
                }
            });
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        State state = mRenderer.begin();
        mRenderer.drawVideoBackground();
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // handle face culling, we need to detect if we are using reflection
        // to determine the direction of the culling
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        if (Renderer.getInstance().getVideoBackgroundConfig().getReflection() == VIDEO_BACKGROUND_REFLECTION.VIDEO_BACKGROUND_REFLECTION_ON)
            GLES20.glFrontFace(GLES20.GL_CW); // Front camera
        else
            GLES20.glFrontFace(GLES20.GL_CCW); // Back camera

        HashMap<String, Matrix44F> modelViewMap = new HashMap<String, Matrix44F>();
        getAllModelViewMap(state, modelViewMap);

        float[] projectionMatrix = vuforiaAppSession.getProjectionMatrix().getData();

        if (modelViewMap.containsKey("stones")) {
            float[] bottomModelViewMatrix = modelViewMap.get("stones").getData();
            float[] invertedBottomModelViewMatrix = new float[16];
            Matrix.invertM(invertedBottomModelViewMatrix, 0, bottomModelViewMatrix, 0);

            if (modelViewMap.containsKey("chips")) {
                float[] boardModelViewMatrix = modelViewMap.get("chips").getData();
                float[] boardToBottomModelViewMatrix = new float[16];
                Matrix.multiplyMM(boardToBottomModelViewMatrix, 0, invertedBottomModelViewMatrix, 0, boardModelViewMatrix, 0);
                float[] bottomToBoardModelViewMatrix = new float[16];
                Matrix.invertM(bottomToBoardModelViewMatrix, 0, boardToBottomModelViewMatrix, 0);

                if (!fallingObject.isFalling) {
                    double slopingAngle = getSlopingAngle(bottomModelViewMatrix, boardModelViewMatrix);
                    fallingObject.updateBottomXYZ(boardToBottomModelViewMatrix,pileObject,Object3D.NOT_FALLING);
                    if (slopingAngle > Const.fallingAngle && fallingObject.ableToFall) fallingObject.isFalling = true;
                } else {
                    //if (fallingObject.isControlled) {
                    if (!fallingObject.leaveBottom) {
                        fallingObject.leaveBottom = true;
                        Matrix.multiplyMM(fallingObject.onBoardSelfRotationMatrix, 0
                                , getRotationMatrix(bottomToBoardModelViewMatrix), 0
                                , fallingObject.onBottomSelfRotationMatrix, 0);
                        //fallingObject.updateBoardXYZ(boardToBottomModelViewMatrix);
                    }


                    float currentZ = fallingObject.bottomCenterZ;

                    float boardCenterInBottomCoordinateX=boardToBottomModelViewMatrix[12];
                    float boardCenterInBottomCoordinateY=boardToBottomModelViewMatrix[13];
                    float boardCenterInBottomCoordinateZ=boardToBottomModelViewMatrix[14];

                    if(manualSwitch) {
                        if (fallingObject.moveCount == Const.moveInterval) {
//                        if (fallingObject.boardCenterX > -60 - Const.cubeSize / 2 - Const.cubeSize * 2
//                                && fallingObject.boardCenterX < -60)
//                            fallingObject.moveLeft(pileObject);
//
//                        if (fallingObject.boardCenterX < 60 + Const.cubeSize / 2 + Const.cubeSize * 2
//                                && fallingObject.boardCenterX > 60)
//                            fallingObject.moveRight(pileObject);
//
//                        if (fallingObject.boardCenterY > -43 - Const.cubeSize / 2 - Const.cubeSize * 2
//                                && fallingObject.boardCenterY < -43)
//                            fallingObject.moveBack(pileObject);
//
//                        if (fallingObject.boardCenterY < 43 + Const.cubeSize / 2 + Const.cubeSize * 2
//                                && fallingObject.boardCenterY > 43)
//                            fallingObject.moveFront(pileObject);
//

                            if (boardCenterInBottomCoordinateX - fallingObject.lastCenterX > Const.cubeSize
                                    /4.0f*3.0f
                                    )
                                fallingObject.moveRight(pileObject);
                            if (fallingObject.lastCenterX - boardCenterInBottomCoordinateX > Const.cubeSize
                            /4.0f*3.0f
                            )
                                fallingObject.moveLeft(pileObject);
                            if (boardCenterInBottomCoordinateY - fallingObject.lastCenterY > Const.cubeSize
                                    /4.0f*3.0f
                                    )
                                fallingObject.moveFront(pileObject);
                            if (fallingObject.lastCenterY - boardCenterInBottomCoordinateY > Const.cubeSize
                                    /4.0f*3.0f
                                    )
                                fallingObject.moveBack(pileObject);

                            fallingObject.lastCenterX = boardCenterInBottomCoordinateX;
                            fallingObject.lastCenterY = boardCenterInBottomCoordinateY;
                            fallingObject.lastCenterZ = boardCenterInBottomCoordinateZ;

                        } else {
                            fallingObject.moveCount++;
                        }
                    }

                    fallingObject.updateBottomXYZ(boardToBottomModelViewMatrix,pileObject,Object3D.FALLING);

                    if (fallingObject.fallCount == Const.fallingInterval) {
                        if (fallingObject.detectCollision(pileObject) && !fallingObject.isOnGround) {
                            fallingObject.isOnGround = true;
                            ((PileObject) pileObject).mergeAnObject(fallingObject);
                        } else {
                            fallingObject.bottomCenterZ =currentZ - Const.cubeSize;
                            fallingObject.fallCount = 0;
                        }
                    } else
                    {
                        fallingObject.bottomCenterZ=currentZ;
                        fallingObject.fallCount++;
                    }

                    fallingObject.updateBoardXYZ(boardToBottomModelViewMatrix);
                }

                if(!fallingObject.isOnGround)
                    render3DObject(boardModelViewMatrix, projectionMatrix, fallingObject, ON_BOARD);

            } else {
                if (fallingObject.isFalling) {

                    fallingObject.leaveBottom = false;

                    if (fallingObject.fallCount == Const.fallingInterval) {
                        if (fallingObject.detectCollision(pileObject) && !fallingObject.isOnGround) {
                            fallingObject.isOnGround = true;
                            ((PileObject) pileObject).mergeAnObject(fallingObject);
                        } else {
                            fallingObject.bottomCenterZ -= Const.cubeSize;
                            fallingObject.fallCount = 0;
                        }
                    } else fallingObject.fallCount++;

                    if (!fallingObject.isOnGround)
                        render3DObject(bottomModelViewMatrix, projectionMatrix, fallingObject, ON_BOTTOM);
                }
            }

            render3DObject(bottomModelViewMatrix, projectionMatrix, pileObject, ON_BOTTOM_GRID);
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mRenderer.end();
    }

    private boolean isInPileBorder(Object3D obj)
    {
        for(float[] oneOffset : obj.bottomOffsetList) {

            int indexX = (int)(obj.bottomCenterX/Const.cubeSize)+Const.bottomWidth/2;
            int indexY = (int)(obj.bottomCenterY/Const.cubeSize)+Const.bottomLength/2;
            int indexZ = (int)(obj.bottomCenterZ/Const.cubeSize);

            if (indexX<0 || indexX>=Const.bottomWidth
                    || indexY<0 || indexY>=Const.bottomLength
                    || indexZ<0 || indexZ>=Const.bottomHeight)
                return false;

            if(pileObject.pileIsOrNotOccupied.get(indexZ)[indexX][indexY])
                return false;
        }
        return true;
    }

    private void multiply(float [][]tempMatrix, float[][]a, int r1,int c1,float[][]b,int r2,int c2)
    {
        for (int i=0;i<r1;i++)
            for (int j=0 ;j<c2;j++) {
                tempMatrix[i][j] = 0;
                for (int k = 0; k < c1; k++) {
                    tempMatrix[i][j] +=a[i][k]*b[k][j];
                }
            }
    }



    private double getSlopingAngle(float[] bottomModelViewMatrix, float[] boardModelViewMatrix) {
        double[] bottomAngleList = getPlaneAngle(bottomModelViewMatrix);
        double[] boardAngleList = getPlaneAngle(boardModelViewMatrix);


        double boardBottomAngle = Math.acos(bottomAngleList[0] * boardAngleList[0] +
                bottomAngleList[1] * boardAngleList[1] +
                bottomAngleList[2] * boardAngleList[2]);

        double cosBoardBottomAngle = bottomAngleList[0] * boardAngleList[0] +
                bottomAngleList[1] * boardAngleList[1] +
                bottomAngleList[2] * boardAngleList[2];
        if (cosBoardBottomAngle > 1 || cosBoardBottomAngle < -1) boardBottomAngle = Math.PI;

        boardBottomAngle = boardBottomAngle / Math.PI * 180;
        return boardBottomAngle;
    }
    public final static int ON_BOARD = 1;
    public final static int ON_BOTTOM = 2;
    public final static int ON_BOTTOM_GRID = 3;


    private void render3DObject(float[] modelViewMatrix, float[] projectionMatrix, Object3D obj, int mode) {
        float[] modelViewMatrixCopy = modelViewMatrix.clone();
        List<float[]> boardOrBottomOffsetList = new ArrayList<>();
        float boardOrBottomCenterX = 0.0f;
        float boardOrBottomCenterY = 0.0f;
        float boardOrBottomCenterZ = 0.0f;

        float[] boardOrBottomSelfRotationMatrix = new float[16];

        switch (mode){
            case ON_BOARD:
                boardOrBottomOffsetList = new ArrayList<>(obj.boardOffsetList);
                boardOrBottomCenterX = obj.boardCenterX;
                boardOrBottomCenterY = obj.boardCenterY;
                boardOrBottomCenterZ = obj.boardCenterZ;
                boardOrBottomSelfRotationMatrix= obj.onBoardSelfRotationMatrix.clone();
                //boardOrBottomSelfRotationMatrix = getGridRotationMatrix(obj.onBoardSelfRotationMatrix).clone();
                break;
            case ON_BOTTOM:
                boardOrBottomOffsetList = new ArrayList<>(obj.boardOffsetList);
                boardOrBottomCenterX = obj.bottomCenterX;
                boardOrBottomCenterY = obj.bottomCenterY;
                boardOrBottomCenterZ = obj.bottomCenterZ;
                boardOrBottomSelfRotationMatrix = obj.onBottomSelfRotationMatrix.clone();
                //boardOrBottomSelfRotationMatrix = getGridRotationMatrix(obj.onBottomSelfRotationMatrix).clone();
                break;
            case ON_BOTTOM_GRID:
                break;

        }
        if(mode == ON_BOTTOM_GRID)
            for (int[] offset :obj.offsetList){
                int color;


                if(offset[2]>11)
                {
                    color=offset[2]-6*2;
                }
                else if(offset[2]>5)
                {
                    color=offset[2]-6;
                }
                else
                {
                    color=offset[2];
                }


                float[] newModelViewMatrixCopy  = new float[16];
                Matrix.multiplyMM(newModelViewMatrixCopy, 0,
                        modelViewMatrix,0,
                        getTranslationMatrix((offset[0]) * Const.cubeSize, (offset[1]) * Const.cubeSize,
                        (offset[2]) * Const.cubeSize), 0);

                float[] modelViewProjectionMatrix = new float[16];
                Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, newModelViewMatrixCopy, 0);
                GLES20.glUseProgram(shaderProgramID);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                        false, 0, obj.cube.getVertices());
                GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                        false, 0, obj.cube.getNormals());
                GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                        GLES20.GL_FLOAT, false, 0, obj.cube.getTexCoords());
                GLES20.glEnableVertexAttribArray(vertexHandle);
                GLES20.glEnableVertexAttribArray(normalHandle);
                GLES20.glEnableVertexAttribArray(textureCoordHandle);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                        mTextures.get(color).mTextureID[0]);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                        modelViewProjectionMatrix, 0);
                GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                        obj.cube.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                        obj.cube.getIndices());
                GLES20.glDisableVertexAttribArray(vertexHandle);
                GLES20.glDisableVertexAttribArray(normalHandle);
                GLES20.glDisableVertexAttribArray(textureCoordHandle);
                SampleUtils.checkGLError("Render Frame");
            }
        else{
            for (float[] offset :boardOrBottomOffsetList){

                float[] tempMatrix2 = new float[16];
                Matrix.multiplyMM(tempMatrix2, 0, boardOrBottomSelfRotationMatrix, 0, getTranslationMatrix(offset[0]*Const.cubeSize,offset[1]*Const.cubeSize,offset[2]*Const.cubeSize), 0);

                float[] tempMatrix3 = new float[16];
                //if(offset.equals(boardOrBottomOffsetList.get(0))) tempMatrix2=boardOrBottomSelfRotationMatrix.clone();
                Matrix.multiplyMM(tempMatrix3,0, getTranslationMatrix(boardOrBottomCenterX
                        ,boardOrBottomCenterY,boardOrBottomCenterZ),0
                        ,tempMatrix2,0);

                float[] newModelViewMatrix = new float[16];
                Matrix.multiplyMM(newModelViewMatrix,
                        0, modelViewMatrix, 0, tempMatrix3, 0);

                //Matrix.multiplyMM(tempMatrix, 0, getAutoRotationMatrix(autoRotateAngle), 0, newModelViewMatrix.clone(), 0);

                float[] modelViewProjectionMatrix = new float[16];
                Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, newModelViewMatrix, 0);

                GLES20.glUseProgram(shaderProgramID);
                GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                        false, 0, obj.cube.getVertices());
                GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                        false, 0, obj.cube.getNormals());
                GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                        GLES20.GL_FLOAT, false, 0, obj.cube.getTexCoords());
                GLES20.glEnableVertexAttribArray(vertexHandle);
                GLES20.glEnableVertexAttribArray(normalHandle);
                GLES20.glEnableVertexAttribArray(textureCoordHandle);
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                        mTextures.get(obj.textureId).mTextureID[0]);
                GLES20.glUniform1i(texSampler2DHandle, 0);
                GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                        modelViewProjectionMatrix, 0);
                GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                        obj.cube.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                        obj.cube.getIndices());
                GLES20.glDisableVertexAttribArray(vertexHandle);
                GLES20.glDisableVertexAttribArray(normalHandle);
                GLES20.glDisableVertexAttribArray(textureCoordHandle);
                SampleUtils.checkGLError("Render Frame");
            }
        }
    }


    public void renderObjects(Object3D obj, float[] modelViewProjectionMatrix)
    {
        if (!mActivity.isExtendedTrackingActive()) {
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                    false, 0, obj.cube.getVertices());
            GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                    false, 0, obj.cube.getNormals());
            GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                    GLES20.GL_FLOAT, false, 0, obj.cube.getTexCoords());

            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(normalHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);

            // activate texture 0, bind it, and pass to shader
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                    mTextures.get(obj.textureId).mTextureID[0]);
            GLES20.glUniform1i(texSampler2DHandle, 0);

            // pass the model view matrix to the shader
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                    modelViewProjectionMatrix, 0);

            // finally draw the teapot
            GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                    obj.cube.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                    obj.cube.getIndices());

            // disable the enabled arrays
            GLES20.glDisableVertexAttribArray(vertexHandle);
            GLES20.glDisableVertexAttribArray(normalHandle);
            GLES20.glDisableVertexAttribArray(textureCoordHandle);
        } else {
            GLES20.glDisable(GLES20.GL_CULL_FACE);
            GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                    false, 0, mBuildingsModel.getVertices());
            GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                    false, 0, mBuildingsModel.getNormals());
            GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                    GLES20.GL_FLOAT, false, 0, mBuildingsModel.getTexCoords());

            GLES20.glEnableVertexAttribArray(vertexHandle);
            GLES20.glEnableVertexAttribArray(normalHandle);
            GLES20.glEnableVertexAttribArray(textureCoordHandle);

            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                    mTextures.get(3).mTextureID[0]);
            GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                    modelViewProjectionMatrix, 0);
            GLES20.glUniform1i(texSampler2DHandle, 0);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
                    mBuildingsModel.getNumObjectVertex());

            SampleUtils.checkGLError("Renderer DrawBuildings");
        }
    }
    //
    private void getNewSelfRotationMatrix(float[] modelViewMatrix, Object3D obj) {
        float[] temp=new float[16];
        float[] result_temp=new float[16];
        Matrix.setIdentityM(temp,0);

        temp[0]=modelViewMatrix[0];
        temp[1]=modelViewMatrix[1];
        temp[2]=modelViewMatrix[2];
        temp[4]=modelViewMatrix[4];
        temp[5]=modelViewMatrix[5];
        temp[6]=modelViewMatrix[6];
        temp[8]=modelViewMatrix[8];
        temp[9]=modelViewMatrix[9];
        temp[10]=modelViewMatrix[10];

        Matrix.multiplyMM(result_temp, 0, temp, 0, obj.selfRotationMatrix, 0);
        obj.selfRotationMatrix = result_temp;
    }

    private void getAllModelViewMap(State state, HashMap<String, Matrix44F> modelViewMap) {
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            printUserData(trackable);
            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(result.getPose());
            modelViewMap.put(trackable.getName(),modelViewMatrix_Vuforia);
        }
    }

    private void printUserData(Trackable trackable)
    {
        String userData = (String) trackable.getUserData();
//        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }


    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;

    }



    void renderTeapot(float[] modelViewProjection, int textureIndex)
    {
        Teapot mTeapot = new Teapot();
        // activate the shader program and bind the vertex/normal/tex coords
        GLES20.glUseProgram(shaderProgramID);

        GLES20.glVertexAttribPointer(vertexHandle, 3, GLES20.GL_FLOAT,
                false, 0, mTeapot.getVertices());
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT,
                false, 0, mTeapot.getNormals());
        GLES20.glVertexAttribPointer(textureCoordHandle, 2,
                GLES20.GL_FLOAT, false, 0, mTeapot.getTexCoords());

        GLES20.glEnableVertexAttribArray(vertexHandle);
        GLES20.glEnableVertexAttribArray(normalHandle);
        GLES20.glEnableVertexAttribArray(textureCoordHandle);

        // activate texture 0, bind it, and pass to shader
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,
                mTextures.get(textureIndex).mTextureID[0]);
        GLES20.glUniform1i(texSampler2DHandle, 0);

        // pass the model view matrix to the shader
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false,
                modelViewProjection, 0);

        // finally draw the teapot
        GLES20.glDrawElements(GLES20.GL_TRIANGLES,
                mTeapot.getNumObjectIndex(), GLES20.GL_UNSIGNED_SHORT,
                mTeapot.getIndices());

        // disable the enabled arrays
        GLES20.glDisableVertexAttribArray(vertexHandle);
        GLES20.glDisableVertexAttribArray(normalHandle);
        GLES20.glDisableVertexAttribArray(textureCoordHandle);
//        Matrix.invertM()
    }




    private double[] Mat2Quat(float[][] R) {

        double[] Quat = new double[4];
        double[] trace = new double[4];

        trace[0] = 1.0f + R[0][0] + R[1][1] + R[2][2];
        trace[1] = 1.0f + R[0][0] - R[1][1] - R[2][2];
        trace[2] = 1.0f - R[0][0] + R[1][1] - R[2][2];
        trace[3] = 1.0f - R[0][0] - R[1][1] + R[2][2];

        int j = 0;
        for (int i = 1; i < 4; i++) {
            if (trace[i] > trace[j]) j = i;
        }

        if (j == 0) {
            Quat[0] = trace[0];
            Quat[1] = R[1][2] - R[2][1];
            Quat[2] = R[2][0] - R[0][2];
            Quat[3] = R[0][1] - R[1][0];
        } else if (j == 1) {
            Quat[0] = R[1][2] - R[2][1];
            Quat[1] = trace[1];
            Quat[2] = R[0][1] + R[1][0];
            Quat[3] = R[2][0] + R[0][2];
        } else if (j == 2) {
            Quat[0] = R[2][0] - R[0][2];
            Quat[1] = R[0][1] + R[1][0];
            Quat[2] = trace[2];
            Quat[3] = R[1][2] + R[2][1];
        } else //j==3
        {
            Quat[0] = R[0][1] - R[1][0];
            Quat[1] = R[2][0] + R[0][2];
            Quat[2] = R[1][2] + R[1][0];
            Quat[3] = trace[3];
        }

        double sum = Math.sqrt(0.25 / trace[j]);
        Quat[0] *= sum;
        Quat[1] *= sum;
        Quat[2] *= sum;
        Quat[3] *= sum;

        return Quat;
    }

    double[] getPlaneAngle(float[] modelViewMatrix)
    {
        double[] angle_degree=new double[3];

        float[][] mat=new float[3][3];
        for(int index_row=0;index_row<=2;index_row++)
            for(int index_col=0;index_col<=2;index_col++)
            {
                mat[index_row][index_col]=modelViewMatrix[index_col*4+index_row];
            }
        //旋转矩阵求四元数
        double[] Quat=new double[4];
        Quat = Mat2Quat(mat);

        double q0 = Quat[0];
        double q1=Quat[1];
        double q2=Quat[2];
        double q3=Quat[3];
        double fi,theta,thi;
        //四元数算和平面夹角
        fi=Math.atan2(2 * (q0 * q1 + q2 * q3), 1 - 2 * (q1 * q1 + q2 * q2));
        theta=Math.asin(2 * (q0 * q2 - q3 * q1));
        thi=Math.atan2(2 * (q0 * q3 + q1 * q2), 1 - 2 * (q2 * q2 + q3 * q3));

        double fi_temp=fi/Math.PI*180;
        double theta_temp=theta/Math.PI*180;

        if(fi<0) fi+=Math.PI;
        else fi-=Math.PI;

        double cos_angle;

        if((1-Math.cos(Math.PI/2-fi)*Math.cos(Math.PI/2-fi)-Math.cos(Math.PI/2-theta)*Math.cos(Math.PI/2-theta))<0)
            cos_angle=0.0f;
        else
            cos_angle=Math.sqrt(1 - Math.cos(Math.PI / 2 - fi) * Math.cos(Math.PI / 2 - fi) - Math.cos(Math.PI / 2 - theta) * Math.cos(Math.PI / 2 - theta));

        angle_degree[0]=Math.cos(Math.PI / 2 - fi);
        angle_degree[1]=Math.cos(Math.PI / 2 - theta);
        angle_degree[2]=cos_angle;

        fi=fi/Math.PI*180;
        theta=theta/Math.PI*180;

        return angle_degree;
    }

    private float[] getTranslationMatrix(float X,float Y, float Z)
    {
        float[] result=new float[16];
        Matrix.setIdentityM(result,0);
        result[12]=X;
        result[13]=Y;
        result[14]=Z;

        return result;
    }

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

    float[] getAutoRotationMatrix(float angle)
    {
        float[] autoRotationMatrix=new float[16];
        Matrix.setIdentityM(autoRotationMatrix,0);

        autoRotationMatrix[0]=(float)Math.cos(angle / 180 * Math.PI);
        autoRotationMatrix[1]=(float)Math.sin(angle / 180 * Math.PI);
        autoRotationMatrix[4]=-(float)Math.sin(angle/180*Math.PI);
        autoRotationMatrix[5]=(float)Math.cos(angle/180*Math.PI);

        return autoRotationMatrix;
    }
}
