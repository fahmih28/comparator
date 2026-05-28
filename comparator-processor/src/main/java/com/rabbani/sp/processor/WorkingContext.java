package com.rabbani.sp.processor;

import javax.lang.model.element.Element;
import java.util.List;

class WorkingContext {
    Element field;

    String pathIdentifier;

    String listResultSubIdentifier;

    List<String> isSameIdenfitierRegistries;

    static WorkingContext of(
            Element field,
            String pathIdentifier,
            String listResultSubIdentifier,
            List<String> isSameIdenfitierRegistries
    ) {
        WorkingContext fieldContext = new WorkingContext();

        fieldContext.field = field;

        fieldContext.pathIdentifier = pathIdentifier;

        fieldContext.listResultSubIdentifier = listResultSubIdentifier;

        fieldContext.isSameIdenfitierRegistries = isSameIdenfitierRegistries;

        return fieldContext;
    }
}
