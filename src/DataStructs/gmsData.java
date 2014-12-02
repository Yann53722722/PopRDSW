/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author bickhart
 */
public class gmsData {
    protected ConcurrentHashMap<String, gmsMapChr> windows;
    protected ArrayList<String> fileList;
    protected ArrayList<String> currentchrs;
    
    public gmsData(String fileList){
        this.windows = new ConcurrentHashMap<String, gmsMapChr>();
        this.currentchrs = new ArrayList<String>();
        //loadFileLines(fileList);
    }
    public void loadFileLines(String infile){
        this.fileList = new ArrayList<String>();
        try{            
            BufferedReader greader = new BufferedReader(new FileReader(infile));
           
            String line;
            while((line = greader.readLine()) != null){
                line = line.trim();
                this.fileList.add(line);
            }
            greader.close();            
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }
    public void fillDataStructure(int threads, int count){
        if(!this.currentchrs.isEmpty()){
            this.currentchrs.clear();
        }
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        fillGMSHash[] hashrunners = new fillGMSHash[count];
        String[] removal = new String[count];
        System.out.println("[DOC GMS]Loading " + count + " GMS files");
        for(int i = 0; i < count && i < this.fileList.size(); i++){
            String file = this.fileList.get(i);
            hashrunners[i] = new fillGMSHash(file);
            executor.execute(hashrunners[i]);
            removal[i] = this.fileList.get(i);            
        }
        
        executor.shutdown();
        while(!executor.isTerminated()){
            //Wait until all threads are closed
        }
        for(fillGMSHash t : hashrunners){
            if(t != null){
            this.currentchrs.add(t.retChr());
            }
        }
        
        System.out.println("[DOC GMS]Done with this set of " + count + " files");
        // remove completed files from list
        for(String name : removal){
            this.fileList.remove(name);            
        }
    }
    public void fillDataStructure(boolean usegms){
        System.out.println("[DOC GMS]Skipping GMS normalization");
        for(int i = 0; i < this.fileList.size(); i++){
            BufferedReader in = null;
            try{
                //in = openFile(new File(this.fileList.get(i)));
                //String line = in.readLine();
                String[] segs = this.fileList.get(i).split("\t");
                // Just a blank entry to keep the chromosome field for the next method
                this.windows.put(segs[0], new gmsMapChr());
                in.close();
            }catch(IOException ex){
                ex.printStackTrace();
            }
        }
        this.fileList.clear();
    }
    
    public class fillGMSHash implements Runnable{
        private String file;
        private String chrome;
        
        public fillGMSHash(String file){
            this.file = file;
        }
        @Override
        public void run() {
            gmsMapChr data = new gmsMapChr();
            BufferedReader in = null;
            String chr = null;
            try{
                in = openFile(new File(this.file));
                String line = in.readLine();
                String[] segs = line.split("\t");
                chr = segs[0];
                if(segs.length == 3){
                    int counter = 1;
                    String startStr = segs[1];
                    float[] sum = new float[50];
                    sum[0] = Float.valueOf(segs[2]);
                    chr = gmsFromUnconverted(in, counter, chr, startStr, sum, data);
                }else if (segs.length == 4){
                    int bin = Integer.valueOf(segs[2]);
                    float val = Float.valueOf(segs[3]);
                    data.ImportElement(bin, val, true);
                    chr = gmsFromSimple(in, chr, data);
                }
            }catch(IOException ex){
                ex.printStackTrace();
            }finally{
                try{
                    in.close();
                    System.out.println("[DOC GMS]Finished loading GMS file: " + file);
                }catch(IOException ex){
                    ex.printStackTrace();
                }
            }
            
            gmsData.this.windows.put(chr, data);
            this.chrome = chr;
        }
        public String retChr(){
            return this.chrome;
        }
        private String gmsFromUnconverted(BufferedReader in, int counter, String chr, String startStr, float[] sum, gmsMapChr data) throws NumberFormatException, IOException {
            String line;
            String[] segs;
            while((line = in.readLine()) != null){
                line = line.replaceAll("\n", "");
                segs = line.split("\t");
                if(counter == 0){
                    chr = segs[0];
                    startStr = segs[1];
                    sum[counter] = Float.valueOf(segs[2]);
                    counter++;
                }else if(counter < 49){                    
                    sum[counter] = Float.valueOf(segs[2]);
                    counter++;
                }else{
                    sum[counter] = Float.valueOf(segs[2]);
                    counter = 0;                    
                    float val = StatUtils.StdevAvg.FloatAvg(sum);
                    if(Float.isNaN(val)){
                        System.out.println("gmsData: float is NaN " + sum.toString());
                    }
                    data.ImportElement(Integer.valueOf(startStr), val);
                }
            }
            if(counter > 0){
                float val = StatUtils.StdevAvg.FloatAvg(sum);
                if(Float.isNaN(val)){
                    System.out.println("gmsData: float is NaN " + sum.toString());
                }
                data.ImportElement(Integer.valueOf(startStr), val);
            }
            return chr;
        }
        
        private String gmsFromSimple(BufferedReader in, String chr, gmsMapChr data) throws NumberFormatException, IOException{
            String line;
            String[] segs;
            while((line = in.readLine()) != null){
                line = line.trim();
                segs = line.split("\t");
                chr = segs[0];
                int bin = Integer.valueOf(segs[2]);
                float val = Float.valueOf(segs[3]);
                data.ImportElement(bin, val, true);
            }
            return chr;
        }
    }
    public HashSet<String> getCurrentChrs(){        
        return new HashSet(this.currentchrs);
    }
    
    public void clearCurrentChrs(){
        this.windows.clear();
    }
    public int sizeOfFileList(){
        return this.fileList.size();
    }
    
    public double getValue(String chr, int start){
        double val = 0.0f;
        if(this.windows.containsKey(chr)){
            val = this.windows.get(chr).RetrieveValue(start);
        }
        return val;
    }
    
    public static boolean isGZipped(File f) {
        int magic = 0;
        try {
         RandomAccessFile raf = new RandomAccessFile(f, "r");
         magic = raf.read() & 0xff | ((raf.read() << 8) & 0xff00);
         raf.close();
        } catch (Throwable e) {
         e.printStackTrace(System.err);
        }
        return magic == GZIPInputStream.GZIP_MAGIC;
   }
    
    private static BufferedReader openFile(File file){
        BufferedReader output = null;
        try {
            if(isGZipped(file)){
                InputStream StrFq1 = new FileInputStream(file);
                InputStream gzFq1 = new GZIPInputStream(StrFq1);
                Reader decoder = new InputStreamReader(gzFq1, "UTF-8");
                output = new BufferedReader(decoder);
                return output;
            }else{
                InputStream StrFq1 = new FileInputStream(file);
                Reader decoder = new InputStreamReader(StrFq1, "UTF-8");
                output = new BufferedReader(decoder);
                return output;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(gmsData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex){
            Logger.getLogger(gmsData.class.getName()).log(Level.SEVERE, null, ex);
        }
        return output;
    }
}
