package com.rabbani.sp.processor;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProcessorUtils {
    private final Elements elementUtils;

    public ProcessorUtils(ProcessingEnvironment processingEnvironment) {
        this.elementUtils = processingEnvironment.getElementUtils();
    }

    private <R> R mapEnclosedElements(
            TypeElement targetElement,
            Predicate<Element> preFilter,
            Predicate<ExecutableElement> filter,
            Collector<ExecutableElement, ?, R> collector) {
        Stream<ExecutableElement> executableElementStream = elementUtils
                .getAllMembers(targetElement)
                .stream()
                .filter(preFilter)
                .map(ExecutableElement.class::cast);

        if (filter != null) {
            executableElementStream = executableElementStream.filter(filter);
        }
        return executableElementStream
                .collect(collector);
    }

    public Map<String, ExecutableElement> mapAllMethodFlat(TypeElement targetElement) {
        return new SensitiveHashMap<>(
                mapEnclosedElements(
                        targetElement,
                        element -> element.getKind() == ElementKind.METHOD,
                        null,
                        Collectors.toMap(
                                executableElement -> executableElement.getSimpleName().toString(),
                                Function.identity(),
                                (oldOne, _) -> oldOne
                        )
                )
        );
    }
}
