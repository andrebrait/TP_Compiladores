package com.piler.kecia.datatypes.token;

import com.piler.kecia.datatypes.Tag;
import lombok.*;

/**
 * Created by andre on 01/05/16.
 */
@Data
public abstract class Token<V> {

    @Data
    @RequiredArgsConstructor
    public static final class TokenValue<L> {
        private final L value;
    }

    private final TokenValue<V> tokenValue;
    private final Tag tag;

    protected Token(V value, Tag tag){
        this.tokenValue = new TokenValue<V>(value);
        this.tag = tag;
    }

}
