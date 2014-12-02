/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MainClasses;

import DataStructs.GcMapTemp;
import DataStructs.GcTempWin;
import file.BedAbstract;
import file.BedMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author derek.bickhart
 */
public class FastaFile{
    private final String file;
    private final String outfile;
    private final String outprefix;
    private final File outputdir;
    private final int winsize;
    
    private GcMapTemp fOneWindows = new GcMapTemp();
    private GcMapTemp fTwoWindows = new GcMapTemp();
    private GcMapTemp fThreeWindows = new GcMapTemp();

    
    
    public FastaFile(String f, String outprefix, File outputdir, int winsize){
        this.file = f.replaceAll("\\n", "");
        this.outfile = f.substring(0, f.length() - 3);
        this.outprefix = outprefix;
        this.outputdir = outputdir;
        this.winsize = winsize;
    }
    
    public void readFasta(int readLen) throws IOException{
        if(checkRefFaLockFile(readLen)){
            // Check if windows have been generated for the fasta file using this window size
            // If so, read them in and exit
            readExistingWindows(readLen);
            return;
        }
        
        
        BufferedReader inFile = null;
        String line;
        int counter = 0;
        String chrname = null;
        String file3 = outfile + ".win";
        ArrayList<Character> seq = new ArrayList<Character>();
        seq.ensureCapacity(100000000);
        
        //<editor-fold defaultstate="collapsed" desc="inFile declaration">
        try{
            inFile = new BufferedReader(new FileReader(this.file));
        }catch(FileNotFoundException ex){
            System.out.println(ex + "\nCould not find file! ln29\n");
            System.exit(-1);
        }
        //</editor-fold>
        
        while((line = inFile.readLine()) != null){
            line = line.replaceAll("\\n", "");
            if (line.startsWith(">") && counter != 0){
                ChrWinThread cthread = new ChrWinThread(chrname, seq, winsize, readLen);
                cthread.run();
                this.fOneWindows.combineBedMaps(cthread.getOneResults());
                this.fTwoWindows.combineBedMaps(cthread.getTwoResults());
                this.fThreeWindows.combineBedMaps(cthread.getThreeResults());
                seq.clear();
                cthread.closeout();
                System.gc();
                System.out.println("[DOC CHR]Working on " + line);
                chrname = line.replaceAll(">", "");
            } else if(line.startsWith(">") && counter == 0){
                System.out.println("[DOC CHR]Working on " + line);
                chrname = line.replaceAll(">", "");
            } else{
                char[] lineHolder = line.toCharArray();
                for (char ch : lineHolder){
                    seq.add(ch);
                }
            }
            
            counter++;
        }
        if(seq.size() > 0){
            ChrWinThread cthread = new ChrWinThread(chrname, seq, winsize, readLen);
            cthread.run();
            this.fOneWindows.combineBedMaps(cthread.getOneResults());
            this.fTwoWindows.combineBedMaps(cthread.getTwoResults());
            this.fThreeWindows.combineBedMaps(cthread.getThreeResults());
            seq.clear();
            cthread.closeout();
            System.gc();
        }
        
        if(createLockFile(readLen)){
            // This process has access to the lock file so it will generate the windows for future runs
            printOutExistingWins(readLen);
        }
    }
    private boolean readExistingWindows(int readLen) throws IOException{
        System.out.println("[DOC FASTA] Checking for file: " + this.file + "." + this.winsize + "." + readLen + ".file");
        File file1 = new File(this.file + "." + this.winsize + "." + readLen + ".file1");
        File file2 = new File(this.file + "." + this.winsize + "." + readLen + ".file2");
        File file3 = new File(this.file + "." + this.winsize + "." + readLen + ".file3");
        
        if(file1.isFile()){
            populateBedMap(this.fOneWindows, file1);
            System.out.println("[DOC FASTA]Finished loading file1 windows");
        }else{
            System.out.println("[DOC FASTA]Error! Could not find file1 windows for fasta file! Remaking...");
            return false;
        }
        
        if(file2.isFile()){
            populateBedMap(this.fTwoWindows, file2);
            System.out.println("[DOC FASTA]Finished loading file2 windows");
        }else{
            System.out.println("[DOC FASTA]Error! Could not find file2 windows for fasta file! Remaking...");
            return false;
        }
            
        if(file3.isFile()){
            populateBedMap(this.fThreeWindows, file3);
            System.out.println("[DOC FASTA]Finished loading file3 windows");
        }else{
            System.out.println("[DOC FASTA]Error! Could not find file3 windows for fasta file! Remaking...");
            return false;
        }
        return true;
    }
    private void printOutExistingWins(int readLen) throws IOException{
        File file1 = new File(this.file + "." + this.winsize + "." + readLen + ".file1");
        File file2 = new File(this.file + "." + this.winsize + "." + readLen + ".file2");
        File file3 = new File(this.file + "." + this.winsize + "." + readLen + ".file3");
        
        existPrintWins(this.fOneWindows, file1);
        existPrintWins(this.fTwoWindows, file2);
        existPrintWins(this.fThreeWindows, file3);
        
        System.out.println("[DOC FASTA]Printed existing windows to speed future calculations");
    }
    private void existPrintWins(BedMap wins, File file) throws IOException{
        BufferedWriter output = new BufferedWriter(new FileWriter(file));
        for(String chr : utils.SortByChr.ascendingChr(wins.getListChrs())){
            ArrayList<BedAbstract> working = wins.getSortedBedAbstractList(chr);        
            for(int x = 0; x < working.size(); x++){
                GcTempWin w = (GcTempWin) working.get(x);
                output.write(w.Chr() + "\t" + w.Start() + "\t" + w.End() + "\t" + String.format("%.8f", w.getGC()) + "\n");
            }
        }
        output.close();
    }
    private void populateBedMap(BedMap windows, File file) throws IOException{
        BufferedReader input = new BufferedReader(new FileReader(file));
        String line;
        while((line = input.readLine()) != null){
            line = line.trim();
            String[] segs = line.split("\t");
            windows.addBedData(new GcTempWin(segs[0], Integer.valueOf(segs[1]), Integer.valueOf(segs[2]), Float.valueOf(segs[3])));
        }
        input.close();
    }
    private boolean checkRefFaLockFile(int readLen){
        File fastalock = new File(this.file + "." + this.winsize + "." + readLen + ".lock");
        if(fastalock.isFile()){
            return true; // We don't have to generate new windows
        }
        return false;
    }
    private boolean createLockFile(int readLen) throws IOException{
        File fastalock = new File(this.file + "." + this.winsize + "." + readLen +".lock") {};
        if(fastalock.createNewFile()){
            return true; // We created the file successfully and can dump window contents to output
        }else{
            return false;
        }
    }
    private class ChrWinThread{
        private final String chrname;
        private ArrayList<Character> seq;
        private final int winsize;
        private final int readLen;
        private final GcMapTemp one = new GcMapTemp();
        private final GcMapTemp two = new GcMapTemp();
        private final GcMapTemp three = new GcMapTemp();
        private boolean isFinished = false;
        
