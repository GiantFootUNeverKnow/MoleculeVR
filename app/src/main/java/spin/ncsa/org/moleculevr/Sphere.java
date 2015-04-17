package spin.ncsa.org.moleculevr;

/**
 * Created by Radhir on 4/14/15.
 */
import android.util.Log;
public class Sphere
{
    public Sphere(float x, float y, float z, float red, float green, float blue)
    {
        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;

        this.redColor = red;
        this.greenColor = green;
        this.blueColor = blue;

        setCoordinates();
        buildSphere();
    }

    public static final float X = (float) 0.52573111;
    public static final float Z = (float) 0.85065081;
    public int index = 0;
    public float xCoord = 0;
    public float yCoord = 0;
    public float zCoord = 0;

    public float redColor;
    public float greenColor;
    public float blueColor;


    //NEED TO CHANGE THESE. BUT DON'T KNOW HOW
    public float[][] vdata = new float[][]{
            {-X , 0.0f , Z }, {X , 0.0f, Z },
            {-X , 0.0f , -Z }, {X , 0.0f , -Z },
            {0.0f, Z , X }, {0.0f , Z, -X },
            {0.0f , -Z , X }, {0.0f , -Z , -X },
            {Z ,X ,0.0f },{-Z  ,X ,0.0f },{Z ,-X,0.0f},
            {-Z,-X ,0.0f}    };

    public static final int[][] tindices = new int[][]{
            {1,4,0},{4,9,0},{4,5,9},{8,5,4},{1,8,4},
            {1,10,8},{10,3,8},{8,3,5},{3,2,5},{3,7,2},
            {3,10,7},{10,6,7},{6,11,7},{6,0,11},{6,1,0},
            {10,1,6},{11,0,9},{2,11,9},{5,2,9},{11,2,7}
    };

    /*isosahedron
    public static final float[] NEW_THING = new float[180];
    public static final float[] NEW_COLOR = new float[240];
*/
    public void setCoordinates(){//set up the prototyppe coordinate for the sphere
        for (float[] coords : vdata){//maybe wrong
            coords[0] += xCoord;
            coords[1] += yCoord;
            coords[2] += zCoord;
        }
    }

    public static void normalize(float v[]){
        float d = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (d == 0.0){
            Log.e("World","normalize");
        }
        v[0] /= d;
        v[1] /= d;
        v[2] /= d;
    }

    public void subdivide(float[] v1,float[] v2,float[] v3, long depth){
        float[] v12,v23,v31;
        if (depth == 0){
            vertices[index] = v1[0];
            vertices[index + 1] = v1[1];
            vertices[index + 2] = v1[2];
            vertices[index + 3] = v2[0];
            vertices[index + 4] = v2[1];
            vertices[index + 5] = v2[2];
            vertices[index + 6] = v3[0];
            vertices[index + 7] = v3[1];
            vertices[index + 8] = v3[2];
            index += 9;
            return;
        }
        v12 = new float[3];
        v23 = new float[3];
        v31 = new float[3];
        for (int i = 0; i < 3; i++){
            v12[i] = (float)( (v1[i] + v2[i])/2.0 );
            v23[i] = (float)( (v2[i] + v3[i])/2.0 );
            v31[i] = (float)( (v3[i] + v1[i])/2.0 );
        }
        normalize(v12);
        normalize(v23);
        normalize(v31);
        subdivide(v1,v12,v31,depth-1);
        subdivide(v2,v23,v12,depth-1);
        subdivide(v3,v31,v23,depth-1);
        subdivide(v12,v23,v31,depth-1);
    }


    //sphere
    public float[] vertices = new float[2880];
    public float[] colors = new float[3840];

    private void buildSphere(){
        //For sphere
        for (int i = 0; i < 20; i++) {
            subdivide(vdata[tindices[i][0]], vdata[tindices[i][1]], vdata[tindices[i][2]], 2);
        }

        for (int i = 0; i < 320; i++) {
            colors[i * 12] = redColor + 0.05f;// Because we don't have shading/lighting yet, manually adjust the shades by adding +-0.05
            colors[i * 12 + 1] = greenColor;
            colors[i * 12 + 2] = blueColor;
            colors[i * 12 + 3] = 1.0f;
            colors[i * 12 + 4] = redColor -0.05f;
            colors[i * 12 + 5] = greenColor;
            colors[i * 12 + 6] = blueColor;
            colors[i * 12 + 7] = 1.0f;
            colors[i * 12 + 8] = redColor;
            colors[i * 12 + 9] = greenColor;
            colors[i * 12 + 10] = blueColor;
            colors[i * 12 + 11] = 1.0f;
        }
    }

