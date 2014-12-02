/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ThreadRunners;

import DataStructs.BedStdAvgMed;
import DataStructs.GcMapTemp;
import DataStructs.GcTempWin;
import DataStructs.gcNormList;
import DataStructs.gcWinData;
import DataStructs.gcWinOut;
import StatUtils.Median;
import StatUtils.StdevAvg;
import file.BedAbstract;
import file.BedMap;
import MainClasses.OutputNormWindows;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 *
 * @author bickhart
 */
public class ThreadedWinNormStats implements Runnable{
    private boolean isControl = false;
    private GcMapTemp data;
    private GcMapTemp sexdata;
    private gcNormList normList;
    private BedStdAvgMed autostats;
    private BedStdAvgMed sexstats;
    private String animal;
    private Path printout;
    
    public ThreadedWinNormStats(GcMapTemp data, gcNormList list, BedStdAvgMed autostats, BedStdAvgMed sexstats, String animal, Path out){
        this.data = data;
        this.normList = list;
        this.autostats = autostats;
        this.sexstats = sexstats;
        this.animal = animal;
        this.printout = out;
    }
    
    public ThreadedWinNormStats(GcMapTemp data, GcMapTemp sexdata, gcNormList list, BedStdAvgMed autostats, BedStdAvgMed sexstats, boolean isCont, String animal, Path out){
        this(data, list, autostats, sexstats, animal, out);
        this.sexdata = sexdata;
        this.isControl = isCont;
    }
    
    @Override
    public void run() {
        try{
            BufferedWriter writer = Files.newBufferedWriter(printout, Charset.forName("UTF-8"));
                
            ArrayList<Double> values = data.normalizeLowess(this.autostats.getAvg(), normList, writer, animal, isControl);
            if(isControl){
                BufferedWriter stats = Files.newBufferedWriter(Paths.get(printout.toString() + ".stats"), Charset.forName("UTF-8"));
                double avg = StdevAvg.DoubleAvg(values);
                double median = Median.DMedian(values);
                double stdev = StdevAvg.stdevDBL(avg, values);
                this.autostats = new BedStdAvgMed(avg, median, stdev); 
                OutputNormWindows.DumpOutStatistics(avg, stdev, median, stats, "auto");
                values.clear();
                RunSexStats(writer, stats);
            }else{
                writer.close();
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    private void RunSexStats(BufferedWriter writer, BufferedWriter stats){
        ArrayList<Double> values = sexdata.normalizeLowess(this.sexstats.getAvg(), normList, writer, animal, isControl);
        double avg = StdevAvg.DoubleAvg(values);
        double median = Median.DMedian(values);
        double stdev = StdevAvg.stdevDBL(avg, values);
        OutputNormWindows.DumpOutStatistics(avg, stdev, median, stats, "sex");
        values.clear();
        this.sexstats = new BedStdAvgMed(avg, median, stdev);
        try{
            writer.close();
            stats.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public BedStdAvgMed getAutoStats(){
        return this.autostats;
    }
    
    public BedStdAvgMed getSexStats(){
        if(this.sexstats == null){
            return new BedStdAvgMed();
        }
        return this.sexstats;
    }
}
