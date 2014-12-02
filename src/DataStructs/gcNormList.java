/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructs;

import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author bickhart
 */
public class gcNormList {
    private final ConcurrentHashMap<Double, gcNormWin> gcWins;
    
    public gcNormList(){
        this.gcWins = new ConcurrentHashMap<Double, gcNormWin>();
        initializeVals();
    }
    
    public double normVal(float gc){
        double gcd = roundDbl(gc);
        if(this.gcWins.containsKey(gcd)){
            return this.gcWins.get(gcd).getNormVal();
        }else{
            System.out.println("Error! " + gc + " " + gcd);
            return -0.123d;
        }
    }
    public double normVal(double gc){
        double gcd = roundDbl(gc);
        if(this.gcWins.containsKey(gcd)){
            return this.gcWins.get(gcd).getNormVal();
        }else{
            System.out.println("Error! " + gc + " " + gcd);
            return -0.123d;
        }
    }
    private void initializeVals(){
        for(double x = 0.000d; x < 1.001d; x += 0.001d){
            double gcd = roundDbl(x);
            this.gcWins.put(gcd, new gcNormWin(gcd));
        }
    }
    public void setVal(double gc, double val){
        if(this.gcWins.containsKey(gc)){
            this.gcWins.get(gc).setVal(val);
            this.gcWins.get(gc).setUsed();
        }else{
            System.out.println("[gcNormList] accessing uninitialized value! " + gc);
        }
    }
    public void setVal(double gc, double val, int num){
        if(this.gcWins.containsKey(gc)){
            this.gcWins.get(gc).setVal(val, num);
            this.gcWins.get(gc).setUsed();
        }else{
            System.out.println("[gcNormList] accessing uninitialized value! " + gc);
        }
    }
    public boolean isUsed(double gc){
        if(this.gcWins.containsKey(gc)){
            return this.gcWins.get(gc).isUsed();
        }else{
            return false;
        }
    }
    public static double roundDbl(float gc){ 
        return (double) Math.round(gc * 1000) / 1000;
    }
    public static double roundDbl(double gc){
        return (double) Math.round(gc * 1000) / 1000;
    }
    public gcNormWin getValue(double gc){
        double gcd = roundDbl(gc);
        if(this.gcWins.containsKey(gcd)){
            return this.gcWins.get(gcd);
        }else{
            return new gcNormWin(gcd, -0.123);
        }
    }
    public int getNumWins(double gc){
        double gcd = roundDbl(gc);
        if(this.gcWins.containsKey(gcd)){
            return this.gcWins.get(gcd).numWins();
        }else{
            return 0;
        }
    }
}
