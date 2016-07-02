package com.piler.kecia.datatypes;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Created by andre on 28/06/16.
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum Type {

    //No caso da String, o tamanho é na verdade o tamanho de um ponteiro para o primeiro caractere
    INTEGER(Tag.INT, 4),
    STRING(Tag.STRING, 1),
    VOID(null, null),
    ERROR(null, null),
    BOOLEAN(null, 1);

    private Tag tag;
    private Integer length;

}
