package com.rabbani.sp.processor;

import com.rabbani.sp.annotation.Ignore;
import com.rabbani.sp.annotation.Traverse;
import com.rabbani.sp.core.*;
import com.rabbani.sp.core.path.Path;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes({Constants.IMPL_COMPARABLE_QUALIFIED_NAME})
public class CoreProcessor extends AbstractProcessor {

    private static final String DISCRIMINATOR_METHOD_NAME = "discriminate";

    private static final String RESULT_IDENTIFIER = "_result";

    private static final String CURRENT_VALUE_ITERATOR_IDENTIFIER = "_currentValueIterator";

    private static final String NEW_VALUE_ITERATOR_IDENTIFIER = "_newValueIterator";

    private static final String IS_SAME_IDENTIFIER = "_isSame";

    private static final String VALUE_IDENTIFIER = "value";

    private static final String NEW_VALUE_IDENTIFIER = "newValue";

    private static final String CONTEXT_IDENTIFIER = "context";

    private static final String RESULT_SUBS_IDENTIFIER = "_result_sub";

    private static final String CLASS_IMPLEMENTATION_SUFFIX = "Discriminator";

    private static final Set<Modifier> NOT_ACCEPTABLE_MODIFERS = Set.of(
            Modifier.STATIC,
            Modifier.FINAL,
            Modifier.PRIVATE
    );

    private static final String PATH_VARIABLE = "path";

    public static final String MAX_VARIABLE = "max";

    private Elements elementUtils;

    private Types typeUtils;

    private Log log;

    private TypeElement discriminatorTypeElement;

    private TypeMirror resultTypeMirror;

    private TypeMirror pathTypeMirror;

    private TypeMirror arrayListResultSubsTypeMirror;

    private TypeMirror companyFactoryTypeMirror;

    private TypeMirror utilsTypeMirror;

    private ExecutableElement utilsNewResultExecutableElement;

    private ExecutableElement utilsNewContextExecutableElement;

    private ExecutableElement utilsNewValueExecutableElement;

    private ExecutableElement utilsNewFieldPathExecutableElement;

    private ExecutableElement utilsNewArrayPathExecutableElement;

    private ExecutableElement utilsNewKeyPathExecutableElement;

    private ExecutableElement utilsNewCollectionPathExecutableElement;

    private ExecutableElement utilsRootPathExecutableElement;

    private ExecutableElement discriminatorDiscriminateExecutableElement;

    private ExecutableElement discriminatorTypeExecutableElement;

    private ExecutableElement contextPathExecutableElement;

    private ExecutableElement resultSubExecutableElement;

    private ExecutableElement resultIsTheSameExecutableElement;

    private TypeMirror resultTypeTypeMirror;

    private TypeElement resultValueTypeElement;

    private JavaUtils javaUtils;

    private ProcessorUtils processorUtils;

    private Writer discriminatorServiceFile;

    private int lineDiscriminatorServiceFile;

    private Map<String, String> discriminatorImplementationRegistries = new HashMap<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        annotations
                .stream()
                .map(roundEnv::getElementsAnnotatedWith)
                .flatMap(Collection::stream)
                .map(TypeElement.class::cast)
                .forEach(this::proceedEntity);

