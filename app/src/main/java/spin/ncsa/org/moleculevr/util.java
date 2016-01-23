
/**
 * Created by Xusheng on 22/05/2015.
 */
package spin.ncsa.org.moleculevr;

public final class util {
    //calculate Euclidean distance between two n-dimensional points
    public static float EuclidDistance(float[] a, float[] b){
        if (a.length != b.length)
        {
            throw new IllegalArgumentException("length mismatches");
        }
        float dist = 0.0f;
        for (int i = 0; i < a.length; i++){
            dist += (a[i] - b[i]) * (a[i]- b[i]);
        }
        dist = (float)Math.sqrt(dist);
        return dist;
    }

    //calculate linear interpolation of origValue in origScale to an equivalent value in newScale
    //subject of conversion is a n-dimensional vector of floats
    public static float[] interpolate(float origValue, float origScaleMax, float origScaleMin,
                                            float[] newScaleMax, float[] newScaleMin){
        float tolerance = 0.000001f;

        //check if arguments provided are valid
        if (newScaleMax.length != newScaleMin.length    )
            throw new IllegalArgumentException("length mismatches");

        if (Math.abs(origValue-origScaleMax) < tolerance || origValue > origScaleMax){
            return newScaleMax;
        }
        else if (Math.abs(origValue-origScaleMin) < tolerance || origValue < origScaleMin){
            return newScaleMin;
        }

        int n = newScaleMax.length;
        float mu;
        float[] ret = new float[n];
        mu = (origValue - origScaleMin) / (origScaleMax - origScaleMin);
        for (int i = 0; i < n; i++){
            ret[i] = newScaleMin[i] + mu * (newScaleMax[i] - newScaleMin[i]);
        }

        return ret;
    }

    //calculate linear interpolation of origValue in origScale to an equivalent value in newScale
    //subject of conversion is a n-dimensional vector of floats
    public static float[] interpolate(float[] origValue, float[] origScaleMax, float[] origScaleMin,
                                            float[] newScaleMax, float[] newScaleMin){
        float tolerance = 0.000001f;

        //check if arguments provided are valid
        int n = origValue.length;
        if (n != origScaleMax.length ||
                n != origScaleMin.length ||
                n != newScaleMax.length ||
                n != newScaleMin.length )
            throw new IllegalArgumentException("length mismatches");
        
        if (EuclidDistance(origValue,origScaleMax) < tolerance){
            return newScaleMax;
        }
        else if (EuclidDistance(origValue,origScaleMin) < tolerance){
            return newScaleMin;
        }
        
        float mu;
        float[] ret = new float[n];
        for (int i = 0; i < n; i++){
            mu = (origValue[i] - origScaleMin[i]) / (origScaleMax[i] - origScaleMin[i]);
            ret[i] = newScaleMin[i] + mu * (newScaleMax[i] - newScaleMin[i]);
        }

        return ret;
    }


    public static float max(float[] values) {
        float max = Character.MIN_VALUE;
        for(float value : values) {
            if(Float.compare(value,max) > 0)
                max = value;
        }
        return max;
    }

    public static float max(float a, float b){
        if (Float.compare(a,b) >0)
            return a;
        else return b;
    }

    public static float min(float[] values) {
        float min = Character.MAX_VALUE;
        for(float value : values) {
            if(Float.compare(value,min) < 0)
                min = value;
        }
        return min;
    }

    public static float min(float a, float b){
        if (Float.compare(a,b) < 0)
            return a;
        else return b;
    }
    /*Six sets of max and min functions defined for all primitives
        Credited to http://pastebin.com/f57ae8d47
        However, there is no such function for float...I still have to write it myself
    * */
    public static char max(char[] values) {
        char max = Character.MIN_VALUE;
        for(char value : values) {
            if(value > max)
                max = value;
        }
        return max;
    }

    public static byte max(byte[] values) {
        byte max = Byte.MIN_VALUE;
        for(byte value : values) {
            if(value > max)
                max = value;
        }
        return max;
    }

    public static short max(short[] values) {
        short max = Short.MIN_VALUE;
        for(short value : values) {
            if(value > max)
                max = value;
        }
        return max;
    }

    public static int max(int[] values) {
        int max = Integer.MIN_VALUE;
        for(int value : values) {
            if(value > max)
                max = value;
        }
        return max;
    }

    public static long max(long[] values) {
        long max = Long.MIN_VALUE;
        for(long value : values) {
            if(value > max)
                max = value;
        }
        return max;
    }

    public static char min(char[] values) {
        char min = Character.MAX_VALUE;
        for(char value : values) {
            if(value < min)
                min = value;
        }
        return min;
    }

    public static byte min(byte[] values) {
        byte min = Byte.MAX_VALUE;
        for(byte value : values) {
            if(value < min)
                min = value;
        }
        return min;
    }

    public static short min(short[] values) {
        short min = Short.MAX_VALUE;
        for(short value : values) {
            if(value < min)
                min = value;
        }
        return min;
    }

    public static int min(int[] values) {
        int min = Integer.MAX_VALUE;
        for(int value : values) {
            if(value < min)
                min = value;
        }
        return min;
    }

    public static long min(long[] values) {
        long min = Long.MAX_VALUE;
        for(long value : values) {
            if(value < min)
                min = value;
        }
        return min;
    }


}
