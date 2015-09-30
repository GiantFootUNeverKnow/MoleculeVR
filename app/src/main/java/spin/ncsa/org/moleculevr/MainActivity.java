package spin.ncsa.org.moleculevr;
/*
    Created by Xusheng on 04/14/2015
 */
import android.media.MediaPlayer;
import android.os.Bundle;

import com.google.vrtoolkit.cardboard.CardboardActivity;
import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Vibrator;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.lang.Math;

import javax.microedition.khronos.egl.EGLConfig;

public class MainActivity extends CardboardActivity implements CardboardView.StereoRenderer{

    /**
            * Sets the view to our CardboardView and initializes the transformation matrices we will use
    * to render our scene.
            * @param savedInstanceState
    */
    private static final String TAG = "MainActivity_";
    private Vibrator vibrator;
    private CardboardOverlayView mOverlay;

    //If you added molecules or deleted molecules, please change this variable
    private static final int NUM_MOLECULE = 3;

    // We keep the light always position just above the user.
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] { 0.0f, 2.0f, 0.0f, 1.0f };

    //names of molecule
    private String titles[] = {null,"Polyvinyl alcohol+boric acid", "magnesium oxide "};

    private final float[] lightPosInEyeSpace = new float[4];

    //molecule objects and bonding objects
    Drawable molecules[] = new Drawable[NUM_MOLECULE];
    Drawable bondings[] = new Drawable[NUM_MOLECULE];
    Drawable isosurface[][] = new Drawable[NUM_MOLECULE][2];

    private int[][] nTriangleInIso;

    private float DistanceToScreen = 0f;

    private float[] mModelMolecule;
    private float[] mModelBonding;
    private float[] mModelIsosurface;

    private float[] mHeadView;
    private float[] mHeadUpVector;
    private float[] mCamera;
    private float[] mView;
    private float[] mModelView;
    //private float[] mModelViewProjection;

    private int idx;
    private int switchSignalCounter;

    private int debugging;
    private String debuggingStr;

    private MediaPlayer mPlayer;
    private int[] bgmResID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);
        setCardboardView(cardboardView);

        Log.i(TAG,"onCreate");

        mModelMolecule = new float[16];
        mModelBonding = new float[16];
        mModelIsosurface = new float[16];

        mHeadView = new float[16];
        mHeadUpVector = new float[3];
        mCamera = new float[16];
        mView = new float[16];
        mModelView = new float[16];
      //  mModelViewProjection = new float[16];

        nTriangleInIso = new int[NUM_MOLECULE][2];

        mOverlay = (CardboardOverlayView) findViewById(R.id.overlay);
        mOverlay.show3DToast("Succeeded in creating this!");
        mOverlay.show3DToast("Xusheng Zhang, SPIN@NCSA \n https://github.com/GiantFootUNeverKnow/MoleculeVR");

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        debugging = 0;
        switchSignalCounter = 0;

        mPlayer = null;
        bgmResID = new int[NUM_MOLECULE];
    }


    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "Surface Created");

        //set up background color
        //GLES20.glClearColor(0.4f, 0.4f, 0.8f, 0.5f);

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.vertex);
        int fragShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.frag);
        int passthroughShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.passthrough);

        //set the index of molecule to be drawn to zero
        idx = 0;

        //init the textparser parser
        TextParser parser = new TextParser();

        //load the predefined colors for each element from file R.raw.xxx
        InputStream iS = getResources().openRawResource(R.raw.cpk_coloring);
        BufferedReader readColor = new BufferedReader(new InputStreamReader(iS));
        parser.loadColor(readColor);

        //load table of atom mass from file R.raw.atommass
        iS = getResources().openRawResource(R.raw.atommass);
        BufferedReader readAtomMass = new BufferedReader(new InputStreamReader(iS));
        parser.loadAtomMass(readAtomMass);

        //get the resources(vertices of molecules from files R.raw.xxx!
        InputStream[] inputStreams = new InputStream[NUM_MOLECULE];
        BufferedReader[][] readers = new BufferedReader[NUM_MOLECULE][];

        for (int i = 0; i <NUM_MOLECULE; i++){
            readers[i] = new BufferedReader[2];

            String resourceName = "molecule" + i ;
            //open the resource by its resourceIdetifier which could be gained through the resourceName
            int iD = getResources().getIdentifier(resourceName, "raw", getPackageName());
            inputStreams[i] = getResources().openRawResource(iD);
            readers[i][0] = new BufferedReader(new InputStreamReader(inputStreams[i]));

            resourceName = "bonding" + i ;
            //open the resource by its resourceIdetifier which could be gained through the resourceName
            iD = getResources().getIdentifier(resourceName, "raw", getPackageName());
            inputStreams[i] = getResources().openRawResource(iD);
            readers[i][1] = new BufferedReader(new InputStreamReader(inputStreams[i]));

            resourceName = "bgm" + i;
            bgmResID[i] = getResources().getIdentifier(resourceName, "raw", getPackageName());
        }

        //Enable depth test
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Enable blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //Enable both faces cull
        //GLES20.glCullFace(GLES20.GL_FRONT_AND_BACK);
        //GLES20.glEnable(GLES20.GL_CULL_FACE);

        //constructing molecule objects, bonding objects and isosurface objects if available
        for (int i = 0; i < NUM_MOLECULE; i++) {
            //molecule[i].txt and bonding[i].txt are read ib
            try {
                parser.parse(readers[i][0],readers[i][1]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            //obtained meaningful resultes from TextParser
            float[] vMolecule = parser.outputVertices();
            float[] vBondings = parser.outputBonds();
            float[] cMolecule = parser.outputColors();
            float[] cBondings = parser.outputBondingColors();
            int nAtoms = parser.outputNumOfAtoms();
            int nBonds = parser.outputNumOfBonds();

            float[] nMolecule = parser.outputNormals();
            float[] nBondings = parser.outputBondingNormals();

            //constructed molecules and bonding objects using data obtained from TextParser
            molecules[i] = new Drawable(vMolecule, cMolecule, nMolecule, vertexShader, fragShader, nAtoms, "Molecule "+ i);
            bondings[i] = new Drawable(vBondings,cBondings, nBondings, vertexShader, fragShader, nBonds, "Bonding " + i);

            //Read, parse, and construct two isosurfaces if density[i].txt exists
            String resourceName = "density" + i;
            int iD = getResources().getIdentifier(resourceName, "raw", getPackageName());
            if (iD != 0) {

                InputStream inputStream = getResources().openRawResource(iD);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                float[][][] l_values = parser.loadDensity(reader);

                Isosurface I8 = new Isosurface(l_values, parser.primaryLevel, -0.8f,0.8f,-0.8f,0.8f,-0.8f,0.8f);
                Isosurface I9 = new Isosurface(l_values, parser.secondaryLevel, -0.8f,0.8f,-0.8f,0.8f,-0.8f,0.8f);

                float vIsosurface[][] = new float[2][];
                float cIsosurface[][] = new float[2][];
                //int nTriangleInIso[] = new int[2];

                vIsosurface[0] = I8.vertices;
                cIsosurface[0] = I8.colors;
                nTriangleInIso[i][0] = I8.nTriang;

                vIsosurface[1] = I9.vertices;
                cIsosurface[1] = I9.colors;
                nTriangleInIso[i][1] = I9.nTriang;

                for (int j = 0; j < 2; j++) {
                    isosurface[i][j] =
                            new Drawable(vIsosurface[j], cIsosurface[j], passthroughShader, fragShader, 1,
                                    "Isosurface " + i + "-" + j);
                }
                //end of Isosurface specs
            }
            else {
                isosurface[i][0] = null;
                isosurface[i][1] = null;
            }
        }

        // Matrix Initialization
        Matrix.setIdentityM(mModelMolecule, 0);
        Matrix.translateM(mModelMolecule, 0, 0, 0, -DistanceToScreen);

        Matrix.setIdentityM(mModelBonding, 0);
        Matrix.translateM(mModelBonding, 0, 0, 0, -DistanceToScreen);

        Matrix.setIdentityM(mModelIsosurface, 0);
        Matrix.translateM(mModelIsosurface,0,0,0,-DistanceToScreen);

        checkGLError("onSurfaceCreated");


        //play sound
        mPlayer = MediaPlayer.create(this,bgmResID[0]);
        mPlayer.start();
        mPlayer.setLooping(true);

        showTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mPlayer != null)
            mPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayer != null)
            mPlayer.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mPlayer != null)
            mPlayer.release();
    }

    @Override
    public void onRendererShutdown(){ //temporarily useless
        Log.i(TAG, "onRenderShutdown");
    }


    @Override
    public void onSurfaceChanged(int width, int height){ //temporarily useless
    }

    @Override
    public void onFinishFrame(Viewport viewport){ //temporarily useless
    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        //Enable depth test
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(mView, 0, eye.getEyeView(), 0, mCamera, 0);
        /*eye.getEyeView() and mHeadView(the variable obtained by calling HeadTransform.getHeadView()) are almost the same
        * on the documentation; it is said that getEyeView() includes also position shift and interpupillary distance shift,
        * which I didn't find noticeable trace of effect
        * */
        //Matrix.multiplyMM(mView, 0, mHeadView, 0, mCamera, 0);

        // Set the position of the light
        Matrix.multiplyMV(lightPosInEyeSpace, 0, mView, 0, LIGHT_POS_IN_WORLD_SPACE, 0);

        drawMolecule(idx);

        drawBonding(idx);

        drawIsosurface(idx,0);
        drawIsosurface(idx,1);

        // Draw rest of the scene.
    }

    /*Draw Molecules*/
    public void drawMolecule(int index){
        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        //    float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelMolecule, 0);
        //  Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
        molecules[index].draw(lightPosInEyeSpace, mModelMolecule, mModelView, Sphere.NUMBER_OF_VERTICES, true);
    }

    /*Draw Bondings*/
    public void drawBonding(int index){
        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelBonding, 0);
        //Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
        bondings[index].draw(lightPosInEyeSpace, mModelBonding, mModelView, Cylinder.NUMBER_OF_VERTICES,false);
    }

    /*Draw Isosurface*/
    public void drawIsosurface(int index, int subindex){
        //if density exists
        if (isosurface[index][subindex] != null) {
            // Build the ModelView and ModelViewProjection matrices
            // for calculating cube position and light.
            //float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
            Matrix.multiplyMM(mModelView, 0, mView, 0, mModelIsosurface, 0);
            //Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
            isosurface[index][subindex].draw(null, mModelIsosurface, mModelView, nTriangleInIso[index][subindex], false);
        }
    }

    @Override
    public void onCardboardTrigger() {
       /* if (isLookingAtObject()) {
            mScore++;
            mOverlayView.show3DToast("Found it! Look around for another one.\nScore = " + mScore);
            ...
        } else {
            mOverlayView.show3DToast("Look around to find the object!");
        }*/
        // Always give user feedback
        vibrator.vibrate(50);
        idx = (idx + 1) % NUM_MOLECULE;
        switchBGM(idx);
        /*
        switch (idx){
            case 0:
                mOverlay.show3DToast(titles );
                break;
            case 1:
                mOverlay.show3DToast("Switched to the second molecule" );
                break;
            case 2:
                mOverlay.show3DToast("Switched to the third molecule" );
                break;
            default:
                mOverlay.show3DToast("Switched to the "+(idx+1) +"-th molecule" );
                break;
        }*/

        if (titles[idx] != null)
            mOverlay.show3DToast(titles[idx]);
        else
            mOverlay.show3DToast("unknown molecule");
    }

    private void MediaStopPlaying(){
        if (mPlayer != null){
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void switchBGM(int index){
        Log.i(TAG," " + index);
        MediaStopPlaying();
        mPlayer = MediaPlayer.create(getApplicationContext(),bgmResID[index]);
        if (mPlayer != null){
            mPlayer.start();
            mPlayer.setLooping(true);
        }
    }

    //a method, calling UIthread to display on screen the title of molecule
    private void showTitle(){
        MainActivity.this.runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        if (titles[idx] != null)
                            mOverlay.show3DToast(titles[idx]);
                        else
                            mOverlay.show3DToast("unknown molecule");
                    }
                }
        );
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {

        Matrix.setLookAtM(mCamera,0,0.0f,0.0f,0.01f,0.0f,0.0f,0.0f,0.0f,1.0f,0.0f);//Viewing

        headTransform.getHeadView(mHeadView,0);

        headTransform.getUpVector(mHeadUpVector,0);

        /*When the phone is landscape, the second coordinate for upVector is near either 1 or -1
          When the phone is portrait, the first cordinate for upVector is near either 1 or -1
          When the phone is sitting on table, the third coordinate for upVector is near either 1 or -1
        * */
        switchSignalCounter++;
        if (switchSignalCounter == 40) {
            if ((Math.abs(mHeadUpVector[0])  > 0.85)
                    && (Math.abs(mHeadUpVector[1])  < 0.5)
                    && (Math.abs(mHeadUpVector[2])  < 0.5)
                    ) {
                vibrator.vibrate(50);
                idx = (idx + 1) % NUM_MOLECULE;
                switchBGM(idx);
                //show title for idx-th molecule
                showTitle();
            }
            switchSignalCounter = 0;
        }

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
