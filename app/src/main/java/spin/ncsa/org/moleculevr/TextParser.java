package spin.ncsa.org.moleculevr;

/**
 * Created by Radhir on 4/17/15.
 */
import android.graphics.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;


public class TextParser {

    ArrayList<Sphere> m = new ArrayList<>();
    ArrayList<Cylinder> l = new ArrayList<>();

    Hashtable<String,Integer> colorHashtable = null;
    Hashtable<String, Float> massHashtable =  null;

    Hashtable<Sphere,ArrayList<Sphere>> NearestNeighbor = null;

    int num_bonds;

    private static final float bonding_tolerance = 0.03f;

    //A TAG for debugging display messages
    static final String TAG = "TextParser";

    //output coordinates of atoms
    public float[] outputVertices(){
        if (m==null || m.isEmpty()) return null;
        float[] v = new float[Sphere.NUMBER_OF_COORDS * m.size()]; // we need (# atom ) * (# coords per atom) floats
        int i = 0;
        for (Sphere atom : m){
            for  (float k :atom.vertices){
                v[i] = k;
                i++;
            }
        }
        return v;
    }

    public float[] outputNormals(){
        if (m==null || m.isEmpty()) return null;
        float[] n = new float[Sphere.NUMBER_OF_NORMALS * m.size()]; // we need (# of atoms) * (# of normals per atom) floats
        int i= 0;
        for (Sphere atom: m){
            for (float k : atom.normals){
                n[i] = k;
                i++;
            }
        }
        return n;
    }

    //output colors of atoms
    public float[] outputColors(){
        if (m == null || m.isEmpty()) return null;
        float[] c = new float[Sphere.NUMBER_OF_COLORS * m.size()];// we need (# atom ) * (# colors per atom) floats
        int i = 0;
        for (Sphere atom : m){
            for  (float k :atom.colors){
                c[i] = k;
                i++;
            }
        }
        return c;
    }

    //return bonds stored in Nearest Neighbors in forms of coordinates
    public float[] outputBonds(){
        if (l == null || l.isEmpty()) return null;
        float[] v = new float[Cylinder.NUMBER_OF_COORDS * l.size()]; // we need (# atom ) * (# coords per atom) float
        int i = 0;
        for (Cylinder bond : l){
            for  (float k :bond.vertices){
                v[i] = k;
                i++;
            }
        }
        return v;
    }


    //return colors of bonds
    public float[] outputBondingColors(){
        if (l == null || l.isEmpty()) return null;
        float[] c = new float[Cylinder.NUMBER_OF_COLORS * l.size()];// we need (# atom ) * (# colors per atom) float
        int i = 0;
        for (Cylinder bond : l){
            for  (float k :bond.colors){
                c[i] = k;
                i++;
            }
        }
        return c;
    }

    //return normals of bonds
    public float[] outputBondingNormals(){
        if (l == null || l.isEmpty()) return null;
        float[] n = new float[Cylinder.NUMBER_OF_NORMALS * l.size()];// we need (# atom ) * (# normals per atom) float
        int i = 0;
        for (Cylinder bond : l){
            for  (float k :bond.normals){
                n[i] = k;
                i++;
            }
        }
        return n;
    }


    public int outputNumOfAtoms(){
        return m.size();
    }

    public int outputNumOfBonds() { return num_bonds; }

    public void loadAtomMass(BufferedReader bf){
        if (massHashtable != null)
            return;
        massHashtable = new Hashtable<>();
        Scanner s = new Scanner(bf);

        //each iteration the parser parses a line
        //I will try a different way to parse the string than the way I did with color, just for fun
        //all error checking are also just practice of usage of scanner
        while(s.hasNext()){
            //skip the atomic number
            if (s.hasNextInt())
                s.next();
            //read atomic symbol
            String element = null ;
            if (s.hasNext())
                element = s.next();
            //skip the name of element
            if (s.hasNext())
                s.next();
            //read atom mass
            float f = -1;
            if (s.hasNextDouble())
                f = (float)s.nextDouble();
            //add a hash
            if (f!=-1 && element != null)
                massHashtable.put(element,f);
        }

        s.close();
    }

