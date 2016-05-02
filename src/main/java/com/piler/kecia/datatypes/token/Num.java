package com.piler.kecia.datatypes.token;

import com.piler.kecia.datatypes.Tag;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Created by andre on 02/05/16.
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Num extends Token<Integer> {

    public Num(Integer value) {
        super(value, Tag.NUM);
    }

}
