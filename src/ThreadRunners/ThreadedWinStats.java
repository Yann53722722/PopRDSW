/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ThreadRunners;

import DataStructs.BedStdAvgMed;
import DataStructs.GcMapTemp;
import DataStructs.GcTempWin;
import DataStructs.gcNormList;
import StatUtils.Median;
import StatUtils.StdevAvg;
import file.BedAbstract;
import file.BedMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author bickhart
 */
public class ThreadedWinStats implements Runnable{
    protected GcMapTemp windows;
    protected GcMapTemp sexwins;
    protected int num;
    protected BedStdAvgMed autostats;
    protected BedStdAvgMed sexstats;
    protected boolean calcGC = false;
    protected boolean calcSex = false;
    protected HashMap<Double, ArrayList<Double>> autogcvals;
    protected HashMap<Double, ArrayList<Double>> sexgcvals;
    protected gcNormList autogclist;
    protected gcNormList sexgclist;
    protected String animal;
    
    // Used for normal windows
    public ThreadedWinStats(GcMapTemp windows, int num, String animal){
        this.windows = windows;
        this.num = num;
        this.animal = animal;
    }
    
    // Used for control windows
    public ThreadedWinStats(GcMapTemp windows, GcMapTemp sexwins, int num, boolean calcGC, String animal){
        this.windows = windows;
        this.num = num;
        this.calcGC = calcGC;
        this.autogcvals = new HashMap<>(1000);
        this.sexgcvals = new HashMap<>(1000);
        this.autogclist = new gcNormList();
        this.sexgclist = new gcNormList();
        this.sexwins = sexwins;
        this.calcSex = true;
        this.animal = animal;
    }
    
    @Override
    public void run() {
        this.windows.loadAnimalValues(animal);
        ArrayList<Double> autovalues = rotateThroughEntries(this.windows, false);
        ArrayList<Double> sexvalues = null;
        if(calcSex){
            this.sexwins.loadAnimalValues(animal);
            sexvalues = rotateThroughEntries(this.sexwins, true);
        }        
        
        CreateStats(autovalues, true);
        
        double[] sexVals = null;
        if(calcSex){
            sexVals = ListToDblArray(sexvalues);
            CreateStats(sexvalues, false);
        }
        
        if(this.calcGC){
            for(double gc : this.autogcvals.keySet()){      
                ArrayList<Double> tempvals = this.autogcvals.get(gc);
                double gcavg = StdevAvg.convertDblAvg(tempvals);
                if(Double.isNaN(gcavg)){
                    double testval = StdevAvg.stdevDBL(this.autogcvals.get(gc));
                }
                this.autogclist.setVal(gc, gcavg, tempvals.size());
            }
            
            this.autogcvals = null;
            this.sexgcvals = null;
            // distribute values
            distributeGCVals();
            //fillinGCVals();
        }
        
    }
    private void fillinGCVals(){
        
        for(double i = 0.000d; i < 1.000d; i += 0.001d){
            double current = gcNormList.roundDbl(i);
            if(!this.autogclist.isUsed(current)){
                double j = current - 0.001d;
                while(j >= 0.000d && !this.autogclist.isUsed(j)){
                    j -= 0.001d;
                }
                
                if(!this.autogclist.isUsed(j)){
                    j = current + 0.001d;
                    while(j < 1.000d && !this.autogclist.isUsed(j)){
                        j += 0.001d;
                    }
                }
                double test = gcNormList.roundDbl(j);
                this.autogclist.setVal(current, this.autogclist.normVal(test), this.autogclist.getNumWins(test));
            }
        }
    }
    private void distributeGCVals(){
        double lastval;
        
        // Get average number of window values per GC entry
        ArrayList<Integer> winvals = new ArrayList<Integer>();
        for(double i = 0.000d; i < 1.000d; i += 0.001d){
            winvals.add(this.autogclist.getNumWins(i));
        }
        int avgwins = StdevAvg.IntAvg(winvals);
        
        // Distribute to lower values if possible
        for(double i = 0.000d; i < 1.000d; i += 0.001d){
            double current = gcNormList.roundDbl(i);
            if(!this.autogclist.isUsed(current)){
                continue;
            }else if(this.autogclist.getNumWins(i) < avgwins / 5){
                continue;
            }
            lastval = this.autogclist.normVal(current);
            if(Double.isNaN(lastval)){
                System.out.println( current + "is NaN distributevals");
            }
            for(double j = current - 0.001d; j >= 0.000d; j -= 0.001d){
                double test = gcNormList.roundDbl(j);
                if(!this.autogclist.isUsed(test) || this.autogclist.getNumWins(j) < avgwins / 5){
                    this.autogclist.setVal(test, lastval, this.autogclist.getNumWins(i));
                }
            }
        }
        
        // Now get the higher values that were likely missed
        for(double i = 0.998d; i > 0.000d; i -= 0.001d){
            double current = gcNormList.roundDbl(i);
            if(!this.autogclist.isUsed(current)){
                continue;
            }else if(this.autogclist.getNumWins(i) < avgwins / 5){
                continue;
            }
            lastval = this.autogclist.normVal(current);
            if(Double.isNaN(lastval)){
                System.out.println( current + "is NaN distribute vals");
            }
            for(double j = current + 0.001d; j < 1.000d; j += 0.001d){
                double test = gcNormList.roundDbl(j);
                if(!this.autogclist.isUsed(test) || this.autogclist.getNumWins(j) < avgwins / 5){
                    this.autogclist.setVal(test, lastval, this.autogclist.getNumWins(i));
                }
            }
        }
    }
    private void addGCVals(double gc, double val, boolean isSex){
        if(isSex){
            if(!(this.sexgcvals.containsKey(gc))){
                this.sexgcvals.put(gc, new ArrayList<Double>());
            }
            this.sexgcvals.get(gc).add(val);
        }else{
            if(!(this.autogcvals.containsKey(gc))){
                this.autogcvals.put(gc, new ArrayList<Double>());
            }
            this.autogcvals.get(gc).add(val);
        }
    }
    
