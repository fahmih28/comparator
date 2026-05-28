package com.rabbani.sp.processor;

import java.util.HashMap;
import java.util.Map;

public class SensitiveHashMap<K,V> extends HashMap<K,V> {

    public SensitiveHashMap(Map<K,V> wrapped){
        super(wrapped);
    }

    @Override
    public V get(Object key){
        V value = super.get(key);
        if(value == null){
            throw new IllegalArgumentException("cannot find any mapping value for '"+key+"'");
        }
        return value;
    }
}
