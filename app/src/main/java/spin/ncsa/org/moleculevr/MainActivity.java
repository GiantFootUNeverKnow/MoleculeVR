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

    private static final int NUM_MOLECULE = 1;
    private static final int COORDS_PER_VERTEX = 3;

    private FloatBuffer[] moleculeVertices ;
    private FloatBuffer[] mMoleculeColor ;

    float[][] vMolecule;
    float[][] cMolecule;

    private int[] mMoleculeProgram;
    private int[] mMoleculePositionParam;
    private int[] mMoleculeColorParam;
    private int[] mMoleculeModelParam;
    private int[] mMoleculeModelViewParam;

    private float DistanceToScreen = (float)0.0;

    private float[] mModelMolecule;
    private float[] mHeadView;
    private float[] mCamera;
    private float[] mView;
    private float[] mModelView;

    private int idx;
    private int[] nAtoms;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setContentView(R.layout.activity_main);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        Log.i(TAG,"onCreate");

        mModelMolecule = new float[16];
        mHeadView = new float[16];
        mCamera = new float[16];
        mModelView = new float[16];
        mView = new float[16];

        mMoleculeProgram = new int[NUM_MOLECULE];
        mMoleculePositionParam = new int[NUM_MOLECULE];
        mMoleculeColorParam = new int[NUM_MOLECULE];
        mMoleculeModelParam = new int[NUM_MOLECULE];
        mMoleculeModelViewParam = new int[NUM_MOLECULE];

        moleculeVertices = new FloatBuffer[NUM_MOLECULE];
        mMoleculeColor = new FloatBuffer[NUM_MOLECULE];

        vMolecule = new float[NUM_MOLECULE][];
        cMolecule = new float[NUM_MOLECULE][];
        nAtoms = new int[NUM_MOLECULE];

        mOverlay = (CardboardOverlayView) findViewById(R.id.overlay);
        mOverlay.show3DToast("Succeeded in creating this!");
    }


    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "Surface Created");

        //set up background color
       // GLES20.glClearColor(0.4f, 0.4f, 0.8f, 0.5f);

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.thing_vertex);
        int passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.thing_frag);

        //set the index of molecule to be drawn to zero
        idx = 0;

        //init the textparser parser
        TextParser parser = new TextParser();

        //get the resources(vertices of molecules)!
        InputStream inputStream = getResources().openRawResource(R.raw.molecule);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));


        try {
            parser.parse(reader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < NUM_MOLECULE; i++) {

            vMolecule[i] = parser.outputVertices();
            cMolecule[i] = parser.outputColors();
            nAtoms[i] = parser.outputNumOfAtoms();

            //debugging
//                if (vMolecule[i] == null)
//                    Log.e(TAG,"No vertices");
            //Create ByteBuffer of vertices' position and color based on the float array created by "World"
            ByteBuffer ByteVertices = ByteBuffer.allocateDirect(vMolecule[i].length * 4);
            ByteVertices.order(ByteOrder.nativeOrder());
            moleculeVertices[i] = ByteVertices.asFloatBuffer();
            moleculeVertices[i].put(vMolecule[i]);
            moleculeVertices[i].position(0);

            ByteBuffer ByteColors = ByteBuffer.allocateDirect(cMolecule[i].length * 4);
            ByteColors.order(ByteOrder.nativeOrder());
            mMoleculeColor[i] = ByteColors.asFloatBuffer();
            mMoleculeColor[i].put(cMolecule[i]);
            mMoleculeColor[i].position(0);

            mMoleculeProgram[i] = GLES20.glCreateProgram();
            GLES20.glAttachShader(mMoleculeProgram[i], vertexShader);
            GLES20.glAttachShader(mMoleculeProgram[i], passthroughShader);
            GLES20.glLinkProgram(mMoleculeProgram[i]);
            GLES20.glUseProgram(mMoleculeProgram[i]);

            checkGLError("Create program ");
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);

            mMoleculePositionParam[i] = GLES20.glGetAttribLocation(mMoleculeProgram[i], "a_Position");
            mMoleculeColorParam[i] = GLES20.glGetAttribLocation(mMoleculeProgram[i], "a_Color");

            mMoleculeModelParam[i] = GLES20.glGetUniformLocation(mMoleculeProgram[i],"u_Model");
            mMoleculeModelViewParam[i] = GLES20.glGetUniformLocation(mMoleculeProgram[i],"u_MVMatrix");

            GLES20.glEnableVertexAttribArray(mMoleculePositionParam[i]);
            GLES20.glEnableVertexAttribArray(mMoleculeColorParam[i]);

            checkGLError("Thing Program Params");
       }

        Matrix.setIdentityM(mModelMolecule, 0);
        Matrix.translateM(mModelMolecule, 0, 0, 0, -DistanceToScreen);

        checkGLError("onSurfaceCreated");

    }



    @Override
    public void onRendererShutdown(){ //temporarily useless
        Log.i(TAG,"onRenderShutdown");
    }


    @Override
    public void onSurfaceChanged(int width, int height){ //temporarily useless
       // Log.i(TAG,"onSurfaceChanged");

    }

    @Override
    public void onFinishFrame(Viewport viewport){ //temporarily useless

        //Log.i(TAG,"onFinishFrame");
    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(mView, 0, eye.getEyeView(), 0, mCamera, 0);

        // Set the position of the light
       // Matrix.multiplyMV(mLightPosInEyeSpace, 0, mView, 0, LIGHT_POS_IN_WORLD_SPACE, 0);

        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        //float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelMolecule, 0);
        //Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
        drawMolecule(idx);

        // Draw rest of the scene.
    }

    //the function to draw the i-th molecule
    public void drawMolecule(int index){
        GLES20.glUseProgram(mMoleculeProgram[index]);

        GLES20.glUniformMatrix4fv(mMoleculeModelParam[index],1,false,mModelMolecule,0);//set the model in the shader
        GLES20.glUniformMatrix4fv(mMoleculeModelViewParam[index], 1, false, mModelView, 0);//set the modelView in the shader

        GLES20.glVertexAttribPointer(mMoleculePositionParam[index], COORDS_PER_VERTEX, GLES20.GL_FLOAT,false,0,moleculeVertices[index]);
        GLES20.glVertexAttribPointer(mMoleculeColorParam[index],4, GLES20.GL_FLOAT, false,0,mMoleculeColor[index]);

        int sizOfMol = vMolecule[index].length / COORDS_PER_VERTEX;

        for  (int i = 0; i < nAtoms[index]; i++){
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES,i*Sphere.NUMBER_OF_VERTICES, Sphere.NUMBER_OF_VERTICES);
        }
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 32 * Sphere.NUMBER_OF_VERTICES, Sphere.NUMBER_OF_VERTICES);

        checkGLError("Drawing Thing");
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


    /**
     * Prepares OpenGL ES before we draw a frame.
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {

        Matrix.setLookAtM(mCamera,0,0.0f,0.0f,0.01f,0.0f,0.0f,0.0f,0.0f,1.0f,0.0f);//Viewing

        headTransform.getHeadView(mHeadView,0);

        checkGLError("OnReadyToDraw");
    }

    //utility method
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
     *
     * @param type The type of shader we will be creating.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The shader object handler.
     */
    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }


    /**
     * Checks if we've had an error inside of OpenGL ES, and if so what that error is.
     *
     * @param label Label to report in case of error.
     */
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }


}