    public BedStdAvgMed returnAutoStats(){
        return this.autostats;
    }
    
    public BedStdAvgMed returnSexStats(){
        if(this.sexstats == null){
            return new BedStdAvgMed();
        }
        return this.sexstats;
    }
    
    public gcNormList returnNormWins(){
        return this.autogclist;
    }

    private double[] ListToDblArray(ArrayList<Double> values) {
        double[] Vals = new double[values.size()];
        for(int i = 0; i < values.size(); i++){
            Vals[i] = (double) values.get(i);
        }
        return Vals;
    }

    private void CreateStats(ArrayList<Double> values, boolean isAuto) {
        double avg = StdevAvg.convertDblAvg(values);
        double median = Median.DMedian(values);
        double stdev = StdevAvg.stdevDBL(avg, values);
        if(isAuto){
            this.autostats = new BedStdAvgMed(avg, median, stdev);
        }else{
            this.sexstats = new BedStdAvgMed(avg, median, stdev);
        }
    }

    private ArrayList<Double> rotateThroughEntries(BedMap windows, boolean isSex) {
        ArrayList<Double> values = new ArrayList<Double>();
        for(String chr : windows.getListChrs()){
            for(int bin : windows.getBins(chr)){
                for(BedAbstract bed : windows.getBedAbstractList(chr, bin)){
                    GcTempWin work = (GcTempWin) bed;
                    values.add(work.getHits());
                    if(Double.isNaN(work.getHits())){
                        System.out.println(work.toString() + "is NaN theadwinstats");
                    }
                    if(this.calcGC){
                        double gc = gcNormList.roundDbl(work.getGC());                        
                        addGCVals(gc, work.getHits(), isSex);
                    }
                }
            }
        }
        return values;
    }
}
