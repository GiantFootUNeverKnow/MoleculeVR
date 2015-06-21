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
import android.provider.MediaStore;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

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

    //private static final float Z_NEAR = 0.1f;
    //private static final float Z_FAR = 20.0f;
    //If you added molecules or deleted molecules, please change this variable
    private static final int NUM_MOLECULE = 4;

    private static final int COORDS_PER_VERTEX = 3;

    //JIGGLING_FREQUENCY = x means that for x frames generated, 1 jiggling occurs
    private static final int JIGGLING_FREQUENCY = 5;

    // We keep the light always position just above the user.
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] { 0.0f, 2.0f, 0.0f, 1.0f };
    private final float[] lightPosInEyeSpace = new float[4];

    //molecule objects and bonding objects
    Drawable molecules[] = new Drawable[4];
    Drawable bondings[] = new Drawable[4];

    private FloatBuffer[] IsoSVertices;
    private FloatBuffer[] IsoSColor;

    float[][] vIsosurface;
    float[][] cIsosurface;

    private int[] mIsoProgram;
    private int[] mIsoPositionParam;
    private int[] mIsoColorParam;
    //private int mIsoNormalParam;
    private int[] mIsoModelParam;
    private int[] mIsoModelViewParam;

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
    private int[] nTriangleInIso;
    private int switchSignalCounter;
    private int jigglingCounter;

    private int debugging;
    private String debuggingStr;


    private MediaPlayer mPlayer;
    private int[] bgmResID;

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
        mModelBonding = new float[16];
        mModelIsosurface = new float[16];

        mHeadView = new float[16];
        mHeadUpVector = new float[3];
        mCamera = new float[16];
        mView = new float[16];
        mModelView = new float[16];
      //  mModelViewProjection = new float[16];

        mIsoProgram = new int[NUM_MOLECULE];
        mIsoPositionParam = new int[NUM_MOLECULE];
        mIsoColorParam = new int[NUM_MOLECULE];
        mIsoModelParam = new int[NUM_MOLECULE];
        mIsoModelViewParam = new int[NUM_MOLECULE];

        IsoSVertices = new FloatBuffer[NUM_MOLECULE];
        IsoSColor = new FloatBuffer[NUM_MOLECULE];

        vIsosurface = new float[NUM_MOLECULE][];
        cIsosurface = new float[NUM_MOLECULE][];
        nTriangleInIso = new int[NUM_MOLECULE];

        mOverlay = (CardboardOverlayView) findViewById(R.id.overlay);
        mOverlay.show3DToast("Succeeded in creating this!");

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        debugging = 0;
        switchSignalCounter = 0;
        jigglingCounter = 0;

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

        //constructing molecule objects
        for (int i = 0; i < NUM_MOLECULE; i++) {
            try {
                parser.parse(readers[i][0],readers[i][1]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            float[] vMolecule = parser.outputVertices();
            float[] vBondings = parser.outputBonds();
            float[] cMolecule = parser.outputColors();
            float[] cBondings = parser.outputBondingColors();
            int nAtoms = parser.outputNumOfAtoms();
            int nBonds = parser.outputNumOfBonds();

            float[] nMolecule = parser.outputNormals();
            float[] nBondings = parser.outputBondingNormals();
            molecules[i] = new Drawable(vMolecule, cMolecule, nMolecule, vertexShader, fragShader, nAtoms, "Molecule "+ i);
            bondings[i] = new Drawable(vBondings,cBondings, nBondings, vertexShader, fragShader, nBonds, "Bonding " + i);
        }

        //one real density file
        String resourceName = "density2";
        int iD = getResources().getIdentifier(resourceName, "raw", getPackageName());
        InputStream inputStream = getResources().openRawResource(iD);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        float [][][]l_values = parser.loadDensity(reader);

        //2 Isosurface examples

       // float [][][]s_values = s_functions.s();
        //float [][][]n_values = s_functions.n();

        //Isosurface I6 = new Isosurface(s_values,0.15f,-0.7f,0.7f,-0.7f,0.7f,-0.3f,0.3f);
        //Isosurface I7 = new Isosurface(n_values,0.03f);
        Isosurface I8 = new Isosurface(l_values,8960.0f);
        Isosurface I9 = new Isosurface(l_values,4888.0f);

        /*
        vIsosurface = I6.vertices;
        cIsosurface = I6.colors;
        nTriangleInIso = I6.nTriang;x
        */

        /*
        vIsosurface = I7.vertices;
        cIsosurface = I7.colors;
        nTriangleInIso = I7.nTriang;
        */

        vIsosurface[0] = I8.vertices;
        cIsosurface[0] = I8.colors;
        nTriangleInIso[0] = I8.nTriang;
        
        vIsosurface[1] = I9.vertices;
        cIsosurface[1] = I9.colors;
        nTriangleInIso[1] = I9.nTriang;

        for (int i = 0; i < 2; i++) {

            ByteBuffer ByteVertices = ByteBuffer.allocateDirect(vIsosurface[i].length * 4);
            ByteVertices.order(ByteOrder.nativeOrder());
            IsoSVertices[i] = ByteVertices.asFloatBuffer();
            IsoSVertices[i].put(vIsosurface[i]);
            IsoSVertices[i].position(0);

            ByteBuffer ByteColors = ByteBuffer.allocateDirect(cIsosurface[i].length * 4);
            ByteColors.order(ByteOrder.nativeOrder());
            IsoSColor[i] = ByteColors.asFloatBuffer();
            IsoSColor[i].put(cIsosurface[i]);
            IsoSColor[i].position(0);

            mIsoProgram[i] = GLES20.glCreateProgram();

            GLES20.glAttachShader(mIsoProgram[i], passthroughShader);
            GLES20.glAttachShader(mIsoProgram[i], fragShader);
            GLES20.glLinkProgram(mIsoProgram[i]);
            GLES20.glUseProgram(mIsoProgram[i]);

            mIsoPositionParam[i] = GLES20.glGetAttribLocation(mIsoProgram[i], "a_Position");
            mIsoColorParam[i] = GLES20.glGetAttribLocation(mIsoProgram[i], "a_Color");

            mIsoModelParam[i] = GLES20.glGetUniformLocation(mIsoProgram[i], "u_Model");
            mIsoModelViewParam[i] = GLES20.glGetUniformLocation(mIsoProgram[i], "u_MVMatrix");

            GLES20.glEnableVertexAttribArray(mIsoPositionParam[i]);
            GLES20.glEnableVertexAttribArray(mIsoColorParam[i]);
        }
        //end of Isosurface specs

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
        Log.i(TAG,"onRenderShutdown");
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

        /*Draw Molecules*/
        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
         //    float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelMolecule, 0);
      //  Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
        molecules[idx].draw(lightPosInEyeSpace, mModelMolecule, mModelView, Sphere.NUMBER_OF_VERTICES);

        /*Draw Bondings*/
        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelBonding, 0);
        //Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
        bondings[idx].draw(lightPosInEyeSpace,mModelBonding,mModelView,Cylinder.NUMBER_OF_VERTICES);

         /*Draw Isosurface*/
        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        //float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelIsosurface, 0);
        //Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
        //drawIsosurface(0);
        //drawIsosurface(1);
        // Draw rest of the scene.


    }

    //adjust vertices to fit oscillation, which randomly move to any direction in scale +-0.01
    /*
    private void adjustVertices(int index){

        moleculeVertices[index].clear();//clear out vertices in this buffer
        float [] temp = vMolecule[index].clone();//make a local copy of the molecules
        float SCALE = 0.01f;

        for (int i = 0; i < nAtoms[index]; i++){
            //generate a random vector of size 3 in scale of +-0.01 for each atom
            float incre_x = (float)(Math.random() - 0.5f) * SCALE;
            float incre_y = (float)(Math.random() - 0.5f) * SCALE;
            float incre_z = (float)(Math.random() - 0.5f) * SCALE;
                        //add the increment_vector to each vertices of the atom
                        for (int j = 0; j < Sphere.NUMBER_OF_VERTICES; j++){
                            //access the x-axis coordinate of the j-th vertex on the i-th atom, that for y-axis and that for z-axis following
                            int x_index = i * Sphere.NUMBER_OF_COORDS + COORDS_PER_VERTEX * j + 0;
                            int y_index = x_index + 1;
                            int z_index = x_index + 2;
                            temp[x_index] += incre_x;
                            temp[y_index] += incre_y;
                            temp[z_index] += incre_z;
                        }
        }
        moleculeVertices[index].put(temp);
        moleculeVertices[index].position(0);

    }*/

    //the function to draw the i-th molecule
    /*
    public void drawMolecule(int index){
        GLES20.glUseProgram(mMoleculeProgram[index]);

        GLES20.glUniform3fv(mMoleculeLightPosParam, 1, lightPosInEyeSpace, 0);

        GLES20.glUniformMatrix4fv(mMoleculeModelParam[index],1,false,mModelMolecule,0);//set the model in the shader
        GLES20.glUniformMatrix4fv(mMoleculeModelViewParam[index], 1, false, mModelView, 0);//set the modelView in the shader
        //GLES20.glUniformMatrix4fv(mMoleculeModelViewProjectionParam[index], 1, false, mModelViewProjection,0);  // Set the ModelViewProjection matrix in the shader.

        //adjust the positions to form random oscillation
        if (jigglingCounter == 0)
            adjustVertices(index);
        jigglingCounter = (jigglingCounter ++) % JIGGLING_FREQUENCY;

        // Set the normal positions of atoms, again for shading
        GLES20.glVertexAttribPointer(mMoleculeNormalParam[index], 3, GLES20.GL_FLOAT, false, 0, mMoleculeNormals[index]);
        GLES20.glVertexAttribPointer(mMoleculePositionParam[index], COORDS_PER_VERTEX, GLES20.GL_FLOAT,false,0,moleculeVertices[index]);
        GLES20.glVertexAttribPointer(mMoleculeColorParam[index],4, GLES20.GL_FLOAT, false,0,mMoleculeColor[index]);

        for  (int i = 0; i < nAtoms[index]; i++){
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES,i*Sphere.NUMBER_OF_VERTICES, Sphere.NUMBER_OF_VERTICES);
        }

        checkGLError("Drawing Molecule");
    }*/

    //the function to draw bondings of the i-th molecule
    /*
    public void drawBondings(int index){
        GLES20.glUseProgram(mBondingProgram[index]);

        GLES20.glUniform3fv(mMoleculeLightPosParam, 1, lightPosInEyeSpace, 0);

        GLES20.glUniformMatrix4fv(mBondingModelParam[index],1,false,mModelBonding,0);//set the model in the shader
        GLES20.glUniformMatrix4fv(mBondingModelViewParam[index], 1, false, mModelView, 0);//set the modelView in the shader
        //GLES20.glUniformMatrix4fv(mBondingModelViewProjectionParam[index],1,false, mModelViewProjection,0);  // Set the ModelViewProjection matrix in the shader.

        GLES20.glVertexAttribPointer(mBondingNormalParam[index],3,GLES20.GL_FLOAT,false,0,mBondingNormals[index]);
        GLES20.glVertexAttribPointer(mBondingPositionParam[index], COORDS_PER_VERTEX, GLES20.GL_FLOAT,false,0,bondingVertices[index]);
        GLES20.glVertexAttribPointer(mBondingColorParam[index],4, GLES20.GL_FLOAT, false,0,mBondingColor[index]);

        for  (int i = 0; i < nBonds[index]; i++){
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES,i*Cylinder.NUMBER_OF_VERTICES, Cylinder.NUMBER_OF_VERTICES);
        }

        checkGLError("Drawing Bondings");
    }*/

    //the function to draw the isosurface
    public void drawIsosurface(int index){
        GLES20.glUseProgram(mIsoProgram[index]);

        GLES20.glUniformMatrix4fv(mIsoModelParam[index],1,false,mModelIsosurface,0);//set the model in the shader
        GLES20.glUniformMatrix4fv(mIsoModelViewParam[index], 1, false, mModelView, 0);//set the modelView in the shader

        GLES20.glVertexAttribPointer(mIsoPositionParam[index],COORDS_PER_VERTEX,GLES20.GL_FLOAT,false,0,IsoSVertices[index]);
        GLES20.glVertexAttribPointer(mIsoColorParam[index],4,GLES20.GL_FLOAT,false,0,IsoSColor[index]);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,nTriangleInIso[index]);

        checkGLError("Drawing Molecule");
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
        switch (idx){
            case 0:
                mOverlay.show3DToast("Switched to the first molecule" );
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
        }
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
