/*===============================================================================
Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of QUALCOMM Incorporated, registered in the United States 
and other countries. Trademarks of QUALCOMM Incorporated are used with permission.
===============================================================================*/

package com.qualcomm.vuforia.samples.VuforiaSamples.app.ImageTargets;

import java.io.IOException;
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
import com.qualcomm.vuforia.Vec3F;
import com.qualcomm.vuforia.Vuforia;
import com.qualcomm.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.qualcomm.vuforia.samples.SampleApplication.utils.CubeShaders;
import com.qualcomm.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleApplication3DModel;
import com.qualcomm.vuforia.samples.SampleApplication.utils.SampleUtils;
import com.qualcomm.vuforia.samples.SampleApplication.utils.Teapot;
import com.qualcomm.vuforia.samples.SampleApplication.utils.Texture;
import com.qualcomm.QCAR.*;
import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.Vec3F;

// The renderer class for the ImageTargets sample. 
public class ImageTargetRenderer implements GLSurfaceView.Renderer
{
    boolean[] isSticked=new boolean[4];
    boolean hasSticked=false;
    boolean[] lock=new boolean[4];
    int[] interval=new int[4];


    float[] X=new float[4];
    float[] Y=new float[4];
    float[] Z=new float[4];

    float X_touch=0.0f;
    float Y_touch=0.0f;
    boolean touched=false;

    Vector<float[]> SelfRotMat=new Vector<>();

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

    private Teapot mTeapot;

    private float kBuildingScale = 12.0f;
    private SampleApplication3DModel mBuildingsModel;

    private Renderer mRenderer;

    boolean mIsActive = false;

    private static final float OBJECT_SCALE_FLOAT = 10.0f;

    private static final float CUBE_SIDE = 50.0f;

    private static final double PI = 3.1415926f;


    public ImageTargetRenderer(ImageTargets activity,
                               SampleApplicationSession session)
    {
        mActivity = activity;
        vuforiaAppSession = session;

        SelfRotMat.setSize(4);
        for(int i=0;i<=3;i++)
        {
            //initialize teapot positions
            X[i] = -120.0f+(80.0f*i);
            Y[i] = 86.5f;
            Z[i] = OBJECT_SCALE_FLOAT;

            //initialize self-rotation matrix
            float[] Identity=new float[16];
            Matrix.setIdentityM(Identity,0);
            SelfRotMat.add(i, Identity);

            //initialize stick or not
            isSticked[i]=false;
            interval[i]=50;

            //initialize locks
            lock[i]=false;
        }

    }


