/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ThreadRunners;

import DataStructs.GcMapTemp;
import DataStructs.gmsData;
import file.BedMap;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import MainClasses.FastaFile;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;

/**
 *
 * @author derek.bickhart
 */
public class samIntersector implements Runnable{
    private File inFile;
    private GcMapTemp one;
    private GcMapTemp two;
    private GcMapTemp three;
    private gmsData gData;
    private int threadnum;
    private HashSet<String> chrs;
    private boolean usegms;
    private String animal;
    
    public samIntersector(File inFile, FastaFile winset, gmsData gData, int i, boolean usegms, String animal){
        try{
            this.inFile = inFile;
            
        }catch(Exception e){
            System.out.println(e);
        }
        this.one = winset.getWins(1);
        this.two = winset.getWins(2);
        this.three = winset.getWins(3);
        this.gData = gData;
        this.threadnum = i;
        this.chrs = gData.getCurrentChrs();
        this.usegms = usegms;
        this.animal = animal;
    }
    
    @Override
    public void run() {
        SAMFileReader inputSam = null;
        try{
            inputSam = new SAMFileReader(this.inFile);
            inputSam.setValidationStringency(SAMFileReader.ValidationStringency.SILENT);
            SAMFileHeader header = inputSam.getFileHeader();
            if(inputSam.hasIndex()){
                //System.out.println("[DOC SAM]Using index to parse iterators");
                if(this.usegms){
                    for(String chr : this.chrs){
                        SAMRecordIterator sIterator = inputSam.query(chr, 0, 0, false);
                        iterateThroughSam(sIterator);
                    }
                }else{
                    SAMSequenceDictionary dict = header.getSequenceDictionary();
                    List<SAMSequenceRecord> seqRecords = dict.getSequences();
                    for(SAMSequenceRecord sr : seqRecords){
                        SAMRecordIterator sIterator = inputSam.query(sr.getSequenceName(), 0, 0, false);
                        iterateThroughSam(sIterator);
                    }
                }
            }else{
                SAMRecordIterator sIterator = inputSam.iterator();
                iterateThroughSam(sIterator);
            }
        }catch(Exception ex){
            System.out.println(ex);
        }finally{
            inputSam.close();
        }
        
        
    }

    private void iterateThroughSam(SAMRecordIterator sIterator) {
        SAMRecord s = null;
        double gms;
        try {
            while (sIterator.hasNext()) {
                s = sIterator.next();
                int samStart = s.getAlignmentStart();
                int samEnd = s.getAlignmentStart() + s.getReadLength();
                String samChr = s.getReferenceName();
                if(!this.chrs.contains(samChr) && this.usegms){
                    continue;
                }
                
                
                if(this.usegms){
                    try{
                        gms = this.gData.getValue(samChr, samStart);
                    }catch(Exception ex){
                        System.out.println("[DOC SAM]Error for gms score in " + samChr + " at coordinate: " + samStart + ". (Probably from read mapping to end of chromosome)");
                        gms = 0.0d;
                    }
                }else{
                    gms = 1.0d;
                }
                
                StatUtils.incrementHitBed.intersectIncrement(one, samChr, samStart, samEnd, gms);
                StatUtils.incrementHitBed.intersectIncrement(two, samChr, samStart, samEnd, gms);
                StatUtils.incrementHitBed.intersectIncrement(three, samChr, samStart, samEnd, gms);
                
                //<editor-fold defaultstate="collapsed" desc="debug counter">
                /*if(counter % 1000 == 0){
                    System.out.println("Done with iteration: " + counter);

                }
                counter++;*/
                //</editor-fold>
            }
        } catch (Exception e) {
            System.out.println(System.lineSeparator() + e + " in: " + this.inFile);
            System.out.println(s.getSAMString());
            double bin = 0.0d;
            try{
                bin = s.getAlignmentStart() / (double) 50;
                gms = this.gData.getValue(s.getReferenceName(), s.getAlignmentStart());
                System.out.println(gms);
                
            }catch(Exception ex){
                System.out.println("Could not retrieve GMS value for bin: " + bin);
                ex.printStackTrace();
            }
            
        } finally{
            sIterator.close();
            System.out.print("[DOC BAM]Finished reading from " + this.animal + " in thread:\t" + this.threadnum + "\r");
        }
    }
    
    
}
