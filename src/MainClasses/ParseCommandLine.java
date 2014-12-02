/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MainClasses;

import DataStructs.GcMapTemp;
import DataStructs.GcTempWin;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author bickhart
 */
public class ParseCommandLine {
    public ArrayList<String> directories;
    public Finder filter;
    public String output;
    public String refFasta;
    public int threads = 1;
    public int winsize = -1;
    public String gmsList;
    public boolean useGms = true;
    public int readLen = -1;
    public HashMap<String, String> condensedFiles = new HashMap<>();
    
    public boolean premade = false;
    public boolean condense = false;
    public String[] preFiles = new String[5];
    public GcMapTemp file1;
    public GcMapTemp file2;
    public GcMapTemp file3;
    public ControlWins f1control;
    public ControlWins f3control;
    private HashMap<String, RestartWins> winstore = new HashMap<>();
    
    public boolean restart = false;
    
    public String usage = "Usage: (-I directory of bams ...) -S filefilter -O output string for windows -R reference fasta -n number of threads -w winsize -rl readLength -g gmsFileList -ng nogms"
            + System.lineSeparator() + "[Optional premade windows]: -f1 f1wins -f2 f2wins -f3 f3wins -f1c f1controls -f3c f3controls"
            + System.lineSeparator() + "[Optional restart files]: -rf1 animal,f1wins ... -rf2 animal,f2wins ... -rf3 animal,f3wins ..."
            + System.lineSeparator() + "[Optional input file]: -C animal,condensedfile";
    public String blacklist;
    
    public ParseCommandLine(String[] args){
        this.directories = new ArrayList<>();
        String[] segs;
        for(int i = 0; i < args.length; i++){
            switch(args[i]){
                case "-S":
                    this.filter = new Finder(args[i+1]);
                    break;
                case "-I":
                    this.directories.add(args[i+1]);
                    break;
                case "-O":
                    this.output = args[i+1];
                    break;
                case "-R":
                    this.refFasta = args[i+1];
                    break;
                case "-n":
                    this.threads = Integer.valueOf(args[i+1]);
                    break;
                case "-w":
                    this.winsize = Integer.valueOf(args[i+1]);
                    break;
                case "-g":
                    this.gmsList = args[i+1];                    
                    break;
                case "-f1":
                    this.premade = true;
                    this.preFiles[0] = args[i+1];
                    //createBedMap(args[i+1], 1);
                    break;
                case "-f2":
                    this.premade = true;
                    this.preFiles[1] = args[i+1];
                    //createBedMap(args[i+1], 2);
                    break;
                case "-f3":
                    this.premade = true;
                    this.preFiles[2] = args[i+1];
                    //createBedMap(args[i+1], 3);
                    break;
                case "-f1c":
                    this.premade = true;
                    this.preFiles[3] = args[i+1];
                    //createBedMap(args[i+1], 4);
                    break;
                case "-f3c":
                    this.premade = true;
                    this.preFiles[4] = args[i+1];
                    //createBedMap(args[i+1], 5);
                    break;
                case "-rf1":
                    this.restart = true;
                    segs = args[i+1].split(",");
                    if(!this.winstore.containsKey(segs[0])){
                        this.winstore.put(segs[0], new RestartWins());
                    }
                    this.winstore.get(segs[0]).rf1 = segs[1];
                    //restartBedMap(segs[1], segs[0], 1);
                    break;
                case "-rf2":
                    this.restart = true;
                    segs = args[i+1].split(",");
                    if(!this.winstore.containsKey(segs[0])){
                        this.winstore.put(segs[0], new RestartWins());
                    }
                    this.winstore.get(segs[0]).rf2 = segs[1];
                    //restartBedMap(segs[1], segs[0], 2);
                    break;
                case "-rf3":
                    this.restart = true;
                    segs = args[i+1].split(",");
                    if(!this.winstore.containsKey(segs[0])){
                        this.winstore.put(segs[0], new RestartWins());
                    }
                    this.winstore.get(segs[0]).rf3 = segs[1];
                    //restartBedMap(segs[1], segs[0], 3);
                    break;
                case "-ng":
                    this.useGms = false;
                    break;
                case "-rl":
                    this.readLen = Integer.valueOf(args[i+1]);
                    break;
                case "-bl":
                    this.blacklist = args[i+1];
                    break;
                case "-C":
                    String[] set = args[i+1].split(",");
                    if(set.length < 2){
                        System.err.println("Error with input condensed file option! " + args[i+1]);
                        System.exit(-1);
                    }
                    this.condensedFiles.put(set[0], set[1]);
                    this.condense = true;
                    break;
                    
            }
        }
        
        if(this.output == null ){
            System.out.println("The -O option must be supplied to run!");
            System.out.println(usage);
        }
        
        for(String animal : this.winstore.keySet()){
            restartBedMap(this.winstore.get(animal).rf1, animal, 1);
            restartBedMap(this.winstore.get(animal).rf2, animal, 2);
            restartBedMap(this.winstore.get(animal).rf3, animal, 3);
        }
        
        if(this.premade){
            // Just to make sure that the windows are populated in order so that I can intersect the control wins
            for(int x = 0; x < 5; x++){
                int i = x + 1;
                createBedMap(this.preFiles[x], i);
            }
        }
        
        if(this.directories.isEmpty() && this.condensedFiles.isEmpty()){
            System.out.println(usage);
            System.exit(0);
        }else if(this.output == null || this.filter == null || this.refFasta == null || this.winsize == -1 ){
            System.out.println(usage);
            System.exit(0);
        }else if(this.premade && (this.file1 == null || this.file2 == null || this.file3 == null || this.f1control == null || this.f3control == null)){
            System.out.println("All windows must be supplied (f1-3, f1c and f3c) if using premade windows!");
            System.out.println(usage);
            System.exit(0);
        }
    }
    private class RestartWins{
        public String rf1;
        public String rf2;
        public String rf3;
    }
    