    public void getTouchPoint(float x,float y,boolean touched)
    {
        this.touched=touched;

        if(touched) {
            X_touch = x;
            Y_touch = y;
        }
        else
        {
            X_touch = x;
            //0.0f;
            Y_touch = y;
            //0.0f;
        }

        //Log.i(LOGTAG,X_touch+" "+Y_touch);
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
        mTeapot = new Teapot();

        mRenderer = Renderer.getInstance();

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


    // The render function.
    private void renderFrame()
    {
        //vuforiaAppSession.storeScreenDimensions();

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

        //float nearPlane = 2.0f;
        //float farPlane = 2000.0f;
        //CameraCalibration cameraCalibration=;

        // The following code reproduces the projectionMatrix above using the camera parameters
        //Vec2F size = cameraCalibration.getSize();
        //Vec2F focalLength = cameraCalibration.getFocalLength();
        //Vec2F principalPoint = cameraCalibration.getPrincipalPoint();

        // did we find any trackables this frame?
        for (int tIdx = 0; tIdx < state.getNumTrackableResults(); tIdx++)
        {
            TrackableResult result = state.getTrackableResult(tIdx);
            Trackable trackable = result.getTrackable();
            printUserData(trackable);
            Matrix44F modelViewMatrix_Vuforia = Tool
                    .convertPose2GLMatrix(result.getPose());

            int textureIndex = trackable.getName().equalsIgnoreCase("stones") ? 0
                    : 1;
            textureIndex = trackable.getName().equalsIgnoreCase("tarmac") ? 2
                    : textureIndex;

            //Log.i(LOGTAG, X_touch+" "+Y_touch);

            for(int i=0;i<=3;i++) {

                float[] modelViewMatrix = modelViewMatrix_Vuforia.getData();
                // deal with the modelview and projection matrices
                float[] modelViewProjection = new float[16];


                float[] Projectionmatrix = vuforiaAppSession.getProjectionMatrix().getData();

                CameraCalibration camCal = CameraDevice.getInstance()
                        .getCameraCalibration();

                Vec2F f = camCal.getFocalLength();
                Vec2F center = camCal.getPrincipalPoint();
                Vec2F size = camCal.getSize();

                float fx=f.getData()[0];
                float fy=f.getData()[1];
                float cx=center.getData()[0];
                float cy=center.getData()[1];
                float width=size.getData()[0];
                float height=size.getData()[1];

                float X_camera,Y_camera,Z_camera;
                double distance;

                if(isSticked[i]==true)
                {

                    float X_temp,Y_temp,Z_temp=OBJECT_SCALE_FLOAT;
                    X_camera=X[i];Y_camera=Y[i];

                    float X_=X_camera-modelViewMatrix[12]-modelViewMatrix[8]*Z_temp;
                    float Y_=Y_camera-modelViewMatrix[13]-modelViewMatrix[9]*Z_temp;

                    X_temp= (X_*modelViewMatrix[5]-Y_*modelViewMatrix[4])/(modelViewMatrix[0]*modelViewMatrix[5]-modelViewMatrix[1]*modelViewMatrix[4]);
                    Y_temp= (X_*modelViewMatrix[1]-Y_*modelViewMatrix[0])/(modelViewMatrix[4]*modelViewMatrix[1]-modelViewMatrix[5]*modelViewMatrix[0]);
                    Z_camera=modelViewMatrix[2]*X_temp+modelViewMatrix[6]*Y_temp+modelViewMatrix[10]*Z_temp+modelViewMatrix[14];

                    X_temp*=OBJECT_SCALE_FLOAT;
                    Y_temp*=OBJECT_SCALE_FLOAT;

                    distance=Math.sqrt(X_camera * X_camera+ Y_camera * Y_camera + Z_camera * Z_camera);

                    if(distance>100) lock[i]=false;

                    if(interval[i]==50)
                    {
                        //after sticked up, whether less than fixed distance, to re-trigger
                        //unsticking process and drop the teapot
                        //have to exceed interval time to re-trigger

                        if(distance<= 100
                                && isNotIntersected(i, X_temp, Y_temp)
                                && lock[i]==false)
                        {
                            X[i] = X_temp;
                            Y[i] = Y_temp;
                            Z[i] = Z_temp;

                            isSticked[i]=false;
                            hasSticked=false;
                            interval[i]=0;

                            float[] temp=new float[16];
                            float[] result_temp=new float[16];
                            float[] modelViewMatrix_inv=new float[16];

                            Matrix.invertM(modelViewMatrix_inv,0,modelViewMatrix,0);

                            Matrix.setIdentityM(temp,0);
                            temp[0]=modelViewMatrix_inv[0];
                            temp[1]=modelViewMatrix_inv[1];
                            temp[2]=modelViewMatrix_inv[2];

                            temp[4]=modelViewMatrix_inv[4];
                            temp[5]=modelViewMatrix_inv[5];
                            temp[6]=modelViewMatrix_inv[6];

                            temp[8]=modelViewMatrix_inv[8];
                            temp[9]=modelViewMatrix_inv[9];
                            temp[10]=modelViewMatrix_inv[10];

                            Matrix.multiplyMM(result_temp, 0, temp, 0, SelfRotMat.get(i), 0);
                            SelfRotMat.set(i, result_temp);

                            for(int m=0;m<=3;m++) lock[m]=true;
                        }
                        else Matrix.setIdentityM(modelViewMatrix, 0);
                    }
                    else
                    {
                        interval[i]++;
                        Matrix.setIdentityM(modelViewMatrix, 0);
                    }
                }

                //under initial state and after repositioned, whether less than fixed distance
                //have to exceed interval time to re-trigger the sticking process
                else
                {
                    if(hasSticked==false) {

                        X_camera = X[i] * OBJECT_SCALE_FLOAT * modelViewMatrix[0] + Y[i] * OBJECT_SCALE_FLOAT * modelViewMatrix[4]
                                + Z[i] * OBJECT_SCALE_FLOAT * modelViewMatrix[8] + modelViewMatrix[12];
                        Y_camera = X[i] * OBJECT_SCALE_FLOAT * modelViewMatrix[1] + Y[i] * OBJECT_SCALE_FLOAT * modelViewMatrix[5]
                                + Z[i] * OBJECT_SCALE_FLOAT * modelViewMatrix[9] + modelViewMatrix[13];
                        Z_camera = X[i] * OBJECT_SCALE_FLOAT * modelViewMatrix[2] + Y[i] * OBJECT_SCALE_FLOAT * modelViewMatrix[6]
                                + Z[i] * OBJECT_SCALE_FLOAT * modelViewMatrix[10] + modelViewMatrix[14];

                        distance = Math.sqrt(X_camera * X_camera + Y_camera * Y_camera + Z_camera * Z_camera);

                        if (distance > 100) lock[i] = false;

                        if (interval[i] == 50) {

                            float x, y;
                            x = fx * X_camera / Z_camera + cx;
                            y = fy * Y_camera / Z_camera + cy;


/* if(i==0)
                        {
                            Log.i(LOGTAG,x+","+y);
                            Log.i(LOGTAG,cx+","+cy);
                        }*/

                            //Log.i(LOGTAG,width+","+height);

                            if ((x > 0 && x < width - 20 && y > 0
                                    && y < height - 20) && distance <= 100
                                    //&& isNotIntersected(i, X_camera, Y_camera)
                                    && lock[i] == false) {

                                float[] temp = new float[16];
                                float[] result_temp = new float[16];

                                Matrix.setIdentityM(temp, 0);
                                temp[0] = modelViewMatrix[0];
                                temp[1] = modelViewMatrix[1];
                                temp[2] = modelViewMatrix[2];

                                temp[4] = modelViewMatrix[4];
                                temp[5] = modelViewMatrix[5];
                                temp[6] = modelViewMatrix[6];

                                temp[8] = modelViewMatrix[8];
                                temp[9] = modelViewMatrix[9];
                                temp[10] = modelViewMatrix[10];

                                Matrix.multiplyMM(result_temp, 0, temp, 0, SelfRotMat.get(i), 0);
                                SelfRotMat.set(i, result_temp);

                                Matrix.setIdentityM(modelViewMatrix, 0);
                                X[i] = X_camera;
                                Y[i] = Y_camera;
                                Z[i] = Z_camera;

                                isSticked[i] = true;
                                hasSticked = true;
                                interval[i] = 0;

                                for(int m=0;m<=3;m++) lock[m] = true;
                            }
                        } else interval[i]++;
                    }
                }

                float[][] mat=new float[3][3];
                for(int index_row=0;index_row<=2;index_row++)
                    for(int index_col=0;index_col<=2;index_col++)
                    {
                        mat[index_row][index_col]=modelViewMatrix[index_col*4+index_row];
                    }
/*
                mat[0][0] = 0.8898077712f;
                mat[0][1] = -0.0332875157f;
                mat[0][2] = -0.4551198431f;

                mat[1][0] = 0.0816309549f;
                mat[1][1] = 0.9928600026f;
                mat[1][2] = 0.0869793214f;

                mat[2][0] = 0.4489749631f;
                mat[2][1] = -0.1145467435f;
                mat[2][2] = 0.8861718378f;
*/
                double[] Quat=new double[4];
                Quat = Mat2Quat(mat);

                double q0 = Quat[0];
                double q1=Quat[1];
                double q2=Quat[2];
                double q3=Quat[3];

                Log.i(LOGTAG,q0+" "+q1+" "+q2+" "+q3);

                double fi,theta,thi;
                fi=Math.atan2(2 * (q0 * q1 + q2 * q3), 1 - 2 * (q1 * q1 + q2 * q2));
                theta=Math.asin(2 * (q0 * q2 - q3 * q1));
                thi=Math.atan2(2 * (q0 * q3 + q1 * q2), 1 - 2 * (q2 * q2 + q3 * q3));

                if(fi<0) fi+=Math.PI;
                else fi-=Math.PI;

                //double temp=Math.cos(Math.PI-fi);
                //Log.i(LOGTAG,temp+" ");
                double angle=Math.acos(Math.sqrt(1-Math.cos(Math.PI/2-fi)*Math.cos(Math.PI/2-fi)-Math.cos(Math.PI/2-theta)*Math.cos(Math.PI/2-theta)));
                Log.i(LOGTAG,angle+" ");

                fi=fi/Math.PI*180;
                theta=theta/Math.PI*180;
                thi=thi/Math.PI*180;
                angle=angle/Math.PI*180;

                Log.i(LOGTAG,angle+" ");
                Log.i(LOGTAG,fi+" "+theta+" "+thi);

                if (isSticked[i]==true || (!mActivity.isExtendedTrackingActive())) {
                    Matrix.translateM(modelViewMatrix, 0, X[i], Y[i],
                            Z[i]);
                    Matrix.scaleM(modelViewMatrix, 0, OBJECT_SCALE_FLOAT,
                            OBJECT_SCALE_FLOAT, OBJECT_SCALE_FLOAT);

                } else {
                    Matrix.rotateM(modelViewMatrix, 0, 90.0f, 1.0f, 0, 0);
                    Matrix.scaleM(modelViewMatrix, 0, kBuildingScale,
                            kBuildingScale, kBuildingScale);
                }

                float[] modelViewMatrix_=new float[16];
                Matrix.multiplyMM(modelViewMatrix_, 0, modelViewMatrix, 0, SelfRotMat.get(i), 0);
                Matrix.multiplyMM(modelViewProjection, 0, Projectionmatrix, 0, modelViewMatrix_, 0);

                // activate the shader program and bind the vertex/normal/tex coords
                GLES20.glUseProgram(shaderProgramID);

                if ( isSticked[i]==true || (!mActivity.isExtendedTrackingActive())) {
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
                            modelViewProjection, 0);
                    GLES20.glUniform1i(texSampler2DHandle, 0);
                    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0,
                            mBuildingsModel.getNumObjectVertex());

                    SampleUtils.checkGLError("Renderer DrawBuildings");
                }

                SampleUtils.checkGLError("Render Frame");
            }
        }

        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        mRenderer.end();
    }

    private void printUserData(Trackable trackable)
    {
        String userData = (String) trackable.getUserData();
        Log.d(LOGTAG, "UserData:Retreived User Data	\"" + userData + "\"");
    }


    public void setTextures(Vector<Texture> textures)
    {
        mTextures = textures;

    }

    private boolean isNotIntersected(int i,float X_temp,float Y_temp)
    {
        for(int m=0;m<=3;m++)
        {
            if(m==i) continue;
            else if(isSticked[m] == isSticked[i]) continue;
            else {
                if (isInBox(m, X_temp - CUBE_SIDE / 2, Y_temp - CUBE_SIDE / 2)
                        || isInBox(m, X_temp - CUBE_SIDE / 2, Y_temp + CUBE_SIDE / 2)
                        || isInBox(m, X_temp + CUBE_SIDE / 2, Y_temp - CUBE_SIDE / 2)
                        || isInBox(m, X_temp + CUBE_SIDE / 2, Y_temp + CUBE_SIDE / 2))
                    return false;
            }
        }

        return true;
    }

    private boolean isInBox(int i,float curX,float curY)
    {
        if(curX>X[i]-CUBE_SIDE/2 && curX<X[i]+CUBE_SIDE/2
                && curY>Y[i]-CUBE_SIDE/2 && curY<Y[i]+CUBE_SIDE/2)
            return true;
        else return false;
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

}
