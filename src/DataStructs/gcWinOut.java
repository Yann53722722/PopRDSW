/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructs;

import file.BedAbstract;

/**
 *
 * @author bickhart
 */
public class gcWinOut extends BedAbstract{
    private double hits;
    private double dhits;
    private double gms;
    private boolean shoulduse = true;
    
    public gcWinOut(String chr, int start, int end){
        this.chr = chr;
        this.start = start;
        this.end = end;
    }
    public void setHits(double hits){
        this.hits = hits;
    }
    public void setDHits(double dhits){
        this.dhits = dhits;
    }
    public double getNormHits(){
        return this.hits;
    }
    public void toggleUse(){
        this.shoulduse = false;
    }
    public void setGMS(double gms){
        this.gms = gms;
    }
    @Override
    public int compareTo(BedAbstract t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public String createFormatOutStr(){
        String out = this.chr + "\t" + this.start + "\t" + this.end + "\t" + String.format("%.2f", this.hits) + "\t" + String.format("%.2f", this.dhits) + "\t" + String.format("%.4f", this.gms);
        if(!this.shoulduse){
            out += "\t1";
        }
        out += "\n";
        return out;
    }
}
