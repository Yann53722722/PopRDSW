/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructs;

import StatUtils.AtomicDouble;
import file.BedAbstract;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author derek.bickhart
 */
public class gcWinData extends BedAbstract{
    float gc;
    private ConcurrentHashMap<String, AtomicDouble> hits = new ConcurrentHashMap<>(); //GMS score sum
    private ConcurrentHashMap<String, AtomicInteger> dhits = new ConcurrentHashMap<>(); //raw one-based count
    
    private double stdev;  //Normalization for window picking
    
    private ConcurrentHashMap<String, Double> normdhits = new ConcurrentHashMap<>(); //normalized one-based count
    private ConcurrentHashMap<String, Double> normhits = new ConcurrentHashMap<>(); //normalized gms score sum
    private boolean use = true;
    private boolean isCtrl = false;
    
    
    
    
    public gcWinData (String chr, int start, int end, float gc){
        this.chr = chr;
        this.start = start;
        this.end = end;
        this.gc = gc;
    }
    public void incHits(String animal, double val){
        if(!hits.containsKey(animal)){
            hits.put(animal, new AtomicDouble());
        }
        hits.get(animal).getAndAdd(val);
        if(!dhits.containsKey(animal)){
            dhits.put(animal, new AtomicInteger());
        }
        dhits.get(animal).getAndIncrement();
    }
    public void normalizeLowess(double avg, double value, String animal){
        double temphits = this.hits.get(animal).doubleValue() - (value - avg);
        double tempint = this.dhits.get(animal).doubleValue() - (value - avg);
        if(temphits >= 0.0d){
            this.normhits.put(animal, temphits);
            this.normdhits.put(animal, tempint);
        }else{
            if(dhits.get(animal).get() > 0){
                //System.out.println("[DOC DEBUG]NormalizeLowess, original value: " + hits.floatValue() + " average win value: " + avg + " norm val: " + value + " nongms hits: " + dhits.get());
                // Window did not correspond to normalization expectations
                this.use = false;
            }
            this.normhits.put(animal, 0.0d);
            this.normdhits.put(animal, 0.0d);
        }
      
    }
    public gcWinOut normalizeLowess(double avg, double value, String animal, boolean yes){
        gcWinOut output = new gcWinOut(chr, start, end);
        double temphits = this.hits.get(animal).doubleValue() - (value - avg);
        double tempint = this.dhits.get(animal).doubleValue() - (value - avg);
        if(temphits < 0.0d){
            output.toggleUse();
            output.setHits(0.0d);
            output.setDHits(0.0d);
            output.setGMS(this.getGMSRatio(animal));
        }else{
            output.setHits(temphits);
            output.setDHits(tempint);
            output.setGMS(this.getGMSRatio(animal));
        }
        return output;
    }
    public void CtrlStdev(double stdev){
        this.stdev = stdev;
    }
    @Override
    public int compareTo(BedAbstract t) {
        if(this.start > t.Start()){
            return 1;
        }else if(this.start == t.Start()){
            return 0;
        }else{
            return -1;
        }
    }
    
    /*
     * Setters
     */
    
    public void setHits(String animal, double val){
        this.hits.put(animal, new AtomicDouble());
        this.hits.get(animal).getAndSet(val);
    }
    
    public void setDHits(String animal, int val){
        this.dhits.put(animal, new AtomicInteger());
        this.dhits.get(animal).getAndSet(val);
    }
    
    /*
     * Switches
     */
    public boolean shouldUse(){
        return this.use;
    }
    public void toggleCtrl(){
        this.isCtrl = true;
    }
    public boolean isCtrl(){
        return this.isCtrl;
    }
    public void toggleUse(){
        this.use = false;
    }
    
    /*
     * Getters
     */
    public String getChr(){
        return super.chr;
    }
    public int getStart(){
        return super.start;
    }
    public int getEnd(){
        return super.end;
    }
    public float getGC(){
        return this.gc;
    }
    public double getHits(String animal){
        if(hits.containsKey(animal))
            return hits.get(animal).get();
        else
            return 0.0f;
    }
    public double getNormHits(String animal){
        return this.normhits.get(animal);
    }
    public double getNormDHits(String animal){
        return this.normdhits.get(animal);
    }
    public int getDebugCount(String animal){
        if(dhits.containsKey(animal))
            return this.dhits.get(animal).get();
        else
            return 0;
    }
    public double getGMSRatio(String animal){
        if(this.hits.containsKey(animal))
            return this.hits.get(animal).doubleValue() / (double) this.dhits.get(animal).get();
        else
            return 0.0d;
    }
    public double getNormPreHitCount(){
        return this.stdev;
    }
}
