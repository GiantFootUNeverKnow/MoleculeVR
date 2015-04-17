package spin.ncsa.org.moleculevr;

/**
 * Created by Radhir on 4/17/15.
 */
import java.io.*;
import java.util.Scanner;


public class TextParser {
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
