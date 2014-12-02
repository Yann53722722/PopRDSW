/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ThreadRunners;

import DataStructs.GcMapTemp;
import DataStructs.gmsData;
import file.BedMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import MainClasses.ControlWins;
import MainClasses.FastaFile;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 *
 * @author bickhart
 */
public class ThreadMethods {

    public static void threadedIntersector(gmsData data, File[] files, FastaFile fas, int threads, boolean usegms, String animal) {
        
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < files.length; i++) {
            Runnable worker = new samIntersector(files[i], fas, data, i, usegms, animal);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            //Wait until all threads are closed
        }
        fas.createTempFiles(animal);
        System.out.println("[INTERSECT] Printing " + animal + " windows to temporary files...");
    }

    public static void threadedNormalize(FastaFile fas, ControlWins one, ControlWins two, ControlWins three, int threads, ArrayList<String> animals, String output, File outdir) {
        
        for(String a : animals){
            ExecutorService executor;
            System.out.println("[NORMALIZE] Normalizing control windows for: " + a );
            Path original = outdir.toPath();
            Path foneCOut = original.resolve(output + "." + a + ".file1.control.bed");
            Path ftwoCOut = original.resolve(output + "." + a + ".file2.control.bed");
            Path fthreeCOut = original.resolve(output + "." + a + ".file3.control.bed");
            Path foneOut = original.resolve(output + "." + a + ".file1.bed");
            Path ftwoOut = original.resolve(output + "." + a + ".file2.bed");
            Path fthreeOut = original.resolve(output + "." + a + ".file3.bed");
            
            executor = Executors.newFixedThreadPool(3);
            ThreadedWinNormStats oneCtrl = new ThreadedWinNormStats(one.getAutoMap(), one.getSexMap(), one.getAutoNormWins(a), one.getAutoStats(a), one.getSexStats(a), true, a, foneCOut);
            ThreadedWinNormStats twoCtrl = new ThreadedWinNormStats(two.getAutoMap(), two.getSexMap(), two.getAutoNormWins(a), two.getAutoStats(a), two.getSexStats(a), true, a, ftwoCOut);
            ThreadedWinNormStats threeCtrl = new ThreadedWinNormStats(three.getAutoMap(), three.getSexMap(), three.getAutoNormWins(a), three.getAutoStats(a), three.getSexStats(a), true, a, fthreeCOut);
            
            executor.execute(oneCtrl);
            executor.execute(twoCtrl);
            executor.execute(threeCtrl);
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
            one.addAutoNormStats(oneCtrl.getAutoStats(), a);
            two.addAutoNormStats(twoCtrl.getAutoStats(), a);
            three.addAutoNormStats(threeCtrl.getAutoStats(), a);
            one.addSexNormStats(oneCtrl.getSexStats(), a);
            two.addSexNormStats(twoCtrl.getSexStats(), a);
            three.addSexNormStats(threeCtrl.getSexStats(), a);
                        
            executor = Executors.newFixedThreadPool(3);
            ThreadedWinNormStats oneNorm = new ThreadedWinNormStats(fas.getWins(1), one.getAutoNormWins(a), one.getAutoNormStats(a), one.getSexNormStats(a), a, foneOut);
            ThreadedWinNormStats twoNorm = new ThreadedWinNormStats(fas.getWins(2), two.getAutoNormWins(a), two.getAutoNormStats(a), two.getSexNormStats(a), a, ftwoOut);
            ThreadedWinNormStats threeNorm = new ThreadedWinNormStats(fas.getWins(3), three.getAutoNormWins(a), three.getAutoNormStats(a), three.getSexNormStats(a), a, fthreeOut);
            executor.execute(oneNorm);
            executor.execute(twoNorm);
            executor.execute(threeNorm);
            executor.shutdown();
            while (!executor.isTerminated()) {
            }
          }
      }
    
    public static void threadedNormalize(GcMapTemp file1, GcMapTemp file2, GcMapTemp file3, ControlWins one, ControlWins two, ControlWins three, int threads, ArrayList<String> animals, String output, File outdir) {
         
        int actualt = 3;
        if(threads < 3){
            actualt = 1;
        }
        for(String a : animals){
            System.out.println("[NORMALIZE] Normalizing control windows for: " + a );
            
            Path original = outdir.toPath();
            Path foneCOut = original.resolve(output + "." + a + ".file1.control.bed");
            Path ftwoCOut = original.resolve(output + "." + a + ".file2.control.bed");
            Path fthreeCOut = original.resolve(output + "." + a + ".file3.control.bed");
            Path foneOut = original.resolve(output + "." + a + ".file1.bed");
            Path ftwoOut = original.resolve(output + "." + a + ".file2.bed");
            Path fthreeOut = original.resolve(output + "." + a + ".file3.bed");
            
            ExecutorService executor = Executors.newFixedThreadPool(actualt);
            ThreadedWinNormStats oneCtrl = new ThreadedWinNormStats(one.getAutoMap(), one.getSexMap(), one.getAutoNormWins(a), one.getAutoStats(a), one.getSexStats(a), true, a, foneCOut);
            ThreadedWinNormStats twoCtrl = new ThreadedWinNormStats(two.getAutoMap(), two.getSexMap(), two.getAutoNormWins(a), two.getAutoStats(a), two.getSexStats(a), true, a, ftwoCOut);
            ThreadedWinNormStats threeCtrl = new ThreadedWinNormStats(three.getAutoMap(), three.getSexMap(), three.getAutoNormWins(a), three.getAutoStats(a), three.getSexStats(a), true, a, fthreeCOut);
            oneCtrl.run();
            twoCtrl.run();
            threeCtrl.run();
            //executor.execute(oneCtrl);
            //executor.execute(twoCtrl);
            //executor.execute(threeCtrl);
            //executor.shutdown();
            //while (!executor.isTerminated()) {
            //}
            one.addAutoNormStats(oneCtrl.getAutoStats(), a);
            two.addAutoNormStats(twoCtrl.getAutoStats(), a);
            three.addAutoNormStats(threeCtrl.getAutoStats(), a);
            one.addSexNormStats(oneCtrl.getSexStats(), a);
            two.addSexNormStats(twoCtrl.getSexStats(), a);
            three.addSexNormStats(threeCtrl.getSexStats(), a);
        
            System.out.println("[NORMALIZE] Normalizing normal windows for: " + a + " Using: " + actualt + " threads.");
            //ExecutorService executor = Executors.newFixedThreadPool(actualt);
            ThreadedWinNormStats oneNorm = new ThreadedWinNormStats(file1, one.getAutoNormWins(a), one.getAutoNormStats(a), one.getSexNormStats(a), a, foneOut);
            ThreadedWinNormStats twoNorm = new ThreadedWinNormStats(file2, two.getAutoNormWins(a), two.getAutoNormStats(a), two.getSexNormStats(a), a, ftwoOut);
            ThreadedWinNormStats threeNorm = new ThreadedWinNormStats(file3, three.getAutoNormWins(a), three.getAutoNormStats(a), three.getSexNormStats(a), a, fthreeOut);
            oneNorm.run();
            twoNorm.run();
            threeNorm.run();
            //executor.execute(oneNorm);
            //executor.execute(twoNorm);
            //executor.execute(threeNorm);
            //executor.shutdown();
            //while (!executor.isTerminated()) {
            //}
        }
    }

    public static void threadedControlCalculator(ControlWins oneControl, ControlWins twoControl, ControlWins threeControl, int threads, ArrayList<String> animals) {
        
        for(String a : animals){
            ExecutorService executor = Executors.newFixedThreadPool(threads);
            ThreadedWinStats onestats = new ThreadedWinStats(oneControl.getAutoMap(), oneControl.getSexMap(), 0, true, a);
            //onestats.run();
            executor.execute(onestats);
            ThreadedWinStats twostats = new ThreadedWinStats(twoControl.getAutoMap(), twoControl.getSexMap(), 1, true, a);
            //twostats.run();
            executor.execute(twostats);
            ThreadedWinStats threestats = new ThreadedWinStats(threeControl.getAutoMap(), threeControl.getSexMap(), 2, true, a);
            //threestats.run();
            executor.execute(threestats);

            executor.shutdown();
            while (!(executor.isTerminated())) {
            }
            oneControl.addAutoStats(onestats.returnAutoStats(), a);
            twoControl.addAutoStats(twostats.returnAutoStats(), a);
            threeControl.addAutoStats(threestats.returnAutoStats(), a);
            oneControl.addSexStats(onestats.returnSexStats(), a);
            twoControl.addSexStats(twostats.returnSexStats(), a);
            threeControl.addSexStats(threestats.returnSexStats(), a);
            oneControl.addAutoNorm(onestats.returnNormWins(), a);
            twoControl.addAutoNorm(twostats.returnNormWins(), a);
            threeControl.addAutoNorm(threestats.returnNormWins(), a);
        }
    }

    public static int GMSSize(String gmsFile) {
        int i = 0;
        try {
            BufferedReader greader = new BufferedReader(new FileReader(gmsFile));
            while (greader.readLine() != null) {
                i++;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return i;
    }
    
}
