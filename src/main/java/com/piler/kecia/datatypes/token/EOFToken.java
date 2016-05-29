package com.piler.kecia.datatypes.token;

import com.piler.kecia.datatypes.Tag;
import lombok.ToString;

/**
 * Created by andre on 02/05/16.
 */
@ToString
public class EOFToken extends Token<String> {

    public static final String VALUE = "fim de arquivo";

    public EOFToken() {
        super(VALUE, Tag.EOF);
    }
}
