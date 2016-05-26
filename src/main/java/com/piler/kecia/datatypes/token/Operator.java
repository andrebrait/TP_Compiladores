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
public class Operator extends Token<String> {

    public static final Operator EQ = new Operator("=", Tag.EQ);
    public static final Operator GT = new Operator(">", Tag.GT);
    public static final Operator GE = new Operator(">=", Tag.GE);
    public static final Operator LT = new Operator("<", Tag.LT);
    public static final Operator LE = new Operator("<=", Tag.LE);
    public static final Operator NEQ = new Operator("<>", Tag.NEQ);
    public static final Operator SUM = new Operator("+", Tag.SUM);
    public static final Operator SUBT = new Operator("-", Tag.SUBT);
    public static final Operator MULT = new Operator("*", Tag.MULT);
    public static final Operator DIV = new Operator("/", Tag.DIV);
    public static final Operator ASSIGN = new Operator(":=", Tag.ASSIGN);

    private Operator(String lexeme, Tag tag) {
        super(lexeme, tag);
    }
}
