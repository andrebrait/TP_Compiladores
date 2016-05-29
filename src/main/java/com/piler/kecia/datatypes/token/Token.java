package com.piler.kecia.datatypes.token;

import com.piler.kecia.datatypes.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Created by andre on 01/05/16.
 */
@Data
public abstract class Token<V> {

    private final static Token[] RELOP = {Operator.EQ, Operator.NEQ, Operator.GE, Operator.GT, Operator.LE, Operator.LT};
    private final static Token[] ADDOP = {Operator.SUM, Operator.SUBT, Word.AND};
    private final static Token[] MULOP = {Operator.MULT, Operator.DIV, Word.OR};
    private final static Token[] TYPE = {Word.INT, Word.STRING};

    private final TokenValue<V> tokenValue;
    private final Tag tag;

    protected Token(V value, Tag tag) {
        this.tokenValue = new TokenValue<V>(value);
        this.tag = tag;
    }

    @Data
    @RequiredArgsConstructor
    public static final class TokenValue<L> {
        private final L value;
    }

}
