package com.piler.kecia.datatypes;

import com.piler.kecia.Main;
import com.piler.kecia.datatypes.token.Operator;
import com.piler.kecia.datatypes.token.Token;
import com.piler.kecia.datatypes.token.Word;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by andre on 24/04/16.
 */
public class SymbolTable {

    public final static Token[] RELOP = {Operator.EQ, Operator.NEQ, Operator.GE, Operator.GT, Operator.LE, Operator.LT};
    public final static Token[] ADDOP = {Operator.SUM, Operator.SUBT, Word.AND};
    public final static Token[] MULOP = {Operator.MULT, Operator.DIV, Word.OR};
    public final static Token[] TYPE = {Word.INT, Word.STRING};

    public final static Tag[] RELOP_TAG = {Tag.EQ, Tag.NEQ, Tag.GE, Tag.GT, Tag.LE, Tag.LT};
    public final static Tag[] ADDOP_TAG = {Tag.SUM, Tag.SUBT, Tag.AND};
    public final static Tag[] MULOP_TAG = {Tag.MULT, Tag.DIV, Tag.OR};
    public final static Tag[] TYPE_TAG = {Tag.INT, Tag.STRING};

    public final static Map<Tag, Token[]> TOKEN_GROUP_MAP = new HashMap<>();
    private final static Map<Token.TokenValue<String>, Word> TOKEN_MAP = new LinkedHashMap<>();

    static {
        for (Field f : SymbolTable.class.getFields()) {
            if (Modifier.isStatic(f.getModifiers()) && Modifier.isPublic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()) && f.getType().isAssignableFrom(Token[].class)) {
                try {
                    Token[] val = (Token[]) f.get(null);
                    for (Token t : val) {
                        TOKEN_GROUP_MAP.put(t.getTag(), val);
                    }
                } catch (IllegalAccessException e) {
                    // Não há como acontecer
                }
            }
        }
    }

    public static boolean hasToken(Token.TokenValue<String> tokenValue) {
        return TOKEN_MAP.containsKey(tokenValue);
    }

    public static boolean hasToken(String value) {
        return TOKEN_MAP.containsKey(new Token.TokenValue<>(value));
    }

    public static void putToken(Word token) {
        TOKEN_MAP.put(token.getTokenValue(), token);
    }

    public static Word getToken(Token.TokenValue<String> tokenValue) {
        return TOKEN_MAP.get(tokenValue);
    }

    public static Word getToken(String value) {
        return TOKEN_MAP.get(new Token.TokenValue<>(value));
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

    private static String strValue() {
        StringBuilder sb = new StringBuilder();
        sb.append("SymbolTable(");
        sb.append(System.lineSeparator());
        for (Map.Entry<Token.TokenValue<String>, Word> entry : TOKEN_MAP.entrySet()) {
            sb.append("\tValue=");
            sb.append(entry.getKey().getValue());
            sb.append(", Token(Tag=");
            sb.append(entry.getValue().getTag());
            sb.append(")");
            sb.append(System.lineSeparator());
        }
        sb.append(")");
        return sb.toString();
    }

    public static void print(){
        if(Main.DEBUG){
            System.out.print("DEBUG: ");
        }
        System.out.println(strValue());
    }

}