    /*
        //almost sphere
    public static final float[] NEW_THING = new float[720];
    public static final float[] NEW_COLOR = new float[960];

    static{
        //For almost sphere

        for (int i = 0; i < 20; i++){
            subdivide(vdata[tindices[i][0]],vdata[tindices[i][1]],vdata[tindices[i][2]],1);
        }

        for (int i = 0; i < 80; i++){
            NEW_COLOR[i * 12] = 0.8f;
            NEW_COLOR[i * 12 + 1] = 0.2f;
            NEW_COLOR[i * 12 + 2] = 0.2f;
            NEW_COLOR[i * 12 + 3] = 1.0f;
            NEW_COLOR[i * 12 + 4] = 0.8f;
            NEW_COLOR[i * 12 + 5] = 0.1f;
            NEW_COLOR[i * 12 + 6] = 0.3f;
            NEW_COLOR[i * 12 + 7] = 1.0f;
            NEW_COLOR[i * 12 + 8] = 0.8f;
            NEW_COLOR[i * 12 + 9] = 0.3f;
            NEW_COLOR[i * 12 + 10] = 0.1f;
            NEW_COLOR[i * 12 + 11] = 1.0f;
        }
        */

        /* For isosahedron
        for (int i = 0; i < 20; i++){
            for(int j = 0; j < 3; j++){
                NEW_THING[i*9 + j*3 ] = vdata[tindices[i][j]][0];
                NEW_THING[i*9 + j*3 + 1] = vdata[tindices[i][j]][1];
                NEW_THING[i*9 + j*3 + 2] = vdata[tindices[i][j]][2];
            }
        }

       for (int i = 0; i < 20; i++){
            NEW_COLOR[i * 12] = 0.8f;
            NEW_COLOR[i * 12 + 1] = 0.2f;
            NEW_COLOR[i * 12 + 2] = 0.2f;
            NEW_COLOR[i * 12 + 3] = 1.0f;
           NEW_COLOR[i * 12 + 4] = 0.8f;
           NEW_COLOR[i * 12 + 5] = 0.1f;
           NEW_COLOR[i * 12 + 6] = 0.3f;
           NEW_COLOR[i * 12 + 7] = 1.0f;
           NEW_COLOR[i * 12 + 8] = 0.8f;
           NEW_COLOR[i * 12 + 9] = 0.3f;
           NEW_COLOR[i * 12 + 10] = 0.1f;
           NEW_COLOR[i * 12 + 11] = 1.0f;
        }*/



    //A =  0.5f,0.0f,0.5f, // =A
    //B = -0.5f,0.0f,-0.5f,// = B
    //C = 0.5f,0.0f,-0.5f,// = C
    //D = -0.5f,0.0f,0.5f, // = D
    //E = 0.0f,0.5f,0.0f, // = E
    //F = 0.0f,-0.5f,0.0f, // = F

    //diamond
    /*
        public static final float[] NEW_THING = new float[]{
                0.0f,0.5f,0.0f, // = E
                -0.5f,0.0f,0.5f, // = D
                0.5f,0.0f,0.5f, // =A

                0.0f,0.5f,0.0f, // = E
                0.5f,0.0f,0.5f, // =A
                0.5f,0.0f,-0.5f,// = C

                0.0f,0.5f,0.0f, // = E
                0.5f,0.0f,-0.5f,// = C
                -0.5f,0.0f,-0.5f,// = B

                0.0f,0.5f,0.0f, // = E
                -0.5f,0.0f,-0.5f,// = B
                -0.5f,0.0f,0.5f, // = D

                0.0f,-0.5f,0.0f, // = F
                -0.5f,0.0f,0.5f, // = D
                0.5f,0.0f,0.5f, // =A

                0.0f,-0.5f,0.0f, // = F
                0.5f,0.0f,0.5f, // =A
                0.5f,0.0f,-0.5f,// = C

                0.0f,-0.5f,0.0f, // = F
                0.5f,0.0f,-0.5f,// = C
                -0.5f,0.0f,-0.5f,// = B

                0.0f,-0.5f,0.0f, // = F
                -0.5f,0.0f,-0.5f,// = B
                -0.5f,0.0f,0.5f, // = D
        };

        //color of diamond
        //E = 0.8f,0.8f,0.8f,1.0f, //E
        //F = 0.2f,0.2f,0.2f,1.0f, //F
        //A = 0.6f,0.55f,0.4f,  //A
        public static final float[] NEW_COLOR = new float[]{
                0.8f,0.8f,0.8f,1.0f, //E
                0.78f, 0.24f, 0.25f,1.0f,   //D
                0.78f, 0.24f, 0.25f,1.0f,   //A
                0.8f,0.8f,0.8f,1.0f, //E
                0.78f, 0.24f, 0.25f,1.0f,   //A
                0.78f, 0.24f, 0.25f,1.0f,   //C
                0.8f,0.8f,0.8f,1.0f, //E
                0.78f, 0.24f, 0.25f,1.0f,   //C
                0.78f, 0.24f, 0.25f,1.0f,   //B
                0.8f,0.8f,0.8f,1.0f, //E
                0.78f, 0.24f, 0.25f,1.0f,   //B
                0.78f, 0.24f, 0.25f,1.0f,   //D
                0.2f,0.2f,0.2f,1.0f, //F
                0.78f, 0.24f, 0.25f,1.0f,   //D
                0.78f, 0.24f, 0.25f,1.0f,   //A
                0.2f,0.2f,0.2f,1.0f, //F
                0.78f, 0.24f, 0.25f,1.0f,   //A
                0.78f, 0.24f, 0.25f,1.0f,   //C
                0.2f,0.2f,0.2f,1.0f, //F
                0.78f, 0.24f, 0.25f,1.0f,   //C
                0.78f, 0.24f, 0.25f,1.0f,   //B
                0.2f,0.2f,0.2f,1.0f, //F
                0.78f, 0.24f, 0.25f,1.0f,   //B
                0.78f, 0.24f, 0.25f,1.0f,   //D
        };
*/

