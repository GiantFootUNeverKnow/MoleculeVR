package spin.ncsa.org.moleculevr;
/*
    Created by Xusheng on 04/14/2015
 */
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

    //If you added molecules or deleted molecules, please change this variable
    private static final int NUM_MOLECULE = 5;

    private static final int COORDS_PER_VERTEX = 3;

    //JIGGLING_FREQUENCY = x means that for x frames generated, 1 jiggling occurs
    private static final int JIGGLING_FREQUENCY = 5;

    private FloatBuffer[] moleculeVertices ;
    private FloatBuffer[] mMoleculeColor ;

    private FloatBuffer[] bondingVertices;
    private FloatBuffer[] mBondingColor;

    private FloatBuffer IsoSVertices;
    private FloatBuffer IsoSColor;

    float[][] vMolecule;
    float[][] vBondings;
    float[][] cMolecule;
    float[][] cBondings;
    float[] vIsosurface;
    float[] cIsosurface;


    private int[] mMoleculeProgram;
    private int[] mMoleculePositionParam;
    private int[] mMoleculeColorParam;
    private int[] mMoleculeModelParam;
    private int[] mMoleculeModelViewParam;

    private int[] mBondingProgram;
    private int[] mBondingPositionParam;
    private int[] mBondingColorParam;
    private int[] mBondingModelParam;
    private int[] mBondingModelViewParam;

    private int mIsoProgram;
    private int mIsoPositionParam;
    private int mIsoColorParam;
    private int mIsoModelParam;
    private int mIsoModelViewParam;

    private float DistanceToScreen = (float)0.0;

    private float[] mModelMolecule;
    private float[] mModelBonding;
    private float[] mModelIsosurface;

    private float[] mHeadView;
    private float[] mHeadUpVector;
    private float[] mCamera;
    private float[] mView;
    private float[] mModelView;

    private int idx;
    private int[] nAtoms;
    private int[] nBonds;
    private int switchSignalCounter;
    private int jigglingCounter;

    private int debugging;
    private String debuggingStr;


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
        mModelView = new float[16];
        mView = new float[16];

        mMoleculeProgram = new int[NUM_MOLECULE];
        mMoleculePositionParam = new int[NUM_MOLECULE];
        mMoleculeColorParam = new int[NUM_MOLECULE];
        mMoleculeModelParam = new int[NUM_MOLECULE];
        mMoleculeModelViewParam = new int[NUM_MOLECULE];

        mBondingProgram = new int[NUM_MOLECULE];
        mBondingPositionParam = new int[NUM_MOLECULE];
        mBondingColorParam = new int[NUM_MOLECULE];
        mBondingModelParam = new int[NUM_MOLECULE];
        mBondingModelViewParam = new int[NUM_MOLECULE];

        moleculeVertices = new FloatBuffer[NUM_MOLECULE];
        mMoleculeColor = new FloatBuffer[NUM_MOLECULE];

        bondingVertices = new FloatBuffer[NUM_MOLECULE];
        mBondingColor = new FloatBuffer[NUM_MOLECULE];

        vMolecule = new float[NUM_MOLECULE][];
        vBondings = new float[NUM_MOLECULE][];
        cMolecule = new float[NUM_MOLECULE][];
        cBondings = new float[NUM_MOLECULE][];
        nAtoms = new int[NUM_MOLECULE];
        nBonds = new int[NUM_MOLECULE];

        mOverlay = (CardboardOverlayView) findViewById(R.id.overlay);
        mOverlay.show3DToast("Succeeded in creating this!");

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        debugging = 0;
        switchSignalCounter = 0;
        jigglingCounter = 0;

        //testing interpolation
