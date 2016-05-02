package com.piler.kecia.datatypes.token;

import lombok.ToString;

/**
 * Created by andre on 02/05/16.
 */
@ToString
public class EOFToken extends Token<Integer> {
    public EOFToken() {
        super(null, null);
    }
}
