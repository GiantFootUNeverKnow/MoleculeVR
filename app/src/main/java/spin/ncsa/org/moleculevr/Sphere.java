package spin.ncsa.org.moleculevr;

/**
 * Created by Radhir on 4/14/15.
 */
import android.util.Log;
public class Sphere
{
    //If degree of refinement is changed, be sure to change NUMBER_OF_FACES to corresponding value, 20 * (4^degree_of_refinement)
    public final static int DEGREE_OF_REFINEMENT = 2;// do only once the refinement
    public final static int NUMBER_OF_FACES = 320; //do only once the refinement, 80 faces

    public final static int NUMBER_OF_VERTICES = NUMBER_OF_FACES * 3; //do only once the refinement, 80 faces, each with 3 vertices
    public final static int NUMBER_OF_COORDS = NUMBER_OF_VERTICES * 3; //do only once the refinement, 320 vertices, each with 3 coords
    public final static int NUMBER_OF_COLORS = NUMBER_OF_VERTICES * 4; //do only once the refinement, 320 vertices, each with 4 colors


    public Sphere(float x, float y, float z, float red, float green, float blue)
    {
        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;

        this.redColor = red;
        this.greenColor = green;
        this.blueColor = blue;

       /* setCoordinates();
        buildSphere();
*/
        buildSphere();
        setCoordinates();
    }

    public static final float X = (float) 0.52573111;
    public static final float Z = (float) 0.85065081;

    public static final float UB = (float) 10.0;

    public int index = 0;
    public float xCoord = 0;
    public float yCoord = 0;
    public float zCoord = 0;

    public float redColor;
    public float greenColor;
    public float blueColor;


    //sphere
    public float[] vertices = new float[NUMBER_OF_COORDS];
    public float[] colors = new float[NUMBER_OF_COLORS];


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

    public void setCoordinates(){//move sphere from origin to the given coordinates
       for (int i = 0; i < NUMBER_OF_COORDS; i+=3){
           vertices[i] = (float)( (vertices[i] + xCoord) / UB );
           vertices[i+1] = (float)( (vertices[i+1] + yCoord) / UB );
           vertices[i+2] = (float)( (vertices[i+2] + zCoord) / UB );
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


    private void buildSphere(){
        //For sphere
        for (int i = 0; i < 20; i++) {
            subdivide(vdata[tindices[i][0]], vdata[tindices[i][1]], vdata[tindices[i][2]],DEGREE_OF_REFINEMENT);
        }

        for (int i = 0; i < NUMBER_OF_FACES; i++) { // for each face, we have 3 vertices, each needs 4 parameter to specify a color
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

    }//ends Sphere class