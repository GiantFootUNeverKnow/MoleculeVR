package spin.ncsa.org.moleculevr;

/**
 * Created by Radhir on 4/14/15.
 */
import android.util.Log;
public class Sphere
{
    //If degree of refinement is changed, be sure to change NUMBER_OF_FACES to corresponding value, 20 * (4^degree_of_refinement)
    private final static int DEGREE_OF_REFINEMENT = 2;// do only once the refinement
    private final static int NUMBER_OF_FACES = 320; //do only twice the refinement, 320 faces

    final static int NUMBER_OF_VERTICES = NUMBER_OF_FACES * 3; //do only twice the refinement, 320 faces, each with 3 vertices
    final static int NUMBER_OF_COORDS = NUMBER_OF_VERTICES * 3; //do only twice the refinement, 960 vertices, each with 3 coords
    final static int NUMBER_OF_COLORS = NUMBER_OF_VERTICES * 4; //do only twice the refinement, 960 vertices, each with 4 colors
    final static int NUMBER_OF_NORMALS = NUMBER_OF_COORDS;

    private final static float RESIZING_FACTOR = 20.0f;//resize the ball by factor of 20

    /*Precondition: x,y,z should be in range[-0.8,0.8],
                    red,green,blue should be in range [0,1]
    *               radius should be in range [1,2]
    */
    public Sphere(String name,float x, float y, float z, float red, float green, float blue,float radius)
    {
        //argument checking
        if ( Float.compare(x, 0.8f) > 0 || Float.compare(x, -0.8f) < 0 )
            throw new IllegalArgumentException(" x is out of range ");
        if ( Float.compare(y , 0.8f) > 0 || Float.compare(y,  -0.8f) < 0 )
            throw new IllegalArgumentException("y is out of range ");
        if ( Float.compare(z, 0.8f) > 0 || Float.compare(z,  -0.8f) < 0  )
            throw new IllegalArgumentException("z is out of range ");
        if ( Float.compare(red, 1.0f) > 0 || Float.compare(red , 0.0f) < 0 ||
                Float.compare(green, 1.0f) > 0 || Float.compare(green, 0.0f) < 0 ||
                Float.compare(blue, 1.0f) > 0 || Float.compare(blue , 0.0f) < 0  )
            throw new IllegalArgumentException("Colors out of range ");
        if ( Float.compare(radius, 2.0f)>0 || Float.compare(radius, 1.0f)<0 )
            throw new IllegalArgumentException("Radius is out of range ");

        this.xCoord = x;
        this.yCoord = y;
        this.zCoord = z;

        this.redColor = red;
        this.greenColor = green;
        this.blueColor = blue;

        this.radius = radius;

        this.elem_name = name;

        buildSphere();
        setNormal();
        setCoordinates();
    }

    private static final float X = (float) 0.52573111;
    private static final float Z = (float) 0.85065081;

    //public static final float UB = (float) 15.0;

    private int index = 0;

    public float xCoord = 0;
    public float yCoord = 0;
    public float zCoord = 0;
    public String  elem_name = null;

    public float redColor;
    public float greenColor;
    public float blueColor;

    public float radius;

    //sphere
    public float[] vertices = new float[NUMBER_OF_COORDS];
    public float[] colors = new float[NUMBER_OF_COLORS];
    public float[] normals = new float[NUMBER_OF_NORMALS];

    private float[][] vdata = new float[][]{
            {-X , 0.0f , Z }, {X , 0.0f, Z },
            {-X , 0.0f , -Z }, {X , 0.0f , -Z },
            {0.0f, Z , X }, {0.0f , Z, -X },
            {0.0f , -Z , X }, {0.0f , -Z , -X },
            {Z ,X ,0.0f },{-Z  ,X ,0.0f },{Z ,-X,0.0f},
            {-Z,-X ,0.0f}    };

    private static final int[][] tindices = new int[][]{
            {1,4,0},{4,9,0},{4,5,9},{8,5,4},{1,8,4},
            {1,10,8},{10,3,8},{8,3,5},{3,2,5},{3,7,2},
            {3,10,7},{10,6,7},{6,11,7},{6,0,11},{6,1,0},
            {10,1,6},{11,0,9},{2,11,9},{5,2,9},{11,2,7}
    };

    private void setNormal(){//calculate normals based on vertex's pointing direction
        System.arraycopy(vertices,0,normals,0,NUMBER_OF_COORDS);
    }

    private void setCoordinates(){//move sphere from origin to the given coordinates
       for (int i = 0; i < NUMBER_OF_COORDS; i+=3){
           vertices[i] = ( (vertices[i]  * radius )/RESIZING_FACTOR + xCoord);
           vertices[i+1] = ( (vertices[i+1]  * radius )/RESIZING_FACTOR + yCoord);
           vertices[i+2] = ( (vertices[i+2]  * radius )/RESIZING_FACTOR + zCoord);
       }
    }

    private static void normalize(float v[]){
        float d = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        if (d == 0.0){
            Log.e("Sphere","normalize");
        }
        v[0] /= d;
        v[1] /= d;
        v[2] /= d;
    }

    private void subdivide(float[] v1,float[] v2,float[] v3, long depth){
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
            colors[i * 12] = redColor ;
            colors[i * 12 + 1] = greenColor;
            colors[i * 12 + 2] = blueColor;
            colors[i * 12 + 3] = 1.0f;
            colors[i * 12 + 4] = redColor;
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