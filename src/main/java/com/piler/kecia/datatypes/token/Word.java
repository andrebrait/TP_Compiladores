package com.piler.kecia.datatypes.token;

import com.piler.kecia.datatypes.Tag;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Created by andre on 01/05/16.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Word extends Token<String> {

    public static final Word VAR = new Word("var", Tag.VAR);
    public static final Word BEGIN = new Word("begin", Tag.BEGIN);
    public static final Word END = new Word("end", Tag.END);
    public static final Word IS = new Word("is", Tag.IS);
    public static final Word INT = new Word("int", Tag.INT);
    public static final Word STRING = new Word("string", Tag.STRING);
    public static final Word IF = new Word("if", Tag.IF);
    public static final Word THEN = new Word("then", Tag.THEN);
    public static final Word ELSE = new Word("else", Tag.ELSE);
    public static final Word DO = new Word("do", Tag.DO);
    public static final Word WHILE = new Word("while", Tag.WHILE);
    public static final Word IN = new Word("in", Tag.IN);
    public static final Word OUT = new Word("out", Tag.OUT);
    public static final Word NOT = new Word("not", Tag.NOT);
    public static final Word OR = new Word("or", Tag.OR);
    public static final Word AND = new Word("and", Tag.AND);

    public Word(String lexeme, Tag tag) {
        super(lexeme, tag);
    }

}
