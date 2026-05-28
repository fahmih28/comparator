package com.rabbani.sp.core;

import com.rabbani.sp.core.path.Path;

import java.util.List;

record ResultImpl(
        Path path,
        Result.Value<?> value,
        List<Result> subs,
        boolean isTheSame,
        Result.Type type
) implements Result {
}
