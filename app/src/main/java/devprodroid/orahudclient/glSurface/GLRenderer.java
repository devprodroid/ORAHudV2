package devprodroid.orahudclient.glSurface;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import devprodroid.bluetooth.DataModel;

public class GLRenderer implements GLSurfaceView.Renderer {
    private HorizonRight mHorizonRight;
    private HorizonLeft mHorizonLeft;


    private UpArrow mUpArrow;
    private DownArrow mDownArrow;
    private Attitude mAttitude;
    private AttitudeCenter mAttitudeCenter;
    private Compass mCompass;

    private Battery mBattery;

    private Line vertLine1;
    private Line vertLine2;
    private Line vertLine3;
    private Line vertLine4;



    private static final String TAG = "MyGLRenderer";

    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    private final float[] mRotationMatrixCompass = new float[16];
    private final float[] mTranslationMatrix = new float[16];
    private final float[] mBatteryScaleMatrix = new float[16];


    private float mAngle;
    private float mPitch;
    private Integer mBatteryLevel;
    private DataModel mDataModel;
    private long lastFrameTime = 0;
    private static Context mContext;
    private boolean UpActive;
    private boolean DownActive;


    // Our UV texture buffer.
    private FloatBuffer mTextureBuffer;

    private FPSCounter fps;

    public GLRenderer(Context context, DataModel dataModel) {
        mDataModel = dataModel;
        fps = new FPSCounter();
        this.mContext = context;

    }

    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        mAttitude = new Attitude(); //attitude
        mAttitudeCenter = new AttitudeCenter(); //attitudeCenter Arrow

        mCompass = new Compass();   //Compass Image

        mHorizonRight = new HorizonRight(); //Artificial horizon
        mHorizonLeft = new HorizonLeft(); //Artificial horizon

        mUpArrow = new UpArrow();
        mDownArrow = new DownArrow();
        mBattery = new Battery(); //batterystatus

        //-5 deg
                                   // x y z x y z x y z
        vertLine1 = new Line();
        vertLine1.SetVerts(0.5f, -0.25f, 0.0f, -0.5f, -0.25f, 0f);
        vertLine1.SetColor(.0f, .8f, 0f, 1.0f);

        //+5 deg

        vertLine2 = new Line();
        vertLine2.SetVerts(0.5f, 0.25f, 0.0f, -0.5f, 0.25f, 0f);
        vertLine2.SetColor(.0f, .8f, 0f, 1.0f);

        //-10deg
                                   // x y z x y z x y z
        vertLine3 = new Line();
        vertLine3.SetVerts(0.5f, -0.5f, 0.0f, -0.5f, -0.5f, 0f);
        vertLine3.SetColor(.0f, .8f, 0f, 1.0f);

        //+10 deg
                                  // x y z x y z x y z
        vertLine4 = new Line();
        vertLine4.SetVerts(0.5f, 0.5f, 0.0f, -0.5f, 0.5f, 0f);
        vertLine4.SetColor(.0f, .8f, 0f, 1.0f);

    }

    public void onDrawFrame(GL10 unused) {

        // drawHorizonFrame();
        float[] scratch = new float[16];
        float[] transMatrix = new float[16];
        float[] batteryScaleMatrix = new float[16];

        //fps.logFrame();


        //Get Values from DataModel

        // getModelValues();
        float mAngleNew = mDataModel.getRoll().floatValue();
        float mPitchNew = (float) Math.sin(mDataModel.getPitch().doubleValue() * Math.PI / 90.0);
        float mHeading = mDataModel.getYaw().floatValue();

        int mBatteryLevelNew = mDataModel.getBatteryLevel();
      //  Log.d("Pitch", Double.toString(mDataModel.getPitch().doubleValue()));

        UpActive = false;
        DownActive = false;
        //>10 down , <10 up
        if (mDataModel.getAccZ() >= 50) {
            UpActive = false;
            DownActive = true;
        } else if (mDataModel.getAccZ() <= -50) {
            UpActive = true;
            DownActive = false;
        }


        //interpolation for added smoothness

        mAngle = mAngle - ((mAngle - mAngleNew) / 2);
        mPitch = mPitch - ((mPitch - mPitchNew) / 2);

       // Log.d("Pitch", Float.toString(mPitch));
        mBatteryLevel = mBatteryLevelNew;



        // Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);



        Matrix.setRotateM(mRotationMatrixCompass, 0, mAngle, 0, 0, 1.0f);

        //Set Translation for attitude Marker
        Matrix.setIdentityM(transMatrix, 0);
        Matrix.translateM(transMatrix, 0, 0f, mPitch, 0);


        //Set Rotation for attitude Marker
        Matrix.rotateM(transMatrix, 0, mAngle, 0, 0, 1.0f);

        //Matrix.multiplyMM(transMatrix, 0, transMatrix, 0, mRotationMatrix, 0);

        // Combine the rotation matrix with the projection and camera view
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, transMatrix, 0);

        //Draw attitude lines
        mAttitude.draw(scratch);
        mAttitudeCenter.draw(scratch);








        //Draw fixed HorizonRight
        mHorizonRight.draw();
        mHorizonLeft.draw();

        mUpArrow.draw(UpActive);
        mDownArrow.draw(DownActive);

        //Scale battery bar
        Matrix.scaleM(mBatteryScaleMatrix, 0, 0f, 0f, 0f);
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, transMatrix, 0);

        //Draw Battery Bar, color change is done in the Battery Class
        mBattery.draw(mBatteryLevel);

        vertLine1.draw(mMVPMatrix);
        vertLine2.draw(mMVPMatrix);
        vertLine3.draw(mMVPMatrix);
        vertLine4.draw(mMVPMatrix);


        Matrix.setRotateM(mRotationMatrixCompass, 0, mHeading, 0, 0, 1.0f);
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrixCompass, 0);
        mCompass.draw(scratch);


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

    public static int loadShader(int type, String shaderCode) {

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
     * <p/>
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

class FPSCounter {
    long startTime = System.nanoTime();
    int frames = 0;

    public void logFrame() {
        frames++;
        if (System.nanoTime() - startTime >= 1000000000) {
            Log.d("FPSCounter", "fps: " + frames);
            frames = 0;
            startTime = System.nanoTime();
        }
    }
}