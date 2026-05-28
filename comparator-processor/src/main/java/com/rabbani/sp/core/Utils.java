package com.rabbani.sp.core;

import com.rabbani.sp.core.path.*;

import java.util.List;

public abstract class Utils {

    private static final RootPath ROOT_PATH = new PathsImpl.RootPathImpl();

    public static <T> Result.Value<T> newValue(
            T value,
            T newValue,
            Class<? extends T> type
    ) {
        return new ValueImpl<>(value, newValue, type);
    }

    public static Result newResult(
            Path path,
            Result.Value<?> value,
            List<Result> fields,
            boolean isSame,
            Result.Type type
    ) {
        return new ResultImpl(
                path,
                value,
                fields,
                isSame,
                type
        );
    }

    public static FieldPath newFieldPath(String name, Path parent) {
        return new PathsImpl.FieldPathImpl(name, parent);
    }

    public static KeyPath newKeyPath(String name, Path parent) {
        return new PathsImpl.KeyPathImpl(name, parent);
    }

    public static ArrayPath newArrayPath(int index, Path parent) {
        return new PathsImpl.ArrayPathImpl(index, parent);
    }

    public static CollectionPath newCollectionPath(int index, Path parent) {
        return new PathsImpl.CollectionPathImpl(index, parent);
    }

    public static RootPath rootPath() {
        return ROOT_PATH;
    }

    public static Context newContext(Path path){
        return new ContextImpl(path);
    }
}
