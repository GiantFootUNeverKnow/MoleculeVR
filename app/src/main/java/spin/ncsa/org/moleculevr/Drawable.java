package spin.ncsa.org.moleculevr;


import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by Xusheng on 19/06/2015.
 */

public class Drawable {

        int numOfSubItem;
        String drawableName;

        private FloatBuffer mCoordinates;
        private FloatBuffer mColors;
        private FloatBuffer mNormals;

        private float[] coords;
        private float[] colors;
        private float[] normals;

        private int GLprogram;
        private int GLpositionParam;
        private int GLnormalParam;
        private int GLcolorParam;
        private int GLmodelParam;
        private int GLmodelViewParam;
        private int GLlightPosParam;

        private static final String TAG = "Drawable";

        private static final int COORDS_PER_VERTEX = 3;
        //JIGGLING_FREQUENCY = x means that for x frames generated, 1 jiggling occurs
        private static final int JIGGLING_FREQUENCY = 5;

        private int jigglingCounter = 0;

    //constructor that accepts an array of normals
    public Drawable(  float[] coordinates,float[] colors, float[] normals, int vertexShader, int fragShader, int NI, String name) {

        //init two basic attributes of the drawable: its name and number of items it has to draw
        drawableName = name;
        numOfSubItem = NI;
        coords = coordinates;
        this.colors = colors;
        this.normals = normals;

        //Create ByteBuffer of vertices' position , normal vectors and color based on the float array created by "Sphere"
        ByteBuffer ByteVertices = ByteBuffer.allocateDirect(coordinates.length * 4);
        ByteVertices.order(ByteOrder.nativeOrder());
        mCoordinates = ByteVertices.asFloatBuffer();
        mCoordinates.put(coordinates);
        mCoordinates.position(0);

        ByteBuffer ByteColors = ByteBuffer.allocateDirect(colors.length * 4);
        ByteColors.order(ByteOrder.nativeOrder());
        mColors = ByteColors.asFloatBuffer();
        mColors.put(colors);
        mColors.position(0);

        ByteBuffer ByteNormals = ByteBuffer.allocateDirect(normals.length * 4);
        ByteNormals.order(ByteOrder.nativeOrder());
        mNormals = ByteNormals.asFloatBuffer();
        mNormals.put(normals);
        mNormals.position(0);

        GLprogram = GLES20.glCreateProgram();
        GLES20.glAttachShader(GLprogram, vertexShader);
        GLES20.glAttachShader(GLprogram, fragShader);
        GLES20.glLinkProgram(GLprogram);
        GLES20.glUseProgram(GLprogram);

        //we can add a name variable for each drawable
        checkGLError("Create a program of " + name);

        GLpositionParam = GLES20.glGetAttribLocation(GLprogram, "a_Position");
        GLnormalParam = GLES20.glGetAttribLocation(GLprogram,"a_Normal");
        GLcolorParam = GLES20.glGetAttribLocation(GLprogram, "a_Color");
        GLmodelParam = GLES20.glGetUniformLocation(GLprogram,"u_Model");
        GLmodelViewParam = GLES20.glGetUniformLocation(GLprogram,"u_MVMatrix");
        //GLmodelViewProjectionParam = GLES20.glGetUniformLocation(GLprogram,"u_MVPMatrix");
        GLlightPosParam = GLES20.glGetUniformLocation(GLprogram,"u_LightPos");

        GLES20.glEnableVertexAttribArray(GLpositionParam);
        GLES20.glEnableVertexAttribArray(GLnormalParam);
        GLES20.glEnableVertexAttribArray(GLcolorParam);

        checkGLError("Program of " + name + " has params Set");

    }

