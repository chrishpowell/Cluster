/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.test2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author chrispowell
 */
public class LoopRemoveTest
{
    public static void main(String[] args)
    {
        List<String> lst = Collections.EMPTY_LIST;
        Collections.addAll(lst = new ArrayList<>(), "Mercury","Venus","Earth","Mars","Jupiter","Saturn","Uranus","Neptune");
        
        String minP = Collections.min(lst);
        lst.remove(minP);
        
        for( Iterator<String> iter = lst.iterator(); iter.hasNext(); )
        {
            String planet = iter.next();
            System.out.println(">> " +planet);
            iter.remove();
        }
    }
}
