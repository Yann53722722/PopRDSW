/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructs;

import file.BedAbstract;
import file.BedMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bickhart
 */
public class GcWinTempFile extends BedAbstract implements tempDataFile{
    private Path tempFile;
    private String animal;
    private BufferedReader fileHandle;
    private int lines;
    
    public GcWinTempFile(String tempFile, String animal){
        try {
            this.tempFile = Files.createTempFile(tempFile + "." + animal, ".tmp");
        } catch (IOException ex) {
            Logger.getLogger(GcWinTempFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.animal = animal;
        this.tempFile.toFile().deleteOnExit();
        try{
            this.fileHandle = Files.newBufferedReader(this.tempFile, Charset.defaultCharset());
            
        }catch(IOException ex){
            Logger.getLogger(GcWinTempFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /*
     * Overrides
     */
    @Override
    public int compareTo(BedAbstract t) {
        if(this.start > t.Start()){
            return 1;
        }else if(this.start == t.Start()){
            return 0;
        }else{
            return -1;
        }
    }

    @Override
    public void createTemp(Path file) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteTemp() {
        try{
            if(this.fileHandle.ready()){
                this.fileHandle.close();
            }
            Files.deleteIfExists(tempFile);
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public BedAbstract readTempBed(BedAbstract bed) {
        String line;
        try{
            String sstart = String.valueOf(bed.Start());
            String send = String.valueOf(bed.End());
            while((line = this.fileHandle.readLine()) != null){
                line = line.trim();
                String[] segs = line.split("\t");
                if(segs[0].equals(bed.Chr()) && segs[1].equals(sstart) && segs[2].equals(send)){
                    return new GcTempWin(segs);
                }
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public BedAbstract readSequentialFile() {
        try{
            String line;
            if((line = this.fileHandle.readLine()) != null){
                line = line.trim();
                String[] segs = line.split("\t");
                return new GcTempWin(segs);
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return null;
    }
    
    public String readSequentialLines(){
        try{
            String line;
            if((line = this.fileHandle.readLine()) != null){
                line = line.trim();
                
                return line;
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void closeTemp() {
        try{
            this.fileHandle.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void dumpFileToDisk(BedMap gcWinMap) {
        try(BufferedWriter out = Files.newBufferedWriter(tempFile, Charset.defaultCharset())){
            ArrayList<String> chrs = utils.SortByChr.ascendingChr(gcWinMap.getListChrs());
            for(String c : chrs){
                for(BedAbstract b : gcWinMap.getSortedBedAbstractList(c)){
                    GcTempWin working = (GcTempWin) b;
                    out.write(working.Chr() + "\t" + working.Start() + "\t" + working.End() + "\t");
                    out.write(working.getGC() + "\t");
                    out.write(working.getHits() + "\t" + working.getDHits() + "\t" + working.getGMSRatio());
                    out.write(System.lineSeparator());
                    this.lines++;
                }
            }
        }catch(IOException ex){
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public void debugWriter(BedMap gcWinMap, String outname){
        try(BufferedWriter out = Files.newBufferedWriter(Paths.get(outname), Charset.defaultCharset())){
            ArrayList<String> chrs = utils.SortByChr.ascendingChr(gcWinMap.getListChrs());
            for(String c : chrs){
                for(BedAbstract b : gcWinMap.getSortedBedAbstractList(c)){
                    GcTempWin working = (GcTempWin) b;
                    out.write(working.Chr() + "\t" + working.Start() + "\t" + working.End() + "\t");
                    out.write(working.getGC() + "\t");
                    out.write(working.getHits() + "\t" + working.getDHits() + "\t" + working.getGMSRatio());
                    out.write(System.lineSeparator());
                    this.lines++;
                }
            }
        }catch(IOException ex){
            System.err.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    public void rewind(){
        try {
            if(this.fileHandle.ready()){
                this.fileHandle.close();
            }
            this.fileHandle = Files.newBufferedReader(this.tempFile, Charset.defaultCharset());
        } catch (IOException ex) {
            Logger.getLogger(GcWinTempFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public int getLines(){
        return this.lines;
    }
    
}
