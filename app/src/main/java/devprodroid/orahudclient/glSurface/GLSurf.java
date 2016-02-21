package devprodroid.orahudclient.glSurface;

import android.content.Context;
import android.opengl.GLSurfaceView;

import devprodroid.bluetooth.DataModel;

/**
 * Custom Surface view for Redering the hud
 */
public class GLSurf extends GLSurfaceView {

    private final GLRenderer mRenderer;
    private DataModel mDataModel;

    public void setDroneAngle(float droneAngle) {
        DroneAngle = droneAngle;
    }

    float DroneAngle;

    public GLSurf(Context context, DataModel dataModel) {
        super(context);
        mDataModel=dataModel;


        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new GLRenderer(context,mDataModel);
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    /**
     * Rotate the horizon according to the input angle
     * @param angle
     */
    public void rotateHorizon(float angle){
        mRenderer.setAngle(angle);
        requestRender();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mRenderer.onPause();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mRenderer.onResume();
    }
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

}