//        float [] a = {2.0f,3.0f,4.0f,5.0f};
//        float [] b = {0.0f,0.0f,0.0f,0.0f};
//
//        float [] c = {100.0f,100.0f,100.0f,100.0f};
//        float [] d  = {0.0f,0.0f,0.0f,0.0f};
//
//        float [] v = {1.0f,1.0f,1.0f,1.0f};
//
//        float [] ret = util.interpolate(v,a,b,c,d);
//        Log.i(TAG,ret[0] + " " + ret[1] + " " +ret[2] + " " + ret[3]);

    }


    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.i(TAG, "Surface Created");

        //set up background color
       // GLES20.glClearColor(0.4f, 0.4f, 0.8f, 0.5f);

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.vertex);
        int passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.frag);

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
        BufferedReader[] readers = new BufferedReader[NUM_MOLECULE];
        for (int i = 0; i <NUM_MOLECULE; i++){
            String resourceName = "molecule" + i ;
            //open the resource by its resourceIdetifier which could be gained through the resourceName
            int iD = getResources().getIdentifier(resourceName, "raw", getPackageName());
            inputStreams[i] = getResources().openRawResource(iD);
            readers[i] = new BufferedReader(new InputStreamReader(inputStreams[i]));
        }

        //enable depth test
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Enable blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //build model and shader machine
        for (int i = 0; i < NUM_MOLECULE; i++) {

            try {
                parser.parse(readers[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            vMolecule[i] = parser.outputVertices();
            vBondings[i] = parser.outputBonds();
            cMolecule[i] = parser.outputColors();
            cBondings[i] = parser.outputBondingColors();
            nAtoms[i] = parser.outputNumOfAtoms();
            nBonds[i] = parser.outputNumOfBonds();

            //Build Molecule Program

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

            checkGLError("Create molecule program ");

            mMoleculePositionParam[i] = GLES20.glGetAttribLocation(mMoleculeProgram[i], "a_Position");
            mMoleculeColorParam[i] = GLES20.glGetAttribLocation(mMoleculeProgram[i], "a_Color");

            mMoleculeModelParam[i] = GLES20.glGetUniformLocation(mMoleculeProgram[i],"u_Model");
            mMoleculeModelViewParam[i] = GLES20.glGetUniformLocation(mMoleculeProgram[i],"u_MVMatrix");

            GLES20.glEnableVertexAttribArray(mMoleculePositionParam[i]);
            GLES20.glEnableVertexAttribArray(mMoleculeColorParam[i]);

            checkGLError("Molecule Program Params");

            //Build Bonding Program

            //Create ByteBuffer of vertices' position and color based on the float array created by "World"
            ByteBuffer ByteVertices2 = ByteBuffer.allocateDirect(vBondings[i].length * 4);
            ByteVertices2.order(ByteOrder.nativeOrder());
            bondingVertices[i] = ByteVertices2.asFloatBuffer();
            bondingVertices[i].put(vBondings[i]);
            bondingVertices[i].position(0);

            ByteBuffer ByteColors2 = ByteBuffer.allocateDirect(cBondings[i].length * 4);
            ByteColors2.order(ByteOrder.nativeOrder());
            mBondingColor[i] = ByteColors2.asFloatBuffer();
            mBondingColor[i].put(cBondings[i]);
            mBondingColor[i].position(0);

            mBondingProgram[i] = GLES20.glCreateProgram();
            GLES20.glAttachShader(mBondingProgram[i], vertexShader);
            GLES20.glAttachShader(mBondingProgram[i], passthroughShader);
            GLES20.glLinkProgram(mBondingProgram[i]);
            GLES20.glUseProgram(mBondingProgram[i]);

            checkGLError("Create bondings program ");

            mBondingPositionParam[i] = GLES20.glGetAttribLocation(mBondingProgram[i], "a_Position");
            mBondingColorParam[i] = GLES20.glGetAttribLocation(mBondingProgram[i], "a_Color");

            mBondingModelParam[i] = GLES20.glGetUniformLocation(mBondingProgram[i],"u_Model");
            mBondingModelViewParam[i] = GLES20.glGetUniformLocation(mBondingProgram[i],"u_MVMatrix");

            GLES20.glEnableVertexAttribArray(mBondingPositionParam[i]);
            GLES20.glEnableVertexAttribArray(mBondingColorParam[i]);

            checkGLError("Bonding Program Params");
       }

        //Isosurface example
        float [][][]s_values = s_functions.s();
        Isosurface I6 = new Isosurface(s_values,0.15f,-0.7f,0.7f,-0.7f,0.7f,-0.3f,0.3f);

        vIsosurface = I6.vertices;
        cIsosurface = I6.colors;

        //Create ByteBuffer of vertices' position and color based on the float array created by "World"
        ByteBuffer ByteVertices = ByteBuffer.allocateDirect(vIsosurface.length * 4);
        ByteVertices.order(ByteOrder.nativeOrder());
        IsoSVertices = ByteVertices.asFloatBuffer();
        IsoSVertices.put(vIsosurface);
        IsoSVertices.position(0);

        ByteBuffer ByteColors = ByteBuffer.allocateDirect(cIsosurface.length * 4);
        ByteColors.order(ByteOrder.nativeOrder());
        IsoSColor = ByteColors.asFloatBuffer();
        IsoSColor.put(cIsosurface);
        IsoSColor.position(0);

        mIsoProgram = GLES20.glCreateProgram();

        GLES20.glAttachShader(mIsoProgram,vertexShader);
        GLES20.glAttachShader(mIsoProgram,passthroughShader);
        GLES20.glLinkProgram(mIsoProgram);
        GLES20.glUseProgram(mIsoProgram);

        mIsoPositionParam = GLES20.glGetAttribLocation(mIsoProgram,"a_Position");
        mIsoColorParam = GLES20.glGetAttribLocation(mIsoProgram,"a_Color");

        mIsoModelParam = GLES20.glGetUniformLocation(mIsoProgram,"u_Model");
        mIsoModelViewParam = GLES20.glGetUniformLocation(mIsoProgram, "u_MVMatrix");

        GLES20.glEnableVertexAttribArray(mIsoPositionParam);
        GLES20.glEnableVertexAttribArray(mIsoColorParam);
        //end of Isosurface specs

        Matrix.setIdentityM(mModelMolecule, 0);
        Matrix.translateM(mModelMolecule, 0, 0, 0, -DistanceToScreen);

        Matrix.setIdentityM(mModelBonding, 0);
        Matrix.translateM(mModelBonding, 0, 0, 0, -DistanceToScreen);

        Matrix.setIdentityM(mModelIsosurface, 0);
        Matrix.translateM(mModelIsosurface,0,0,0,-DistanceToScreen);

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
        //mOverlay.show3DToast(debuggingStr);
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
        /*eye.getEyeView() and mHeadView(the variable obtained by calling HeadTransform.getHeadView()) are almost the same
        * on the documentation it is said that getEyeView() includes also position shift and interpupillary distance shift,
        * which I didn't find noticeable trace of effect
        * */
        //Matrix.multiplyMM(mView, 0, mHeadView, 0, mCamera, 0);


        // Set the position of the light
       // Matrix.multiplyMV(mLightPosInEyeSpace, 0, mView, 0, LIGHT_POS_IN_WORLD_SPACE, 0);

        /*Draw Molecules*/
        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        //float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelMolecule, 0);
        //Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
        drawMolecule(idx);

        /*Draw Bondings*/
        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        //float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelBonding, 0);
        //Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
        drawBondings(idx);

         /*Draw Isosurface*/
        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        //float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(mModelView, 0, mView, 0, mModelIsosurface, 0);
        //Matrix.multiplyMM(mModelViewProjection, 0, perspective, 0, mModelView, 0);
        drawIsosurface();

        // Draw rest of the scene.
    }

    //adjust vertices to fit oscillation, which randomly move to any direction in scale +-0.01
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

    }

    //the function to draw the i-th molecule
    public void drawMolecule(int index){
        GLES20.glUseProgram(mMoleculeProgram[index]);

        GLES20.glUniformMatrix4fv(mMoleculeModelParam[index],1,false,mModelMolecule,0);//set the model in the shader
        GLES20.glUniformMatrix4fv(mMoleculeModelViewParam[index], 1, false, mModelView, 0);//set the modelView in the shader

        //adjust the positions to form random oscillation
        if (jigglingCounter == 0)
            adjustVertices(index);
        jigglingCounter = (jigglingCounter ++) % JIGGLING_FREQUENCY;

        GLES20.glVertexAttribPointer(mMoleculePositionParam[index], COORDS_PER_VERTEX, GLES20.GL_FLOAT,false,0,moleculeVertices[index]);
        GLES20.glVertexAttribPointer(mMoleculeColorParam[index],4, GLES20.GL_FLOAT, false,0,mMoleculeColor[index]);

        for  (int i = 0; i < nAtoms[index]; i++){
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES,i*Sphere.NUMBER_OF_VERTICES, Sphere.NUMBER_OF_VERTICES);
        }

        checkGLError("Drawing Molecule");
    }

    //the function to draw bondings of the i-th molecule
    public void drawBondings(int index){
        GLES20.glUseProgram(mBondingProgram[index]);

        GLES20.glUniformMatrix4fv(mBondingModelParam[index],1,false,mModelBonding,0);//set the model in the shader
        GLES20.glUniformMatrix4fv(mBondingModelViewParam[index], 1, false, mModelView, 0);//set the modelView in the shader

        GLES20.glVertexAttribPointer(mBondingPositionParam[index], COORDS_PER_VERTEX, GLES20.GL_FLOAT,false,0,bondingVertices[index]);
        GLES20.glVertexAttribPointer(mBondingColorParam[index],4, GLES20.GL_FLOAT, false,0,mBondingColor[index]);

        for  (int i = 0; i < nBonds[index]; i++){
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES,i*Cylinder.NUMBER_OF_VERTICES, Cylinder.NUMBER_OF_VERTICES);
        }

        checkGLError("Drawing Bondings");
    }

    //the function to draw the isosurface
    public void drawIsosurface(){
        GLES20.glUseProgram(mIsoProgram);

        GLES20.glUniformMatrix4fv(mIsoModelParam,1,false,mModelIsosurface,0);//set the model in the shader
        GLES20.glUniformMatrix4fv(mIsoModelViewParam, 1, false, mModelView, 0);//set the modelView in the shader

        GLES20.glVertexAttribPointer(mIsoPositionParam,COORDS_PER_VERTEX,GLES20.GL_FLOAT,false,0,IsoSVertices);
        GLES20.glVertexAttribPointer(mIsoColorParam,4,GLES20.GL_FLOAT,false,0,IsoSColor);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,cIsosurface.length/3);

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
        mOverlay.show3DToast("Switched to "+idx +"-th molecule" );
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
        if (switchSignalCounter == 100) {
            if ((Math.abs(mHeadUpVector[0])  > 0.95)
                    && (Math.abs(mHeadUpVector[1])  < 0.5)
                    && (Math.abs(mHeadUpVector[2])  < 0.5)
                    ) {
                vibrator.vibrate(50);
                idx = (idx + 1) % NUM_MOLECULE;
            }
            switchSignalCounter = 0;
        }
         /*
        debugging++;
        if (debugging == 100) {
            String temp = mHeadUpVetcor[0] + " " + mHeadUpVetcor[1] + " " + mHeadUpVetcor[2];
            //mOverlay.show3DToast(temp);
            Log.i(TAG,temp);
            debugging = 0;
        }
        */

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
