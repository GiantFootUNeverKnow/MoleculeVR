package spin.ncsa.org.moleculevr;

/**
 * Created by Xusheng on 31/05/2015.
 */

//This class generates all kinds of temporary functions
    //s is a function for testing Isosurface class
public class s_functions {
    public static float[][][] s(){
        float x_i, y_i, z_i;

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
        return ret;
    }

    public static float[][][] n(){
        float x_i, y_i, z_i;

        float [][][] ret = new float[51][][];

        for (int i = 0;i <= 50;i++){
            ret[i] = new float[51][];
            for (int j = 0; j <= 50; j++){
                ret[i][j] = new float[51];
                for (int k = 0; k <= 50; k++ ){
                    x_i = -0.5f + i * 0.02f;
                    y_i = -0.5f + j * 0.02f;
                    z_i = -0.5f + k * 0.02f;
                    ret[i][j][k] = x_i * x_i / 1.3f + y_i * y_i/1.2f - z_i * z_i/2.0f;
                }
            }
        }
        return ret;
    }

}
