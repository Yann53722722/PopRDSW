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
public class gmsMapChr {
    // This is the construct for GMS (Genome Map Score) windows that will be partitioned within a HashMap of each chromosome
    // I am going to make 50 bp windows and bin them by dividing the start position (zero based) by 50
    // lookup will be just as simple, with division of the start position of the read by 50 and then taking the average of the two windows that intersect the read
    // (if it is not a perfect overlap)
    
    private ConcurrentHashMap<Integer, Double> gms;
    
    public gmsMapChr(){
        this.gms = new ConcurrentHashMap<Integer, Double>();
    }
    
    public void ImportElement(int start, double avg){
        int bin = start / 50;
        this.gms.put(bin, avg);
    }
    public void ImportElement(int bin, double value, boolean issimple){
        this.gms.put(bin, value);
    }
    
    public double RetrieveValue(int start){
        double[] farray;
        double sum = 0.0d;
        int bin = start / 50; //Taking advantage of the fact that the int type drops the decimal values
        if(bin > this.gms.size()){
            System.out.println("[DOC GMC]Error: RetrieveValue call larger than gms chr size: " + bin);
            return 0.0f;
        }
        if(start % 50 != 0){
            farray = new double[2];
            farray[0] = this.gms.get(bin);
            farray[1] = this.gms.get(bin + 1);
            sum = farray[0] + farray[1];
        }else{
            farray = new double[1];
            farray[0] = this.gms.get(bin);
            sum = farray[0];
        }
        
        double avg = sum / farray.length;
        return avg;
    }
}
