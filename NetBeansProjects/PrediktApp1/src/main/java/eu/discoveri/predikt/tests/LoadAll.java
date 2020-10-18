/*
 * Copyright (C) Discoveri SIA - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited.
 * Proprietary and confidential.
 */
package eu.discoveri.predikt.tests;

import com.hazelcast.config.Config;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.MapStore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Chris Powell, Discoveri OU
 * @email info@astrology.ninja
 */
public class LoadAll
{
    public static void main(String[] args) {
        String mapName = LoadAll.class.getCanonicalName();

        Config config = createNewConfig(mapName);
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        System.out.println("...Create map");
        IMap<Integer, Integer> map = hz.getMap(mapName);
        System.out.println("... Init map size: " +map.size());

        System.out.println("# Adding 5 elements to the map");
        for (int i = 0; i < 5; i++) {
            map.put(i, i);
        }

        map.evictAll();
        System.out.printf("# After evictAll map size: %d\n", map.size());

        map.loadAll(true);
        System.out.printf("# After loadAll map size: %d\n", map.size());

        // stop the cluster
        Hazelcast.shutdownAll();
        // start newly the cluster
        hz = Hazelcast.newHazelcastInstance(config);
        map = hz.getMap(mapName);

        System.out.printf("# After new cluster start - map size: %d\n", map.size());
        // stop the cluster
        Hazelcast.shutdownAll();
    }

    private static Config createNewConfig(String mapName)
    {
        SimpleStore simpleStore = new SimpleStore();

        MapStoreConfig mapStoreConfig = new MapStoreConfig();
        mapStoreConfig.setImplementation(simpleStore);
        mapStoreConfig.setWriteDelaySeconds(0);

        XmlConfigBuilder configBuilder = new XmlConfigBuilder();
        Config config = configBuilder.build();
        MapConfig mapConfig = config.getMapConfig(mapName);
        mapConfig.setMapStoreConfig(mapStoreConfig);

        return config;
    }

    private static class SimpleStore implements MapStore<Integer, Integer> {

        private ConcurrentMap<Integer, Integer> store = new ConcurrentHashMap<Integer, Integer>();

        @Override
        public void store(Integer key, Integer value) {
            System.out.println("SimpleStore - storing key: " + key);
            store.put(key, value);
        }

        @Override
        public void storeAll(Map<Integer, Integer> map) {
            Set<Map.Entry<Integer, Integer>> entrySet = map.entrySet();
            for (Map.Entry<Integer, Integer> entry : entrySet) {
                Integer key = entry.getKey();
                Integer value = entry.getValue();
                store(key, value);
            }
        }

        @Override
        public void delete(Integer key) {
            System.out.println("SimpleStore - deleting key: " + key);
            store.remove(key);
        }

        @Override
        public void deleteAll(Collection<Integer> keys) {
            for (Integer key : keys) {
                delete(key);
            }
        }

        @Override
        public Integer load(Integer key) {
            System.out.println("SimpleStore - loading value for key: " + key);
            return store.get(key);
        }

        @Override
        public Map<Integer, Integer> loadAll(Collection<Integer> keys) {
            System.out.println("SimpleStore - load map, keys and values");
            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            for (Integer key : keys) {
                Integer value = load(key);
                map.put(key, value);
            }
            return map;
        }

        @Override
        public Set<Integer> loadAllKeys() {
            System.out.println("SimpleStore - loading all keys");
            Set<Integer> ks = store.keySet();
            System.out.println("...Keyset: "+ks);
            return ks;
        }
    }
}
