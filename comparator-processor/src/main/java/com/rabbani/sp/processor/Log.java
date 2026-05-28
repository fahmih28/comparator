package com.rabbani.sp.processor;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import java.io.PrintWriter;
import java.io.StringWriter;

public class Log {
    private final Messager messager;

    public Log(ProcessingEnvironment processingEnvironment){
        this.messager = processingEnvironment.getMessager();
    }

    public void info(String message, Object... args) {
        messager.printMessage(Diagnostic.Kind.NOTE, String.format(message, args));
    }

    public void warning(String message, Object... args) {
        messager.printMessage(Diagnostic.Kind.WARNING, String.format(message, args));
    }

    public void error(String message, Throwable err,Object... args) {
        messager.printMessage(Diagnostic.Kind.ERROR, String.format(message, args));
        if(err != null){
            StringWriter stringWriter = new StringWriter();
            err.printStackTrace(new PrintWriter(stringWriter));
            messager.printMessage(Diagnostic.Kind.ERROR,stringWriter.toString());
        }
    }

    public void error(String message,Object... args) {
        error(message,null,args);
    }
}
