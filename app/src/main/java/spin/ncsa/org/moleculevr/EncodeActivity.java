/**
 *   Created by Xusheng on 11/07/2015
 *
 *   This is an activity to serialize molecules' data.
 *
 *   Files read from few different files concerning the same molecule would be put together and
 *   be serialized into an mlc format
 *
 */
package spin.ncsa.org.moleculevr;

import spin.ncsa.org.moleculevr.utils.SystemUiHider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.Eye;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Provides drawing instructions for a GLSurfaceView object. This class
 * must override the OpenGL ES drawing lifecycle methods:
 * <ul>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceCreated}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onDrawFrame}</li>
 *   <li>{@link android.opengl.GLSurfaceView.Renderer#onSurfaceChanged}</li>
 * </ul>
 */
public class EncodeActivity extends Activity implements CardboardView.StereoRenderer {


    public MoleculeSet moleculeSet;

    private static String TAG = "ENCODERAWFILE";

    private GLSurfaceView mGLView;
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_encode);

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        setContentView(R.layout.activity_main);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };




    @Override
    public void onSurfaceCreated( EGLConfig config) {
        //real work -- Create an instance of serialized molecule
        readRawFiles(1);//This line creates molecule1
    }

    /**
     * Read files for a molecule, assemble the molecule set object and serialize it
     * @param idx: number of molecule to read
     */
    public void readRawFiles(int idx){

        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.vertex);
        int fragShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.frag);
        int passthroughShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.passthrough);

        TextParser parser = new TextParser();

        //load the predefined colors for each element from file R.raw.xxx
        InputStream iS = getResources().openRawResource(R.raw.cpk_coloring);
        BufferedReader readColor = new BufferedReader(new InputStreamReader(iS));
        parser.loadColor(readColor);

        //load table of atom mass from file R.raw.atommass
        iS = getResources().openRawResource(R.raw.atommass);
        BufferedReader readAtomMass = new BufferedReader(new InputStreamReader(iS));
        parser.loadAtomMass(readAtomMass);

        //load molecule coordinates and bondings fromraw text files
        String resourceName0 = "molecule" + idx ;
        String resourceName1 = "bonding" + idx ;
        int iD0 = getResources().getIdentifier(resourceName0, "raw", getPackageName());
        int iD1 = getResources().getIdentifier(resourceName1, "raw", getPackageName());
        InputStream inputStream0 = getResources().openRawResource(iD0);
        InputStream inputStream1 = getResources().openRawResource(iD1);

        Log.d(TAG,"molecule read");

        //declarations of fields in moleculeSet
        Drawable molecule = null;
        Drawable bonding = null;
        Drawable[] isosurface = new Drawable[2];
        int nTriangleInIso[] = new int[2];

        try
        {
            BufferedReader reader0 = new BufferedReader(new InputStreamReader(inputStream0));
            BufferedReader reader1 = new BufferedReader(new InputStreamReader(inputStream1));
            parser.parse(reader0,reader1);

            //obtained meaningful results from TextParser
            float[] vMolecule = parser.outputVertices();
            float[] vBondings = parser.outputBonds();
            float[] cMolecule = parser.outputColors();
            float[] cBondings = parser.outputBondingColors();
            int nAtoms = parser.outputNumOfAtoms();
            int nBonds = parser.outputNumOfBonds();

            float[] nMolecule = parser.outputNormals();
            float[] nBondings = parser.outputBondingNormals();

            //constructed molecules and bonding objects using data obtained from TextParser
            molecule = new Drawable(vMolecule, cMolecule, nMolecule, vertexShader, fragShader, nAtoms, "Molecule "+ idx);
            bonding = new Drawable(vBondings,cBondings, nBondings, vertexShader, fragShader, nBonds, "Bonding " + idx);

            Log.d(TAG,"molecule built");

            //Read, parse, and construct two isosurfaces if density[i].txt exists
            String resourceName2 = "density" + idx;
            int iD2 = getResources().getIdentifier(resourceName2, "raw", getPackageName());
            InputStream inputStream2 = getResources().openRawResource(iD2);

            BufferedReader reader2 = new BufferedReader(new InputStreamReader(inputStream2));
            float[][][] l_values = parser.loadDensity(reader2);

            Isosurface I8 = new Isosurface(l_values, parser.primaryLevel, -0.8f,0.8f,-0.8f,0.8f,-0.8f,0.8f);
            Isosurface I9 = new Isosurface(l_values, parser.secondaryLevel, -0.8f,0.8f,-0.8f,0.8f,-0.8f,0.8f);

            Log.d(TAG,"isosurface read");

            float vIsosurface[][] = new float[2][];
            float cIsosurface[][] = new float[2][];

            vIsosurface[0] = I8.vertices;
            cIsosurface[0] = I8.colors;
            nTriangleInIso[0] = I8.nTriang;

            vIsosurface[1] = I9.vertices;
            cIsosurface[1] = I9.colors;
            nTriangleInIso[1] = I9.nTriang;

            for (int j = 0; j < 2; j++) {
                isosurface[j] =
                        new Drawable(vIsosurface[j], cIsosurface[j], passthroughShader, fragShader, 1,
                                "Isosurface " + idx + "-" + j);
            }
            Log.d(TAG,"isosurface built");

        //If there is no corresponding density file, we assign field isosurface to be null
        }catch(IOException i)
        {
            i.printStackTrace();

            isosurface[0] = null;
            isosurface[1] = null;
        }

        //Assemble a molecule set from molecule coordinates, bonding and isosurface density
        moleculeSet = new MoleculeSet(molecule,bonding,isosurface,nTriangleInIso);
        Log.d(TAG,"molecule set is ready");

        //Write serialization of the molecule set to external directory
        try
        {
            if (isExternalStorageWritable()){
                FileOutputStream fileOut =
                        new FileOutputStream(Environment.getExternalStorageDirectory()+"/moleculeSet/"+idx+".mlc");
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                Log.d(TAG, "Serialized thing is done");
                out.writeObject(moleculeSet);
                out.close();
                Log.i(TAG, "Serialized data is saved in " + idx + ".mlc");
            }
            else{
                Log.d(TAG,"External Storage is not writable");
            }
        }catch(Exception i)
        {
            i.printStackTrace();
        }

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /*utility methods*/

    /**
     *
     * @param resId : resourse ID of loaded textfile
     * @return: a string representing all texts in the read file
     */
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

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public void onSurfaceChanged(int width, int height){ //temporarily useless
    }

    @Override
    public void onFinishFrame(Viewport viewport){ //temporarily useless
    }


    @Override
    public void onRendererShutdown(){ //temporarily useless
        Log.i(TAG, "onRenderShutdown");
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {

    }

    /**
     * Draws a frame for an eye.
     *
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {

    }


}
