package devprodroid.orahudclient.glSurface;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import devprodroid.bluetooth.DataModel;

public class GLRenderer implements GLSurfaceView.Renderer {
    private Horizon   mHorizon;

    private Attitude mAttitude;

    private Battery mBattery;
    private static final String TAG = "MyGLRenderer";

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mTranslationMatrix = new float[16];
    private final float[] mBatteryScaleMatrix = new float[16];


    private float mAngle;
    private float mPitch;
    private Integer mBatteryLevel;
    private DataModel mDataModel;

    public GLRenderer(Context context, DataModel dataModel) {
        mDataModel=dataModel;

    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);




        mAttitude   = new Attitude(); //attitude

        mHorizon = new Horizon(); //Atificial horizon
        mBattery = new Battery(); //batterystatus
    }

    public void onDrawFrame(GL10 unused) {

        // drawHorizonFrame();
        float[] scratch = new float[16];
        float[] transMatrix = new float[16];
        float[] batteryScaleMatrix = new float[16];

        //Get Values from DataModel

       // getModelValues();
        mAngle= mDataModel.getRoll().floatValue();
        mPitch =(float)Math.sin(mDataModel.getPitch().doubleValue()* Math.PI / 180.0);
        mBatteryLevel =mDataModel.getBatteryLevel();


        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        //Set Rotation for attitude Marker
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, 1.0f);

        //Set Translation for attidude Marker
        Matrix.setIdentityM(transMatrix, 0);
        Matrix.translateM(transMatrix, 0, 0f, mPitch, 0);
        Matrix.multiplyMM(transMatrix, 0, mRotationMatrix, 0, transMatrix, 0);
//
//
//        // Combine the rotation matrix with the projection and camera view
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, transMatrix, 0);
////
        mAttitude.draw(scratch);
//
          mHorizon.draw();
//
//
//
        Matrix.scaleM(mBatteryScaleMatrix, 0, 0f, 0f, 0f);
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, transMatrix, 0);
//
//        //Scale battery bar
        mBattery.draw(mBatteryLevel);

    }

    private void drawHorizonFrame(){

        //  mHorizon.draw();


    }


    public void onSurfaceChanged(GL10 unused, int width, int height) {
        // Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }

    public void onPause() {
    }

    public void onResume() {
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Returns the rotation angle of the triangle shape (mTriangle).
     *
     * @return - A float representing the rotation angle.
     */
    public float getAngle() {
        return mAngle;
    }

    /**
     * Sets the rotation angle of the triangle shape (mTriangle).
     */
    public void setAngle(float angle) {
        mAngle = angle;
    }

}