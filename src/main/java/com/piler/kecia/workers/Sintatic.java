package com.piler.kecia.workers;

import com.piler.kecia.datatypes.Tag;
import com.piler.kecia.datatypes.token.EOFToken;
import com.piler.kecia.datatypes.token.Operator;
import com.piler.kecia.datatypes.token.Token;
import com.piler.kecia.datatypes.token.Word;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;

/**
 * Analisador sintático para a gramática MODIFICADA que consta no relatório.
 * <p>
 * Created by andre on 27/05/16.
 */
@RequiredArgsConstructor
public class Sintatic {

    @Getter
    @Setter
    @RequiredArgsConstructor
    private static class State{

        private static final int ZERO_OU_MAIS = 0, UM_OU_MAIS = 1, OBRIGATORIO = 2;

        private final State prev, next;
        private final String name;
        private final Integer mode;

        private String error;
        private 
    }

    private final Lexer lexer;

    private Token<?> tok;

    @Getter
    private List<String> errorList = new ArrayList<String>;

    private void advance() {
        tok = lexer.scan();
    }

    private void eat(Tag t){
        eat(t, State.OBRIGATORIO);
    }

    private Tag eat(Tag t, Integer mode) {
        if (tok != null && t.equals(tok.getTag())) {
            advance();
        } else if (!optional) {
            StringBuilder sb = new StringBuilder();
            sb.append("ERRO: Valor inválido na linha ");
            sb.append(lexer.getLine());
            sb.append(". Esperado ");
            if (t.equals(Tag.EOF)) {
                sb.append(EOFToken.VALUE);
            } else if (t.equals(Tag.ID)) {
                sb.append("identificador");
            } else if (t.equals(Tag.NUM)) {
                sb.append("número inteiro");
            } else if (t.equals(Tag.LITERAL)) {
                sb.append("valor literal");
            } else {
                sb.append("um dos seguintes valores: ");
                ext:
                for (Field f : Token.class.getFields()) {
                    if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())) {
                        try {
                            Token[] fields = (Token[]) f.get(null);
                            for (Token tk : fields) {
                                if (t.equals(tk.getTag())) {
                                    for (int i = 0; i < fields.length; i++) {
                                        sb.append(fields[i].getTokenValue().getValue());
                                        if (i < fields.length - 1) {
                                            sb.append(", ");
                                        }
                                    }
                                    break ext;
                                }
                            }
                        } catch (IllegalAccessException e) {
                            // Não há essa possibilidade.
                        }
                    }
                }
            }
            sb.append(". Encontrado: ");
            if (!(tok instanceof EOFToken)) {
                sb.append(tok != null ? tok.getTokenValue().getValue() : lexer.getCharSeq().toString());
                sb.append(" (Identificado como ");
                sb.append(tok != null ? tok.getTag() : "erro");
                sb.append(")");
            } else {
                sb.append(EOFToken.VALUE);
            }
            sb.append(".");
        }
    }

    private void program() {
        eat(Tag.VAR); decl_list();
    }
}
