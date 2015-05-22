package spin.ncsa.org.moleculevr;

/**
 * Created by Xusheng on 22/05/2015.
 */
public final class util {
    public final static float EuclidDistance(float[] a, float[] b){
        if (a.length != b.length)
        {
            throw new IllegalArgumentException();
        }
        float dist = 0.0f;
        for (int i = 0; i < a.length; i++){
            dist += (a[i] - b[i]) * (a[i]- b[i]);
        }
        dist = (float)Math.sqrt(dist);
        return dist;
    }
}
