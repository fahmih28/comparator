package com.rabbani.sp.processor;

import javax.lang.model.type.TypeMirror;

 class FieldIdentifier {
    String name;

    TypeMirror type;

    static FieldIdentifier of(String name, TypeMirror type) {

        FieldIdentifier identifier = new FieldIdentifier();

        identifier.name = name;

        identifier.type = type;

        return identifier;
    }

}
