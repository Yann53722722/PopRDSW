/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package StatUtils;

import DataStructs.GcMapTemp;
import DataStructs.GcTempWin;
import DataStructs.gcWinData;
import DataStructs.gcWinData;
import file.BedAbstract;
import file.BedMap;
import java.util.ArrayList;
import java.util.Set;
import utils.BinBed;
import utils.LineIntersect;

/**
 *
 * @author bickhart
 */
public class incrementHitBed extends LineIntersect{
    public static void intersectIncrement(GcMapTemp a, String chr, int start, int end, double value){
        if(a.containsChr(chr)){
            Set<Integer> bins = BinBed.getBins(start, end);
            for(int bin : bins){
                if(a.containsBin(chr, bin)){
                    ArrayList<BedAbstract> list = a.getBedAbstractList(chr, bin);
                    for(int x = 0; x < list.size(); x++){
                        GcTempWin bed = (GcTempWin) list.get(x);
                        if(ovCount(bed.Start(), bed.End(), start, end) > 0){
                            bed.incHits(value);
                        }
                    }
                }
            }
        }        
    }
    public static void baseIncrement(BedMap a, String chr, int start, int end, String Animal){
        if(a.containsChr(chr)){
            Set<Integer> bins = BinBed.getBins(start, end);
            for(int bin : bins){
                if(a.containsBin(chr, bin)){
                    ArrayList<BedAbstract> list = a.getBedAbstractList(chr, bin);
                    for(int x = 0; x < list.size(); x++){
                        gcWinData bed = (gcWinData) list.get(x);
                        if(ovCount(bed.Start(), bed.End(), start, end) > 0){
                            bed.incHits(Animal, 1.0f);
                        }
                    }
                }
            }
        }
    }
    public static void setWinValues(BedMap a, String chr, int start, int end, double hits, int dhits, String animal){
        if(a.containsChr(chr)){
            Set<Integer> bins = BinBed.getBins(start, end);
            for(int bin : bins){
                if(a.containsBin(chr, bin)){
                    ArrayList<BedAbstract> list = a.getBedAbstractList(chr, bin);
                    for(int x = 0; x < list.size(); x++){
                        gcWinData bed = (gcWinData) list.get(x);
                        if(bed.Start() == start){
                            bed.setHits(animal, hits);
                            bed.setDHits(animal, dhits);
                            break;
                        }
                    }
                }
            }
        }
    }
    
}
