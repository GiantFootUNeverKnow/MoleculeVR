package spin.ncsa.org.moleculevr;

/**
 * Created by Xusheng on 22/05/2015.
 */
public final class util {
    //calculate Euclidean distance between two n-dimensional points
    public final static float EuclidDistance(float[] a, float[] b){
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
    public final static float[] interpolate(float[] origValue, float[] origScaleMax, float[] origScaleMin,
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

}
