package com.rabbani.sp.core;

import com.rabbani.sp.core.path.Path;

import java.util.List;

public interface Result {

    Path path();

    Type type();

    boolean isTheSame();

    List<Result> subs();

    Value<?> value();

    interface Value<T> {

        T value();

        T newValue();

        Class<? extends T> type();
    }

    enum Type{
        ROOT,
        FIELD,
        ARRAY,
        COLLECTION,
        KEY;
    }
}
