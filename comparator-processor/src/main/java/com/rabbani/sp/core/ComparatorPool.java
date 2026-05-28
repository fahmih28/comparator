package com.rabbani.sp.core;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class ComparatorPool {

    private static final ComparatorPool INSTANCE;

    static{
        INSTANCE = new ComparatorPool();
    }

    public final Map<Class<?>, Discriminator<?>> cache;

    private ComparatorPool() {
        cache = new HashMap<>();
        loadFromServiceProvider();
    }
    public static ComparatorPool getInstance() {
        return INSTANCE;
    }

    private void loadFromServiceProvider(){
        ServiceLoader
                .load(Discriminator.class)
                .forEach(discriminator ->
                        cache.put(discriminator.type(), discriminator)
                );
    }

    public <T> Discriminator<T> get(Class<T> tClass) {
        try {

            Discriminator<T> instance = (Discriminator<T>) cache.get(tClass);
            if (instance == null) {
                throw new IllegalArgumentException(
                        "there is no discriminator for '" + tClass.getCanonicalName() + "'"
                );
            }

            cache.putIfAbsent(
                    tClass,
                    instance
            );
            return instance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
