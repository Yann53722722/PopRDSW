/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructs;

import file.BedAbstract;
import file.BedMap;
import file.BedSimple;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import utils.BedIntersect;
import utils.LineIntersect;

/**
 *
 * @author bickhart
 */
public class Blacklist extends LineIntersect{
    private BedMap filterWins;
    private String input;
    
    public Blacklist(String input){
        this.input = input;
        this.filterWins = new BedMap();
        LoadFile();
    }
    
    private void LoadFile(){
        try(BufferedReader in = Files.newBufferedReader(Paths.get(input), Charset.defaultCharset())){
            String line;
            while((line = in.readLine()) != null){
                line = line.trim();
                String[] segs = line.split("\t");
                this.filterWins.addBedData(new BedSimple(segs[0], Integer.valueOf(segs[1]), Integer.valueOf(segs[2])));
            }
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public boolean blacklistIntersect(BedAbstract bed){
        if(this.doesIntersect(filterWins, bed.Chr(), bed.Start(), bed.End())){
            return true;
        }else{
            return false;
        }
    }
    
}