        public ChrWinThread(String chrname, ArrayList<Character> seq, int winsize, int readLen){
            this.chrname = chrname;
            this.seq = seq;
            this.winsize = winsize;
            this.readLen = readLen;
        }
        
        
        public void run() {
            createOneWins(chrname, readLen);
            createTwoWins(chrname, readLen);
            createThreeWins(chrname, readLen);
            this.isFinished = true;
        }
        public void closeout(){
            this.seq = null;
        }
        
        public GcMapTemp getOneResults(){
            return this.one;
        }
        public GcMapTemp getTwoResults(){
            return this.two;
        }
        public GcMapTemp getThreeResults(){
            return this.three;
        }
        
        public boolean isFinished(){
            return isFinished;
        }

        private void createOneWins(String chrname, int readLen){
            int start = 0;
            int end = 0;
            int len = 0, tempholder = 0;
            int gc = 0, tempgc = 0;
            float percgc;
            int i = 0;
            int chrlen = this.seq.size();
            //DecimalFormat dec = new DecimalFormat("#.#####");
            
            if(chrlen < this.winsize)
                return; // We cannot create a window if the window size is larger than the length of the chromosome!
            
            while (i < chrlen){
                //Conditionals to count the number of absolute bases
                if(this.seq.get(i)!= 'N' && this.seq.get(i)!= 'X' && tempholder > readLen){
                    len++;
                    if(this.seq.get(i) == 'G' || this.seq.get(i) == 'C'){
                        gc++;
                    }
                }else if(this.seq.get(i) != 'N' && this.seq.get(i) != 'X' && tempholder == readLen){
                    len += tempholder + 1;
                    tempholder++;
                    if(this.seq.get(i) == 'G' || this.seq.get(i) == 'C'){
                        gc += tempgc + 1;
                    }
                }else if(this.seq.get(i) != 'N' && this.seq.get(i) != 'X' && tempholder < readLen){
                    tempholder++;
                    if(this.seq.get(i) == 'G' || this.seq.get(i) == 'C'){
                        tempgc++;
                    }
                }else if(this.seq.get(i) == 'N' || this.seq.get(i) == 'X'){
                    tempholder = 0;
                    tempgc = 0;
                }
                
                //Conditional to terminate the current window
                if((len  >= this.winsize || this.seq.get(i) == 'X') && len != 0){
                    end = i;
                    if(this.seq.get(i)=='X' && len < this.winsize);
                    else{
                        percgc = (float) gc / len;
                        this.one.addBedData(chrname, new GcTempWin(chrname, start, end, percgc));
                    }
                    start = start + (this.winsize / 5);
                    i = start - 1;
                    len = 0; tempholder = 0;
                    gc = 0; tempgc = 0;
                }
                if(i > chrlen) break;
                
                // Resets the window if a gap is encountered
                if (this.seq.get(i) == 'X'){
                    start = i + 1;
                    i = start - 1;
                }
                i++;
            }

        }
        private void createTwoWins(String chrname, int readLen){
            int start = 0;
            int end = 0;
            int len = 0, tempholder = 0;
            int gc = 0, tempgc = 0;
            float percgc;
            int twostep = (this.winsize / 5);
            int i = 0;
            int chrlen = this.seq.size();
            
            if(chrlen < this.winsize)
                return; // We cannot create a window if the window size is larger than the length of the chromosome!
            
            while(i < chrlen){
                //Conditionals to count the number of absolute bases
                if(this.seq.get(i) != 'N' && this.seq.get(i) != 'X' && tempholder > readLen){
                    len++;
                    if(this.seq.get(i) == 'G' || this.seq.get(i) == 'C'){
                        gc++;
                    }
                }else if(this.seq.get(i) != 'N' && this.seq.get(i) != 'X' && tempholder == readLen){
                    len += tempholder + 1;
                    tempholder++;
                    if(this.seq.get(i) == 'G' || this.seq.get(i) == 'C'){
                        gc += tempgc + 1;
                    }
                }else if(this.seq.get(i) != 'N' && this.seq.get(i) != 'X' && tempholder < readLen){
                    tempholder++;
                    if(this.seq.get(i) == 'G' || this.seq.get(i) == 'C'){
                        tempgc++;
                    }
                }else if(this.seq.get(i) == 'N' || this.seq.get(i) == 'X'){
                    tempholder = 0;
                    tempgc = 0;
                }
                // Conditional to terminate the window
                if((len  >= twostep || this.seq.get(i) == 'X') && len != 0){
                    end = i;
                    if(this.seq.get(i) == 'X' && len < twostep);
                    else {
                        percgc = (float) gc / len;
                        this.two.addBedData(chrname, new GcTempWin(chrname, start, end, percgc));
                    }
                    start = start + twostep;
                    i = start - 1;
                    len = 0; tempholder = 0;
                    gc = 0; tempgc = 0;
                }
                if(i > chrlen) break;
                // Conditional to terminate when a gap is encountered
                if(this.seq.get(i)== 'X'){
                    start = i +1;
                    i = start - 1;
                }
                i++;
            }
        }
        private void createThreeWins(String chrname, int readLen){
            int start = 0;
            int end = 0;
            int len = 0, tempholder = 0;
            int gc = 0, tempgc = 0;
            int threewin = (this.winsize / 5);
            float percgc;
            int chrlen = this.seq.size();
            
            if(chrlen < this.winsize)
                return; // We cannot create a window if the window size is larger than the length of the chromosome!
            
            for (int i = 0; i < chrlen; i++){
                //Conditionals to count the number of absolute bases
                if(this.seq.get(i) != 'N' && this.seq.get(i) != 'X' && tempholder > readLen){
                    len++;
                    if(this.seq.get(i) == 'G' || this.seq.get(i) == 'C'){
                        gc++;
                    }
                }else if(this.seq.get(i) != 'N' && this.seq.get(i) != 'X' && tempholder == readLen){
                    len += tempholder + 1;
                    tempholder++;
                    if(this.seq.get(i) == 'G' || this.seq.get(i) == 'C'){
                        gc += tempgc + 1;
                    }
                }else if(this.seq.get(i) != 'N' && this.seq.get(i) != 'X' && tempholder < readLen){
                    tempholder++;
                    if(this.seq.get(i) == 'G' || this.seq.get(i) == 'C'){
                        tempgc++;
                    }
                }else if(this.seq.get(i) == 'N' || this.seq.get(i) == 'X'){
                    tempholder = 0;
                    tempgc = 0;
                }
                // Conditional to terminate the window
                if((len  >= threewin || this.seq.get(i) == 'X') && len != 0){
                    end = i; 
                    if(this.seq.get(i) == 'X' && len < threewin);
                    else{
                        percgc = (float) gc / len;
                        this.three.addBedData(chrname, new GcTempWin(chrname, start, end, percgc));

                    }
                    start = i + 1;
                    len = 0; tempholder = 0;
                    gc = 0; tempgc = 0;
                }
                if(i > chrlen) break;
                // Conditional to terminate when a gap is encountered
                if (this.seq.get(i) == 'X'){
                    start = i + 1;
                }
            }
        }
    }
    
    
    public void printOutWins(String animal){
        Path original = outputdir.toPath();
        Path fOneOut = original.resolve(outprefix + "." + animal + ".file1.preNorm.bed");
        Path fTwoOut = original.resolve(outprefix + "." + animal + ".file2.preNorm.bed");
        Path fThreeOut = original.resolve(outprefix + "." + animal + ".file3.preNorm.bed");
        BufferedWriter fOneWriter = null;
        BufferedWriter fTwoWriter = null;
        BufferedWriter fThreeWriter = null;
        
        try {
            Charset charset = Charset.forName("UTF-8");
            fOneWriter = Files.newBufferedWriter(fOneOut, charset);
            fTwoWriter = Files.newBufferedWriter(fTwoOut, charset);
            fThreeWriter = Files.newBufferedWriter(fThreeOut, charset);
            //List<String> chrArray = asSortedList(this.fOneWindows.getListChrs());
            for (String c : utils.SortByChr.ascendingChr(this.fOneWindows.getListChrs())){
                SubGCPrintWins(c, this.fOneWindows, fOneWriter);
                SubGCPrintWins(c, this.fTwoWindows, fTwoWriter);
                SubGCPrintWins(c, this.fThreeWindows, fThreeWriter);
            }
        } catch (IOException ex) {
            System.out.println(ex);
        } finally{
            try{
                fOneWriter.close();
                fTwoWriter.close();
                fThreeWriter.close();
            }catch(IOException ex){
                System.out.println(ex);
            }
        }
    }
    private void SubGCPrintWins(String chr, BedMap wins, BufferedWriter output) throws IOException{
        ArrayList<BedAbstract> working = wins.getSortedBedAbstractList(chr);
        for(int x = 0; x < working.size(); x++){
            GcTempWin w = (GcTempWin) working.get(x);
            output.write(w.Chr() + "\t" + w.Start() + "\t" + w.End() + "\t" + String.format("%.8f", w.getGC()) + "\t" + String.format("%.8f", w.getHits()) + "\t" + w.getDHits() + "\n");
        }
    }
    public <T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
        List<T> list = new ArrayList<T>(c);
        java.util.Collections.sort(list);
        return list;
    }
    public GcMapTemp getWins(int x){
        switch(x){
            case 1:
                return this.fOneWindows;
            case 2:
                return this.fTwoWindows;
            case 3:
                return this.fThreeWindows;
            default:
                System.out.println("Unknown option in getWins call: " + x);
                return null;
        }                
    }
    
    public void createTempFiles(String animal){
        this.calcInitialGMSRatio();
        this.fOneWindows.dumpToTempFile(animal, "file1.prenorm");
        this.fTwoWindows.dumpToTempFile(animal, "file2.prenorm");
        this.fThreeWindows.dumpToTempFile(animal, "file3.prenorm");
        this.fOneWindows.resetWins();
        this.fTwoWindows.resetWins();
        this.fThreeWindows.resetWins();
    }
    public void calcInitialGMSRatio(){
        this.fOneWindows.calcInitialGMS();
        this.fTwoWindows.calcInitialGMS();
        this.fThreeWindows.calcInitialGMS();
    }
    public void bypassWindowGeneration(ParseCommandLine cmd){
        this.fOneWindows = (GcMapTemp) cmd.file1;
        this.fTwoWindows = (GcMapTemp) cmd.file2;
        this.fThreeWindows = (GcMapTemp) cmd.file3;
    }
}
