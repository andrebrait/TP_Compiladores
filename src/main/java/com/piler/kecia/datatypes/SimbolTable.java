package com.piler.kecia.datatypes;

import com.piler.kecia.datatypes.token.Token;
import com.piler.kecia.datatypes.token.Word;
import lombok.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by andre on 24/04/16.
 */
@ToString
public class SimbolTable {

    private final static Map<Token.TokenValue<?>, Token<?>> tokenMap = new HashMap();

    public static <L> boolean hasToken(Token.TokenValue<L> tokenValue){
        return tokenMap.containsKey(tokenValue);
    }

    public static <L> boolean hasToken(L value){
        return tokenMap.containsKey(new Token.TokenValue<>(value));
    }

    public static <L> void putToken(Token<L> token){
        tokenMap.put(token.getTokenValue(), token);
    }

    public static <L> Token<L> getToken(Token.TokenValue<L> tokenValue){
        return (Token<L>) tokenMap.get(tokenValue);
    }

    public static <L> Token<L> getToken(L value){
        return (Token<L>) tokenMap.get(new Token.TokenValue<>(value));
    }

    public static void initialize(){
        for (Field f : Word.class.getFields()){
            if(Modifier.isStatic(f.getModifiers())){
                putToken((Token<?>)f.get());
            }
        }
    }

}
