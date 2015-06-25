package spin.ncsa.org.moleculevr;

/**
 * Created by Radhir on 4/17/15.
 */
import android.graphics.Color;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;


public class TextParser {

    ArrayList<Sphere> m = new ArrayList<>();
    ArrayList<Cylinder> l = new ArrayList<>();

    private Hashtable<String,Integer> colorHashtable = null;
    private Hashtable<String, Float> massHashtable =  null;

    private float bonding_UB[][];
    private float bonding_LB[][];

    //Hashtable elem_number takes a name of element to its atom_number
    private Hashtable<String, Integer> elem_atom_number = null;
    private int num_bonds = 0;

    //isolevel, initialized in loadDensity()
    float primaryLevel;
    float secondaryLevel;

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
        float[] v = new float[Cylinder.NUMBER_OF_COORDS * l.size()]; // we need (# bonc ) * (# coords per bond) floats
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
        float[] c = new float[Cylinder.NUMBER_OF_COLORS * l.size()];// we need (# bond ) * (# colors per bond) floats
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
        float[] n = new float[Cylinder.NUMBER_OF_NORMALS * l.size()];// we need (# bond ) * (# normals per bond) floats
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
        //init
        if (massHashtable != null || elem_atom_number != null)
            return;
        massHashtable = new Hashtable<>();
        elem_atom_number = new Hashtable<>();

        Scanner s = new Scanner(bf);

        //each iteration the parser parses a line
        //I will try a different way to parse the string than the way I did with color, just for fun
        //all error checking are also just practice of usage of scanner
        while(s.hasNext()){
            //read the atomic number
            int a_number = -1;
            if (s.hasNextInt())
                a_number = s.nextInt();
            //read atomic symbol
            String element = null ;
            if (s.hasNext())
                element = s.next();
            //fill in element_atom_number hashtable
            elem_atom_number.put(element,a_number);
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

    }

    public float[][][] loadDensity(BufferedReader bf){
        Scanner s = null;
        s = new Scanner(bf);
        int a = s.nextInt();
        int b = s.nextInt();
        int c = s.nextInt();
        primaryLevel = Float.parseFloat(s.next());
        secondaryLevel = Float.parseFloat(s.next());
        s.next();
/*
        float ret[][][] = new float[a][b][c];

        for (int i = 0; i < a; i++)
            for (int j = 0; j < b; j++)
                for (int k = 0; k < c; k++) {
                    //sometimes data get lost, so it should provide dummy data if such missing happens
                    //however, this way to solve the problem might not be correct, since it is drawing different geometry than the real one
                    //not sure how to perfectly solve the problem
                    if (s.hasNext())
                        ret[i][j][k] = Float.parseFloat(s.next());
                    else
                        ret[i][j][k] = 0.0f;
                }
*/
        //among abc, bac, acb, bca, cab, cba
        //bca and cba are optimal

        float ret[][][] = new float[b][c][a];

        for (int i = 0; i < a; i++)
            for (int j = 0; j < b; j++)
                for (int k = 0; k < c; k++) {
                    //sometimes data get lost, so it should provide dummy data if such missing happens
                    //however, this way to solve the problem might not be correct, since it is drawing different geometry than the real one
                    //not sure how to perfectly solve the problem
                    if (s.hasNext())
                        ret[j][k][i] = Float.parseFloat(s.next());
                    else
                        ret[j][k][i] = 0.0f;
                }

        s.close();
        return ret;
    }

