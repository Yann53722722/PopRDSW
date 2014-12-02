/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructs;

import file.BedAbstract;
import file.BedMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import utils.BinBed;

/**
 *
 * @author bickhart
 */
public class GcMapTemp extends BedMap{
    private HashMap<String, GcWinTempFile> prenormFiles = new HashMap<>();
    private int numlines;
    private GcTempWin currentWin;
    
    /*
     * Setters
     */    
    public void addTempFiles(HashMap<String, GcWinTempFile> files){
        this.prenormFiles = files;
    }
    
    
    /*
     * Getters
     */
    public GcWinTempFile getTempFile(String animal){
        return this.prenormFiles.get(animal);
    }
    public HashMap<String, GcWinTempFile> getTempFileList(){
        return this.prenormFiles;
    }
    /*
     * Regular methods
     */
    public void dumpToTempFile(String animal, String outprefix){
        this.prenormFiles.put(animal, new GcWinTempFile(outprefix, animal));
        this.prenormFiles.get(animal).dumpFileToDisk(this);
        this.prenormFiles.get(animal).debugWriter(this, animal + "." + outprefix);
    }
    public void calcStdevWins(ConcurrentHashMap<String, ArrayList<BedStdAvgMed>> stats, int num){
        Set<String> animals = this.prenormFiles.keySet();
        String[] ts = new String[animals.size()];
        animals.toArray(ts);
        this.numlines = this.prenormFiles.get(ts[0]).getLines();
        
        while(true){
            ArrayList<Double> vals = new ArrayList<>();
            String line;
            String[] segs = null;
            boolean stop = false;
            for(String a : animals){
                line = this.prenormFiles.get(a).readSequentialLines();
                if(line == null){
                    stop = true;
                    break;
                }
                segs = line.split("\t");
                double avg = stats.get(a).get(num).getAvg();
                double val = Double.valueOf(segs[4]);
                double gms = Double.valueOf(segs[6]);
                if(val <= avg / 5.0d || val >= 5.0d * avg || gms < 0.75){
                    this.setCtrlUse(segs[0], Integer.valueOf(segs[1]), Integer.valueOf(segs[2]));
                }
                vals.add(val);
            }
            if(stop){
                break;
            }
            this.setStdev(segs[0], Integer.valueOf(segs[1]), Integer.valueOf(segs[2]), StatUtils.StdevAvg.stdevDBL(vals));
        }
        
        for(String a : animals){
            this.prenormFiles.get(a).rewind();
        }
    }
    
    public ArrayList<Double> normalizeLowess(double avg, gcNormList gcnorm, BufferedWriter outfile, String animal, boolean isCtrl){
        ArrayList<Double> vals = new ArrayList<>();
        this.numlines = this.prenormFiles.get(animal).getLines();
        
        String line;
        while((line = this.prenormFiles.get(animal).readSequentialLines()) != null){
            String[] segs = line.split("\t");
            double hits = Double.valueOf(segs[4]);
            double nvalue = gcnorm.normVal(Float.valueOf(segs[3]));
            //TODO: I am going to hard code male normalization of the X chromosome, but I need to change this in the future
            double norm = 0.0d;
            if(segs[0].equals("chrX")){
                nvalue /= 2;
                norm = hits - (nvalue - avg);
            }else{
                norm = hits - (nvalue - avg);
            }
            if(norm < 0){
                norm = 0.0d;
            }
            double dnorm = Double.valueOf(segs[5]) - (nvalue - avg);
            if(dnorm < 0){
                dnorm = 0;
            }
            boolean add = false;
            if(this.containsChr(segs[0])){
                add = this.normValSet(segs[0], Integer.valueOf(segs[1]), Integer.valueOf(segs[2]), norm, Double.valueOf(segs[6]), dnorm, isCtrl);
            }
            if(isCtrl && this.containsChr(segs[0]) && add){
                vals.add(norm);
            }
        }            
        
        ArrayList<String> chrs = utils.SortByChr.ascendingChr(this.getListChrs());
        for(String c : chrs){
            for(BedAbstract b : this.getSortedBedAbstractList(c)){
                GcTempWin bed = (GcTempWin) b;
                if(isCtrl && bed.isCTRL()){
                    try{
                        outfile.write(bed.createFormatOutStr());
                    }catch(IOException ex){
                        ex.printStackTrace();
                    }
                }else if(!isCtrl){
                    try{
                        outfile.write(bed.createFormatOutStr());
                    }catch(IOException ex){
                        ex.printStackTrace();
                    }
                }
            }            
        }
        this.prenormFiles.get(animal).rewind();
        return vals;
    }
    
