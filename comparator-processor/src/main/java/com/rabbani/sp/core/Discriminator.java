package com.rabbani.sp.core;

public interface Discriminator<T> {

    Result discriminate(T value, T newValue, Context context);

    Result discriminate(T value, T newValue);

    Class<T> type();
}
