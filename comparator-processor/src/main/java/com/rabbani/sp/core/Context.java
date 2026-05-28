package com.rabbani.sp.core;

import com.rabbani.sp.core.path.Path;

public interface Context {

    Context empty = new ContextImpl(null);

    Path path();

}
