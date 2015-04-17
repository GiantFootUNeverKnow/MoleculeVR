package spin.ncsa.org.moleculevr;

/**
 * Created by Radhir on 4/17/15.
 */
import android.app.backup.SharedPreferencesBackupHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;


public class TextParser {

    ArrayList<Sphere> m = new ArrayList<Sphere>();

    public float[] outputVertices(){
        float[] v = new float[2880 * m.size()]; //2880 should be defined as constant of Sphere class
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
        float[] c = new float[3840 * m.size()]; //3840 should be defined as constant of Sphere class
        int i = 0;
        for (Sphere atom : m){
            for  (float k :atom.colors){
                c[i] = k;
                i++;
            }
        }
        return c;
    }

    public void parse() throws IOException {
        Scanner s = null;

        try {
            s = new Scanner(new BufferedReader(new FileReader("molecule.txt")));

            boolean reachedCoordinates = false;
            int lineCount = 0;
            while (s.hasNext())
            {
                String line = s.nextLine();
                boolean check = line.contains("x") && line.contains("y") && line.contains("z");
                lineCount ++;
                if(check && line.contains("Occ.") && lineCount <= 98)
                    reachedCoordinates = true;
                if(lineCount >= 98)
                    reachedCoordinates = false;
                if(reachedCoordinates)
                {
                    String temp = line;
                    temp = temp.trim();
                    if(check || temp.isEmpty())
                        continue;
                    else
                    {
                        float xCoord = (float)Double.parseDouble(line.substring(20,28));
                        float yCoord = (float)Double.parseDouble(line.substring(31,39));
                        float zCoord = (float)Double.parseDouble(line.substring(42,50));
                        //CREATE SPHERE OBJECTS IN HERE
                        Sphere ball = new Sphere(xCoord,yCoord,zCoord,0.8f,0.1f,0.3f);//Color should be replaced by fixed color of ann element
                        m.add(ball);
                    }

                }
            }
        } finally {
            if (s != null) {
                s.close();
            }
        }
    }
}