    //The smiley
        /*public static final float[] NEW_THING = new float[]{
            -0.65f, 0.7f, 0.0f,
            -0.9f, 0.28f, -0.4f,
            -0.58f, 0.4f, 0.0f,
            -0.65f, 0.7f, 0.0f,
            -0.35f, 0.3f, 0.0f,
            -0.58f, 0.4f, 0.0f,
            //left eye

            0.65f, 0.7f, 0.0f,
            0.9f, 0.28f, -0.4f,
            0.58f, 0.4f, 0.0f,
            0.65f, 0.7f, 0.0f,
            0.35f, 0.3f, 0.0f,
            0.58f, 0.4f, 0.0f,
            //right eye

            -0.68f,-0.1f,0.0f,
            -0.54f,-0.3f,0.0f,
            -0.33f,-0.65f,0.0f,
            -0.68f,-0.1f,0.0f,
            -0.33f,-0.45f,0.0f,
            -0.33f,-0.65f,0.0f,
            -0.33f,-0.45f,0.0f,
            -0.33f,-0.65f,0.0f,
            0.0f,-0.75f,0.0f,
            -0.33f,-0.45f,0.0f,
            0.0f,-0.6f,0.0f,
            0.0f,-0.75f,0.0f,

            0.68f,-0.1f,0.0f,
            0.54f,-0.3f,0.0f,
            0.33f,-0.65f,0.0f,
            0.68f,-0.1f,0.0f,
            0.33f,-0.45f,0.0f,
            0.33f,-0.65f,0.0f,
            0.33f,-0.45f,0.0f,
            0.33f,-0.65f,0.0f,
            0.0f,-0.75f,0.0f,
            0.33f,-0.45f,0.0f,
            0.0f,-0.6f,0.0f,
            0.0f,-0.75f,0.0f
    };*/
    //color of the smiley
        /*
        public static final float[] NEW_COLOR = new float[]{
                0.8f,0.8f,0.0f,1.0f,
                0.8f,0.8f,0.0f,1.0f,
                0.8f,0.8f,0.0f,1.0f,
                0.8f,0.8f,0.0f,1.0f,
                0.8f,0.8f,0.0f,1.0f,
                0.8f,0.8f,0.0f,1.0f,

                0.8f,0.8f,0.0f,1.0f,
                0.8f,0.8f,0.0f,1.0f,
                0.8f,0.8f,0.0f,1.0f,
                0.8f,0.8f,0.0f,1.0f,
                0.8f,0.8f,0.0f,1.0f,
                0.8f,0.8f,0.0f,1.0f,

                0.0f,0.1f,0.8f,1.0f,
                0.0f,0.1f,0.8f,1.0f,
                0.0f,0.1f,0.8f,1.0f,
                0.0f,0.2f,0.8f,1.0f,
                0.0f,0.2f,0.8f,1.0f,
                0.0f,0.2f,0.8f,1.0f,
                0.0f,0.25f,0.75f,1.0f,
                0.0f,0.25f,0.75f,1.0f,
                0.0f,0.25f,0.75f,1.0f,
                0.0f,0.3f,0.75f,1.0f,
                0.0f,0.3f,0.75f,1.0f,
                0.0f,0.3f,0.75f,1.0f,

                0.0f,0.3f,0.75f,1.0f,
                0.0f,0.3f,0.75f,1.0f,
                0.0f,0.3f,0.75f,1.0f,
                0.0f,0.25f,0.75f,1.0f,
                0.0f,0.25f,0.75f,1.0f,
                0.0f,0.25f,0.75f,1.0f,
                0.0f,0.25f,0.7f,1.0f,
                0.0f,0.25f,0.7f,1.0f,
                0.0f,0.25f,0.7f,1.0f,
                0.0f,0.2f,0.7f,1.0f,
                0.0f,0.2f,0.7f,1.0f,
                0.0f,0.2f,0.7f,1.0f,
        };
        */


}//ends Sphere class