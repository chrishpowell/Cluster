/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.fptree;

import eu.discoveri.utils.Subsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 *
 * @author chrispowell
 */
public class SupportCalc
{
    public static void main(String[] args)
    {
        // Items (with count of docs that contain that item)
        Item itemA = new Item("a",4);
        Item itemB = new Item("b",6);
        Item itemC = new Item("c",4);
        Item itemD = new Item("d",4);
        Item itemE = new Item("e",5);
        Item itemX = new Item("x",0);
        List<Item> uli = Arrays.asList(new Item[]{itemA,itemB,itemC,itemD,itemE});

        // Documents
        Doc ln01 = new Doc("ln01",Arrays.asList(new Item[]{itemE,itemA,itemB,itemD}));
        Doc ln02 = new Doc("ln02",Arrays.asList(new Item[]{itemC,itemB,itemE}));
        Doc ln03 = new Doc("ln03",Arrays.asList(new Item[]{itemB,itemE,itemA,itemD}));
        Doc ln04 = new Doc("ln04",Arrays.asList(new Item[]{itemB,itemE,itemA,itemC}));
        Doc ln05 = new Doc("ln05",Arrays.asList(new Item[]{itemB,itemD,itemE,itemA,itemC}));
        Doc ln06 = new Doc("ln06",Arrays.asList(new Item[]{itemB,itemC,itemD}));
        Doc ln0x = new Doc("ln0x",Arrays.asList(new Item[]{itemX}));

        List<Doc> lds= Arrays.asList(ln01,ln02,ln03,ln04,ln05,ln06);
        
        // Calc. support (count)
        Map<String,FISets> lfis = SupportCount.bruteForceFreqItems(lds);
        
        lfis.forEach((s,fis) -> {
            if( fis.getCount() > 2 ) //Constants.MINSUP )
            {
                System.out.print("[");
                fis.getSi().forEach(i -> {
                    System.out.print(i.getName());
                });
                System.out.println("]> " +fis.getCount());
            }
        });
    }
}
