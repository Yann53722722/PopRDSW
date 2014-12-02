/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MainClasses;

import DataStructs.BedStdAvgMed;
import DataStructs.gcWinData;
import DataStructs.gcWinOut;
import file.BedAbstract;
import file.BedMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bickhart
 */
public class OutputNormWindows {
    public static void PrintOutMainWindows(FastaFile fasta, String outPrefix, File outputDir, ArrayList<String> animals) throws IOException{
        for(String a : animals){
            Path original = outputDir.toPath();
            Path foneOut = original.resolve(outPrefix + "." + a + ".file1.bed");
            Path ftwoOut = original.resolve(outPrefix + "." + a + ".file2.bed");
            Path fthreeOut = original.resolve(outPrefix + "." + a + ".file3.bed");

            Charset charset = Charset.forName("UTF-8");
            BufferedWriter oneWrite = Files.newBufferedWriter(foneOut, charset);
            BufferedWriter twoWrite = Files.newBufferedWriter(ftwoOut, charset);
            BufferedWriter threeWrite = Files.newBufferedWriter(fthreeOut, charset);

            int rejects;

            rejects = PrintSeparateFastaWindows(fasta.getWins(1), oneWrite, a);
            System.out.println("[DOC OUTPUT]Rejected " + rejects + " windows from 0 window set");
            rejects = PrintSeparateFastaWindows(fasta.getWins(2), twoWrite, a);
            System.out.println("[DOC OUTPUT]Rejected " + rejects + " windows from 1 window set");
            rejects = PrintSeparateFastaWindows(fasta.getWins(3), threeWrite, a);
            System.out.println("[DOC OUTPUT]Rejected " + rejects + " windows from 2 window set");

            oneWrite.close();
            twoWrite.close();
            threeWrite.close();
        }
    }
    public static void DumpOutMainWindows(gcWinOut bed, BufferedWriter output){
        try {
            output.write(bed.createFormatOutStr());
        } catch (IOException ex) {
            Logger.getLogger(OutputNormWindows.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public static void PrintOutMainWindows(BedMap file1, BedMap file2, BedMap file3, String outPrefix, File outputDir, ArrayList<String> animals) throws IOException{
        for(String a : animals){
            Path original = outputDir.toPath();
            Path foneOut = original.resolve(outPrefix + "." + a + ".file1.bed");
            Path ftwoOut = original.resolve(outPrefix + "." + a + ".file2.bed");
            Path fthreeOut = original.resolve(outPrefix + "." + a + ".file3.bed");

            Charset charset = Charset.forName("UTF-8");
            BufferedWriter oneWrite = Files.newBufferedWriter(foneOut, charset);
            BufferedWriter twoWrite = Files.newBufferedWriter(ftwoOut, charset);
            BufferedWriter threeWrite = Files.newBufferedWriter(fthreeOut, charset);

            int rejects;

            rejects = PrintSeparateFastaWindows(file1, oneWrite, a);
            System.out.println("[DOC OUTPUT]Rejected " + rejects + " windows from 0 window set");
            rejects = PrintSeparateFastaWindows(file2, twoWrite, a);
            System.out.println("[DOC OUTPUT]Rejected " + rejects + " windows from 1 window set");
            rejects = PrintSeparateFastaWindows(file3, threeWrite, a);
            System.out.println("[DOC OUTPUT]Rejected " + rejects + " windows from 2 window set");

            oneWrite.close();
            twoWrite.close();
            threeWrite.close();
        }
    }
    private static int PrintSeparateFastaWindows(BedMap wins, BufferedWriter output, String animal) throws IOException{
        int rejectedwins = 0;
        for(String chr : utils.SortByChr.ascendingChr(wins.getListChrs())){
            ArrayList<BedAbstract> working = wins.getSortedBedAbstractList(chr);
            for(int x = 0; x < working.size(); x++){
                gcWinData w = (gcWinData) working.get(x);
                if(w.shouldUse()){
                    output.write(w.Chr() + "\t" + w.Start() + "\t" + w.End() + "\t" + String.format("%.2f", w.getNormHits(animal)) + "\t" + String.format("%.2f", w.getNormDHits(animal)) + "\t" + String.format("%.4f", w.getGMSRatio(animal)) + "\n");
                }else{
                    rejectedwins++;
                    output.write(w.Chr() + "\t" + w.Start() + "\t" + w.End() + "\t" + String.format("%.2f", w.getNormHits(animal)) + "\t" + String.format("%.2f", w.getNormDHits(animal)) + "\t" + String.format("%.4f", w.getGMSRatio(animal)) + "\t1\n");
                }
            }
        }
        return rejectedwins;
    }
    public static void PrintOutControlWindows(ControlWins control, String outPrefix, File outputDir, ArrayList<String> animals) throws IOException{
        for(String a : animals){
            Path original = outputDir.toPath();
            Path fControl = original.resolve(outPrefix + "." + a + ".bed");

            Charset charset = Charset.forName("UTF-8");
            BufferedWriter write = Files.newBufferedWriter(fControl, charset);

            // Printing autosomes first
            PrintSeparateFastaWindows(control.getAutoMap(), write, a);
            PrintSeparateFastaWindows(control.getSexMap(), write, a);

            // Now printing out the GC norm list for debugging purposes
            String gcnormout = outPrefix + ".gcnorm";
            control.printOUTGCNormList(gcnormout, outputDir, a);
            write.close();
        }
    }
    public static void DumpOutStatistics(double avg, double stdev, double median, BufferedWriter write, String type){
        try{
            write.write(type + ";avg;" + String.format("%.8f", avg) + "\n");
            write.write(type + ";std;" + String.format("%.8f", stdev) + "\n");
            write.write(type + ";median;" + String.format("%.8f", median) + "\n");
            write.flush();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    public static void PrintOutStatistics(ControlWins control, String outPrefix, File outputDir, ArrayList<String> animals) throws IOException{
        for(String a : animals){
            Path original = outputDir.toPath();
            Path fControl = original.resolve(outPrefix + "." + a + ".stats");

            Path dControl = original.resolve(outPrefix + "." + a + ".stats.pre");

            Charset charset = Charset.forName("UTF-8");
            BufferedWriter write = Files.newBufferedWriter(fControl, charset);

            BedStdAvgMed autoVals = control.getAutoNormStats(a);
            BedStdAvgMed sexVals = control.getSexNormStats(a);

            write.write("auto;avg;" + String.format("%.8f", autoVals.getAvg()) + "\n");
            write.write("auto;std;" + String.format("%.8f", autoVals.getStdev()) + "\n");
            write.write("auto;median;" + String.format("%.8f", autoVals.getMedian()) + "\n");

            write.write("sex;avg;" + String.format("%.8f", sexVals.getAvg()) + "\n");
            write.write("sex;std;" + String.format("%.8f", sexVals.getStdev()) + "\n");
            write.write("sex;median;" + String.format("%.8f", sexVals.getMedian()) + "\n");

            write.close();

            write = Files.newBufferedWriter(dControl, charset);
            autoVals = control.getAutoStats(a);
            sexVals = control.getSexStats(a);

            write.write("auto;avg;" + String.format("%.8f", autoVals.getAvg()) + "\n");
            write.write("auto;std;" + String.format("%.8f", autoVals.getStdev()) + "\n");
            write.write("auto;median;" + String.format("%.8f", autoVals.getMedian()) + "\n");

            write.write("sex;avg;" + String.format("%.8f", sexVals.getAvg()) + "\n");
            write.write("sex;std;" + String.format("%.8f", sexVals.getStdev()) + "\n");
            write.write("sex;median;" + String.format("%.8f", sexVals.getMedian()) + "\n");

            write.close();
        }
    }
}
