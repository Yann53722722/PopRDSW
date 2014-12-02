/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructs;

import StatUtils.AtomicDouble;
import file.BedAbstract;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author bickhart
 */
public class GcTempWin extends BedAbstract{
    private final double gc;
    private double stdev;
    private AtomicDouble hits = new AtomicDouble();
    private AtomicInteger dhits = new AtomicInteger();
    
    private double normhits;
    private double normDhits;
    private double gmsratio;
    private boolean isCtrl = false;
    private boolean useforCtrl = true;
    private boolean normprobs = false;
    
    public GcTempWin(String chr, int start, int end, double gc){
        this.chr = chr;
        this.start = start;
        this.end = end;
        this.gc = gc;
    }
    
    public GcTempWin(String[] segs){
        this.chr = segs[0];
        this.start = Integer.valueOf(segs[1]);
        this.end = Integer.valueOf(segs[2]);
        this.gc = Float.valueOf(segs[3]);
        this.hits = new AtomicDouble(Double.valueOf(segs[4]));
        this.dhits = new AtomicInteger(Integer.valueOf(segs[5]));
        
        double d = this.hits.doubleValue();
        int h = this.dhits.intValue();
        this.gmsratio = d / (double) h;
    }
    
    /*
     * Incrementors
     */
    public void incHits(double val){
        if(this.hits == null){
            hits = new AtomicDouble(val);
            dhits = new AtomicInteger(1);
        }else{
            hits.addAndGet(val);
            dhits.getAndIncrement();
        }
    }
    /*
     * Setters
     */
    public void setHits(double val){
        this.hits.getAndSet(val);
    }
    public void setGMS(double hits, int val){
        if(val == 0){
            this.gmsratio = 0.0d;
        }else{
            this.gmsratio = hits / (double)val;
        }
    }
    public void setGMS(double hits, double dhits){
        if(dhits == 0){
            this.gmsratio = 0.0d;
        }else{
            this.gmsratio = hits / dhits;
        }
    }
    public void setGMS(double gms){
        this.gmsratio = gms;
    }
    public void setStdev(double stdev){
        this.stdev = stdev;
    }
    public void toggleCTRL(){
        this.isCtrl = true;
    }
    public void toggleUseCTRL(){
        this.useforCtrl = false;
    }
    public void toggleNormProbs(){
        this.normprobs = true;
    }
    public void setNormHits(double val){
        this.normhits = val;
    }
    public void setDhits(int val){
        this.dhits.getAndSet(val);
    }
    public void setNormDhits(double val){
        this.normDhits = val;
    }
    /*
     * Getters
     */
    public double getGC(){
        return this.gc;
    }
    public double getHits(){
        return this.hits.doubleValue();
    }
    public int getDHits(){
        return this.dhits.intValue();
    }
    public double getNormHits(){
        return this.normhits;
    }
    public double getGMSRatio(){
        return this.gmsratio;
    }
    public double getStdev(){
        return this.stdev;
    }
    public boolean isCTRL(){
        return this.isCtrl;
    }
    public boolean useCTRL(){
        return this.useforCtrl;
    }
    public String createFormatOutStr(){
        String out = this.chr + "\t" + this.start + "\t" + this.end + "\t" + String.format("%.2f", this.normhits) + "\t" + String.format("%.2f", this.normDhits) + "\t" + String.format("%.4f", this.gmsratio);
        if(this.normprobs){
            out += "\t1";
        }
        out += "\n";
        return out;
    }
    /*
     * Resetting
     */
    public void resetHits(){
        this.hits.set(0.0d);
    }
    public void resetDHits(){
        this.dhits.set(0);
    }
    public void resetNormHits(){
        this.normhits = 0.0d;
    }
    public void resetGMS(){
        this.gmsratio = 0.0d;
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
    
    public void calcGMS(){
        if(this.dhits.intValue() != 0){
            this.gmsratio = this.hits.doubleValue() / (double)this.dhits.intValue();
        }else{
            this.gmsratio = 0.0d;
        }
    }
}
