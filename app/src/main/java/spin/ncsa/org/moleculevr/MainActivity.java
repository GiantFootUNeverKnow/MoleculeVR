package spin.ncsa.org.moleculevr;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;

public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer{

    /**
            * Sets the view to our CardboardView and initializes the transformation matrices we will use
    * to render our scene.
            * @param savedInstanceState
    */
    private static final String TAG = "MainActivity_";
    private CardboardOverlayView mOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setContentView(R.layout.activity_main);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        Log.i(TAG,"onCreate");

        //mOverlay = (CardboardOverlayView) findViewById(R.id.overlay);
        //mOverlay.show3DToast("Succeeded in creating this!");
    }
    @Override
    public void onRendererShutdown(){
     //   Log.i(TAG,"onRenderShutdown");
    }
    @Override
    public void onSurfaceChanged(int width, int height){
       // Log.i(TAG,"onSurfaceChanged");

    }

    @Override
    public void onFinishFrame(Viewport viewport){

    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        /*GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(mView, 0, eye.getEyeView(), 0, mCamera, 0);

        // Set the position of the light
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mView, 0, LIGHT_POS_IN_WORLD_SPACE, 0);

        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelCube, 0);
        Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
        drawCube();

        // Draw rest of the scene.
        ...*/
    }

    /**
     * Increment the score, hide the object, and give feedback if the user pulls the magnet while
     * looking at the object. Otherwise, remind the user what to do.
     */
    @Override
    public void onCardboardTrigger() {
       /* if (isLookingAtObject()) {
            mScore++;
            mOverlayView.show3DToast("Found it! Look around for another one.\nScore = " + mScore);
            ...
        } else {
            mOverlayView.show3DToast("Look around to find the object!");
        }
        // Always give user feedback
        mVibrator.vibrate(50);*/
    }

    @Override
    public void onSurfaceCreated(EGLConfig config) {

    }


    /**
     * Prepares OpenGL ES before we draw a frame.
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
        /*...
        headTransform.getHeadView(mHeadView, 0);
        ...*/
    }


}
