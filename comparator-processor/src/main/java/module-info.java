open module scanner.processor {
    exports com.rabbani.sp.core;
    exports com.rabbani.sp.core.path;
    exports com.rabbani.sp.annotation;
    requires transitive com.squareup.javapoet;
    requires java.compiler;
    requires java.logging;
    uses com.rabbani.sp.core.Discriminator;
}