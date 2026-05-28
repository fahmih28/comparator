package com.rabbani.sp.core;

record ValueImpl<T>(T value, T newValue, Class<? extends T> type)
        implements Result.Value<T> {
}
