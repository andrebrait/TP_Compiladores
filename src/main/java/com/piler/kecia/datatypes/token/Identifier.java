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
public class Identifier extends Word implements TypedExpression {

    private Type type;
    private Integer declarationLine;

    public Identifier(String value) {
        super(value, Tag.ID);
    }

}