    public void loadAnimalValues(String animal){
        valFullLoader(this.prenormFiles.get(animal));
    }
    
    private void valFullLoader(GcWinTempFile file){
        String line;
        while((line = file.readSequentialLines()) != null){
            String[] segs = line.split("\t");
            intersectVals(segs[0], Integer.valueOf(segs[1]), Integer.valueOf(segs[2]), Double.valueOf(segs[4]), Integer.valueOf(segs[5]), Double.valueOf(segs[6]));
            
        }
        file.rewind();
    }
    public void calcInitialGMS(){
        for(String chr : this.getListChrs()){
            for(int bin : this.getBins(chr)){
                for(BedAbstract b : this.getBedAbstractList(chr, bin)){
                    GcTempWin bed = (GcTempWin) b;
                    bed.calcGMS();
                }
            }
        }
    }
    
    public void intersectVals(String chr, int start, int end, double hits, int dhits, double gms){
        if(this.containsChr(chr)){
            Set<Integer> bins = BinBed.getBins(start, end);
            for(int bin : bins){
                if(this.containsBin(chr, bin)){
                    ArrayList<BedAbstract> list = this.getBedAbstractList(chr, bin);
                    for(int x = 0; x < list.size(); x++){
                        GcTempWin bed = (GcTempWin) list.get(x);
                        if(bed.Start() == start){
                            bed.setHits(hits);
                            bed.setDhits(dhits);
                            bed.setGMS(gms);
                            break;
                        }
                    }
                }
            }
        }
    }
    public void setCtrlUse(String chr, int start, int end){
        if(this.containsChr(chr)){
            Set<Integer> bins = BinBed.getBins(start, end);
            for(int bin : bins){
                if(this.containsBin(chr, bin)){
                    ArrayList<BedAbstract> list = this.getBedAbstractList(chr, bin);
                    for(int x = 0; x < list.size(); x++){
                        GcTempWin bed = (GcTempWin) list.get(x);
                        if(bed.Start() == start){
                            bed.toggleUseCTRL();
                            break;
                        }
                    }
                }
            }
        }
    }
    private boolean normValSet(String chr, int start, int end, double norm, double gms, double dhits, boolean isCtrl){
        if(this.containsChr(chr)){
            Set<Integer> bins = BinBed.getBins(start, end);
            for(int bin : bins){
                if(this.containsBin(chr, bin)){
                    ArrayList<BedAbstract> list = this.getBedAbstractList(chr, bin);
                    for(int x = 0; x < list.size(); x++){
                        GcTempWin bed = (GcTempWin) list.get(x);
                        if(bed.Start() == start){
                            if(bed.isCTRL() && isCtrl){
                                bed.setNormHits(norm);
                                bed.setNormDhits(dhits);
                                bed.setGMS(gms);
                                return true;
                            }else if(!bed.isCTRL() && !isCtrl){
                                bed.setNormHits(norm);
                                bed.setNormDhits(dhits);
                                bed.setGMS(gms);
                                return false;
                            }
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }
    private void setStdev(String chr, int start, int end, double stdev){
        if(chr == null){
            System.out.println("[GcMapTemp] Something wrong with input in setStdev!");
        }
        if(this.containsChr(chr)){
            Set<Integer> bins = BinBed.getBins(start, end);
            for(int bin : bins){
                if(this.containsBin(chr, bin)){
                    ArrayList<BedAbstract> list = this.getBedAbstractList(chr, bin);
                    for(int x = 0; x < list.size(); x++){
                        GcTempWin bed = (GcTempWin) list.get(x);
                        if(bed.Start() == start){
                            bed.setStdev(stdev);
                            break;
                        }
                    }
                }
            }
        }
    }
    public void resetWins(){
        for(String chr : this.bedFile.keySet()){
            for(int bin : this.bedFile.get(chr).keySet()){
                for(BedAbstract b : this.getBedAbstractList(chr, bin)){
                    GcTempWin bed = (GcTempWin) b;
                    bed.resetDHits();
                    bed.resetHits();
                    bed.resetGMS();
                }
            }
        }
    }
}
