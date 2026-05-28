package com.rabbani.sp.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

public class JavaUtils {
    private final TypeElement arrayListElement;

    private final TypeElement setElement;

    private final TypeElement hashSetElement;

    private final TypeElement classTypeElement;

    private final TypeElement iteratorElement;

    private final TypeMirror booleanType;

    private final TypeMirror collectionWildcardType;

    private final TypeMirror mapWildcardType;

    private final TypeMirror comparableWildcardType;

    private final Elements elementUtils;

    private final Types typeUtils;

    public JavaUtils(ProcessingEnvironment processingEnvironment) {
        elementUtils = processingEnvironment.getElementUtils();

        typeUtils = processingEnvironment.getTypeUtils();

        WildcardType
                wildcardType = typeUtils.getWildcardType(null,null);

        TypeElement comparableTypeElement = elementUtils
                .getTypeElement(Comparable.class.getName());

        arrayListElement = elementUtils
                .getTypeElement(ArrayList.class.getName());

        TypeElement collectionElement = elementUtils
                .getTypeElement(Collection.class.getCanonicalName());

        collectionWildcardType = typeUtils
                .getDeclaredType(
                        collectionElement,
                        wildcardType
                );

        iteratorElement = elementUtils
                .getTypeElement(Iterator.class.getCanonicalName());

        TypeElement mapElement = elementUtils
                .getTypeElement(Map.class.getCanonicalName());

        mapWildcardType = typeUtils
                .getDeclaredType(
                        mapElement,
                        wildcardType,
                        wildcardType
                );

        setElement = elementUtils
                .getTypeElement(Set.class.getCanonicalName());

        hashSetElement = elementUtils
                .getTypeElement(HashSet.class.getCanonicalName());

        classTypeElement = elementUtils.getTypeElement(
                Class.class.getCanonicalName()
        );

        booleanType = typeUtils.getPrimitiveType(TypeKind.BOOLEAN);

        comparableWildcardType = typeUtils.getDeclaredType(comparableTypeElement,wildcardType);

    }

    public TypeMirror booleanType() {
        return booleanType;
    }

    public TypeMirror classOf(TypeMirror targetType) {
        return typeUtils.getDeclaredType(classTypeElement, targetType);
    }

    public TypeMirror iteratorOf(TypeMirror targetType){
        return typeUtils.getDeclaredType(iteratorElement,targetType);
    }

    public TypeMirror setOf(TypeMirror targetType){
        return typeUtils.getDeclaredType(setElement,targetType);
    }

    public TypeMirror hashSetOf(TypeMirror targetType){
        return typeUtils.getDeclaredType(hashSetElement,targetType);
    }

    public TypeMirror arrayListOf(TypeMirror targetType){
        return typeUtils.getDeclaredType(arrayListElement,targetType);
    }

    public TypeMirror boxIfPrimitive(TypeMirror targetType){
        if (targetType.getKind().isPrimitive()) {
            return typeUtils.boxedClass((PrimitiveType) targetType).asType();
        }
        return targetType;
    }

    public boolean isCollection(TypeMirror targetType) {
        return typeUtils.isSubtype(targetType, collectionWildcardType);
    }

    public boolean isMap(TypeMirror targetType) {
        return typeUtils.isSubtype(targetType, mapWildcardType);
    }

    public boolean isComparable(TypeMirror targetType){
        return typeUtils.isSubtype(targetType,comparableWildcardType);
    }

}
