/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructs;

/**
 *
 * @author bickhart
 */
public class gcNormWin {
    private double gc;
    private double expect;
    private int numwins;
    private boolean used = false;
    
    public gcNormWin(double gc){
        this.gc = gc;
        this.expect = 0.0d;
        this.numwins = 0;
    }
    
    public gcNormWin(double gc, double expect){
        this.gc = gc;
        this.expect = expect;
        this.numwins  = 0;
    }
    
    public void setUsed(){
        this.used = true;
    }
    public void setVal(double expect){
        this.expect = expect;
    }
    public void setVal(double expect, int num){
        this.expect = expect;
        this.numwins = num;
    }
    /*
     * getters
     */
    public double getNormVal(){
        return this.expect;
    }
    public double getGC(){
        return this.gc;
    }
    public boolean isUsed(){
        return this.used;
    }
    public int numWins(){
        return numwins;
    }
}
