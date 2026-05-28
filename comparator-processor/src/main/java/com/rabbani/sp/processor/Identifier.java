package com.rabbani.sp.processor;

import java.util.HashMap;
import java.util.Map;

public class Identifier {
    private Map<String, Integer> incremental = new HashMap<>();

    public Identifier() {
    }

    public String makeIdentifier(String name) {
        int suffix = incremental.getOrDefault(name, 0);
        StringBuilder identifier = new StringBuilder();

        char[] nameChars = name.toCharArray();
        for (int i = 0;i < nameChars.length;i++) {
            char chr = nameChars[i];
            if (Character.isUpperCase(chr) && i > 0) {
                identifier.append("_");
            }

            if(Character.isLetterOrDigit(chr)) {
                identifier.append(Character.toLowerCase(chr));
            }else{
                identifier.append("_");
            }
        }

        identifier
                .append("_")
                .append(suffix);
        incremental.put(name, suffix + 1);


        return identifier.toString();
    }

}