    private void restartBedMap(String input, String animal, int num){
        switch(num){
            case 1:
                if(this.file1 == null){
                    this.file1 = (GcMapTemp) CreateBedMap(input, animal, 1);
                    System.out.println("[PARSE] Created file1 input for: " + animal);
                }else{
                    UpdateBedMap(this.file1, input, animal, 1);
                    System.out.println("[PARSE] Updated file1 input for: " + animal);
                }
                break;
            case 2: 
                if(this.file2 == null){
                    this.file2 = (GcMapTemp) CreateBedMap(input, animal, 2);
                    System.out.println("[PARSE] Created file2 input for: " + animal);
                }else{
                    UpdateBedMap(this.file2, input, animal, 2);
                    System.out.println("[PARSE] Updated file2 input for: " + animal);
                }
                break;
            case 3:
                if(this.file3 == null){
                    this.file3 = (GcMapTemp) CreateBedMap(input, animal, 3);
                    System.out.println("[PARSE] Created file3 input for: " + animal);
                }else{
                    UpdateBedMap(this.file3, input, animal, 3);
                    System.out.println("[PARSE] Updated file3 input for: " + animal);
                }
                break;
            default:
                System.out.println("[Parse CMD] Error with bedmap assignment! Improper number passed to routine! " + num);
                System.exit(1);
        }
    }
    
    
    
    private void createBedMap(String input, int num){        
        switch(num){
            case 1:
                this.file1 = CreateBedMap(input);
                System.out.println("[PARSE] Created file1 windows for: " + input);
                break;
            case 2: 
                this.file2 = CreateBedMap(input);
                System.out.println("[PARSE] Created file2 windows for: " + input);
                break;
            case 3:
                this.file3 = CreateBedMap(input);
                System.out.println("[PARSE] Created file3 windows for: " + input);
                break;
            case 4:
                this.f1control = CreateCtrlWins(input, 1);
                System.out.println("[PARSE] Created file1c windows for: " + input);
                break;
            case 5:
                this.f3control = CreateCtrlWins(input, 3);
                System.out.println("[PARSE] Created file3c windows for: " + input);
                break;
            default:
                System.out.println("[Parse CMD] Error with bedmap assignment! Improper number passed to routine! " + num);
                System.exit(1);
        }
    }
    