    public void loadColor(BufferedReader bf){
        if (colorHashtable != null)
            return;
        colorHashtable = new Hashtable<>();
        Scanner s = new Scanner(bf);

        while(s.hasNext()) {
            String line = s.nextLine();
            boolean ifComments = line.contains("//");
            if (ifComments)
                continue;

            Scanner ss = new Scanner(line);
            String element = ss.next();
            int R = ss.nextInt();
            int G = ss.nextInt();
            int B = ss.nextInt();
            int RGB = Color.rgb(R, G, B);
            colorHashtable.put(element, RGB);

        }

        s.close();

        //debugging
//       Enumeration<String> keys = colorHashtable.keys();
//      while(keys.hasMoreElements()) {
//         String str = (String) keys.nextElement();
//         int RGB = colorHashtable.get(str);
//         Log.i(TAG,str + ": " +
//                Color.red(RGB) +" " + Color.green(RGB)+ " " + Color.blue(RGB)         );
//      }
      //
    }

    public void parse(BufferedReader bf) throws IOException {
        Scanner s = null;
        m = null;
        m = new ArrayList<>();

        ArrayList<Float> x_coords = new ArrayList<>();
        ArrayList<Float> y_coords = new ArrayList<>();
        ArrayList<Float> z_coords = new ArrayList<>();
        ArrayList<Float> masses = new ArrayList<>();
        ArrayList<String> elem_names = new ArrayList<>();

        try {
            s = new Scanner(bf);

            boolean reachedCoordinates = false;
            while (s.hasNext())
            {
                String line = s.nextLine();

                //check that parser is reading relevant data
                boolean check = line.contains("x") && line.contains("y") && line.contains("z") && line.contains("Occ.");
                if (check) {
                    reachedCoordinates = true;
                    continue;
                }

                //parser reached the end of relevant data
                boolean reachEnd = line.contains("===");
                if (reachedCoordinates && reachEnd)
                    break;

                if(reachedCoordinates)
                {
                    String temp = line;
                    temp = temp.trim();
                    if(!temp.isEmpty())
                    {
                        //parse the name of element
                        String elementName = line.split("\\s+")[2];
                        //calculate atommass for the atom
                        float _mass = massHashtable.get(elementName );
                        masses.add(_mass);
                        //parse the coordinates of that atom
                        float xCoord = (float)Double.parseDouble(line.substring(20,28));
                        float yCoord = (float)Double.parseDouble(line.substring(31,39));
                        float zCoord = (float)Double.parseDouble(line.substring(42,50));
                        //put coordinates and name of elements into arrays
                        x_coords.add(xCoord);
                        y_coords.add(yCoord);
                        z_coords.add(zCoord);
                        elem_names.add(elementName);
                    }

                }
            }
        } finally {

            //close scanner and end parsing
            if (s != null) {
                s.close();
            }
        }

        //normalize x,y,z coordinates and atommass separately
        //normalize coordinates using option a, normalize
        normalize(x_coords,'a');
        normalize(y_coords,'a');
        normalize(z_coords,'a');
        normalize(masses,'b');
        //create an array of Spheres
        for (int i = 0; i < elem_names.size(); i++){
            //calculate color for the atom
            int _color = colorHashtable.get( elem_names.get(i) );
            float red_color =  ((float)Color.red(_color)/255);
            float green_color =  ((float)Color.green(_color)/255);
            float blue_color =  ((float)Color.blue(_color)/255);
            //CREATE SPHERE OBJECTS IN HERE
            Sphere ball;
            ball = new Sphere(elem_names.get(i),x_coords.get(i),y_coords.get(i),z_coords.get(i),red_color,green_color,blue_color,masses.get(i));
            m.add(ball);
        }

        //generate bonds based on nearest neighbor policy
        createBonds();

        //create an array of cylinder to represent bonds
        formBonds();

    }

