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

    Hashtable<String,Integer> colorHashtable = null;
    Hashtable<String, Float> massHashtable =  null;

    Hashtable<Sphere,ArrayList<Sphere>> NearestNeighbor = null;

    int num_bonds;

    //A TAG for debugging display messages
    static final String TAG = "TextParser";

    public float[] outputVertices(){
        if (m.isEmpty()) return null;
        float[] v = new float[Sphere.NUMBER_OF_COORDS * m.size()]; // we need (# atom ) * (# coords per atom) float
        int i = 0;
        for (Sphere atom : m){
            for  (float k :atom.vertices){
                v[i] = k;
                i++;
            }
        }
        return v;
    }

    public float[] outputColors(){
        if (m.isEmpty()) return null;
        float[] c = new float[Sphere.NUMBER_OF_COLORS * m.size()];// we need (# atom ) * (# colors per atom) float
        int i = 0;
        for (Sphere atom : m){
            for  (float k :atom.colors){
                c[i] = k;
                i++;
            }
        }
        return c;
    }

    //return colors of bonds
    public float[] outputBondingColors(){
        ArrayList<Float> ret = new ArrayList<>();

        for (Sphere a: m){
            ArrayList<Sphere> neighbors = NearestNeighbor.get(a);
            for (Sphere b : neighbors){
                ret.add(a.redColor);
                ret.add(a.greenColor);
                ret.add(a.blueColor);

                ret.add(b.redColor);
                ret.add(b.greenColor);
                ret.add(b.blueColor);
            }
        }

        float[] floatArray = new float[ret.size()];
        int i = 0;
        for (Float f : ret) {
            floatArray[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }

        return floatArray;
    }

    //return bonds stored in Nearest Neighbors in forms of pairs of vertices
    public float[] outputBonds(){
        ArrayList<Float> ret = new ArrayList<>();

        for (Sphere a: m){
            ArrayList<Sphere> neighbors = NearestNeighbor.get(a);
            for (Sphere b : neighbors){
                ret.add(a.xCoord);
                ret.add(a.yCoord);
                ret.add(a.zCoord);

                ret.add(b.xCoord);
                ret.add(b.yCoord);
                ret.add(b.zCoord);
            }
        }

        float[] floatArray = new float[ret.size()];
        int i = 0;
        for (Float f : ret) {
            floatArray[i++] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }

        return floatArray;
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
        //testing
        System.out.println("Nothing");
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

                //maybe we should think about compare distance with tolerance so that we could have more than one "nearest neighbor"

                //if atom b is nearer than all previous atoms, replace neighbors by an arrayList with b
                if (Float.compare(util.EuclidDistance(a_coords,b_coords) , minDist) < 0 ){
                    neighbors.clear();
                    neighbors.add(b);
                    minDist = util.EuclidDistance(a_coords,b_coords);
                }
                //if atom b is as near as atoms in neighbors, add b into neighbors
                else if (Float.compare(util.EuclidDistance(a_coords,b_coords),0.0f) == 0 ){
                    neighbors.add(b);
                }
            }

            NearestNeighbor.put(a,neighbors);
            num_bonds += neighbors.size();
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