    //bf contains information of atoms' coordinates and bf2 contains information of bonding criteria
    public void parse(BufferedReader bf, BufferedReader bf2) throws IOException {
        Scanner s = null;
        m = null;
        m = new ArrayList<>();

        ArrayList<Float> x_coords = new ArrayList<>();
        ArrayList<Float> y_coords = new ArrayList<>();
        ArrayList<Float> z_coords = new ArrayList<>();
        ArrayList<Float> masses = new ArrayList<>();
        ArrayList<String> elem_names = new ArrayList<>();

        parseBondingInfo(bf2);

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
                        float xCoord = Float.parseFloat(line.substring(20,28));
                        float yCoord = Float.parseFloat(line.substring(31,39));
                        float zCoord = Float.parseFloat(line.substring(42,50));
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

        //make a copy of x,y,z coordinates
        //we need these value when formBonds() is called
        float[] old_x_coords = new float[x_coords.size()];
        float[] old_y_coords = new float[y_coords.size()];
        float[] old_z_coords = new float[z_coords.size()];
        for (int i = 0; i < x_coords.size(); i++){
            old_x_coords[i] = x_coords.get(i);
            old_y_coords[i] = y_coords.get(i);
            old_z_coords[i] = z_coords.get(i);
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

        formBonds(old_x_coords,old_y_coords,old_z_coords);
    }

    /**What parseBondingInfo does is to
     *construct a adjacency matrix to record the lowerbounds and upperbounds for two types of element to form a bond in the molecule
     * If two atoms are in such distance, we bond them together in formBonds()
     */
     private void parseBondingInfo(BufferedReader bf){

         //init these two matrices: the table of this app supports element up tp atomic number 114
         bonding_UB = null;
         bonding_LB = null;
         bonding_LB = new float[114][];
         bonding_UB = new float[114][];
         for (int i = 0; i < 114; i++){
             bonding_LB[i] = new float[114];
             bonding_UB[i] = new float[114];
             for (int j = 0; j < 114; j++){
                 bonding_LB[i][j] = bonding_UB[i][j] = -1;
             }
         }

        //parse info file
         Scanner s = null;
         try {
              s = new Scanner(bf);

             while (s.hasNext()) {
                 String elem1 = s.next();
                 String elem2 = s.next();

                 float lb = s.nextFloat();
                 float ub = s.nextFloat();

                 bonding_LB[elem_atom_number.get(elem1)][elem_atom_number.get(elem2)] =
                         bonding_LB[elem_atom_number.get(elem2)][elem_atom_number.get(elem1)] = lb;
                 bonding_UB[elem_atom_number.get(elem1)][elem_atom_number.get(elem2)] =
                         bonding_UB[elem_atom_number.get(elem2)][elem_atom_number.get(elem1)] = ub;
             }
         }
         catch (Exception e){
             Log.e(TAG,"bonding information is invalid/corrupted");
         }
         finally {
             if (s != null)
                s.close();
         }


     }

   /*Arguments to formBonds are unnormalized coordinates of center of atoms.
     We need these data because our lowerbound and upperbound data are specified in the same way.
     We have to compare distance between each pair of atoms with the lowerbound and upperbound to determine
     if that bond is drawn.
     However, the data we feed Cylinder's constructor should be normalized coordinates contained in arraylist m
     because only normalized are in the range for rendering
   */
    private void formBonds(float[] xc, float[] yc, float[] zc){

        //memory clean up
        if (l != null){
            l = null;
            l = new ArrayList<>();
        }
        num_bonds = 0;

       for (int i = 0; i < m.size(); i++){
           for (int j = i; j < m.size(); j++){
                Sphere a = m.get(i);
                Sphere b = m.get(j);

                    float lb = bonding_LB[ elem_atom_number.get(a.elem_name) ][elem_atom_number.get(b.elem_name) ];
                    float ub = bonding_UB[elem_atom_number.get(a.elem_name)][elem_atom_number.get(b.elem_name)];

                    float[] a_coord = new float[3];
                    float[] b_coord = new float[3];

                    a_coord[0] = xc[i];
                    a_coord[1] = yc[i];
                    a_coord[2] = zc[i];
                    b_coord[0] = xc[j];
                    b_coord[1] = yc[j];
                    b_coord[2] = zc[j];

                    float newDist = util.EuclidDistance(a_coord,b_coord);
                    if ( (Float.compare(newDist,lb) > 0) && (Float.compare(newDist,ub) <= 0) ) {

                        float[] a_color = new float[3];
                        float[] b_color = new float[3];

                        //need to change coordinates because they have been normalized
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

                        Cylinder stick = new Cylinder(a_coord, b_coord, a_color, b_color);
                        l.add(stick);
                        num_bonds++;
                    }
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
