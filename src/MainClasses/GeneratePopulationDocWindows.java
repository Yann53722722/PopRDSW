/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MainClasses;

import DataStructs.Blacklist;
import DataStructs.gmsData;
import ThreadRunners.ThreadMethods;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bickhart
 */
public class GeneratePopulationDocWindows {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        MemoryLogger memlog = new MemoryLogger("GenPopDocWins.status.log");
        ParseCommandLine cmd = new ParseCommandLine(args);
        
        if(cmd.restart){
            runRestartProcess(memlog, cmd);
        }
        
        
        // Generate window objects
        FastaFile fas = new FastaFile(cmd.refFasta, "windows", new File(cmd.output), cmd.winsize);
        if(cmd.premade){
            fas.bypassWindowGeneration(cmd);
        }else{
            try {
                fas.readFasta(cmd.readLen);
            } catch (IOException ex) {
                Logger.getLogger(GeneratePopulationDocWindows.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        memlog.memory("[Fasta]");
              
        gmsData gData = new gmsData(cmd.gmsList);
        int chrcount;
        gData.loadFileLines(cmd.gmsList);
        chrcount = gData.sizeOfFileList();
        

        // Getting number of chromosomes from gmsdata
                     
            
        // Identify bam files and run through them, counting hits 
        // I am using a "15" for now because I know the size of the cattle genome
        // TODO: change this so that the user can set the number of gms files to load into memory
        if(cmd.condense){
            for(String animal : cmd.condensedFiles.keySet()){
                compressedIntersector comp = new compressedIntersector(cmd.condensedFiles.get(animal), fas, animal);
                comp.run();
            }
        }else{
            for(int x = 0; x < chrcount; x += 30){
                if(cmd.useGms){
                    gData.fillDataStructure(cmd.threads, 30);
                }else{
                    gData.fillDataStructure(cmd.useGms);
                }
                for(String dir : cmd.directories){
                    File inputDir = new File(dir);
                    File[] files = inputDir.listFiles(cmd.filter);
                    if(files.length < 1){
                        System.err.println("[MAIN] Error, could not find any bam files for using this search data: ");
                        System.err.println(inputDir.toString() + " " + cmd.filter.toString());
                        System.exit(1);
                    }else{
                        System.out.println("[MAIN] Found " + files.length + " bam files in this directory: " + dir);
                    }
                    ThreadRunners.ThreadMethods.threadedIntersector(gData, files, fas, cmd.threads, cmd.useGms, dir);
                    memlog.memory(dir + "_" + x);
                }

            }
        }
        // Print out the windows
        for(String a : cmd.directories){
            fas.printOutWins(a);
        }
        
        memlog.memory("[Post GMS]");
        // Partition control windows 
        Blacklist filter = new Blacklist(cmd.blacklist);
        CalcControls calculator = new CalcControls(fas.getWins(1), fas.getWins(2), fas.getWins(3), cmd.threads, cmd.directories);
        calculator.generateControlStats();
        ControlWins controlOne;
        ControlWins controlTwo;
        ControlWins controlThree;
        
        if(cmd.premade){
            System.out.println("Using premade control windows");
            controlOne = cmd.f1control;
            controlThree = cmd.f3control;
            controlOne.addTempFiles(cmd.file1.getTempFileList());
            controlThree.addTempFiles(cmd.file3.getTempFileList());
            //controlTwo = calculator.partitionControlWins(fas.getWins(2), 1);
            controlTwo = calculator.FilterControlWins(fas.getWins(2), filter, 1);
        }else{
            //controlOne = calculator.partitionControlWins(fas.getWins(1), 0);
            //controlTwo = calculator.partitionControlWins(fas.getWins(2), 1);
            //controlThree = calculator.partitionControlWins(fas.getWins(3), 2);
            controlOne = calculator.FilterControlWins(fas.getWins(1), filter, 0);
            controlTwo = calculator.FilterControlWins(fas.getWins(2), filter, 1);
            controlThree = calculator.FilterControlWins(fas.getWins(3), filter, 2);
        }
        memlog.memory("[Post CTRL]");
        
        // Calculate statistics on control windows
        System.out.println("[DOC MAIN]Calculating new control statistics");
        ThreadMethods.threadedControlCalculator(controlOne, controlTwo, controlThree, cmd.threads, cmd.directories);
        memlog.memory("[Post Stats]");
        
        // Normalize everything
        System.out.println("[DOC MAIN]Normalizing windows with control statistics");
        //TODO: rewrite this to dump out the windows as needed
        File outfile = new File(cmd.output);
        ThreadMethods.threadedNormalize(fas, controlOne, controlTwo, controlThree, cmd.threads, cmd.directories, "Normalized", outfile);
        memlog.memory("[Post Norm]");
        
        for(String a : cmd.directories){
            controlOne.printOUTGCNormList(".file1.controls.gcnorm", outfile, a);
            controlTwo.printOUTGCNormList(".file2.controls.gcnorm", outfile, a);
            controlThree.printOUTGCNormList(".file3.controls.gcnorm", outfile, a);
        }
        
        // Print out normalized windows and control windows
        /*try {
         * File outfile = new File(cmd.output);
         * OutputNormWindows.PrintOutMainWindows(fas, "Normalized", outfile, cmd.directories);
         * OutputNormWindows.PrintOutControlWindows(controlOne, "F1Control", outfile, cmd.directories);
         * OutputNormWindows.PrintOutControlWindows(controlTwo, "F2Control", outfile, cmd.directories);
         * OutputNormWindows.PrintOutControlWindows(controlThree, "F3Control", outfile, cmd.directories);
         * } catch (IOException ex) {
         * Logger.getLogger(GeneratePopulationDocWindows.class.getName()).log(Level.SEVERE, null, ex);
         * }*/
        
        memlog.close();
    }

    private static void runRestartProcess(MemoryLogger memlog, ParseCommandLine cmd) {
        System.out.println("[MAIN DOC] Restarting normalization from existing file intersections...");
        memlog.memory("[Restart begin]");
        
        // Control calculation
        CalcControls calculator = new CalcControls(cmd.file1, cmd.file2, cmd.file3, cmd.threads, cmd.directories);
        ControlWins controlOne = calculator.partitionControlWins(cmd.file1, 0);
        ControlWins controlTwo = calculator.partitionControlWins(cmd.file2, 1);
        ControlWins controlThree = calculator.partitionControlWins(cmd.file3, 2);
        
        memlog.memory("[Control calc]");
        // Calculate statistics on control windows
        System.out.println("[DOC MAIN]Calculating new control statistics");
        ThreadMethods.threadedControlCalculator(controlOne, controlTwo, controlThree, cmd.threads, cmd.directories);
        memlog.memory("[Post Stats]");

        // Normalize everything
        System.out.println("[DOC MAIN]Normalizing windows with control statistics");
        File outfile = new File(cmd.output);
        ThreadMethods.threadedNormalize(cmd.file1, cmd.file2, cmd.file3, controlOne, controlTwo, controlThree, cmd.threads, cmd.directories, "Normalized", outfile);
        memlog.memory("[Post Norm]");
        
        for(String a : cmd.directories){
            controlOne.printOUTGCNormList(".file1.controls.gcnorm", outfile, a);
            controlTwo.printOUTGCNormList(".file2.controls.gcnorm", outfile, a);
            controlThree.printOUTGCNormList(".file3.controls.gcnorm", outfile, a);
        }
        
        // Print out normalized windows and control windows
        /*try {
         * File outfile = new File(cmd.output);
         * OutputNormWindows.PrintOutMainWindows(cmd.file1, cmd.file2, cmd.file3, "Normalized", outfile, cmd.directories);
         * OutputNormWindows.PrintOutControlWindows(controlOne, "F1Control", outfile, cmd.directories);
         * OutputNormWindows.PrintOutControlWindows(controlTwo, "F2Control", outfile, cmd.directories);
         * OutputNormWindows.PrintOutControlWindows(controlThree, "F3Control", outfile, cmd.directories);
         * } catch (IOException ex) {
         * Logger.getLogger(GeneratePopulationDocWindows.class.getName()).log(Level.SEVERE, null, ex);
         * }*/
        System.exit(0);
    }
    
    
}
