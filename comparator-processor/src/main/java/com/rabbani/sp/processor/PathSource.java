package com.rabbani.sp.processor;

import com.rabbani.sp.core.Result;

class PathSource {
    final static PathSource root = of("$", Result.Type.ROOT);

    String value;

    Result.Type type;

    static PathSource of(String value, Result.Type type) {

        PathSource source = new PathSource();

        source.value = value;

        source.type = type;

        return source;

    }
}
