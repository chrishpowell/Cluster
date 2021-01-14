/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.hcluster.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 *
 * @author chrispowell
 * @param <T>
 */
public class MergeList<T>
{
    private T       contents = null;
    private int     count = 0;

    public MergeList(T contents)
    {
        this.contents = contents;
    }

    public T getContents() { return contents; }

    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    
    public void updCount( int upd ) { this.count += upd; }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.contents);
        return hash;
    }

    /**
     * Equates *contents* (contained class)
     * @param ml
     * @return 
     */
    @Override
    public boolean equals(Object ml)
    {
        if( this == ml ) { return true; }
        if( ml == null ) { return false; }
        if( getClass() != ml.getClass() ) { return false; }

        final MergeList<T> other = (MergeList<T>) ml; 
        return other.getContents().equals(this.getContents());
    }
    
    

    /**
     * M A I N
     * =======
     * @param args
     * @throws CloneNotSupportedException 
     */
    public static void main(String[] args)
            throws CloneNotSupportedException
    {
        X x = new X("x",1);
        X y = new X("y",1);
        X z = new X("z",1);
        X a = new X("a",1);
        X b = new X("b",2);
        X z1 = new X("z",1);
        X a1 = new X("a",1);
        X z2 = new X("z",1);
        X a2 = new X("a",1);
        X b1 = new X("b",2);
        X e = new X("e",2);
        
        MergeList<X> mlx = new MergeList(x);
        mlx.setCount(1);
        MergeList<X> mly = new MergeList(y);
        mly.setCount(1);
        MergeList<X> mlz = new MergeList(z);
        mlz.setCount(1);
        MergeList<X> mla = new MergeList(a);
        mla.setCount(1);
        MergeList<X> mlb = new MergeList(b);
        mlb.setCount(2);
        MergeList<X> mlz1 = new MergeList(z1);
        mlz1.setCount(1);
        MergeList<X> mlz2 = new MergeList(z2);
        mlz2.setCount(1);
        MergeList<X> mla1 = new MergeList(a1);
        mla1.setCount(1);
        MergeList<X> mla2 = new MergeList(a2);
        mla2.setCount(1);
        MergeList<X> mlb1 = new MergeList(b1);
        mlb1.setCount(2);
        MergeList<X> mle = new MergeList(e);
        mle.setCount(2);
        
        
        List<MergeList<X>> lx0 = new ArrayList<>();
        lx0.add(mlx);
        lx0.add(mly);
        lx0.add(mlz);
        lx0.add(mla);
        lx0.add(mlb);
        List<MergeList<X>> lx1 = new ArrayList<>();
        lx1.add(mle);
        lx1.add(mlz1);
        lx1.add(mla1);
        lx1.add(mlb1);
        List<MergeList<X>> lx2 = new ArrayList<>();
        lx2.add(mlz2);
        lx2.add(mla2);
        
//        List<X> lx0 = List.of(x,y,z,a,b);
//        List<X> lx1 = List.of(e,z1,a1,b1);
//        List<X> lx2 = List.of(z2,a2);
        List<List<MergeList<X>>> lofl = List.of(lx1,lx2);
        
        List<MergeList<X>> merge = new ArrayList<>();
        for( MergeList<X> l0: lx0 )
        {
            merge.add(l0);
        }
        
        lofl.forEach(ll ->
        {
            ll.forEach(l ->
            {
                int i = merge.indexOf(l);
                if( i >=0 )
                {
                    MergeList<X> m = merge.get(i);
//                    System.out.println("merge val: " +m.getSs()+"/"+m.getIi()+ ", Upd val: " +l.getSs()+"/"+l.getIi());
                    m.updCount(l.getCount());
                }
            });
        });
        
        merge.forEach(m -> {
            System.out.println("" +m.contents.getSs()+ ": " +m.getCount());
        });
    }
}

//------------------------------------------------------------------------------
class X implements Cloneable
{
    private String  ss;
    private int     ii;

    public X(String ss, int ii)
    {
        this.ss = ss;
        this.ii = ii;
    }

    public String getSs() { return ss; }
    public int getIi() { return ii; }

    public void setSs(String ss) { this.ss = ss; }
    public void updIi(int upd) { ii += upd; }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.ss);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if( this == obj ) { return true; }
        if( obj == null ) { return false; }
        if( getClass() != obj.getClass() ) { return false; }
        
        final X other = (X) obj;
        return this.ss.equals(other.ss);
    }

    @Override
    public X clone()
        throws CloneNotSupportedException
    {
        return (X)super.clone();
    }
    
    @Override
    public String toString()
    {
        return "[" +ss+ ":" +ii+ "]";
    }
}