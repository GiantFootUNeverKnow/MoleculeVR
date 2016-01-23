/**
 * Created by Xusheng on 28/05/2015.
 *
 * 3D primitive object -- Cylinder
 *
 */
package spin.ncsa.org.moleculevr;

import android.util.Log;


public class Cylinder {

    //If degree of refinement is changed, be sure to change NUMBER_OF_FACES to corresponding value, 8 * (2^degree_of_refinement)
    private final static int DEGREE_OF_REFINEMENT = 2;// do only once the refinement
    private final static int NUMBER_OF_FACES = 32; //do only twice the refinement, 320 faces

    final static int NUMBER_OF_VERTICES = NUMBER_OF_FACES * 3; //do only twice the refinement, 320 faces, each with 3 vertices
    final static int NUMBER_OF_COORDS = NUMBER_OF_VERTICES * 3; //do only twice the refinement, 960 vertices, each with 3 coords
    final static int NUMBER_OF_COLORS = NUMBER_OF_VERTICES * 4; //do only twice the refinement, 960 vertices, each with 4 colors
    final static int NUMBER_OF_NORMALS = NUMBER_OF_COORDS;

    private float RESIZING_FACTOR = 75.0f;

    public Cylinder(float [] end_a, float [] end_b, float [] color_a, float[] color_b){
            xcoord1 = end_a[0];
            ycoord1 = end_a[1];
            zcoord1 = end_a[2];

            xcoord2 = end_b[0];
            ycoord2 = end_b[1];
            zcoord2 = end_b[2];

            redColor1 = color_a[0];
            greenColor1 = color_a[1];
            blueColor1 = color_a[2];

            redColor2 = color_b[0];
            greenColor2 = color_b[1];
            blueColor2 = color_b[2];

            builtCylinder();
            setNormal();
            setCoordinate();
    }

    private void setNormal(){//calculate normals based on vertex's pointing direction
        System.arraycopy(vertices,0,normals,0,NUMBER_OF_NORMALS);
    }

    private void setCoordinate(){//move cylinder from origin to the given coordinates
        int coord_choice = 2;
        for (int i = 0; i < NUMBER_OF_VERTICES; i++){
            if (coord_choice == 2){
                vertices[3 * i] = ( vertices[3 * i] / RESIZING_FACTOR) + xcoord2;
                vertices[3 * i + 1] = ( vertices[3 * i + 1] / RESIZING_FACTOR) + ycoord2;
                vertices[3 * i + 2] = ( (vertices[3 * i + 2] - 0.5f) / RESIZING_FACTOR) + zcoord2;
                coord_choice = 1;
            }
            else{//coord_choice == 1
                vertices[3 * i] = ( vertices[3 * i] / RESIZING_FACTOR) + xcoord1;
                vertices[3 * i + 1] = ( vertices[3 * i + 1] / RESIZING_FACTOR) + ycoord1;
                vertices[3 * i + 2] = ( (vertices[3 * i + 2] - 0.5f) / RESIZING_FACTOR) + zcoord1;
                coord_choice = 2;
            }
        }
    }

    private int index = 0;

    public float xcoord1;
    public float ycoord1;
    public float zcoord1;

    public float redColor1;
    public float blueColor1;
    public float greenColor1;

    public float redColor2;
    public float blueColor2;
    public float greenColor2;

    public float xcoord2;
    public float ycoord2;
    public float zcoord2;

    private float vdata[][] = new float[][]{
            {1.0f,0.0f,0.0f},
            {0.0f,1.0f,0.0f},
            {-1.0f,0.0f,0.0f},
            {0.0f,-1.0f,0.0f}
    };

    //cylinder
    public float[] vertices = new float[NUMBER_OF_COORDS];
    public float[] colors = new float[NUMBER_OF_COLORS];
    public float[] normals = new float[NUMBER_OF_COORDS];

    private void subdivide(float [] v1, float[] v2, int depth){
        float[] v11 = {0.0f,0.0f,0.0f};
        float[] v22 = {0.0f,0.0f,0.0f};
        float[] v12 = new float[3];

        if (depth == 0){
            for (int i = 0; i < 2; i++){
                v22[i] = v2[i];
                v11[i] = v1[i];
            }
            v22[2] = v11[2] = 1.0f;

            //a rectangle consists of 2 triangles, vertices arranged in a way that their colors should be alternatively color1 and color2

            //the first triangle
            vertices[index] = v11[0];
            vertices[index+1] = v11[1];
            vertices[index+2] = v11[2];

            vertices[index+3] = v2[0];
            vertices[index+4] = v2[1];
            vertices[index+5] = v2[2];

            vertices[index+6] = v22[0];
            vertices[index+7] = v22[1];
            vertices[index+8] = v22[2];

            //the second triangle
            vertices[index+9] = v1[0];
            vertices[index+10] = v1[1];
            vertices[index+11] = v1[2];

            vertices[index+12] = v11[0];
            vertices[index+13] = v11[1];
            vertices[index+14] = v11[2];

            vertices[index+15] = v2[0];
            vertices[index+16] = v2[1];
            vertices[index+17] = v2[2];

            index += 18;
            return;
        }

        //depth > 0
        for(int i = 0; i < 3; i++){
            v12[i] = v1[i] + v2[i];
        }
        normalize(v12);

        subdivide(v1,v12,depth-1);
        subdivide(v12,v2,depth-1);
    }

    private void builtCylinder(){
        subdivide(vdata[0],vdata[1],DEGREE_OF_REFINEMENT);
        subdivide(vdata[1],vdata[2],DEGREE_OF_REFINEMENT);
        subdivide(vdata[2],vdata[3],DEGREE_OF_REFINEMENT);
        subdivide(vdata[3],vdata[0],DEGREE_OF_REFINEMENT);

        int color_choice= 2;
        for (int i = 0; i < NUMBER_OF_VERTICES; i++){
            if (color_choice == 2){
                colors[i * 4] = redColor2;
                colors[i * 4 + 1] = greenColor2;
                colors[i * 4 + 2] = blueColor2;
                colors[i * 4 + 3] = 1.0f;
                color_choice = 1;
            }
            else{ //color_choice == 1
                colors[i * 4] = redColor1;
                colors[i * 4 + 1] = greenColor1;
                colors[i * 4 + 2] = blueColor1;
                colors[i * 4 + 3] = 1.0f;
                color_choice = 2;
            }
        }
    }

    private static void normalize(float v[]){
        float d = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (d == 0.0){
            Log.e("Cylinder", "normalize");
        }
        v[0] /= d;
        v[1] /= d;
        v[2] /= d;
    }

}
