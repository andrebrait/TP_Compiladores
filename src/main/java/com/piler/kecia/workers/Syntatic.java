package com.piler.kecia.workers;

import com.piler.kecia.datatypes.Tag;
import com.piler.kecia.datatypes.token.EOFToken;
import com.piler.kecia.datatypes.token.Token;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Analisador sintático para a gramática MODIFICADA que consta no relatório.
 * <p>
 * Created by andre on 27/05/16.
 */
@RequiredArgsConstructor
public class Syntatic {

    private final List<String> errorList = new ArrayList<>();
    private final Lexer lexer;
    private Token<?> tok;

    private Tag tag() {
        return tok != null ? tok.getTag() : Tag.INVALIDO;
    }

    private Object tokenValue() {
        return tok != null ? tok.getTokenValue().getValue() : lexer.getCharSeq().toString();
    }

    private void advance() {
        tok = lexer.scan();
    }

    private void eat(Tag t) {
        if (t.equals(tag())) {
            advance();
        } else {
            createError(t);
        }
    }

    public void createError(Tag t) {
        StringBuilder sb = new StringBuilder();
        sb.append("INVALIDO: Valor inválido na linha ");
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
            sb.append(tokenValue());
            sb.append(" (Identificado como ");
            sb.append(tag());
            sb.append(")");
        } else {
            sb.append(EOFToken.VALUE);
        }
        sb.append(".");
        errorList.add(sb.toString());
    }

    private State selectNextState(List<State> list, boolean shouldThrowError) {
        for (State s : list) {
            if (s.test(tag())) {
                return s;
            }
        }
        if (shouldThrowError) {
            for (State s : list) {
                createError(s.tag);
            }
            if (!tag().equals(Tag.EOF)) {
                //Trata como se tivesse retornado o token corretamente e daí prosseguido
                advance();
            }
        }
        return null;
    }

    private void addMultipleStates(List<State> list, Tag[] tags, Runnable... actions) {
        for (Tag t : tags) {
            list.add(new State(t, actions));
        }
    }

    private void executeOne(List<State> expected) {
        State next = selectNextState(expected, true);
        if (next != null) {
            next.go();
        }
    }

    private void executeMany(List<State> expected) {
        State next = null;
        do {
            next = selectNextState(expected, false);
            if (next != null) {
                next.go();
            }
        } while (next != null);
    }

    private void program() {
        advance();
        if (tok instanceof EOFToken) {
            return;
        }
        List<State> expected = new ArrayList<>();
        expected.add(new State(Tag.VAR, () -> eat(Tag.VAR), this::decl_list, () -> eat(Tag.BEGIN), this::stmt_list, () -> eat(Tag.END), () -> eat(Tag.EOF)));
        expected.add(new State(Tag.BEGIN, () -> eat(Tag.BEGIN), this::stmt_list, () -> eat(Tag.END), () -> eat(Tag.EOF)));
        executeOne(expected);
    }

    private void decl_list() {
        List<State> expected = new ArrayList<>();
        expected.add(new State(Tag.ID, this::decl, () -> eat(Tag.SEMICOLON)));

        executeOne(expected);
        executeMany(expected);
    }

    private void decl() {

    }

    private void stmt_list() {
        List<State> expected = new ArrayList<>();
        addMultipleStates(expected, new Tag[]{Tag.ID, Tag.IF, Tag.DO, Tag.IN, Tag.OUT}, this::stmt, () -> eat(Tag.SEMICOLON));

        executeOne(expected);
        executeMany(expected);
    }

    private void stmt() {
        List<State> expected = new ArrayList<>();
        expected.add(new State(Tag.ID, this::assign_stmt));
        expected.add(new State(Tag.IF, this::if_stmt));
        expected.add(new State(Tag.DO, this::do_stmt));
        expected.add(new State(Tag.IN, this::read_stmt));
        expected.add(new State(Tag.OUT, this::write_stmt));

        executeOne(expected);
    }

    private void assign_stmt() {
        List<State> expected = new ArrayList<>();
        expected.add(new State(Tag.ID, () -> eat(Tag.ID), () -> eat(Tag.ASSIGN), this::simple_expr));

        executeOne(expected);
    }

    //Alterada por causa de recursão à esquerda
    private void simple_expr() {

    }

    private void term() {

    }

    private void simple_expr_linha() {

    }

    private void if_stmt() {

    }

    private void do_stmt() {

    }

    private void read_stmt() {

    }

    private void write_stmt() {

    }

    private static class State {
        private final Tag tag;
        private final Runnable[] actions;

        private State(Tag t, Runnable... actions) {
            this.tag = t;
            this.actions = actions;
        }

        private boolean test(Tag t) {
            return tag.equals(t);
        }

        private void go() {
            for (Runnable r : actions) {
                r.run();
            }
        }
    }

}
