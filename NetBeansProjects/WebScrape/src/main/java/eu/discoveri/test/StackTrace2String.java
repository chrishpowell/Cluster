/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author chrispowell
 */
public class StackTrace2String
{
    public StackTraceElement[] methA()
    {
        try
        {
            methB();
        }
        catch( PrediktException pe )
        {
            return pe.getStackTrace();
        }
        
        return null;
    }
    
    public void methB()
            throws PrediktException
    {
        throw new PrediktException("Test throwable");
    }
    
    public static void main(String[] args)
    {
        StackTrace2String st2s = new StackTrace2String();
//        for( StackTraceElement ste: st2s.methA() )
//        {
//            System.out.println(">> "+ste);
//        }
        List<StackTraceElement> lste = Arrays.asList(st2s.methA());
        String ls = lste.stream().map(s -> s.toString()).collect(Collectors.joining("\r\n"));
        System.out.println("----> " +ls);
        
        // ------ Seems useless:
//        StackWalker stackWalker = StackWalker.getInstance(
//          Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE, StackWalker.Option.SHOW_HIDDEN_FRAMES), 16);
//        
//        List<String> ls1 = StackWalker.getInstance()
//          .walk(s -> s.map(frame -> "\n" + frame.getClassName() + "/" + frame.getMethodName())
//              .collect(Collectors.toList()));
//        
//        ls1.forEach(s -> {
//            System.out.println(">>> " +s);
//        });
    }
}
