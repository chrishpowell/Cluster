/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 *
 * @author chrispowell
 */
public class LoopTest
{
    static int totQ=0,totR=0;
    public static void main(String[] args)
    {
        Set<Score> cw = Set.of( new Score(1,"quick",new CountQR(1,7)),
                                new Score(1,"brown",new CountQR(2,1)),
                                new Score(1,"fox",new CountQR(1,2)),
                                new Score(2,"0",new CountQR(2,2)),
                                new Score(2,"fox",new CountQR(1,1)),
                                new Score(7,"fox",new CountQR(2,0)),
                                new Score(8,"argle",new CountQR(1,1)),
                                new Score(8,"fox",new CountQR(1,1)),
                                new Score(11,"compare",new CountQR(1,1)) );
//        Map<String,Integer> sc = Map.of("quick",1,"brown",1,"fox",3,"0",1,"argle",1,"compare",1);
//        List<CountQR> lqr = List.of(new CountQR(1,1),new CountQR(2,1),new CountQR(1,2));//,new CountQR(2,2),new CountQR(1,1),new CountQR(2,2),new CountQR(1,1),new CountQR(1,1),new CountQR(1,1));
//        List<Score> ls = List.of(new Score(1,"quick",new CountQR(1,7)),
//                                 new Score(1,"brown",new CountQR(2,1)),
//                                 new Score(1,"fox",new CountQR(1,2)) );
//        
//        ls.forEach(s -> {
//            totQ += s.getCountQR().getCountQ();
//            totR += s.getCountQR().getCountR();
//        });
//        System.out.println(">> " +totQ+ "/" +totR);
        
//        List<Score> ls = cw.stream().collect(Collectors.toList());
//        Map<Integer,Map<Integer,Long>> mw = cw.stream().collect(Collectors.groupingBy(Score::getId,Collectors.groupingBy(Score::getCountQ,Collectors.counting())));
//        Map<Integer,Long> mw = cw.stream().collect(Collectors.groupingBy(Score::getId, Collectors.counting()));
//        Collector<Score,?,Integer> sumCountQ = Collectors.summingInt(s -> s.getCountQR().getCountQ());
//        Collector<Score,?,Map<Integer,Integer>> sumScoresById = Collectors.groupingBy(Score::getId,sumCountQ);
//        Map<Integer,Integer> mw = cw.stream().collect(Collectors.groupingBy(Score::getId,Collectors.summingInt(s -> s.getCountQR().getCountQ())));
//        Map<Integer,Map<CountQR,List<Score>>> mw1 = cw.stream().collect(Collectors.groupingBy(Score::getId,Collectors.groupingBy(Score::getCountQR)));
        Map<Integer,List<CountQR>> mw1 = cw.stream().collect(Collectors.groupingBy(Score::getId,Collectors.mapping(Score::getCountQR,Collectors.toList())));
        mw1.forEach((k,v) -> {
            System.out.print("QRscoreCW id: " +k);
            totQ = 0; totR = 0;
            v.forEach(s -> {
                totQ += s.getCountQ();
                totR += s.getCountR();
            });
            System.out.println(" > " +totQ+ "/" +totR);
        });
//        System.out.println(">> " +mw1);


//        System.out.println("1> "+mw);
//        Function<Score,CountQR> groupByQR = (Score s) -> {
//            return s.getCountQR();
//        };
//        Map<Integer,QRcounts> mw2 = cw.stream().collect(Collectors.groupingBy(groupByQR()));
//        IntSummaryStatistics iss = cw.stream().collect(Collectors.summarizingInt(Score::getCountQ));
//        System.out.println("2> " +iss);
    }
}

class Score
{
    private final int       id;
    private final String    name;
    private final CountQR   countQR;

    public Score(int id, String name, CountQR countQR)
    {
        this.name = name;
        this.id = id;
        this.countQR = countQR;
    }

    public String getName() { return name; }
    public int getId() { return id; }
    public CountQR getCountQR() { return countQR; }
    
    @Override
    public String toString() { return ""+id; }
}

class CountQR
{
    private final int       countQ, countR;

    public CountQR(int countQ, int countR)
    {
        this.countQ = countQ;
        this.countR = countR;
    }

    public int getCountQ() { return countQ; }
    public int getCountR() { return countR; }
    
    @Override
    public String toString() { return "(Q:"+countQ+"/R:"+countR+")"; }
}

class QRCounts
{
    private int   totQ = 0, totR = 0;

    public QRCounts(){}

    public int getTotQ() { return totQ; }
    public void setTotQ( int totQ ) { this.totQ = totQ; }
    public int getTotR() { return totR; }
    public void setTotR( int totR ) { this.totR = totR; }
}