    private GcMapTemp CreateBedMap(String input, String animal, int file) throws NumberFormatException {
        GcMapTemp working = new GcMapTemp();
        try(BufferedReader in = Files.newBufferedReader(Paths.get(input), Charset.forName("UTF-8"))){
            String line;
            while((line = in.readLine())!= null){
                line = line.trim();
                String[] segs = line.split("\t");
                if(segs.length != 6){
                    throw new IOException("Improper number of line segments for restart file! " + segs.length);
                }
                GcTempWin temp = new GcTempWin(segs[0], Integer.valueOf(segs[1]), Integer.valueOf(segs[2]), Float.valueOf(segs[3]));
                temp.setHits(Double.valueOf(segs[4]));
                temp.setDhits(Integer.valueOf(segs[5]));
                double d = Double.valueOf(segs[4]);
                int v = Integer.valueOf(segs[5]);
                double gms;
                if(v == 0){
                    gms = 0.0d;
                }else{
                    gms = d / (double)v;
                }
                temp.setGMS(gms);
                working.addBedData(temp);
                
            }
        }catch(IOException ex){
            System.out.println("[Parse CMD] Error reading file: " + input + " for restarting windows!");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        working.dumpToTempFile(animal, "file" + file + ".restart");
        return working;
    }
    
    private void UpdateBedMap(GcMapTemp map, String input, String animal, int file){
        try(BufferedReader in = Files.newBufferedReader(Paths.get(input), Charset.forName("UTF-8"))){
            String line;
            while((line = in.readLine())!= null){
                line = line.trim();
                String[] segs = line.split("\t");
                if(segs.length != 6){
                    throw new IOException("Improper number of line segments for restart file! " + segs.length);
                }
                double gms = 0.0d;
                double hits = Double.valueOf(segs[4]);
                int dhits = Integer.valueOf(segs[5]);
                if(dhits > 0){
                    gms = hits / (double) dhits;
                }
                map.intersectVals(segs[0], Integer.valueOf(segs[1]), Integer.valueOf(segs[2]), hits, dhits, gms);
                //incrementHitBed.setWinValues(map, segs[0], Integer.valueOf(segs[1]), Integer.valueOf(segs[2]), Double.valueOf(segs[4]), Integer.valueOf(segs[5]), animal);
                
            }
        }catch(IOException ex){
            System.out.println("[Parse CMD] Error reading file: " + input + " for updating windows!");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        map.dumpToTempFile(animal, "file" + file + ".restart");
    }
    
    private GcMapTemp CreateBedMap(String input) throws NumberFormatException {
        GcMapTemp working = new GcMapTemp();
        try(BufferedReader in = Files.newBufferedReader(Paths.get(input), Charset.forName("UTF-8"))){
            String line;
            while((line = in.readLine())!= null){
                line = line.trim();
                String[] segs = line.split("\t");
                if(segs.length != 4){
                    throw new IOException("Improper number of line segments! " + segs.length);
                }
                working.addBedData(new GcTempWin(segs[0], Integer.valueOf(segs[1]), Integer.valueOf(segs[2]), Float.valueOf(segs[3])));
            }
        }catch(IOException ex){
            System.out.println("[Parse CMD] Error reading file: " + input + " for premade windows!");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        return working;
    }
    
    private ControlWins CreateCtrlWins(String input, int num){
        ControlWins working = new ControlWins();
        GcMapTemp intersection = null;
        switch(num){
            case 1 :
                intersection = this.file1;
                break;
            case 3 :
                intersection = this.file3;
                break;
        }
        try(BufferedReader in = Files.newBufferedReader(Paths.get(input), Charset.forName("UTF-8"))){
            String line;
            while((line = in.readLine())!= null){
                line = line.trim();
                String[] segs = line.split("\t");
                if(segs.length != 4){
                    throw new IOException("Improper number of line segments! " + segs.length);
                }
                GcTempWin b = new GcTempWin(segs[0], Integer.valueOf(segs[1]), Integer.valueOf(segs[2]), Float.valueOf(segs[3]));
                b.toggleCTRL();
                working.addBed(b);
                intersection.setCtrlUse(b.Chr(), b.Start(), b.End());
            }
        }catch(IOException ex){
            System.out.println("[Parse CMD] Error reading file: " + input + " for premade windows!");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
        return working;
    }
}
