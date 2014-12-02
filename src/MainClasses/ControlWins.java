/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MainClasses;

import DataStructs.BedStdAvgMed;
import DataStructs.GcMapTemp;
import DataStructs.GcWinTempFile;
import DataStructs.gcNormList;
import DataStructs.gcNormWin;
import StatUtils.ChromSegregator;
import file.BedAbstract;
import file.BedMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 *
 * @author bickhart
 */
public class ControlWins {
    private GcMapTemp autosomes = new GcMapTemp();
    private GcMapTemp sexchr = new GcMapTemp();
    private HashMap<String, BedStdAvgMed> controlAutoStats =  new HashMap<>();
    private HashMap<String, BedStdAvgMed> controlSexStats =  new HashMap<>();
    private HashMap<String, gcNormList> gcautonorm =  new HashMap<>();
    private HashMap<String, gcNormList> gcsexnorm =  new HashMap<>();
    private HashMap<String, BedStdAvgMed> autoNormStats =  new HashMap<>();
    private HashMap<String, BedStdAvgMed> sexNormStats =  new HashMap<>();
    
    public void ControlWins(){
        
    }
    public void ControlWins(GcMapTemp data){
        for(String chr : data.getListChrs()){
            for(BedAbstract bed : data.getSortedBedAbstractList(chr)){
                if(ChromSegregator.isSex(chr)){
                    this.sexchr.addBedData(chr, bed);
                }else{
                    this.autosomes.addBedData(chr, bed);
                }
            }
        }        
    }
    
    public void addBed(BedAbstract bed){
        if(ChromSegregator.isSex(bed.Chr())){
            this.sexchr.addBedData(bed.Chr(), bed);
        }else{
            this.autosomes.addBedData(bed.Chr(), bed);
        }
    }
    public void addTempFiles(HashMap<String, GcWinTempFile> temps){
        this.autosomes.addTempFiles(temps);
        this.sexchr.addTempFiles(temps);
    }
    public void addAutoStats(BedStdAvgMed stats, String a){
        this.controlAutoStats.put(a, stats);
    }
    public void addSexStats(BedStdAvgMed stats, String a){
        this.controlSexStats.put(a, stats);
    }
    public void addAutoNorm(gcNormList norm, String a){
        this.gcautonorm.put(a, norm);
    }
    public void addSexNorm(gcNormList norm, String a){
        this.gcsexnorm.put(a, norm);
    }
    public void addAutoNormStats(BedStdAvgMed stats, String a){
        this.autoNormStats.put(a, stats);
    }
    public void addSexNormStats(BedStdAvgMed stats, String a){
        this.sexNormStats.put(a, stats);
    }
    /*
     * Getters
     */
    public GcMapTemp getAutoMap(){
        return this.autosomes;
    }
    public GcMapTemp getSexMap(){
        return this.sexchr;
    }
    public gcNormList getAutoNormWins(String a){
        return this.gcautonorm.get(a);
    }
    public gcNormList getSexNormWins(String a){
        return this.gcsexnorm.get(a);
    }
    public BedStdAvgMed getAutoStats(String a){
        return this.controlAutoStats.get(a);
    }
    public BedStdAvgMed getSexStats(String a){
        return this.controlSexStats.get(a);
    }
    public BedStdAvgMed getAutoNormStats(String a){
        return this.autoNormStats.get(a);
    }
    public BedStdAvgMed getSexNormStats(String a){
        return this.sexNormStats.get(a);
    }
    
    public void printOUTGCNormList(String outprefix, File outputdir, String animal){
        Path original = outputdir.toPath();
        Path fControl = original.resolve(animal + outprefix);
        try{
            Charset charset = Charset.forName("UTF-8");
            BufferedWriter write = Files.newBufferedWriter(fControl, charset);
            for(double i = 0.000d; i < 1.000d; i += 0.001d){
                gcNormWin wauto = this.gcautonorm.get(animal).getValue(i);
                //gcNormWin wsex = this.gcsexnorm.getValue(i);
                
                double round = gcNormList.roundDbl(i);
                write.write(round + "\t" + wauto.getNormVal() + "\n");
            }
            write.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
}
