/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MainClasses;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bickhart
 */
public class MemoryLogger {
    private BufferedWriter logfile;
    private long startTimeMs;
    
    public MemoryLogger(String filename){
        this.startTimeMs = System.currentTimeMillis();
        try {
            this.logfile = Files.newBufferedWriter(Paths.get(filename), Charset.forName("UTF-8"));
        } catch (IOException ex) {
            Logger.getLogger(MemoryLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void memory(String feature){
        ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
        log(feature, "Heap", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage());
        log(feature, "NonHeap", ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage());
        List<MemoryPoolMXBean> beans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean bean: beans) {
            log(feature, bean.getName(), bean.getUsage());
        }

        for (GarbageCollectorMXBean bean: ManagementFactory.getGarbageCollectorMXBeans()) {
            log(feature, bean.getName(), bean.getCollectionCount(), bean.getCollectionTime());
        }
        try {
            this.logfile.flush();
        } catch (IOException ex) {
            Logger.getLogger(MemoryLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void log(String feature, String type, MemoryUsage mem){
        try{
            long time = System.currentTimeMillis() - this.startTimeMs;
            logfile.write(feature + "\t" + type + "\t" + time + "\t" + mem.toString() + System.lineSeparator());
        }catch(IOException ex){
            Logger.getLogger(MemoryLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void log(String feature, String name, long collectionCount, long collectionTime) {
        try{
            long time = System.currentTimeMillis() - this.startTimeMs;
            logfile.write("\t" + feature + "\t" + name + "\t" + time + "\t" + collectionCount + "\t" + collectionTime + System.lineSeparator());
        }catch(IOException ex){
            Logger.getLogger(MemoryLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void close(){
        try{
            this.logfile.close();
        }catch(IOException ex){
            Logger.getLogger(MemoryLogger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
