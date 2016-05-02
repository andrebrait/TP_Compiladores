package com.piler.kecia.datatypes.token;

import com.piler.kecia.datatypes.Tag;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Created by andre on 02/05/16.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Identifier extends Word {

    public Identifier(String value) {
        super(value, Tag.ID);
    }

}

