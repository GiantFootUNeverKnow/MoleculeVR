package spin.ncsa.org.moleculevr;

/**
 * Created by Xusheng on 31/05/2015.
 */

//This class generates all kinds of temporary functions
    //s is a function for testing Isosurface class
public class s_functions {
    public static final float[][][] s(){
        /*
        float x_min = -0.7f;
        float y_min = -0.7f;
        float z_min = -0.3f;
        float x_max = 0.7f;
        float y_max = 0.7f;
        float z_max = 0.3f;
*/
        float x_i, y_i, z_i;

        //
        float [][][] ret = new float[71][][];


        for (int i = 0;i <= 70;i++){
            ret[i] = new float[71][];
            for (int j = 0; j <= 70; j++){
                ret[i][j] = new float[31];
                for (int k = 0; k <= 30; k++ ){
                    x_i = -0.7f + i * 0.02f;
                    y_i = -0.7f + j * 0.02f;
                    z_i = -0.3f + k * 0.02f;
                    ret[i][j][k] = x_i * x_i + y_i * y_i + z_i * z_i;
                }
            }
        }
        /*
        while(Float.compare(x_i, x_max) <= 0){
            ret[i] = new float[71][];
            while(Float.compare(y_i,y_max) <= 0 ){
                ret[i][j] = new float[31];
                while (Float.compare(z_i,z_max) <= 0){
                    ret[i][j][k] = x_i * x_i + y_i * y_i + z_i * z_i;
                    z_i += 0.02f; k++;
                }
                y_i += 0.02f; j++;
            }
            x_i += 0.02f; i++;
        }
*/

        return ret;
    }

}
