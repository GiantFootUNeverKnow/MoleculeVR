package spin.ncsa.org.moleculevr;

/**
 * Created by Xusheng on 28/10/2015.
 *
 * MoleculeSet is an agglomeration of all information pertaining a molecule.
 */

import java.io.Serializable;

/**
 * MoleculeSet is a set of all precomputed data of a molecule. Instead of dividing each drawables into molecule ,bonding and isosurfaces,
 * moleculeSet represents the whole molecule geometry in one instance.
 * The most important feature is that moleculeSet can be serialized and stored remotely, with no loss of simplicity.
 */
public class MoleculeSet implements Serializable{

    /**
     * Component variables
     */
    public String title;
    public Drawable molecule;
    public Drawable bonding;
    public Drawable isosurface[];
    public int nTriangleInIso[];

    public MoleculeSet(Drawable mc, Drawable bd, Drawable[] isf, int[] nT){
        //maybe need hard copy
        molecule = mc;
        bonding = bd;
        isosurface = isf;
        nTriangleInIso = nT;
    }

}
