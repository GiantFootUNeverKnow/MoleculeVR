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

    Hashtable colorHashtable = null;
    Hashtable massHashtable =  null;

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

    public int outputNumOfAtoms(){
        return m.size();
    }

    public void loadAtomMass(BufferedReader bf){
        if (massHashtable != null)
            return;
        massHashtable = new Hashtable();
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
        colorHashtable = new Hashtable();
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
                        float _mass = (float)massHashtable.get(elementName );
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
            int _color = (int) colorHashtable.get( elem_names.get(i) );
            float red_color =  ((float)Color.red(_color)/255);
            float green_color =  ((float)Color.green(_color)/255);
            float blue_color =  ((float)Color.blue(_color)/255);
            //CREATE SPHERE OBJECTS IN HERE
            Sphere ball;
            ball = new Sphere(x_coords.get(i),y_coords.get(i),z_coords.get(i),red_color,green_color,blue_color,masses.get(i));
            m.add(ball);
        }
    }

    //option a.
    //normalize an array of floating points to an array in ranges[-0.8,0.8]
    //Normalization formula: z_i = 1.6 * ( (x_i - min)/(max - min) - 0.5 ), where max&&min are the maximum && minimum of the array
    //option b.
    //normalize an array of floating points to an array in ranges[0,1]
    //Normalization formula: z_i = ( (x_i - min)/(max - min)  ), where max&&min are the maximum && minimum of the array
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
                        (arr.get(i) - min)/(max - min));
            }
    }
}
