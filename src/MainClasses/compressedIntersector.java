/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MainClasses;

import DataStructs.GcMapTemp;
import DataStructs.GcTempWin;
import DataStructs.gmsData;
import file.BedAbstract;
import gziputils.ReaderReturn;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 *
 * @author bickhart
 */
public class compressedIntersector implements Runnable{
    private File inFile;
    private GcMapTemp one;
    private GcMapTemp two;
    private GcMapTemp three;
    private FastaFile fas;
    private String animal;
    
    public compressedIntersector(String infile, FastaFile fas, String animal){
        this.inFile = new File(infile);
        this.one = fas.getWins(1);
        this.two = fas.getWins(2);
        this.three = fas.getWins(3);
        this.animal = animal;
        this.fas = fas;
    }

    @Override
    public void run() {
        try(BufferedReader input = ReaderReturn.openFile(inFile)){
            String line, prevChr = "0";
            ArrayList<ArrayList<BedAbstract>> wins = new ArrayList<>();
            ArrayList<Integer> winIt = new ArrayList<>();
            
            // Define array structure
            wins.add(new ArrayList<BedAbstract>());
            wins.add(new ArrayList<BedAbstract>());
            wins.add(new ArrayList<BedAbstract>());
            
            winIt.add(0);
            winIt.add(0);
            winIt.add(0);
            
            while((line = input.readLine()) != null){
                line = line.trim();
                String[] segs = line.split("\t");
                if(segs.length < 3){
                    continue;
                }
                
                double gms = Double.valueOf(segs[2]);
                String samChr = "chr" + segs[0];
                int samStart = Integer.valueOf(segs[1]);
                
                //Conditionals to determine action
                if(!samChr.equals(prevChr)){
                    System.out.print("Working on " + animal + " " + samChr + "\r");
                    prevChr = samChr;
                    
                    wins.set(0, windowRetrieval(samChr, one));
                    wins.set(1, windowRetrieval(samChr, two));
                    wins.set(2, windowRetrieval(samChr, three));
                }
                
                // Loop through each window to update values
                for(int x = 0; x < 3; x++){
                    if(winIt.get(x) >= wins.get(x).size()){
                        // If the iterator was set to the end value, then try the second to last window again for this value
                        winIt.set(x, wins.get(x).size() - 1);
                    }
                    GcTempWin working = workingWindow(wins, x, winIt.get(x));
                    
                    // Check if the position of the SNP is within the current window
                    if(working.Start() <= samStart && working.End() >= samStart){
                        working.incHits(gms);
                    }else if(samStart > working.End()){
                        // our start position is greater than the end position of the current window; time to increase the iterator
                        // I will only advance 2 windows down the set. If I still cannot find the value, then I will keep the iterator but skip the value
                        int y;
                        for(y = winIt.get(x) + 1; y < winIt.get(x) + 2 && y < wins.get(x).size(); y++){
                            working = workingWindow(wins, x, y);
                            if(working.Start() <= samStart && working.End() >= samStart){
                                working.incHits(gms);
                                winIt.set(x, y);
                                break;
                            }
                        }
                        
                        // Set iterator to new value
                        //winIt.set(x, y);
                    }
                }
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        fas.createTempFiles(animal);
        System.out.println();
        System.out.println("[INTERSECT] Printing " + animal + " compressed windows to temporary files...");
    }
    
    private ArrayList<BedAbstract> windowRetrieval(String chr, GcMapTemp map){
        ArrayList<BedAbstract> records = map.getSortedBedAbstractList(chr);        
        return records;
    }
    
    private GcTempWin workingWindow(ArrayList<ArrayList<BedAbstract>> wins, int x, int y){
        GcTempWin working = ((GcTempWin) wins.get(x).get(y));
        return working;
    }
}
