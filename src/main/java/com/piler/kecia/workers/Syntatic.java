package com.piler.kecia.workers;

import com.piler.kecia.Main;
import com.piler.kecia.datatypes.SymbolTable;
import com.piler.kecia.datatypes.Tag;
import com.piler.kecia.datatypes.Type;
import com.piler.kecia.datatypes.TypedExpression;
import com.piler.kecia.datatypes.token.EOFToken;
import com.piler.kecia.datatypes.token.Identifier;
import com.piler.kecia.datatypes.token.Token;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Analisador sintático para a gramática MODIFICADA que consta no relatório.
 * <p>
 * Created by andre on 27/05/16.
 */
@RequiredArgsConstructor
public class Syntatic {

    private final static int SEMANTIC_UNDECLARED = 0, SEMANTIC_WRONG_TYPE = 1;

    private final Lexer lexer;
    private final boolean validateSemantics;
    private final Map<String, Map<Tag, Runnable[]>> stateMap = new HashMap<>();
    private Token<?> tok;

    private void run(Runnable[] actions) {
        for (Runnable r : actions) {
            r.run();
        }
    }

    private void put(String phase, Tag t, Runnable... actions) {
        if (!stateMap.containsKey(phase)) {
            stateMap.put(phase, new HashMap<>());
        }
        stateMap.get(phase).put(t, actions);
    }

    private void putMultiple(String phase, Tag[] tags, Runnable... actions) {
        if (!stateMap.containsKey(phase)) {
            stateMap.put(phase, new HashMap<>());
        }
        Map<Tag, Runnable[]> map = stateMap.get(phase);
        for (Tag t : tags) {
            map.put(t, actions);
        }
    }

    private Tag tag() {
        return tok != null ? tok.getTag() : Tag.INVALIDO;
    }

    private Type tokenType() {
        if (tok instanceof TypedExpression) {
            if (tok instanceof Identifier) {
                return ((Identifier) SymbolTable.getToken(((Identifier) tok).getTokenValue())).getType();
            } else {
                return ((TypedExpression) tok).getType();
            }
        }
        return null;
    }

    private Object tokenValue() {
        return tok != null ? tok.getTokenValue().getValue() : lexer.getCharSeq().toString();
    }

    private void advance() {
        tok = lexer.scan();
    }

    private void eat(Tag t) {
        printDebugMessage("eat", t, null);
        if (!t.equals(tag())) {
            createSyntaxError(t);
        }
        advance();
    }

    private void createSyntaxError(Tag... tags) {
        StringBuilder sb = new StringBuilder();
        sb.append("ERRO SINTÁTICO: Valor inválido na linha ");
        sb.append(lexer.getLine());
        sb.append(". Esperado ");
        generateMultipleTypesMessage(sb, tags);
        sb.append(". Encontrado: ");
        if (!(tok instanceof EOFToken)) {
            sb.append(tokenValue()); //FIXME Pegar tipos de expressões também!
            sb.append(" (identificado como ");
            sb.append(tag());
            sb.append(")");
        } else {
            sb.append(EOFToken.VALUE);
        }
        sb.append(".");
        System.out.println(sb.toString());
    }

    private void createSemanticError(int codigoErro, Type... type) {
        StringBuilder sb = new StringBuilder();
        sb.append("ERRO SEMÂNTICO: ");
        switch (codigoErro) {
            case SEMANTIC_UNDECLARED:
                sb.append("Identificador ");
                sb.append(tokenValue());
                sb.append(" (linha ");
                sb.append(lexer.getLine());
                sb.append(") não declarado anteriormente.");
                break;
            case SEMANTIC_WRONG_TYPE:
                sb.append("Operação entre tipos incompatíveis. Encontrado ");
                sb.append(tokenType());
                sb.append(" quando o esperado era ");
                generateMultipleTypesMessage(sb, type);
                sb.append(".");
                break;
            default:
                return;
        }
        System.out.println(sb.toString());
    }

