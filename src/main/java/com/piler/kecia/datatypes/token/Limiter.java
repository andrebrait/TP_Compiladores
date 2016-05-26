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
public class Limiter extends Token<String> {

    public static final Limiter OP_PAR = new Limiter("(", Tag.OP_PAR);
    public static final Limiter CL_PAR = new Limiter(")", Tag.CL_PAR);
    public static final Limiter SEMICOLON = new Limiter(";", Tag.SEMICOLON);
    public static final Limiter COMMA = new Limiter(",", Tag.COMMA);

    private Limiter(String lexeme, Tag tag) {
        super(lexeme, tag);
    }
}
