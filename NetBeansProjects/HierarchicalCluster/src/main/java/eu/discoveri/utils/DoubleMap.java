/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.discoveri.utils;

import java.io.PrintStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * Represents a 2d mapping of rows/cols. Does not need to be rectangular, eg:
 * Col 0: contains 1 row, col 1: 2 rows, col 2: 3 rows etc. [Column: [Row,
 * Value]].
 *
 * @author chrispowell
 * @param <C>
 * @param <R>
 * @param <V> Needs to implement Comparable<V>
 */
public class DoubleMap<C, R, V>
{
    private final Map<C, Map<R, V>> backingMap;

    /**
     * Constructor.
     */
    public DoubleMap()
    {
        this.backingMap = new HashMap<>();
    }

    /**
     * Get value in cell: column,row.
     *
     * @param col
     * @param row
     * @return
     */
    public V get(C col, R row)
    {
        Map<R, V> innerMap = backingMap.get(col);
        if( innerMap == null )
            return null;
        else
            return innerMap.get(row);
    }

    /**
     * Add new cell: column,row value.
     *
     * @param col
     * @param row
     * @param value
     */
    public void put(C col, R row, V value)
    {
        Map<R, V> innerMap = backingMap.get(col);
        if( innerMap == null )
        {
            innerMap = new HashMap<>();
            backingMap.put(col, innerMap);
        }

        innerMap.put(row, value);
    }

    /**
     * Delete cell: column,row. Note: In a loop with row keys, this can cause
     * java.util.ConcurrentModificationException. Use a copy set to avoid the
     * problem. Eg:
     *     Set<R> rows = getRowKeys(col).stream().collect(Collectors.toSet());
     *
     * @param col
     * @param row
     * @return
     */
    public V remove(C col, R row)
    {
        Map<R, V> innerMap = backingMap.get(col);
        V val = null;
        if( innerMap != null )
        {
            val = innerMap.get(row);
            innerMap.remove(row);
        }

        return val;
    }

    /**
     * Get all Column keys regardless.
     *
     * @return
     */
    public Set<C> getBaseColKeys()
    {
        if( backingMap.isEmpty() )
            return Collections.EMPTY_SET;

        return backingMap.keySet();
    }

    /*
     * Return set of col keys for given row.
     * 
     * @param row Row argument as map may not be rectangular.
     * @return 
     */
    public Set<C> getColKeys(R row)
    {
        if (backingMap.isEmpty())
            return Collections.EMPTY_SET;

        Set<C> cols = new HashSet<>();

        // Assume map may not be rectangular
        backingMap.keySet().forEach(c ->
        {
            Map<R, V> rows = backingMap.get(c);
            rows.forEach((r, v) ->
            {
                if (r == row)
                    cols.add(c);
            });
        });

        return cols;
    }

    /**
     * Return set of row keys for given column.
     *
     * @param col Column argument as map may not be rectangular.
     * @return
     */
    public Set<R> getRowKeys(C col)
    {
        if (backingMap.isEmpty())
            return Collections.EMPTY_SET;

        // Get (row) keys of given col
        return backingMap.get(col).keySet();
    }

    /**
     * Get set of (col) values for a given row.
     *
     * @param row
     * @return
     */
    public Set<V> getColValues(R row)
    {
        if (backingMap.isEmpty())
            return Collections.EMPTY_SET;

        Set<V> colVals = new HashSet<>();
        Set<C> allCols = backingMap.keySet();

        // Assume map be not be rectangular
        allCols.forEach(c ->
        {
            Map<R, V> rows = backingMap.get(c);
            rows.forEach((r, v) ->
            {
                if (r == row)
                    colVals.add(v);
            });
        });

        return colVals;
    }

    /**
     * Get set of (row) values for a given column.
     *
     * @param col
     * @return
     */
    public Set<V> getRowValues(C col)
    {
        if (backingMap.isEmpty())
            return Collections.EMPTY_SET;

        // Get (row) values of given col
        return backingMap.get(col).values().stream().collect(Collectors.toSet());
    }
    
    /**
     * Get Entry Set.
     * 
     * @return 
     */
    public Set<Map.Entry<C,Map<R,V>>> getEntrySet()
    {
        return backingMap.entrySet();
    }

    /**
     * Set of all values of this double map.
     *
     * @return
     */
    public Set<V> allValues()
    {
        Set<V> allValues = new HashSet<>();
        Set<C> allCols = backingMap.keySet();

        allCols.forEach(c -> allValues.addAll(backingMap.get(c).values()));
        return allValues;
    }

    /**
     * Get contents size, rows by cols (count of values).
     *
     * @return
     */
    private int count = 0;
    public int getSize()
    {
        if (backingMap.isEmpty())
            return 0;

        Set<C> allCols = backingMap.keySet();

        // Assume map be not be rectangular
        allCols.forEach(c -> count += backingMap.get(c).size());
        return count;
    }
    
    /**
     * Dump all entries.  Classes are expected to have toString() methods.
     */
    public void dumpMap( PrintStream ps )
    {
        backingMap.entrySet().forEach(s ->
        {
            C key = s.getKey();
            s.getValue().forEach((k,v) ->
            {
                ps.printf("[K1: %s, K2: %s] Val: %s%n", key, k, v);
            });
        });
    }
}
