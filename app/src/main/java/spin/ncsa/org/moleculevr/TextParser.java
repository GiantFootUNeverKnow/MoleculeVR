package spin.ncsa.org.moleculevr;

/**
 * Created by Radhir on 4/17/15.
 */
import android.app.backup.SharedPreferencesBackupHelper;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class TextParser {

    ArrayList<Sphere> m = new ArrayList<Sphere>();

    //a variable used for convenient demo
    int ColorPicker = 0;

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

    public void parse(BufferedReader bf) throws IOException {
        Scanner s = null;
        m = null;
        m = new ArrayList<Sphere>();

        try {
            s = new Scanner(bf);

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
                        Sphere ball;
                        //This selection is used just for demo, it needs to be removed after the proper color setter is coded
                        if (ColorPicker == 0)
                            ball = new Sphere(xCoord,yCoord,zCoord,0.8f,0.1f,0.3f);//Color should be replaced by fixed color of ann element
                        else
                            ball = new Sphere(xCoord,yCoord,zCoord,0.7f,0.1f,0.9f);//Color should be replaced by fixed color of ann element
                        m.add(ball);
                    }

                }
            }
        } finally {
            //This line needs to be removed once the proper color setter is coded.
            ColorPicker++;

            if (s != null) {
                s.close();
            }
        }
    }
}
