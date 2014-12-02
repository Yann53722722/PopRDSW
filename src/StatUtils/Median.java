/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package StatUtils;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author bickhart
 */
public class Median {
    public static int IMedian(List<Integer> list){       
        if(list.size() == 1){
            return list.get(0);
        }else if(list.isEmpty()){
            return 0;
        }else{
            Collections.sort(list);
            if(list.size() % 2 == 0){
                return list.get(list.size() / 2);
            }else{
                return list.get( (int)(list.size() / 2) + 1);
            }
        }        
    }
    public static double FMedian(List<Float> list){
        if(list.size() == 1){
            return (double)list.get(0);
        }else if(list.isEmpty()){
            return 0.0d;
        }else{
            Collections.sort(list);
            if(list.size() % 2 == 0){
                return (double) list.get(list.size() / 2);
            }else{
                return (double) list.get( (int)(list.size() / 2) + 1);
            }
        }
    }
    public static double DMedian(List<Double> list){
        if(list.size() == 1){
            return (double)list.get(0);
        }else if(list.isEmpty()){
            return 0.0d;
        }else{
            Collections.sort(list);
            if(list.size() % 2 == 0){
                return (double) list.get(list.size() / 2);
            }else{
                return (double) list.get( (int)(list.size() / 2) + 1);
            }
        }
    }
    public static double DUpperTenPerc(List<Double> list){
        if(list.size() == 1){
            return (double) list.get(0);
        }else if(list.isEmpty()){
            return 0.0d;
        }else{
            Collections.sort(list);
            if(list.size() % 2 == 0){
                return list.get(Math.round(list.size() * 0.90f));
            }else{
                return list.get(Math.round(list.size() * 0.90f) + 1);
            }
        }
    }
    public static double DUpperQuintile(List<Double> list){
        if(list.size() == 1){
            return (double) list.get(0);
        }else if(list.isEmpty()){
            return 0.0d;
        }else{
            Collections.sort(list);
            if(list.size() % 2 == 0){
                return list.get(Math.round(list.size() * 0.80f));
            }else{
                return list.get(Math.round(list.size() * 0.80f) + 1);
            }
        }
    }
}
