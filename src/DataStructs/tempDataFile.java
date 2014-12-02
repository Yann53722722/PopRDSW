/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package DataStructs;

import file.BedAbstract;
import file.BedMap;
import java.nio.file.Path;

/**
 *
 * @author bickhart
 */
public interface tempDataFile {
    public void createTemp(Path file);
    
    public void deleteTemp();
    
    public BedAbstract readTempBed(BedAbstract bed);
    
    public BedAbstract readSequentialFile();
    
    public void closeTemp();
    
    public void dumpFileToDisk(BedMap gcWinMap);
}