    private void generateMultipleTypesMessage(StringBuilder sb, Object[] type) {
        if (type.length == 1) {
            sb.append(type[0]);
        } else {
            sb.append("um dos seguintes tipos: [");
            for (int i = 0; i < type.length; i++) {
                sb.append(type[i]);
                if (i < type.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        }
    }

    private Runnable[] selectNextActions(String phase, boolean shouldThrowError) {
        Tag t = tag();
        Map<Tag, Runnable[]> map = stateMap.get(phase);
        printDebugMessage(phase, null, map);
        if (map.containsKey(t)) {
            return map.get(t);
        }
        if (shouldThrowError) {
            createSyntaxError(map.keySet().toArray(new Tag[]{}));
            if (!t.equals(Tag.EOF)) {
                //Trata como se tivesse retornado o token corretamente e daí prosseguido
                advance();
            }
        }
        return null;
    }

    private void executeOne(String phase) {
        Runnable[] actions = selectNextActions(phase, true);
        if (actions != null) {
            run(actions);
        }
    }

    private void executeZeroOrOne(String phase) {
        Runnable[] actions = selectNextActions(phase, false);
        if (actions != null) {
            run(actions);
        }
    }

    private void executeMany(String phase) {
        Runnable[] actions;
        do {
            actions = selectNextActions(phase, false);
            if (actions != null) {
                run(actions);
            }
        } while (actions != null);
    }

    private void program() {
        advance();
        if (tok instanceof EOFToken) {
            return;
        }
        String phase = "program";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.VAR, () -> eat(Tag.VAR), this::decl_list, this::begin);
            put(phase, Tag.BEGIN, this::begin);
        }
        executeOne(phase);
    }

