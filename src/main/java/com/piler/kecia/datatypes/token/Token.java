package com.piler.kecia.datatypes.token;

import com.piler.kecia.datatypes.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Created by andre on 01/05/16.
 */
@Data
public abstract class Token<V> {

    private final TokenValue<V> tokenValue;
    private final Tag tag;

    Token(V value, Tag tag) {
        this.tokenValue = new TokenValue<>(value);
        this.tag = tag;
    }

    @Data
    @RequiredArgsConstructor
    public static final class TokenValue<L> {
        private final L value;
    }

}
