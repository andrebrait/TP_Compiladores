package com.piler.kecia.datatypes;

import lombok.Getter;

/**
 * Created by andre on 22/04/16.
 */
public enum Tag {

    //Palavras reservadas
    VAR,
    BEGIN,
    END,
    IS,
    INT,
    STRING,
    IF,
    THEN,
    ELSE,
    DO,
    WHILE,
    IN,
    OUT,
    NOT,

    //Operadores relacionais
    EQ,
    GT,
    GE,
    LT,
    LE,
    NEQ,

    //Operadores aritméticos
    SUM,
    SUBT,
    OR,
    MULT,
    DIV,
    AND,

    //Atribuição
    ASSIGN,

    //Separadores
    OP_PAR,
    CL_PAR,
    SEMICOLON,
    COMMA,

    //Outros
    ID,
    NUM,
    LITERAL,
    EOF;

    private static final int OFFSET = 256;

    public int getValue() {
        return OFFSET + this.ordinal();
    }
}
