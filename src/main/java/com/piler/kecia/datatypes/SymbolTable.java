package com.piler.kecia.datatypes;

import com.piler.kecia.datatypes.token.Token;
import com.piler.kecia.datatypes.token.Word;
import lombok.ToString;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andre on 24/04/16.
 */
@ToString
public class SymbolTable {

    private final static Map<Token.TokenValue<String>, Word> tokenMap = new HashMap();

    public static boolean hasToken(Token.TokenValue<String> tokenValue) {
        return tokenMap.containsKey(tokenValue);
    }

    public static boolean hasToken(String value) {
        return tokenMap.containsKey(new Token.TokenValue<>(value));
    }

    public static void putToken(Word token) {
        tokenMap.put(token.getTokenValue(), token);
    }

    public static Word getToken(Token.TokenValue<String> tokenValue) {
        return tokenMap.get(tokenValue);
    }

    public static Word getToken(String value) {
        return tokenMap.get(new Token.TokenValue<>(value));
    }

    public static void initialize() {
        for (Field f : Word.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers()) &&
                    Modifier.isFinal(f.getModifiers()) && Token.class.isAssignableFrom(f.getType())) {
                try {
                    putToken((Word) f.get(null));
                } catch (IllegalAccessException e) {
                    // Não há essa possibilidade.
                }
            }
        }
    }

}