    /*
    * Construct a Nearest Neighbors' hash table of a molecule specified in array m , which hashes an atom to its nearest neighbors,
    * Note: this method would erase previous defined NN hashtable
    * Note: if m is an empty array, this method exits immediately
    * */
    private void createBonds(){
        //init
        if (m == null)
            return;
        NearestNeighbor = null;
        NearestNeighbor = new Hashtable<>();
        num_bonds = 0;

        //naive way:iterate all vertices
        for (Sphere a: m){
            float minDist = Float.MAX_VALUE;
            ArrayList<Sphere> neighbors = new ArrayList<>();

            float[] a_coords = new float[3];
            a_coords[0] = a.xCoord;
            a_coords[1] = a.yCoord;
            a_coords[2] = a.zCoord;

            for (Sphere b :m){

                //one's neighbor is not one itself
                if (b.equals(a))
                    continue;

                float[] b_coords = new float[3];
                b_coords[0] = b.xCoord;
                b_coords[1] = b.yCoord;
                b_coords[2] = b.zCoord;

                //bonding_tolerance is defined at the beginning of this class
                float newDist = util.EuclidDistance(a_coords,b_coords);
                //distance graph: ______0__________[3]_____(minDist-tolerance)_____[1]__________minDist_______[2]__________(minDist+tolerance)_____[4]_______
                if (Float.compare(newDist,minDist - bonding_tolerance) <= 0){//[3]
                    neighbors.clear();
                    neighbors.add(b);
                    minDist = newDist;
                }
                else if(Float.compare(newDist,minDist) < 0){ //[1]
                    neighbors.add(b);
                    minDist = (minDist + newDist) / 2;
                }
                else if (Float.compare(newDist,minDist + bonding_tolerance) < 0){//[2]
                    neighbors.add(b);
                }
                //[4] is omitted
            }

            NearestNeighbor.put(a,neighbors);
            num_bonds += neighbors.size();
        }
    }

    private void formBonds(){

        //memory clean up
        if (l != null){
            l = null;
            l = new ArrayList<>();
        }


        for (Sphere a:m){
            ArrayList<Sphere> neighbors = NearestNeighbor.get(a);
            for (Sphere b : neighbors){
                float[] a_coord = new float[3];
                float[] b_coord = new float[3];
                float[] a_color = new float[3];
                float[] b_color = new float[3];

                a_coord[0] = a.xCoord;
                a_coord[1] = a.yCoord;
                a_coord[2] = a.zCoord;

                b_coord[0] = b.xCoord;
                b_coord[1] = b.yCoord;
                b_coord[2] = b.zCoord;

                a_color[0] = a.redColor;
                a_color[1] = a.greenColor;
                a_color[2] = a.blueColor;

                b_color[0] = b.redColor;
                b_color[1] = b.greenColor;
                b_color[2] = b.blueColor;

                Cylinder stick = new Cylinder(a_coord,b_coord,a_color,b_color);
                l.add(stick);
            }
        }
    }



    //option a.
    //normalize an array of floating points to an array in ranges[-0.8,0.8]
    //Normalization formula: z_i = 1.6 * ( (x_i - min)/(max - min) - 0.5 ), where max&&min are the maximum && minimum of the array
    //option b.
    //normalize an array of floating points to an array in ranges[1,2]
    //Normalization formula: z_i = ( (x_i - min)/(max - min) + 1 ), where max&&min are the maximum && minimum of the array
    //precondition: The type of arr has to be Float
    private void normalize(ArrayList<Float> arr,char opt){
            float max,min;
            max = Float.MIN_VALUE;
            min = Float.MAX_VALUE;
            for (float f: arr){
                if (f > max)
                    max = f;
                if (f < min)
                    min = f;
            }
            for (int i = 0; i < arr.size(); i++){
            if (opt == 'a')
                arr.set(i,
                        (float)( 1.6 * ( (arr.get(i) - min)/(max - min) - 0.5) ) );
            else if (opt == 'b')
                arr.set(i,
                        (arr.get(i) - min)/(max - min) + 1);
            }
    }
}
