package com.piler.kecia.datatypes.token;

import com.piler.kecia.datatypes.Tag;
import com.piler.kecia.datatypes.Type;
import com.piler.kecia.datatypes.TypedExpression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Created by andre on 02/05/16.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Literal extends Word implements TypedExpression {

    private Type type;

    public Literal(String value) {
        super(value, Tag.LITERAL);
    }

}
