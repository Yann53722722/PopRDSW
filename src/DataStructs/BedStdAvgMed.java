/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructs;

/**
 *
 * @author bickhart
 */
public class BedStdAvgMed {
    private double avg;
    private double median;
    private double stdev;
    
    public BedStdAvgMed(){
        this.avg = 0.0d;
        this.median = 0.0d;
        this.stdev = 0.0d;
    }
    
    public BedStdAvgMed(double a, double m, double s){
        this.avg = a;
        this.median = m;
        this.stdev = s;
    }
    
    /*
     * Getters
     */
    public double getAvg(){
        return this.avg;
    }
    public double getMedian(){
        return this.median;
    }
    public double getStdev(){
        return this.stdev;
    }
}
