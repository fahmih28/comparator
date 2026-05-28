package com.rabbani.sp.core.path;

public interface Path {

    Path parent();

    String relativePath();

    String canonicalPath();

}
