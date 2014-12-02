/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package StatUtils;

/**
 *
 * @author bickhart
 */
public class ChromSegregator {
    
    public static boolean isSex(String chr){
        if(chr.equals("chrX") || chr.equals("chrY") || chr.equals("ChrX") || chr.equals("ChrY")){
            return true;
        }
        return false;
    }
}
