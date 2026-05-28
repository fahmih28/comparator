package com.rabbani.sp.core;

import com.rabbani.sp.core.path.Path;

class ContextImpl implements Context {

    Path path;

    ContextImpl(Path path) {
        this.path = path;
    }
     
    @Override
    public Path path() {
        return path;
    }
}