        return false;
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        initUtilities();
        initUtilsElements();
        initDiscriminatorElements();
        initContextElements();
        initResultElements();
        initDomainTypes();
        initMetaInf();
    }

    private void initUtilities() {
        elementUtils = processingEnv.getElementUtils();

        typeUtils = processingEnv.getTypeUtils();

        log = new Log(processingEnv);

        discriminatorImplementationRegistries = new HashMap<>();

        javaUtils = new JavaUtils(processingEnv);

        processorUtils = new ProcessorUtils(processingEnv);
    }

    private void initUtilsElements() {
        TypeElement utilsTypeElement = elementUtils.getTypeElement(
                Utils.class.getCanonicalName()
        );

        Map<String, ExecutableElement> utilsExecutableRegistries = processorUtils.mapAllMethodFlat(utilsTypeElement);

        utilsNewResultExecutableElement = utilsExecutableRegistries.get("newResult");

        utilsNewContextExecutableElement = utilsExecutableRegistries.get("newContext");

        utilsNewValueExecutableElement = utilsExecutableRegistries.get("newValue");

        utilsNewFieldPathExecutableElement = utilsExecutableRegistries.get("newFieldPath");

        utilsNewArrayPathExecutableElement = utilsExecutableRegistries.get("newArrayPath");

        utilsNewKeyPathExecutableElement = utilsExecutableRegistries.get("newKeyPath");

        utilsNewCollectionPathExecutableElement = utilsExecutableRegistries.get("newCollectionPath");

        utilsRootPathExecutableElement = utilsExecutableRegistries.get("rootPath");

        utilsTypeMirror = utilsTypeElement
                .asType();
    }

    private void initDiscriminatorElements() {
        discriminatorTypeElement = elementUtils.getTypeElement(Discriminator.class.getCanonicalName());

        Map<String, ExecutableElement> discriminatorExecutableRegistries = processorUtils.mapAllMethodFlat(discriminatorTypeElement);

        discriminatorDiscriminateExecutableElement = discriminatorExecutableRegistries.get("discriminate");

        discriminatorTypeExecutableElement = discriminatorExecutableRegistries.get("type");
    }

    private void initContextElements() {
        TypeElement contextTypeElement = elementUtils.getTypeElement(Context.class.getCanonicalName());

        Map<String, ExecutableElement> contextExecutableRegistries = processorUtils.mapAllMethodFlat(contextTypeElement);

        contextPathExecutableElement = contextExecutableRegistries.get("path");
    }


    private void initResultElements() {
        TypeElement resultTypeElement = elementUtils.getTypeElement(Result.class.getName());

        Map<String, ExecutableElement> contextExecutableRegistries = processorUtils.mapAllMethodFlat(resultTypeElement);

        resultSubExecutableElement = contextExecutableRegistries.get("subs");

        resultIsTheSameExecutableElement = contextExecutableRegistries.get("isTheSame");

        resultTypeMirror = resultTypeElement.asType();
    }

    private void initDomainTypes() {
        pathTypeMirror = elementUtils
                .getTypeElement(Path.class.getName())
                .asType();

        companyFactoryTypeMirror = elementUtils
                .getTypeElement(ComparatorPool.class.getCanonicalName())
                .asType();

        arrayListResultSubsTypeMirror = javaUtils.arrayListOf(resultTypeMirror);

        resultTypeTypeMirror = elementUtils.getTypeElement(
                        Result.Type.class.getCanonicalName()
                )
                .asType();

        resultValueTypeElement = elementUtils.getTypeElement(
                Result.Value.class.getCanonicalName()
        );
    }

    private void initMetaInf() {
        try {
            if (discriminatorServiceFile != null) {
                return;
            }
            TypeMirror discriminatorTypeMirror = typeUtils.erasure(discriminatorTypeElement.asType());
            discriminatorServiceFile = processingEnv
                    .getFiler()
                    .createResource(
                            StandardLocation.CLASS_OUTPUT,
                            "",
                            "META-INF/services/" + discriminatorTypeMirror.toString()
                    )
                    .openWriter();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void proceedEntity(
            TypeElement annotatedTypeElement
    ) {
        try {
            validate(annotatedTypeElement);
            writeImplementedCode(annotatedTypeElement);
        } catch (Exception e) {
            log.error(
                    "Error generate discriminator of %s, cause : %s ",
                    e,
                    annotatedTypeElement.getQualifiedName(),
                    e.getMessage()


            );
        }
    }

    private void validate(
            TypeElement targetType
    ) {

        ElementKind typeKind = targetType.getKind();
        if (typeKind != ElementKind.INTERFACE && typeKind != ElementKind.CLASS) {
            String errorMessage = String.format(
                    "Cannot implement discrimination for '%s' not support for %s",
                    targetType.getQualifiedName(),
                    typeKind.name()
            );
            log.error(errorMessage);
            throw new IllegalStateException(errorMessage);
        }
    }

    private void writeImplementedCode(TypeElement sourceType) throws IOException {
        PackageElement packageElement = elementUtils.getPackageOf(sourceType);
        String packageName = packageElement
                .getQualifiedName()
                .toString();
        String className = sourceType.getSimpleName() + CLASS_IMPLEMENTATION_SUFFIX;

        String qualifiedClassName = packageName + "." + className;
        boolean shallProceed = registerClassImplementaton(sourceType.asType(), qualifiedClassName);
        if (!shallProceed) {
            return;
        }

        registerForServiceLoader(qualifiedClassName);

        JavaFile javaFile = JavaFile.builder(
                        packageName,
                        writeTypeSpec(
                                sourceType,
                                className
                        )
                )
                .addStaticImport(Optional.class, "ofNullable")
                .build();
        javaFile.writeTo(processingEnv.getFiler());

    }

    private synchronized boolean registerClassImplementaton(TypeMirror type, String qualifiedClassName) {
        if (discriminatorImplementationRegistries.containsKey(type.toString())) {
            return false;
        }

        discriminatorImplementationRegistries.put(type.toString(), qualifiedClassName);
        return true;
    }

    private void registerForServiceLoader(String className) {
        try {
            if (lineDiscriminatorServiceFile++ > 0) {
                discriminatorServiceFile.write("\n");
            }

            discriminatorServiceFile
                    .write(className);
            discriminatorServiceFile.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private TypeSpec writeTypeSpec(TypeElement sourceType, String className) {
        TypeMirror sourceTypeMirror = sourceType.asType();
        DeclaredType targetType = typeUtils.getDeclaredType(
                discriminatorTypeElement,
                sourceTypeMirror
        );

        Map<String, FieldIdentifier> lazyInitializedTypes = new HashMap<>();

        TypeSpec.Builder builder = TypeSpec
                .classBuilder(className)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(targetType)
                .addMethod(implementDiscriminateWithContext(
                                targetType,
                                sourceType,
                                lazyInitializedTypes
                        )
                )
                .addMethod(implementDiscriminateWithNoContext(
                                targetType
                        )
                )
                .addMethod(implementDiscriminateType(
                                sourceTypeMirror
                        )
                );

        implementDependentDiscriminatorGetter(
                lazyInitializedTypes,
                builder
        );

        implementDependentDiscriminatorFields(
                lazyInitializedTypes,
                builder
        );

        return builder.build();
    }

    private MethodSpec implementDiscriminateType(TypeMirror targetType) {
        return MethodSpec
                .methodBuilder(discriminatorTypeExecutableElement.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(
                        TypeName.get(
                                javaUtils.classOf(targetType)
                        )
                )
                .addAnnotation(Override.class)
                .addStatement(
                        "return $L.class",
                        targetType
                )
                .build();
    }

    private void implementDependentDiscriminatorFields(
            Map<String, FieldIdentifier> lazyInitializedTypes,
            TypeSpec.Builder builder
    ) {
        lazyInitializedTypes
                .forEach((_, fieldIdentifier) ->
                        builder.addField(FieldSpec
                                .builder(
                                        TypeName.get(
                                                typeUtils.getDeclaredType(
                                                        discriminatorTypeElement,
                                                        fieldIdentifier.type
                                                )
                                        ),
                                        fieldIdentifier.name,
                                        Modifier.PRIVATE
                                )
                                .build()
                        ));
    }

    private void implementDependentDiscriminatorGetter(
            Map<String, FieldIdentifier> lazyInitializedTypes,
            TypeSpec.Builder builder
    ) {
        lazyInitializedTypes
                .forEach((_, identifier) -> {
                            DeclaredType declaredType = typeUtils.getDeclaredType(
                                    discriminatorTypeElement,
                                    identifier.type
                            );

                            builder.addMethod(
                                    MethodSpec
                                            .methodBuilder(identifier.name)
                                            .returns(TypeName.get(declaredType))
                                            .addModifiers(Modifier.PRIVATE)
                                            .addCode(
                                                    CodeBlock.builder()
                                                            .beginControlFlow(
                                                                    "if($L == null)",
                                                                    identifier.name
                                                            )
                                                            .addStatement(
                                                                    "$L = $L.getInstance().get($L.class)",
                                                                    identifier.name,
                                                                    companyFactoryTypeMirror,
                                                                    identifier.type.toString())
                                                            .endControlFlow()
                                                            .addStatement("return $L", identifier.name)
                                                            .build()
                                            )
                                            .build()

                            );
                        }
                );
    }

    private MethodSpec implementDiscriminateWithNoContext(
            DeclaredType targetType
    ) {
        ExecutableType implementedMethodNoContext = (ExecutableType) typeUtils.asMemberOf(
                targetType,
                discriminatorDiscriminateExecutableElement
        );
        return MethodSpec
                .methodBuilder(
                        discriminatorDiscriminateExecutableElement
                                .getSimpleName()
                                .toString()
                )
                .addAnnotation(Override.class)
                .returns(TypeName.get(implementedMethodNoContext.getReturnType()))
                .addParameter(
                        TypeName.get(implementedMethodNoContext
                                .getParameterTypes()
                                .get(0)),
                        VALUE_IDENTIFIER
                )
                .addParameter(
                        TypeName.get(implementedMethodNoContext
                                .getParameterTypes()
                                .get(1)),
                        NEW_VALUE_IDENTIFIER
                )
                .addModifiers(Modifier.PUBLIC)
                .addCode(implementCallOverloadCodes())
                .build();
    }


    private MethodSpec implementDiscriminateWithContext(
            DeclaredType targetType,
            TypeElement sourceType,
            Map<String, FieldIdentifier> lazyInitializedTypes
    ) {
        ExecutableType implementedMethodWithContext = (ExecutableType) typeUtils.asMemberOf(
                targetType,
                discriminatorDiscriminateExecutableElement
        );

        return MethodSpec
                .methodBuilder(
                        discriminatorDiscriminateExecutableElement
                                .getSimpleName()
                                .toString()
                )
                .addAnnotation(Override.class)
                .returns(TypeName.get(implementedMethodWithContext.getReturnType()))
                .addParameter(
                        TypeName.get(implementedMethodWithContext
                                .getParameterTypes()
                                .get(0)),
                        VALUE_IDENTIFIER
                )
                .addParameter(
                        TypeName.get(implementedMethodWithContext
                                .getParameterTypes()
                                .get(1)),
                        NEW_VALUE_IDENTIFIER
                )
                .addParameter(
                        TypeName.get(implementedMethodWithContext
                                .getParameterTypes()
                                .get(2)),
                        CONTEXT_IDENTIFIER
                )
                .addModifiers(Modifier.PUBLIC)
                .addCode(implementTheCode(
                        sourceType,
                        lazyInitializedTypes
                ))
                .build();
    }


    private CodeBlock implementCallOverloadCodes() {
        return CodeBlock.builder()
                .addStatement(
                        "return $L($L,$L,$L.$L(null))",
                        discriminatorDiscriminateExecutableElement.getSimpleName(),
                        VALUE_IDENTIFIER,
                        NEW_VALUE_IDENTIFIER,
                        utilsTypeMirror,
                        utilsNewContextExecutableElement.getSimpleName()
                )
                .build();
    }

    private CodeBlock implementTheCode(TypeElement element, Map<String, FieldIdentifier> lazyInitializedTypes) {
        Identifier identifier = new Identifier();
        CodeBlock.Builder codes = CodeBlock.builder();

        ProcessorContext processorContext = new ProcessorContext();
        processorContext.srcElement = element;
        processorContext.identifier = identifier;
        processorContext.codes = codes;
        processorContext.importStatics = new HashSet<>();
        processorContext.lazyInitializedTypes = lazyInitializedTypes;
        writeMethod(processorContext);
        return codes.build();
    }


    private void writeMethod(ProcessorContext processorCtx) {
        CodeBlock.Builder codes = processorCtx.codes;
        String pathIdentifier = writePathIdentifier(processorCtx, null, PathSource.root);
        List<String> isSameIdentifierRegistries = List.of(writeIsSameIdentifier(processorCtx));
        String listResultSubIdentifier = writeResultSubsIdentifier(processorCtx);

        List<? extends Element> enclosedElements = elementUtils.getAllMembers(processorCtx.srcElement);
        log.info(
                "found all member of %s, there are %d member(s)",
                processorCtx
                        .srcElement
                        .getQualifiedName(),
                enclosedElements.size()
        );

        ElementKind srcElementKind = processorCtx.srcElement.getKind();
        for (Element enclosedElement : enclosedElements) {
            if (!isAcceptable(enclosedElement, srcElementKind)) {
                log.warning("skip field %s", enclosedElement.getSimpleName());
                continue;
            }
            writeFieldComparison(
                    processorCtx,
                    srcElementKind,
                    WorkingContext.of(enclosedElement, pathIdentifier, listResultSubIdentifier, isSameIdentifierRegistries)
            );
        }

        String resultIdentifier = writeResult(
                processorCtx,
                WorkingContext.of(null, pathIdentifier, listResultSubIdentifier, isSameIdentifierRegistries),
                VALUE_IDENTIFIER,
                NEW_VALUE_IDENTIFIER,
                processorCtx.srcElement.asType(),
                Result.Type.ROOT

        );
        codes.addStatement("return $L", resultIdentifier);
    }

    private void writeFieldComparison(
            ProcessorContext processorCtx,
            ElementKind elementKind,
            WorkingContext ctx
    ) {
        Identifier identifier = processorCtx.identifier;
        CodeBlock.Builder codes = processorCtx.codes;
        Element element = ctx.field;
        TypeMirror targetTypeMirror;
        String objectAccessor;

        if (elementKind == ElementKind.CLASS) {
            VariableElement fieldElement = (VariableElement) element;
            targetTypeMirror = javaUtils.boxIfPrimitive(fieldElement.asType());
            objectAccessor = fieldElement.getSimpleName().toString();
        } else {
            ExecutableElement executableElement = (ExecutableElement) element;
            targetTypeMirror = javaUtils.boxIfPrimitive(executableElement.getReturnType());
            objectAccessor = executableElement.getSimpleName() + "()";
        }

        add(processorCtx, "\n//start section of comparison for field *" + ctx.field.getSimpleName() + "*");
        String currentValueIdentifier = identifier.makeIdentifier(VALUE_IDENTIFIER);
        codes.addStatement(
                """
                        $1L $2L = $3L != null
                                    ?$3L.$4L
                                    :null""",
                targetTypeMirror,
                currentValueIdentifier,
                VALUE_IDENTIFIER,
                objectAccessor);
        String newValueIdentifier = identifier.makeIdentifier(NEW_VALUE_IDENTIFIER);
        codes.addStatement(
                """
                        $1L $2L = $3L != null
                                    ?$3L.$4L
                                    :null""",
                targetTypeMirror,
                newValueIdentifier,
                NEW_VALUE_IDENTIFIER,
                objectAccessor);
        String pathIdentifier = writePathIdentifier(
                processorCtx,
                ctx,
                PathSource.of(element.getSimpleName().toString(), Result.Type.FIELD)
        );

        List<String> isSameIdentifierRegistries = appendIsSameIdentifierRegistries(processorCtx, ctx);
        WorkingContext subCtx = WorkingContext.of(ctx.field, pathIdentifier, null, isSameIdentifierRegistries);
        expandFieldImplementation(
                processorCtx,
                targetTypeMirror,
                currentValueIdentifier,
                newValueIdentifier,
                subCtx
        );
        appendResult(
                processorCtx,
                ctx,
                subCtx,
                currentValueIdentifier,
                newValueIdentifier,
                element.asType(),
                Result.Type.FIELD
        );
        add(processorCtx, "//end section comparison process for *" + ctx.field.getSimpleName() + "*\n");
    }

    private void add(ProcessorContext context, String... messages) {
        CodeBlock.Builder code = context.codes;
        for (String message : messages) {
            code.add("$L\n", message);
        }
    }

    private String writeResult(
            ProcessorContext processorCtx,
            WorkingContext ctx,
            String currentIdentifier,
            String newIdentifier,
            TypeMirror valueType,
            Result.Type type
    ) {
        CodeBlock.Builder codes = processorCtx.codes;
        Identifier identifier = processorCtx.identifier;

        String resultValueIdentifier = identifier.makeIdentifier(VALUE_IDENTIFIER);
        TypeMirror boxValueType = typeUtils.erasure(javaUtils.boxIfPrimitive(valueType));

        codes.addStatement(
                """
                        $L $L = $L.$L(
                                $L,
                                $L,
                                $L.class
                                )""",
                typeUtils.getDeclaredType(resultValueTypeElement, typeUtils.getWildcardType(boxValueType, null)),
                resultValueIdentifier,
                utilsTypeMirror,
                utilsNewValueExecutableElement.getSimpleName(),
                currentIdentifier,
                newIdentifier,
                boxValueType
        );


        String resultIdentifier = identifier.makeIdentifier(RESULT_IDENTIFIER);
        codes.addStatement("""
                        $L $L = $L.$L(
                            $L,
                            $L,
                            $L,
                            $L,
                            $L.$L
                        )""",
                resultTypeMirror,
                resultIdentifier,
                utilsTypeMirror,
                utilsNewResultExecutableElement.getSimpleName(),
                ctx.pathIdentifier,
                resultValueIdentifier,
                String.valueOf(ctx.listResultSubIdentifier),
                ctx.isSameIdenfitierRegistries.getLast(),
                resultTypeTypeMirror,
                type.name()
        );
        return resultIdentifier;
    }

    private void appendResult(
            ProcessorContext processorCtx,
            WorkingContext ctx,
            WorkingContext subCtx,
            String currentIdentifier,
            String newIdentifier,
            TypeMirror valueType,
            Result.Type type
    ) {
        CodeBlock.Builder codes = processorCtx.codes;

        String resultIdentifier = writeResult(
                processorCtx,
                subCtx,
                currentIdentifier,
                newIdentifier,
                valueType,
                type
        );

        codes.addStatement("$L.add($L)", ctx.listResultSubIdentifier, resultIdentifier);

    }

    private void expandFieldImplementation(
            ProcessorContext processorCtx,
            TypeMirror targetType,
            String currentValueIdentifier,
            String newValueIdentifier,
            WorkingContext ctx
    ) {
        if (targetType instanceof ArrayType) {
            ctx.listResultSubIdentifier = writeResultSubsIdentifier(processorCtx);
            writeArray(
                    processorCtx,
                    ctx,
                    targetType,
                    currentValueIdentifier,
                    newValueIdentifier
            );

        } else if (javaUtils.isCollection(targetType)) {
            ctx.listResultSubIdentifier = writeResultSubsIdentifier(processorCtx);
            writeCollection(
                    processorCtx,
                    ctx,
                    targetType,
                    currentValueIdentifier,
                    newValueIdentifier
            );

        } else if (javaUtils.isMap(targetType)) {
            ctx.listResultSubIdentifier = writeResultSubsIdentifier(processorCtx);
            writeMap(
                    processorCtx,
                    ctx,
                    targetType,
                    currentValueIdentifier,
                    newValueIdentifier
            );

        } else {
            writeSmallestPart(
                    processorCtx,
                    ctx,
                    targetType,
                    currentValueIdentifier,
                    newValueIdentifier
            );

        }
    }

    private void writeArray(
            ProcessorContext processorCtx,
            WorkingContext ctx,
            TypeMirror targetType,
            String currentValueIdentifier,
            String newValueIdentifier
    ) {
        Identifier identifier = processorCtx.identifier;
        CodeBlock.Builder codes = processorCtx.codes;
        ArrayType arrayType = (ArrayType) targetType;

        TypeMirror elementType = javaUtils.boxIfPrimitive(arrayType.getComponentType());
        String maxIdentifier = identifier.makeIdentifier("max");
        codes.addStatement("int $L = 0", maxIdentifier);
        codes.add("\n");
        codes.beginControlFlow(
                "if($1L != null && $1L.length > $2L)",
                currentValueIdentifier,
                maxIdentifier
        );
        codes.addStatement(
                "$L = $L.length",
                maxIdentifier,
                currentValueIdentifier
        );
        codes.endControlFlow();

        codes.add("\n");
        codes.beginControlFlow(
                "if($1L != null && $1L.length > $2L)",
                newValueIdentifier,
                maxIdentifier
        );
        codes.addStatement(
                "$L = $L.length",
                maxIdentifier,
                newValueIdentifier
        );
        codes.endControlFlow();

        String iIdentifier = identifier.makeIdentifier("i");
        codes.add("\n");
        codes.beginControlFlow(
                "for(int $1L = 0;$1L < $2L;$1L++)",
                iIdentifier,
                maxIdentifier
        );

        String nestedCurrentValueIdentifier = identifier.makeIdentifier(VALUE_IDENTIFIER);
        String nestedNewValueIdentifier = identifier.makeIdentifier(NEW_VALUE_IDENTIFIER);

        codes.addStatement(
                """
                        $1L $2L = ($3L != null && $3L.length > $4L)?
                                        $3L[$4L]:
                                        null""",
                elementType,
                nestedCurrentValueIdentifier,
                currentValueIdentifier,
                iIdentifier
        );

        codes.addStatement(
                """
                        $1L $2L = ($3L != null && $3L.length > $4L)?
                                        $3L[$4L]:
                                        null""",
                elementType,
                nestedNewValueIdentifier,
                newValueIdentifier,
                iIdentifier
        );

        String pathIdentifier = writePathIdentifier(
                processorCtx,
                ctx,
                PathSource.of(iIdentifier, Result.Type.ARRAY)
        );

        List<String> isSameIdentifierRegistries = appendIsSameIdentifierRegistries(processorCtx, ctx);

        WorkingContext subCtx = WorkingContext.of(
                ctx.field,
                pathIdentifier,
                null,
                isSameIdentifierRegistries
        );

        expandFieldImplementation(processorCtx,
                javaUtils.boxIfPrimitive(elementType),
                nestedCurrentValueIdentifier,
                nestedNewValueIdentifier,
                subCtx
        );

        appendResult(
                processorCtx,
                ctx,
                subCtx,
                nestedCurrentValueIdentifier,
                nestedNewValueIdentifier,
                elementType,
                Result.Type.ARRAY
        );

        codes.endControlFlow();
    }

    private void writeCollection(
            ProcessorContext processorCtx,
            WorkingContext ctx,
            TypeMirror targetType,
            String currentValueIdentifier,
            String newValueIdentifier
    ) {
        Identifier identifier = processorCtx.identifier;
        CodeBlock.Builder codes = processorCtx.codes;
        DeclaredType parameterType = (DeclaredType) targetType;
        TypeMirror elementType = parameterType.getTypeArguments().getFirst();
        String maxIdentifier = identifier.makeIdentifier(MAX_VARIABLE);
        codes.addStatement("int $L = 0", maxIdentifier);

        codes.add("\n");
        codes.beginControlFlow(
                "if($1L != null && $1L.size() > $2L)",
                currentValueIdentifier,
                maxIdentifier
        );
        codes.addStatement(
                "$1L = $2L.size()",
                maxIdentifier,
                currentValueIdentifier
        );
        codes.endControlFlow();


        codes.add("\n");
        codes.beginControlFlow(
                "if($1L != null && $1L.size() > $2L)",
                newValueIdentifier,
                maxIdentifier
        );
        codes.addStatement(
                "$1L = $2L.size()",
                maxIdentifier,
                newValueIdentifier
        );
        codes.endControlFlow();
        codes.add("\n");


        String iteratorCurrentValueIdentifier = identifier.makeIdentifier(CURRENT_VALUE_ITERATOR_IDENTIFIER);

        TypeMirror elementIteratorType = javaUtils.iteratorOf(elementType);
        codes.addStatement(
                """
                        $1L $2L = ($3L != null)?
                                    $3L.iterator()
                                    :null""",
                elementIteratorType,
                iteratorCurrentValueIdentifier,
                currentValueIdentifier
        );

        String iteratorNewValueIdentifier = identifier.makeIdentifier(NEW_VALUE_ITERATOR_IDENTIFIER);

        codes.addStatement(
                """
                        $1L $2L = ($3L != null)?
                                        $3L.iterator()
                                        :null""",
                elementIteratorType,
                iteratorNewValueIdentifier,
                newValueIdentifier
        );

        String iIdentifier = identifier.makeIdentifier("i");
        codes.add("\n");
        codes.beginControlFlow(
                "for(int $1L = 0;$1L < $2L;$1L++)",
                iIdentifier,
                maxIdentifier
        );

        String nestedCurrentValueIdentifier = identifier.makeIdentifier(VALUE_IDENTIFIER);

        codes.addStatement(
                """
                        $1L $2L = ($3L != null && $3L.hasNext())?
                                        $3L.next()
                                        :null""",
                elementType,
                nestedCurrentValueIdentifier,
                iteratorCurrentValueIdentifier
        );

        String nestedNewValueIdentifier = identifier.makeIdentifier(NEW_VALUE_IDENTIFIER);

        codes.addStatement(
                """
                        $1L $2L = ($3L != null && $3L.hasNext())?
                                        $3L.next()
                                        :null""",
                elementType,
                nestedNewValueIdentifier,
                iteratorNewValueIdentifier
        );

        String pathIdentifier = writePathIdentifier(
                processorCtx,
                ctx,
                PathSource.of(iIdentifier, Result.Type.COLLECTION)
        );

        List<String> isSameIdentifierRegistries = appendIsSameIdentifierRegistries(processorCtx, ctx);
        WorkingContext subCtx = WorkingContext.of(ctx.field, pathIdentifier, null, isSameIdentifierRegistries);
        expandFieldImplementation(
                processorCtx,
                elementType,
                nestedCurrentValueIdentifier,
                nestedNewValueIdentifier,
                subCtx
        );

        appendResult(
                processorCtx,
                ctx,
                subCtx,
                nestedCurrentValueIdentifier,
                nestedNewValueIdentifier,
                elementType,
                Result.Type.COLLECTION
        );
        codes.endControlFlow();
    }

    private void writeMap(
            ProcessorContext processorCtx,
            WorkingContext ctx,
            TypeMirror targetType,
            String currentValueIdentifier,
            String newValueIdentifier
    ) {
        Identifier identifier = processorCtx.identifier;
        CodeBlock.Builder codes = processorCtx.codes;
        DeclaredType parameterType = (DeclaredType) targetType;
        String keysAggregate = identifier.makeIdentifier("keys");
        TypeMirror keyType = parameterType
                .getTypeArguments()
                .get(0);

        TypeMirror valueType = parameterType
                .getTypeArguments()
                .get(1);
        codes.addStatement(
                "$1L $2L = new $3L()",
                javaUtils.setOf(keyType),
                keysAggregate,
                javaUtils.hashSetOf(keyType)
        );

        codes.beginControlFlow(
                "if($1L != null && $1L.size() > 0)",
                currentValueIdentifier
        );

        codes.addStatement(
                "$1L.addAll($2L.keySet())",
                keysAggregate,
                currentValueIdentifier
        );
        codes.endControlFlow();

        codes.beginControlFlow(
                "if($1L != null && $1L.size() > 0)",
                newValueIdentifier
        );

        codes.addStatement(
                "$1L.addAll($2L.keySet())",
                keysAggregate,
                newValueIdentifier
        );
        codes.endControlFlow();

        codes.add("\n");

        String keyIdentifier = identifier.makeIdentifier("key");
        codes.beginControlFlow(
                "for($L $L:$L)",
                keyType,
                keyIdentifier,
                keysAggregate
        );

        String nestedCurrentValueIdentifier = identifier.makeIdentifier(
                VALUE_IDENTIFIER
        );

        codes.addStatement(
                """
                        $1L $2L = ($3L != null)?
                                    $3L.get($4L)
                                    :null""",
                valueType,
                nestedCurrentValueIdentifier,
                currentValueIdentifier,
                keyIdentifier
        );

        String nestedNewValueIdentifier = identifier.makeIdentifier(
                NEW_VALUE_IDENTIFIER
        );

        codes.addStatement(
                """
                        $1L $2L = ($3L != null)?
                                    $3L.get($4L)
                                    :null""",
                valueType,
                nestedNewValueIdentifier,
                newValueIdentifier,
                keyIdentifier
        );

        String pathIdentifier = writePathIdentifier(
                processorCtx,
                ctx,
                PathSource.of(keyIdentifier, Result.Type.KEY)
        );

        List<String> isSameIdentifierRegistries = appendIsSameIdentifierRegistries(processorCtx, ctx);
        WorkingContext subCtx = WorkingContext.of(ctx.field, pathIdentifier, null, isSameIdentifierRegistries);
        expandFieldImplementation(
                processorCtx,
                valueType,
                nestedCurrentValueIdentifier,
                nestedNewValueIdentifier,
                subCtx
        );

        appendResult(
                processorCtx,
                ctx,
                subCtx,
                nestedCurrentValueIdentifier,
                nestedNewValueIdentifier,
                valueType,
                Result.Type.KEY
        );
        codes.endControlFlow();
    }

    private String writeIsSameIdentifier(ProcessorContext context) {
        Identifier identifier = context.identifier;
        CodeBlock.Builder codes = context.codes;
        String isSameIdentifier = identifier.makeIdentifier(IS_SAME_IDENTIFIER);
        codes.addStatement(
                "$1L $2L = true",
                javaUtils.booleanType(),
                isSameIdentifier
        );
        return isSameIdentifier;
    }

    private String writeResultSubsIdentifier(ProcessorContext context) {
        Identifier identifier = context.identifier;
        CodeBlock.Builder codes = context.codes;
        String subIdentifier = identifier.makeIdentifier(RESULT_SUBS_IDENTIFIER);
        codes.addStatement(
                "$1L $2L = new $1L()",
                arrayListResultSubsTypeMirror,
                subIdentifier
        );
        return subIdentifier;
    }

    private String writePathIdentifier(
            ProcessorContext ctx,
            WorkingContext parentCtx,
            PathSource path
    ) {
        Identifier identifier = ctx.identifier;
        CodeBlock.Builder codes = ctx.codes;

        String pathIdentifier = identifier.makeIdentifier(
                PATH_VARIABLE
        );

        switch (path.type) {
            case ARRAY -> codes.addStatement(
                    "$L $L = $L.$L($L,$L)",
                    pathTypeMirror,
                    pathIdentifier,
                    utilsTypeMirror,
                    utilsNewArrayPathExecutableElement.getSimpleName(),
                    path.value,
                    parentCtx.pathIdentifier
            );
            case KEY -> codes.addStatement(
                    "$L $L = $L.$L(\"$L\",$L)",
                    pathTypeMirror,
                    pathIdentifier,
                    utilsTypeMirror,
                    utilsNewKeyPathExecutableElement.getSimpleName(),
                    path.value,
                    parentCtx.pathIdentifier
            );
            case COLLECTION -> codes.addStatement(
                    "$L $L = $L.$L($L,$L)",
                    pathTypeMirror,
                    pathIdentifier,
                    utilsTypeMirror,
                    utilsNewCollectionPathExecutableElement.getSimpleName(),
                    path.value,
                    parentCtx.pathIdentifier
            );
            case FIELD -> codes.addStatement(
                    "$L $L = $L.$L(\"$L\",$L)",
                    pathTypeMirror,
                    pathIdentifier,
                    utilsTypeMirror,
                    utilsNewFieldPathExecutableElement.getSimpleName(),
                    path.value,
                    parentCtx.pathIdentifier
            );
            case ROOT -> codes.addStatement(
                    """
                            $L $L = ofNullable($L.$L())
                                                                            .orElse($L.$L())"""
                    , pathTypeMirror
                    , pathIdentifier
                    , CONTEXT_IDENTIFIER
                    , contextPathExecutableElement.getSimpleName()
                    , utilsTypeMirror
                    , utilsRootPathExecutableElement.getSimpleName()
            );
        }
        return pathIdentifier;
    }

    private void writeSmallestPart(
            ProcessorContext processorCtx,
            WorkingContext ctx,
            TypeMirror targetType,
            String currentValueIdentifier,
            String newValueIdentifier
    ) {
        Element element = ctx.field;
        if (element.getAnnotation(Traverse.class) != null) {
            implementCallAnotherDiscriminator(
                    processorCtx,
                    ctx,
                    currentValueIdentifier,
                    newValueIdentifier,
                    targetType
            );
        } else {
            implementFieldDiscriminationLog(
                    processorCtx,
                    ctx,
                    currentValueIdentifier,
                    newValueIdentifier
            );
        }
    }

    private void implementCallAnotherDiscriminator(
            ProcessorContext processorCtx,
            WorkingContext ctx,
            String currentValueIdentifier,
            String newValueIdentifier,
            TypeMirror targetType
    ) {
        Identifier identifier = processorCtx.identifier;
        CodeBlock.Builder codes = processorCtx.codes;
        TypeElement linkedDiscriminatorElement = (TypeElement) typeUtils.asElement(targetType);
        proceedEntity(linkedDiscriminatorElement);

        String linkedDiscriminatorIdentifier = identifier.makeIdentifier(
                linkedDiscriminatorElement.getSimpleName().toString()
        );

        processorCtx
                .lazyInitializedTypes
                .put(
                        targetType.toString(),
                        FieldIdentifier.of(
                                linkedDiscriminatorIdentifier,
                                targetType
                        )
                );

        String resultIdentifier = identifier.makeIdentifier(RESULT_IDENTIFIER);

        codes.addStatement(
                "$L $L = $L().$L($L,$L,$L.$L($L))",
                resultTypeMirror,
                resultIdentifier,
                linkedDiscriminatorIdentifier,
                DISCRIMINATOR_METHOD_NAME,
                currentValueIdentifier,
                newValueIdentifier,
                utilsTypeMirror,
                utilsNewContextExecutableElement.getSimpleName(),
                ctx.pathIdentifier
        );

        codes.beginControlFlow(
                "if(!$L.$L())",
                resultIdentifier,
                resultIsTheSameExecutableElement.getSimpleName()
        );

        for (String affectedSameIdentifier : ctx.isSameIdenfitierRegistries) {
            codes.addStatement("$L = false", affectedSameIdentifier);
        }

        ctx.listResultSubIdentifier = String.format(
                "%s.%s()",
                resultIdentifier,
                resultSubExecutableElement.getSimpleName()
        );
        codes.endControlFlow();
    }

    private void implementFieldDiscriminationLog(
            ProcessorContext processorContext,
            WorkingContext ctx,
            String currentValueIdentifier,
            String newValueIdentifier
    ) {
        Identifier identifier = processorContext.identifier;
        CodeBlock.Builder codes = processorContext.codes;
        String isSameIdentifier = identifier.makeIdentifier(
                IS_SAME_IDENTIFIER
        );

        codes.addStatement(
                """
                        $1L $2L = ($3L == null && $4L == null)
                                || ($3L != null && $4L != null && $3L.compareTo($4L) == 0)""",
                javaUtils.booleanType(),
                isSameIdentifier,
                currentValueIdentifier,
                newValueIdentifier
        );

        codes.beginControlFlow("if(!$L)", isSameIdentifier);

        for (String affectedSameIdentifier : ctx.isSameIdenfitierRegistries) {
            codes.addStatement("$L = false", affectedSameIdentifier);
        }

        codes.endControlFlow();
    }

    private List<String> appendIsSameIdentifierRegistries(ProcessorContext processorCtx, WorkingContext ctx) {
        List<String> isSameIdentifierRegistries = new ArrayList<>(ctx.isSameIdenfitierRegistries);

        isSameIdentifierRegistries.add(
                writeIsSameIdentifier(processorCtx)
        );

        return List.copyOf(isSameIdentifierRegistries);
    }

    private boolean isAcceptable(Element element, ElementKind parentKind) {
        Set<Modifier> elementModifiers = element.getModifiers();
        ElementKind elementKind = element.getKind();
        if (parentKind == ElementKind.INTERFACE) {
            ExecutableElement executableElement = (ExecutableElement) element;
            return isModifierAcceptable(elementModifiers)
                    && elementKind == ElementKind.METHOD
                    && executableElement.getParameters().isEmpty()
                    && isTypeAcceptable(
                    executableElement.getReturnType(),
                    element
            );
        } else if (parentKind == ElementKind.CLASS) {
            return isModifierAcceptable(elementModifiers)
                    && elementKind == ElementKind.FIELD
                    && isTypeAcceptable(
                    element.asType(),
                    element
            );
        } else {
            String errMessage = String.format(
                    "unimplement isModifierAcceptable for kind %s",
                    parentKind.name()
            );

            log.error(errMessage);
            throw new IllegalStateException(errMessage);
        }
    }

    private boolean isModifierAcceptable(Set<Modifier> targetSets) {
        for (Modifier compared : NOT_ACCEPTABLE_MODIFERS) {
            if (targetSets.contains(compared)) {
                return false;
            }
        }
        return true;
    }

    private boolean isTypeAcceptable(
            TypeMirror elementType,
            Element srcElement
    ) {
        if (elementType.getAnnotation(Ignore.class) != null) {
            return false;
        }

        while (elementType instanceof ArrayType arrayType) {
            elementType = arrayType.getComponentType();
        }

        boolean isCollection;
        while ((isCollection = javaUtils.isCollection(elementType))
                || javaUtils.isMap(elementType)) {

            DeclaredType collectionType = (DeclaredType) elementType;

            if (isCollection) {
                elementType = collectionType.getTypeArguments().getFirst();
            } else {
                elementType = collectionType.getTypeArguments().get(1);
            }
        }

        TypeKind typeKind = elementType.getKind();
        return typeKind.isPrimitive()
                || javaUtils.isComparable(elementType)
                || srcElement.getAnnotation(Traverse.class) != null;
    }

}