    private void begin() {
        String phase = "begin";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.BEGIN, () -> eat(Tag.BEGIN), this::stmt_list, () -> eat(Tag.END), () -> eat(Tag.EOF));
        }
        executeOne(phase);
    }

    private void decl_list() {
        String phase = "decl_list";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.ID, this::decl, () -> eat(Tag.SEMICOLON));
        }
        executeOne(phase);
        executeMany(phase);
    }

    private void decl() {
        String phase = "decl";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.ID, this::ident_list, () -> eat(Tag.IS), this::type);
        }
        executeOne(phase);
    }

    private void ident_list() {
        String phase = "ident_list";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.ID, () -> eat(Tag.ID), this::ident_list_2);
        }
        executeOne(phase);
    }

    private void ident_list_2() {
        String phase = "ident_list_2";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.COMMA, () -> eat(Tag.COMMA), () -> eat(Tag.ID));
        }
        executeMany(phase);
    }

    private void type() {
        String phase = "type";
        if (!stateMap.containsKey(phase)) {
            for (Tag t : SymbolTable.TYPE_TAG) {
                put(phase, t, () -> eat(t));
            }
        }
        executeOne(phase);
    }

    private void stmt_list() {
        String phase = "stmt_list";
        if (!stateMap.containsKey(phase)) {
            putMultiple(phase, new Tag[]{Tag.ID, Tag.IF, Tag.DO, Tag.IN, Tag.OUT}, this::stmt, () -> eat(Tag.SEMICOLON));
        }
        executeOne(phase);
        executeMany(phase);
    }

    private void stmt() {
        String phase = "stmt";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.ID, this::assign_stmt);
            put(phase, Tag.IF, this::if_stmt);
            put(phase, Tag.DO, this::do_stmt);
            put(phase, Tag.IN, this::read_stmt);
            put(phase, Tag.OUT, this::write_stmt);
        }
        executeOne(phase);
    }

    private void assign_stmt() {
        String phase = "assign_stmt";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.ID, () -> eat(Tag.ID), () -> eat(Tag.ASSIGN), this::simple_expr);
        }
        executeOne(phase);
    }

    private void if_stmt() {
        String phase = "if_stmt";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.IF, () -> eat(Tag.IF), this::expression, () -> eat(Tag.THEN), this::stmt_list, this::if_stmt_cont);
        }
        executeOne(phase);
    }

    private void if_stmt_cont() {
        String phase = "if_stmt_cont";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.END, () -> eat(Tag.END));
            put(phase, Tag.ELSE, () -> eat(Tag.ELSE), this::stmt_list, () -> eat(Tag.END));
        }
        executeOne(phase);
    }

    private void do_stmt() {
        String phase = "do_stmt";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.DO, () -> eat(Tag.DO), this::stmt_list, this::stmt_suffix);
        }
        executeOne(phase);
    }

    private void stmt_suffix() {
        String phase = "stmt_suffix";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.WHILE, () -> eat(Tag.WHILE), this::expression);
        }
        executeOne(phase);
    }

    private void read_stmt() {
        String phase = "read_stmt";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.IN, () -> eat(Tag.IN), () -> eat(Tag.OP_PAR), () -> eat(Tag.ID), () -> eat(Tag.CL_PAR));
        }
        executeOne(phase);
    }

    private void write_stmt() {
        String phase = "write_stmt";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.OUT, () -> eat(Tag.OUT), () -> eat(Tag.OP_PAR), this::simple_expr, () -> eat(Tag.CL_PAR));
        }
        executeOne(phase);
    }

    private void expression() {
        String phase = "expression";
        if (!stateMap.containsKey(phase)) {
            putMultiple(phase, new Tag[]{Tag.ID, Tag.NUM, Tag.LITERAL, Tag.OP_PAR, Tag.NOT, Tag.SUBT}, this::simple_expr, this::expression_cont);
        }
        executeOne(phase);
    }

    private void expression_cont() {
        String phase = "expression_cont";
        if (!stateMap.containsKey(phase)) {
            putMultiple(phase, SymbolTable.RELOP_TAG, this::relop, this::simple_expr);
        }
        executeZeroOrOne(phase);
    }

    //Alterada por causa de recursão à esquerda
    private void simple_expr() {
        String phase = "simple_expr";
        if (!stateMap.containsKey(phase)) {
            putMultiple(phase, new Tag[]{Tag.ID, Tag.NUM, Tag.LITERAL, Tag.OP_PAR, Tag.NOT, Tag.SUBT}, this::term, this::simple_expr_linha);
        }
        executeOne(phase);
    }

    private void simple_expr_linha() {
        String phase = "simple_expr_linha";
        if (!stateMap.containsKey(phase)) {
            putMultiple(phase, SymbolTable.ADDOP_TAG, this::addop, this::term, this::simple_expr_linha);
        }
        executeZeroOrOne(phase);
    }

    private void term() {
        String phase = "term";
        if (!stateMap.containsKey(phase)) {
            putMultiple(phase, new Tag[]{Tag.ID, Tag.NUM, Tag.LITERAL, Tag.OP_PAR, Tag.NOT, Tag.SUBT}, this::factor_a, this::term_linha);
        }
        executeOne(phase);
    }

    private void term_linha() {
        String phase = "term_linha";
        if (!stateMap.containsKey(phase)) {
            putMultiple(phase, SymbolTable.MULOP_TAG, this::mulop, this::factor_a, this::term_linha);
        }
        executeZeroOrOne(phase);
    }

    private void factor_a() {
        String phase = "factor_a";
        if (!stateMap.containsKey(phase)) {
            putMultiple(phase, new Tag[]{Tag.ID, Tag.NUM, Tag.LITERAL, Tag.OP_PAR}, this::factor);
            put(phase, Tag.NOT, () -> eat(Tag.NOT), this::factor);
            put(phase, Tag.SUBT, () -> eat(Tag.SUBT), this::factor);
        }
        executeOne(phase);
    }

    private void factor() {
        String phase = "factor";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.ID, () -> eat(Tag.ID));
            putMultiple(phase, new Tag[]{Tag.NUM, Tag.LITERAL}, this::constant);
            put(phase, Tag.OP_PAR, () -> eat(Tag.OP_PAR), this::expression, () -> eat(Tag.CL_PAR));
        }
        executeOne(phase);
    }

    private void relop() {
        String phase = "relop";
        if (!stateMap.containsKey(phase)) {
            for (Tag t : SymbolTable.RELOP_TAG) {
                put(phase, t, () -> eat(t));
            }
        }
        executeOne(phase);
    }

    private void addop() {
        String phase = "addop";
        if (!stateMap.containsKey(phase)) {
            for (Tag t : SymbolTable.ADDOP_TAG) {
                put(phase, t, () -> eat(t));
            }
        }
        executeOne(phase);
    }

    private void mulop() {
        String phase = "mulop";
        if (!stateMap.containsKey(phase)) {
            for (Tag t : SymbolTable.MULOP_TAG) {
                put(phase, t, () -> eat(t));
            }
        }
        executeOne(phase);
    }

    private void constant() {
        String phase = "constant";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.NUM, () -> eat(Tag.NUM));
            put(phase, Tag.LITERAL, () -> eat(Tag.LITERAL));
        }
        executeOne(phase);
    }

    private void printDebugMessage(String phase, Tag t, Map<Tag, Runnable[]> map) {
        if (Main.DEBUG) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            if (t != null) {
                sb.append(t);
            } else {
                int i = 0;
                for (Tag k : map.keySet()) {
                    sb.append(k);
                    if (i < map.keySet().size() - 1) {
                        sb.append(", ");
                    }
                    i++;
                }
            }
            sb.append("]");
            System.out.println("DEBUG: fase " + phase + ", Tags esperadas: " + sb.toString() + ", Token/Sequencia atual: " + tokenValue() + ", Tag atual: " + tag());
        }
    }

    public void analyze() {
        program();
    }

}