    //constructor that omits normals and lightPosition
    public Drawable(  float[] coordinates,float[] colors, int vertexShader, int fragShader, int NI, String name) {

        //init two basic attributes of the drawable: its name and number of items it has to draw
        drawableName = name;
        numOfSubItem = NI;

        //Create ByteBuffer of vertices' position , normal vectors and color based on the float array created by "Sphere"
        ByteBuffer ByteVertices = ByteBuffer.allocateDirect(coordinates.length * 4);
        ByteVertices.order(ByteOrder.nativeOrder());
        mCoordinates = ByteVertices.asFloatBuffer();
        mCoordinates.put(coordinates);
        mCoordinates.position(0);

        ByteBuffer ByteColors = ByteBuffer.allocateDirect(colors.length * 4);
        ByteColors.order(ByteOrder.nativeOrder());
        mColors = ByteColors.asFloatBuffer();
        mColors.put(colors);
        mColors.position(0);

        mNormals = null;

        GLprogram = GLES20.glCreateProgram();
        GLES20.glAttachShader(GLprogram, vertexShader);
        GLES20.glAttachShader(GLprogram, fragShader);
        GLES20.glLinkProgram(GLprogram);
        GLES20.glUseProgram(GLprogram);

        //we can add a name variable for each drawable
        checkGLError("Create a program of " + name);

        GLpositionParam = GLES20.glGetAttribLocation(GLprogram, "a_Position");
        GLcolorParam = GLES20.glGetAttribLocation(GLprogram, "a_Color");
        GLmodelParam = GLES20.glGetUniformLocation(GLprogram,"u_Model");
        GLmodelViewParam = GLES20.glGetUniformLocation(GLprogram,"u_MVMatrix");
        //GLmodelViewProjectionParam = GLES20.glGetUniformLocation(GLprogram,"u_MVPMatrix");

        GLES20.glEnableVertexAttribArray(GLpositionParam);
        GLES20.glEnableVertexAttribArray(GLnormalParam);
        GLES20.glEnableVertexAttribArray(GLcolorParam);

        checkGLError("Program of " + name + " has params Set");

    }



    //jiggle is a boolean that indicates whether the object should jiggle
    public void draw(float[] lightPosInEyeSpace, float[] mModel, float[] mModelView, int numOfVertices, boolean jiggle ){
        GLES20.glUseProgram(GLprogram);

        if (lightPosInEyeSpace != null)
            GLES20.glUniform3fv(GLlightPosParam,1,lightPosInEyeSpace,0);
        GLES20.glUniformMatrix4fv(GLmodelParam,1,false,mModel,0);//set the model in the shader
        GLES20.glUniformMatrix4fv(GLmodelViewParam,1,false,mModelView,0);//set the modelView in the shader
        //GLES20.glUniformMatrix4fv(mMoleculeModelViewProjectionParam[index], 1, false, mModelViewProjection,0);  // Set the ModelViewProjection matrix in the shader.

        //adjust the positions to form random oscillation

        if (jiggle) {
            if (jigglingCounter == 0)
                adjustVertices();
            jigglingCounter = (jigglingCounter++) % JIGGLING_FREQUENCY;
        }

        // Set the normal positions of atoms, again for shading
        if (lightPosInEyeSpace != null)
            GLES20.glVertexAttribPointer(GLnormalParam,3,GLES20.GL_FLOAT,false,0,mNormals);
        GLES20.glVertexAttribPointer(GLpositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, mCoordinates);
        GLES20.glVertexAttribPointer(GLcolorParam,4,GLES20.GL_FLOAT,false,0,mColors);

        for (int i = 0; i < numOfSubItem; i++){
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, i*numOfVertices, numOfVertices);
        }

        checkGLError("Drawing Shape of " + drawableName);
    }

    //adjust vertices to fit oscillation, which randomly move to any direction in scale +-0.01
    private void adjustVertices(){

        mCoordinates.clear();//clear out vertices in this buffer
        float [] temp = coords.clone();//make a local copy of the molecules
        float SCALE = 0.01f;

        for (int i = 0; i < numOfSubItem; i++){
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
        mCoordinates.put(temp);
        mCoordinates.position(0);

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


