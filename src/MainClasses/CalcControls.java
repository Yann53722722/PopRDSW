/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MainClasses;

import DataStructs.BedStdAvgMed;
import DataStructs.Blacklist;
import DataStructs.GcMapTemp;
import DataStructs.GcTempWin;
import DataStructs.gcWinData;
import ThreadRunners.ThreadedWinStats;
import file.BedAbstract;
import file.BedMap;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author bickhart
 */
public class CalcControls {
    protected ConcurrentHashMap<String, ArrayList<BedStdAvgMed>> prelimStats;
    protected ArrayList<String> animals;
    protected GcMapTemp one;
    protected GcMapTemp two;
    protected GcMapTemp three;
    protected int threads;
    
    public CalcControls(GcMapTemp one, GcMapTemp two, GcMapTemp three, int threads, ArrayList<String> animals){
        this.prelimStats = new ConcurrentHashMap<>();
        this.animals = animals;
        this.one = one;
        this.two = two;
        this.three = three;
        this.threads = threads;
    }
    
    public ControlWins partitionControlWins(GcMapTemp data, int num){
        ControlWins control = new ControlWins();
        int total = 0;
        int added = 0;
        
        
            // This Has been changed in this version of the program
            // For starters, I will collect all reads across all of the windows and then:
            // Calculate the stdev (basically, just an excuse to see if the value of the window is above or below a threshold)
            // Only keep windows that have values that are in this region 
            // Afterwards, bad window regions are filtered out by using the genomic blacklist coordinates that I was given.
        data.calcStdevWins(this.prelimStats, num);
        ArrayList<Double> stdevHolder = new ArrayList<>();
        for(String chr : data.getListChrs()){
            for(int bin : data.getBins(chr)){
                for(BedAbstract bed : data.getBedAbstractList(chr, bin)){
                    GcTempWin gc = (GcTempWin) bed;
                    double stdev = gc.getStdev();
                    stdevHolder.add(stdev);
                    total++;
                }
            }
        }
        
        // Determine upper threshold of stdevs for window variance across the population
        // Also use gmsRatio as a criterion for eliminating windows
        //double upper = StatUtils.Median.DUpperQuintile(stdevHolder);
        for(String chr : data.getListChrs()){
            for(int bin : data.getBins(chr)){
                for(BedAbstract bed : data.getBedAbstractList(chr, bin)){
                    GcTempWin work = (GcTempWin) bed;
                    if(work.useCTRL()){
                        work.toggleCTRL();
                        control.addBed(work);
                        added++;
                    }
                }
            }
        }
        
        control.addTempFiles(data.getTempFileList());
        System.out.println("[DOC CTRL]Partitioned " + added + " control windows out of " + total + " for window: " + num);
        return control;
    }
    
    public ControlWins FilterControlWins(GcMapTemp data, Blacklist blist, int num){
        ControlWins control = new ControlWins();
        int total = 0;
        int added = 0;
        
        data.calcStdevWins(prelimStats, num);
        // This will be the filtration protocol that just determines the control wins by filtering out blacklist locations
        for(String chr : data.getListChrs()){
            for(int bin : data.getBins(chr)){
                for(BedAbstract bed : data.getBedAbstractList(chr, bin)){
                    GcTempWin work = (GcTempWin) bed;
                    total++;
                    if(blist.blacklistIntersect(bed) || !work.useCTRL()){
                        continue;
                    }else{
                        work.toggleCTRL();
                        control.addBed(work);
                        added++;
                    }
                }
            }
        }        
        
        control.addTempFiles(data.getTempFileList());
        System.out.println("[DOC CTRL]Partitioned " + added + " control windows out of " + total + " for window: " + num);
        return control;
    }
    
    /*
     * Getters
     */
    public double getPrelimAvg(String animal, int num){
        return this.prelimStats.get(animal).get(num).getAvg();
    }
    public double getPrelimStdev(String animal, int num){
        return this.prelimStats.get(animal).get(num).getStdev();
    }
    public double getPrelimMedian(String animal, int num){
        return this.prelimStats.get(animal).get(num).getMedian();
    }

    public void generateControlStats() {
        for(String a : animals){
            this.prelimStats.put(a, new ArrayList<BedStdAvgMed>());
            ExecutorService executor = Executors.newFixedThreadPool(threads);

            ThreadedWinStats first = new ThreadedWinStats(one, 0, a);
            executor.execute(first);

            ThreadedWinStats second = new ThreadedWinStats(two, 1, a);
            executor.execute(second);

            ThreadedWinStats third = new ThreadedWinStats(three, 2, a);
            executor.execute(third);

            executor.shutdown();
            while(!(executor.isTerminated())){

            }
            prelimStats.get(a).add(first.returnAutoStats());
            prelimStats.get(a).add(second.returnAutoStats());
            prelimStats.get(a).add(third.returnAutoStats()); 
        }
    }
}
