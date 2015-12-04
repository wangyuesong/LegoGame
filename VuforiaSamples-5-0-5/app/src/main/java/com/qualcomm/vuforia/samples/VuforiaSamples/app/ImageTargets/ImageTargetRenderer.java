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
import java.util.Vector;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

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

// The renderer class for the ImageTargets sample.
public class ImageTargetRenderer implements GLSurfaceView.Renderer
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

    float currentZ = 0.0f;

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

        pile= new HashMap<Integer,boolean[][]>();
        for(int i =0 ; i < 10; i ++)
        {
            boolean[][] level = new boolean[13][9];
            if(i == 0)
            {
                level = new boolean[13][9];
                for(int k = 0; k < level.length; k ++)
                    for(int h =0; h < level[0].length; h++)
                    {
                        level[k][h] = true;
                    }
            }
            if(i == 1)
            {
                level = new boolean[13][9];
                for(int k = 0; k < level.length; k ++)
                    for(int h =0; h < level[0].length; h++)
                    {
                        if (k ==6 && h ==4 )level[k][h] = false;
                        else level[k][h] = true;
                    }
            }
            pile.put(i,level);
        }
        Object3D pileObject = pileToPileObject(pile);
        //add pileObject and 3 unique objects to the objectList
        objectList.add(pileObject);
        objectList.add(new ShortStickObject(0,0,6,1));
        objectList.add(new LongStickObject(4,4,6,1));
        objectList.add(new CurveObject(-4,4,6,1));

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
        for(Integer key: pile.keySet())
        {
            boolean[][] level = pile.get(key);
            for(int i = 0; i < level.length; i ++)
                for(int j = 0; j < level[0].length; j ++)
                {
                    if(level[i][j])
                    {
                        pileList.add(new int[]{i - 6, j - 4, key});
                    }
                }
        }

        Object3D pileObject = new PileObject(0,0,0,5,pileList);
        return pileObject;
    }

    boolean hasSticked=false;
    int thresholdDistance = 100;
    int thresholdInterval = 50;
    int count =0;
    int up;
    private HashMap<Integer,boolean[][]> pile;

    List<int[]>pileList =new ArrayList<int[]>();
    List<Object3D> objectList = new ArrayList<Object3D>();

    int addObject =0;
    int random =0;
    int randomfalldown =0;

    Object3D fallingObject = new CurveObject(0,0,0,1);

    private void renderFrame()
    {
        addObject++;
        CameraCalibration camCal = CameraDevice.getInstance()
                .getCameraCalibration();
        //Get intrinsic parameters
        Vec2F f = camCal.getFocalLength();
        Vec2F center = camCal.getPrincipalPoint();
        Vec2F size = camCal.getSize();
        float fx=f.getData()[0];
        float fy=f.getData()[1];
        float cx=center.getData()[0];
        float cy=center.getData()[1];
        float width=size.getData()[0];
        float height=size.getData()[1];
        int[] countlevel = new int[10];

        count ++;
        if (count == 10) {
            count = 0;
            for (int i = 1; i < objectList.size(); i++) {
                if (objectList.get(i).detectCollision(objectList.get(0))) {
                    ((PileObject) objectList.get(0)).mergeAnObject(objectList.get(i));
                    objectList.remove(i);
                    i --;
                }else {
                    objectList.get(i).down(objectList.get(0));
                }
            }
        }

        ((PileObject) objectList.get(0)).elimate();
        if(addObject == 100) {
            addObject = 0;
            randomfalldown = (int) (Math.floor(Math.random() * 4) +0);
            switch (randomfalldown) {
                case 0:
                    objectList.add(new LongStickObject((int) (Math.floor(Math.random() * 13) - 6), (int) (Math.floor(Math.random() * 9) - 4), 6, 0));
                    break;
                case 1:
                    objectList.add(new ShortStickObject((int) (Math.floor(Math.random() * 13) - 6), (int) (Math.floor(Math.random() * 9) - 4), 6, 1));
                    break;
                case 2:
                    objectList.add(new CurveObject((int) (Math.floor(Math.random() * 13) - 6), (int) (Math.floor(Math.random() * 9) - 4), 6, 2));
                    break;
                case 3:
                    objectList.add(new AutoObject((int) (Math.floor(Math.random() * 13) - 6), (int) (Math.floor(Math.random() * 9) - 4), 6, 3));
                    break;
                case 4:
                    objectList.add(new DoubleCurveObject((int) (Math.floor(Math.random() * 13) - 6), (int) (Math.floor(Math.random() * 9) - 4), 6, 4));
                    break;
            }
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

        HashMap<String,Matrix44F> modelViewMap = new HashMap<String,Matrix44F>();
        getAllModelViewMap(state, modelViewMap);

        float[] projectionMatrix = vuforiaAppSession.getProjectionMatrix().getData();


        if(modelViewMap.containsKey("chips") && modelViewMap.containsKey("stones")) {
            float[] bottomModelViewMatrix = modelViewMap.get("stones").getData();
            float[] boardModelViewMatrix = modelViewMap.get("chips").getData();
            float[] invertedBottomModelViewMatrix = new float[16];
            Matrix.invertM(invertedBottomModelViewMatrix, 0, bottomModelViewMatrix, 0);

            float[] boardToBottomModelViewMatrix = new float[16];
            Matrix.multiplyMM(boardToBottomModelViewMatrix, 0, invertedBottomModelViewMatrix, 0, boardModelViewMatrix, 0);

            float[] bottomToBoardModelViewMatrix = new float[16];
            Matrix.invertM(bottomToBoardModelViewMatrix, 0, boardToBottomModelViewMatrix, 0);


            double slopingAngle = getSlopingAngle(bottomModelViewMatrix, boardModelViewMatrix);


           //fallingObject.updateBottomOffset(boardToBottomModelViewMatrix);

            if (!fallingObject.isInBoardCoordinate) {
                fallingObject.isInBoardCoordinate = true;
                float[] tempMatrix = new float[16];

                Matrix.multiplyMM(tempMatrix, 0
                        , getRotationMatrix(bottomToBoardModelViewMatrix), 0
                        , fallingObject.onBottomSelfRotationMatrix, 0);
//                fallingObject.updateBoardOffset(getRotationMatrix(bottomToBoardModelViewMatrix));
                fallingObject.onBoardSelfRotationMatrix = tempMatrix.clone();
            }
            //Render objects on board coordinate when the object is NOT falling
            if (!fallingObject.isFalling) {
                float[] tempModelViewMatrix = boardModelViewMatrix.clone();
                Matrix.setIdentityM(fallingObject.onBoardSelfRotationMatrix, 0);
                if (slopingAngle > 90) {
                    //Fall from board to bottom, change coordinate system
                    fallingObject.updateBottomXYZ(boardToBottomModelViewMatrix);
                    fallingObject.isFalling = true;
                }
                else {
                    render3DObject(tempModelViewMatrix, projectionMatrix, fallingObject, ON_BOARD);
                    Log.i(LOGTAG, "Board XYZ:" + fallingObject.boardCenterX + ", " + fallingObject.boardCenterY + ", " + fallingObject.boardCenterZ);
                    Log.i(LOGTAG, "Bottom XYZ:" + fallingObject.bottomCenterX + ", " + fallingObject.bottomCenterY + ", " + fallingObject.bottomCenterZ);

                }
            }


            //Render objects on board coordinate when the object is falling
            else {
                float boardCenterInBottomCoordinateX = boardToBottomModelViewMatrix[12];
                float boardCenterInBottomCoordinateY = boardToBottomModelViewMatrix[13];
                float boardCenterInBottomCoordinateZ = boardToBottomModelViewMatrix[14];

                double distance = Math.sqrt((fallingObject.bottomCenterX - boardCenterInBottomCoordinateX) * (fallingObject.bottomCenterX - boardCenterInBottomCoordinateX) +
                        (fallingObject.bottomCenterY - boardCenterInBottomCoordinateY) * (fallingObject.bottomCenterY - boardCenterInBottomCoordinateY) +
                        (fallingObject.bottomCenterZ - boardCenterInBottomCoordinateZ) * (fallingObject.bottomCenterZ - boardCenterInBottomCoordinateZ));

                if (distance < 250 && fallingObject.isMoved == false) {
                    fallingObject.updateBoardXYZ(boardToBottomModelViewMatrix);
                    fallingObject.moveCount = 0;
                    fallingObject.isMoved = true;
                }

                //When the object is being pushed
                if (distance < 250 && fallingObject.isMoved && fallingObject.moveCount < 50) {
                    fallingObject.moveCount++;
                    float currentZ = fallingObject.bottomCenterZ;
                    fallingObject.updateBottomXYZ(boardToBottomModelViewMatrix);
//                    if(onGround) fallingObject.Z_Bottom=Z_Ground;
                    if (currentZ - 1 > 0)
                        fallingObject.bottomCenterZ = currentZ - 1;

                    fallingObject.updateBoardXYZ(boardToBottomModelViewMatrix);
                }
                //Not being pushed
                else {
                    fallingObject.isMoved = false;
                    fallingObject.moveCount = 50;

                    if (fallingObject.bottomCenterZ - 1 > 0)
                        fallingObject.bottomCenterZ -= 1;

                    fallingObject.updateBoardXYZ(boardToBottomModelViewMatrix);
                    fallingObject.updateBottomXYZ(boardToBottomModelViewMatrix);
                }

                render3DObject(boardModelViewMatrix.clone(), projectionMatrix, fallingObject, ON_BOARD);
                Log.i(LOGTAG, "Board XYZ:" + fallingObject.boardCenterX + ", " + fallingObject.boardCenterY + ", " + fallingObject.boardCenterZ);
                Log.i(LOGTAG, "Bottom XYZ:" + fallingObject.bottomCenterX + ", " + fallingObject.bottomCenterY + ", " + fallingObject.bottomCenterZ);


            }

            Matrix.multiplyMM(fallingObject.onBottomSelfRotationMatrix, 0
                    , getRotationMatrix(boardToBottomModelViewMatrix), 0
                    , fallingObject.onBoardSelfRotationMatrix, 0);
            //fallingObject.updateBottomOffset(getRotationMatrix(boardToBottomModelViewMatrix));
        }
//
        else if (fallingObject.isFalling && modelViewMap.containsKey("stones")) {
            float[] bottomModelViewMatrix = modelViewMap.get("stones").getData();

            fallingObject.isMoved = false;
            fallingObject.moveCount=50;
            fallingObject.isInBoardCoordinate=false;

            float[] modelViewProjection = new float[16];
            float[] Projectionmatrix = vuforiaAppSession.getProjectionMatrix().getData();


            if (fallingObject.bottomCenterZ - 1 > 0)
                fallingObject.bottomCenterZ -= 1;

            render3DObject(bottomModelViewMatrix,projectionMatrix,fallingObject,ON_BOTTOM);
            Log.i(LOGTAG, "Board XYZ:" + fallingObject.boardCenterX + ", " + fallingObject.boardCenterY + ", " + fallingObject.boardCenterZ);
            Log.i(LOGTAG, "Bottom XYZ:" + fallingObject.bottomCenterX + ", " + fallingObject.bottomCenterY + ", " + fallingObject.bottomCenterZ);


        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mRenderer.end();
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
                boardOrBottomSelfRotationMatrix = getGridRotationMatrix(obj.onBoardSelfRotationMatrix).clone();
                break;
            case ON_BOTTOM:
                boardOrBottomOffsetList = new ArrayList<>(obj.bottomOffsetList);
                boardOrBottomCenterX = obj.bottomCenterX;
                boardOrBottomCenterY = obj.bottomCenterY;
                boardOrBottomCenterZ = obj.bottomCenterZ;
                boardOrBottomSelfRotationMatrix = obj.onBottomSelfRotationMatrix.clone();
                boardOrBottomSelfRotationMatrix = getGridRotationMatrix(obj.onBottomSelfRotationMatrix).clone();
                break;
            case ON_BOTTOM_GRID:
                break;

        }
        if(mode == ON_BOTTOM_GRID)
            for (int[] offset :obj.offsetList){
                Matrix.translateM(modelViewMatrixCopy, 0, (obj.centerX + offset[0]) * 20, (obj.centerY + offset[1]) * 20,
                        (obj.centerZ + offset[2]) * 20);
                Matrix.scaleM(modelViewMatrixCopy, 0, OBJECT_SCALE_FLOAT,
                        OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);

                float[] modelViewProjectionMatrix = new float[16];
                Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrixCopy, 0);
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
        else{
            for (float[] offset :boardOrBottomOffsetList){

                float[] tempMatrix2 = new float[16];
                Matrix.multiplyMM(tempMatrix2, 0, boardOrBottomSelfRotationMatrix, 0, getTranslationMatrix(offset[0]*20,offset[1]*20,offset[2]*20), 0);

                float[] tempMatrix3 = new float[16];
                //if(offset.equals(boardOrBottomOffsetList.get(0))) tempMatrix2=boardOrBottomSelfRotationMatrix.clone();
                Matrix.multiplyMM(tempMatrix3,0, getTranslationMatrix(boardOrBottomCenterX
                        ,boardOrBottomCenterY,boardOrBottomCenterZ),0
                        ,tempMatrix2,0);

                float[] newModelViewMatrix = new float[16];
                Matrix.multiplyMM(newModelViewMatrix,
                        0, modelViewMatrix, 0, tempMatrix3, 0);

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

    float[] getGridRotationMatrix(float[] rotationMatrix)
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

        Log.i(LOGTAG,gridRotationMatrix[0]+" "+gridRotationMatrix[1]+" "+gridRotationMatrix[2]
                +" "+gridRotationMatrix[4]+" "+gridRotationMatrix[5]+" "+gridRotationMatrix[6]
                +" "+gridRotationMatrix[8]+" "+gridRotationMatrix[9]+" "+gridRotationMatrix[10]);

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

    float[] getGridAxisVector(float x,float y,float z,boolean[] axisIsTaken)
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
            if(z*1.0/length>max) {destAxisVector[0]=0;destAxisVector[1]=0;destAxisVector[244]=1;max=z;}
            if(-z*1.0/length>max) {destAxisVector[0]=0;destAxisVector[1]=0;destAxisVector[2]=-1;max=-z;}
        }

        return destAxisVector;
    }
}