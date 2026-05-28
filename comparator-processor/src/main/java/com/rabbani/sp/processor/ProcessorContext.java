package com.rabbani.sp.processor;

import com.squareup.javapoet.CodeBlock;

import javax.lang.model.element.TypeElement;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

public class ProcessorContext {
    TypeElement srcElement;

    CodeBlock.Builder codes;

    Identifier identifier;

    Set<Method> importStatics;

    Map<String, FieldIdentifier> lazyInitializedTypes;
}
