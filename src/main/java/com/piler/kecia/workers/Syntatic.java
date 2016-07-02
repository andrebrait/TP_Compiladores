package com.piler.kecia.workers;

import com.piler.kecia.Main;
import com.piler.kecia.datatypes.SymbolTable;
import com.piler.kecia.datatypes.Tag;
import com.piler.kecia.datatypes.Type;
import com.piler.kecia.datatypes.TypedExpression;
import com.piler.kecia.datatypes.token.EOFToken;
import com.piler.kecia.datatypes.token.Identifier;
import com.piler.kecia.datatypes.token.Token;
import com.piler.kecia.datatypes.token.Word;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * Analisador sintático para a gramática MODIFICADA que consta no relatório.
 * <p>
 * Created by andre on 27/05/16.
 */
@RequiredArgsConstructor
public class Syntatic {

    /*
    Nas funções que retornam Type, retorno nulo significa erro sintático.
    Já ERROR significa ter havido algum erro semântico numa expressão interna a ela.
     */

    private final static int SEMANTIC_UNDECLARED = 0, SEMANTIC_DECLARED_REPEAT = 1, SEMANTIC_WRONG_TYPE = 2;
    private final static int SYNTAX_OK = 0, SYNTAX_ERROR = 1;

    private final Lexer lexer;
    private final boolean validateSemantics;
    private final Map<String, Map<Tag, Runnable[]>> stateMap = new HashMap<>();
    private final Token<?> tok[] = new Token[1];
    private final List<Identifier> identListTemp = new ArrayList<>();
    private final Stack<Type[]> type_stack = new Stack<>();
    private final Stack<Type[]> expression_stack = new Stack<>();
    private final Stack<Type[]> expression_cont_stack = new Stack<>();
    private final Stack<Type[]> simple_expr_stack = new Stack<>();
    private final Stack<Type[]> simple_expr_linha_stack = new Stack<>();
    private final Stack<Type[]> term_stack = new Stack<>();
    private final Stack<Type[]> term_linha_stack = new Stack<>();
    private final Stack<Type[]> factor_a_stack = new Stack<>();
    private final Stack<Type[]> factor_stack = new Stack<>();
    private final Stack<Type[]> relop_stack = new Stack<>();
    private final Stack<Type[]> addop_stack = new Stack<>();
    private final Stack<Type[]> mulop_stack = new Stack<>();
    private final Stack<Type[]> constant_stack = new Stack<>();

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

    private Tag tag(Token t) {
        return t != null ? t.getTag() : Tag.INVALIDO;
    }

    private Type tokenType(Token t) {
        if (t == null) {
            return Type.ERROR;
        }
        if (t instanceof TypedExpression) {
            if (t instanceof Identifier) {
                if (SymbolTable.isDeclaredId(((Identifier) t).getTokenValue())) {
                    return ((Identifier) SymbolTable.getToken(((Identifier) t).getTokenValue())).getType();
                } else {
                    return Type.UNDECLARED_ID;
                }
            } else {
                return ((TypedExpression) t).getType();
            }
        } else if (t instanceof Word) {
            if (t.getTag().equals(Tag.INT)) {
                return Type.INTEGER;
            } else if (t.getTag().equals(Tag.STRING)) {
                return Type.STRING;
            }
        }
        return Type.VOID;
    }

    private Object tokenValue(Token t) {
        return t != null ? t.getTokenValue().getValue() : lexer.getCharSeq().toString();
    }

    private void advance() {
        tok[0] = lexer.scan();
    }

    private int eat(Tag t) {
        printDebugMessage("eat", t, null);
        if (!t.equals(tag(tok[0]))) {
            createSyntaxError(t);
            return SYNTAX_ERROR;
        }
        advance();
        return SYNTAX_OK;
    }

    private void createSyntaxError(Tag... tags) {
        StringBuilder sb = new StringBuilder();
        sb.append("ERRO SINTÁTICO (Linha ");
        sb.append(lexer.getLine());
        sb.append("): Valor inválido. Esperado ");
        generateMultipleTypesMessage(sb, tags);
        sb.append(". Encontrado: ");
        if (!(tok[0] instanceof EOFToken)) {
            sb.append(tokenValue(tok[0]));
            sb.append(" (identificado como ");
            sb.append(tag(tok[0]));
            sb.append(")");
        } else {
            sb.append(EOFToken.VALUE);
        }
        sb.append(".");
        System.out.println(sb.toString());
    }

    private void createSemanticError(int codigoErro, Token token, Type foundType, Integer line, Type... expectedTypes) {
        if (!validateSemantics) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("ERRO SEMÂNTICO (Linha ");
        sb.append(line);
        sb.append("): ");
        switch (codigoErro) {
            case SEMANTIC_UNDECLARED:
                sb.append("Identificador ");
                sb.append(tokenValue(token));
                sb.append(" (linha ");
                sb.append(line);
                sb.append(") não declarado anteriormente.");
                break;
            case SEMANTIC_DECLARED_REPEAT:
                sb.append("Identificador ");
                sb.append(tokenValue(token));
                sb.append(" (linha ");
                sb.append(line);
                sb.append(") declarado mais de uma vez.");
                break;
            case SEMANTIC_WRONG_TYPE:
                sb.append("Operação entre tipos incompatíveis. Encontrado ");
                if (token != null) {
                    sb.append("token ");
                    sb.append(tokenValue(token));
                    sb.append(" do tipo ");
                }
                sb.append(foundType);
                sb.append(" quando o esperado era ");
                generateMultipleTypesMessage(sb, expectedTypes);
                sb.append(".");
                break;
            default:
                return;
        }
        System.out.println(sb.toString());
    }

    private void generateMultipleTypesMessage(StringBuilder sb, Object[] types) {
        if (types.length == 1) {
            sb.append(types[0]);
        } else {
            sb.append("um dos seguintes tipos: [");
            for (int i = 0; i < types.length; i++) {
                sb.append(types[i]);
                if (i < types.length - 1) {
                    sb.append(", ");
                }
            }
            sb.append("]");
        }
    }

    private Runnable[] selectNextActions(String phase, boolean shouldThrowError) {
        Tag t = tag(tok[0]);
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
        if (tok[0] instanceof EOFToken) {
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
            put(phase, Tag.ID, () -> {
                ident_list();
                if (eat(Tag.IS) == SYNTAX_OK) {
                    Type idTypes = type();
                    if (idTypes != null && !idTypes.equals(Type.ERROR)) {
                        for (Identifier i : identListTemp) {
                            i.setType(idTypes);
                            SymbolTable.putToken(i);
                        }
                    }
                }
                identListTemp.clear();
            });
        }
        executeOne(phase);
    }

    private void validateDeclaration(Token actual, Integer actualLine, Type t) {
        if (Type.UNDECLARED_ID.equals(t) && actual instanceof Identifier) {
            identListTemp.add((Identifier) actual);
            ((Identifier) actual).setDeclarationLine(actualLine);
        } else {
            createSemanticError(SEMANTIC_DECLARED_REPEAT, actual, null, actualLine);
        }
    }

    private void ident_list() {
        String phase = "ident_list";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.ID, () -> {
                Integer line = lexer.getLine();
                Token actual = tok[0];
                Type t = tokenType(actual);
                if (eat(Tag.ID) == SYNTAX_OK) {
                    validateDeclaration(actual, line, t);
                }
                ident_list_2();
            });
        }
        executeOne(phase);
    }

    private void ident_list_2() {
        String phase = "ident_list_2";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.COMMA, () -> eat(Tag.COMMA), () -> {
                Integer line = lexer.getLine();
                Token actual = tok[0];
                Type t = tokenType(actual);
                if (eat(Tag.ID) == SYNTAX_OK) {
                    validateDeclaration(actual, line, t);
                }
            });
        }
        executeMany(phase);
    }

    private Type type() {
        String phase = "type";
        type_stack.push(new Type[1]);
        if (!stateMap.containsKey(phase)) {
            for (Tag t : SymbolTable.TYPE_TAG) {
                put(phase, t, () -> {
                    Token actual = tok[0];
                    Type type = tokenType(actual);
                    if (eat(t) == SYNTAX_OK) {
                        type_stack.peek()[0] = type;
                    }
                });
            }
        }
        executeOne(phase);
        return type_stack.pop()[0];
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
            put(phase, Tag.ID, () -> {
                Integer line = lexer.getLine();
                Token lhs = tok[0];
                Type lhsType = tokenType(lhs);
                int eatLhs = eat(Tag.ID);
                int eatAssign = eat(Tag.ASSIGN);
                Type rhsType = simple_expr();
                if (eatLhs == SYNTAX_OK && eatAssign == SYNTAX_OK && rhsType != null) {
                    if (lhsType.equals(Type.UNDECLARED_ID)) {
                        createSemanticError(SEMANTIC_UNDECLARED, lhs, null, line);
                    } else if (notNullOrErrors(rhsType, lhsType) && !lhsType.equals(rhsType)) {
                        //Se o lado direito tiver dado problema, não reportar como erro pois a expressão do lado direito já terá reportado
                        createSemanticError(SEMANTIC_WRONG_TYPE, null, rhsType, line, lhsType);
                    }
                }
            });
        }
        executeOne(phase);
    }

    private void if_stmt() {
        String phase = "if_stmt";
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.IF, () -> {
                Integer line = lexer.getLine();
                int ifOk = eat(Tag.IF);
                Type exprType = expression();
                int thenOk = eat(Tag.THEN);
                stmt_list();
                if_stmt_cont();
                if (ifOk == SYNTAX_OK && thenOk == SYNTAX_OK && notNullOrErrors(exprType)) {
                    //Se não houve erro de sintaxe (ou semântico dentro de expression), então validar semântica.
                    if (!exprType.equals(Type.BOOLEAN)) {
                        createSemanticError(SEMANTIC_WRONG_TYPE, null, exprType, line, Type.BOOLEAN);
                    }
                }
            });
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
            put(phase, Tag.WHILE, () -> {
                Integer line = lexer.getLine();
                int whileOk = eat(Tag.WHILE);
                Type exprType = expression();
                if (whileOk == SYNTAX_OK && notNullOrErrors(exprType)) {
                    //Se não houve erro de sintaxe (ou semântico dentro de expression), então validar semântica.
                    if (!exprType.equals(Type.BOOLEAN)) {
                        expression_stack.peek()[0] = Type.ERROR;
                        createSemanticError(SEMANTIC_WRONG_TYPE, null, exprType, line, Type.BOOLEAN);
                    }
                }
            });
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

    private Type expression() {
        String phase = "expression";
        expression_stack.push(new Type[1]);
        if (!stateMap.containsKey(phase)) {
            putMultiple(phase, new Tag[]{Tag.ID, Tag.NUM, Tag.LITERAL, Tag.OP_PAR, Tag.NOT, Tag.SUBT}, () -> {
                Integer line = lexer.getLine();
                Type simplExprType = simple_expr();
                Type contType = expression_cont();
                if (notNullOrErrors(simplExprType, contType)) {
                    if (contType.equals(Type.INTEGER)) {
                        if (simplExprType.equals(Type.INTEGER)) {
                            expression_stack.peek()[0] = Type.BOOLEAN;
                        } else {
                            expression_stack.peek()[0] = Type.ERROR;
                            createSemanticError(SEMANTIC_WRONG_TYPE, null, simplExprType, line, Type.INTEGER);
                        }
                    } else {
                        expression_stack.peek()[0] = simplExprType;
                    }
                }
            });
        }
        executeOne(phase);
        return expression_stack.pop()[0];
    }

    private Type expression_cont() {
        String phase = "expression_cont";
        expression_cont_stack.push(new Type[]{Type.VOID});
        if (!stateMap.containsKey(phase)) {
            putMultiple(phase, SymbolTable.RELOP_TAG, () -> {
                Integer line = lexer.getLine();
                Type relopType = relop();
                Type simplExprType = simple_expr();
                if (notNullOrErrors(relopType, simplExprType)) {
                    if (simplExprType.equals(Type.INTEGER)) {
                        expression_cont_stack.peek()[0] = Type.INTEGER;
                    } else {
                        expression_cont_stack.peek()[0] = Type.ERROR;
                        createSemanticError(SEMANTIC_WRONG_TYPE, null, simplExprType, line, Type.INTEGER);
                    }
                }
            });
        }
        executeZeroOrOne(phase);
        return expression_cont_stack.pop()[0];
    }

    //Alterada por causa de recursão à esquerda
    private Type simple_expr() {
        String phase = "simple_expr";
        simple_expr_stack.push(new Type[1]);
        if (!stateMap.containsKey(phase)) {
            putMultiple(phase, new Tag[]{Tag.ID, Tag.NUM, Tag.LITERAL, Tag.OP_PAR, Tag.NOT, Tag.SUBT}, () -> {
                Integer line = lexer.getLine();
                Type termType = term();
                Type simplExprLinha = simple_expr_linha();
                if (notNullOrErrors(termType, simplExprLinha)) {
                    if (simplExprLinha.equals(Type.INTEGER) && !termType.equals(Type.INTEGER)) {
                        simple_expr_stack.peek()[0] = Type.ERROR;
                        createSemanticError(SEMANTIC_WRONG_TYPE, null, termType, line, Type.INTEGER);
                    } else {
                        simple_expr_stack.peek()[0] = termType;
                    }
                }
            });
        }
        executeOne(phase);
        return simple_expr_stack.pop()[0];
    }

    private Type simple_expr_linha() {
        String phase = "simple_expr_linha";
        simple_expr_linha_stack.push(new Type[]{Type.VOID});
        if (!stateMap.containsKey(phase)) {
            putMultiple(phase, SymbolTable.ADDOP_TAG, () -> {
                Integer line = lexer.getLine();
                Type addopType = addop();
                Type termType = term();
                Type simpleExprLinha = simple_expr_linha();
                if (notNullOrErrors(addopType, termType, simpleExprLinha)) {
                    if (termType.equals(Type.INTEGER)) {
                        simple_expr_linha_stack.peek()[0] = Type.INTEGER;
                    } else {
                        simple_expr_linha_stack.peek()[0] = Type.ERROR;
                        createSemanticError(SEMANTIC_WRONG_TYPE, null, termType, line, Type.INTEGER);
                    }
                } else {
                    simple_expr_linha_stack.peek()[0] = Type.ERROR;
                }
            });
        }
        executeZeroOrOne(phase);
        return simple_expr_linha_stack.pop()[0];
    }

    private Type term() {
        String phase = "term";
        term_stack.push(new Type[1]);
        if (!stateMap.containsKey(phase)) {
            putMultiple(phase, new Tag[]{Tag.ID, Tag.NUM, Tag.LITERAL, Tag.OP_PAR, Tag.NOT, Tag.SUBT}, () -> {
                Integer line = lexer.getLine();
                Type factorAType = factor_a();
                Type termLinhaType = term_linha();
                if (notNullOrErrors(factorAType, termLinhaType)) {
                    if (termLinhaType.equals(Type.INTEGER) && !factorAType.equals(Type.INTEGER)) {
                        term_stack.peek()[0] = Type.ERROR;
                        createSemanticError(SEMANTIC_WRONG_TYPE, null, factorAType, line, Type.INTEGER);
                    } else {
                        term_stack.peek()[0] = factorAType;
                    }
                }
            });
        }
        executeOne(phase);
        return term_stack.pop()[0];
    }

    private Type term_linha() {
        String phase = "term_linha";
        term_linha_stack.push(new Type[]{Type.VOID});
        if (!stateMap.containsKey(phase)) {
            putMultiple(phase, SymbolTable.MULOP_TAG, () -> {
                Integer line = lexer.getLine();
                Type mulopType = mulop();
                Type factorAType = factor_a();
                Type termLinhaType = term_linha();
                if (notNullOrErrors(mulopType, factorAType, termLinhaType)) {
                    if (factorAType.equals(Type.INTEGER)) {
                        term_linha_stack.peek()[0] = Type.INTEGER;
                    } else {
                        term_linha_stack.peek()[0] = Type.ERROR;
                        createSemanticError(SEMANTIC_WRONG_TYPE, null, factorAType, line, Type.INTEGER);
                    }
                } else {
                    term_linha_stack.peek()[0] = Type.ERROR;
                }
            });
        }
        executeZeroOrOne(phase);
        return term_linha_stack.pop()[0];
    }

    private Type factor_a() {
        String phase = "factor_a";
        factor_a_stack.push(new Type[1]);
        if (!stateMap.containsKey(phase)) {
            putMultiple(phase, new Tag[]{Tag.ID, Tag.NUM, Tag.LITERAL, Tag.OP_PAR}, () -> factor_a_aux(null));
            put(phase, Tag.NOT, () -> factor_a_aux(Tag.NOT));
            put(phase, Tag.SUBT, () -> factor_a_aux(Tag.SUBT));
        }
        executeOne(phase);
        return factor_a_stack.pop()[0];
    }

    private void factor_a_aux(Tag t) {
        int eatOk = t == null ? SYNTAX_OK : eat(t);
        Type factorType = factor();
        if (eatOk == SYNTAX_OK && notNullOrErrors(factorType)) {
            factor_a_stack.peek()[0] = factorType;
        }
    }

    private Type factor() {
        String phase = "factor";
        factor_stack.push(new Type[1]);
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.ID, () -> {
                Integer line = lexer.getLine();
                Token actual = tok[0];
                Type t = tokenType(actual);
                int eatOk = eat(Tag.ID);
                if (eatOk == SYNTAX_OK) {
                    if (t.equals(Type.UNDECLARED_ID)) {
                        factor_stack.peek()[0] = Type.ERROR;
                        createSemanticError(SEMANTIC_UNDECLARED, actual, null, line);
                    } else {
                        factor_stack.peek()[0] = t;
                    }
                }
            });
            putMultiple(phase, new Tag[]{Tag.NUM, Tag.LITERAL}, () -> {
                Type constantType = constant();
                if (notNullOrErrors(constantType)) {
                    factor_stack.peek()[0] = constantType;
                }
            });
            put(phase, Tag.OP_PAR, () -> {
                int eatOpParOk = eat(Tag.OP_PAR);
                Type exprType = expression();
                int eatClParOk = eat(Tag.CL_PAR);
                if (eatOpParOk == SYNTAX_OK && eatClParOk == SYNTAX_OK && notNullOrErrors(exprType)) {
                    factor_stack.peek()[0] = exprType;
                }
            });
        }
        executeOne(phase);
        return factor_stack.pop()[0];
    }

    private Type relop() {
        String phase = "relop";
        relop_stack.push(new Type[1]);
        if (!stateMap.containsKey(phase)) {
            for (Tag t : SymbolTable.RELOP_TAG) {
                put(phase, t, () -> {
                    int eatOk = eat(t);
                    if (eatOk == SYNTAX_OK) {
                        relop_stack.peek()[0] = Type.VOID;
                    }
                });
            }
        }
        executeOne(phase);
        return relop_stack.pop()[0];
    }

    private Type addop() {
        String phase = "addop";
        addop_stack.push(new Type[1]);
        if (!stateMap.containsKey(phase)) {
            for (Tag t : SymbolTable.ADDOP_TAG) {
                put(phase, t, () -> {
                    int eatOk = eat(t);
                    if (eatOk == SYNTAX_OK) {
                        addop_stack.peek()[0] = Type.VOID;
                    }
                });
            }
        }
        executeOne(phase);
        return addop_stack.pop()[0];
    }

    private Type mulop() {
        String phase = "mulop";
        mulop_stack.push(new Type[1]);
        if (!stateMap.containsKey(phase)) {
            for (Tag t : SymbolTable.MULOP_TAG) {
                put(phase, t, () -> {
                    int eatOk = eat(t);
                    if (eatOk == SYNTAX_OK) {
                        mulop_stack.peek()[0] = Type.VOID;
                    }
                });
            }
        }
        executeOne(phase);
        return mulop_stack.pop()[0];
    }

    private Type constant() {
        String phase = "constant";
        constant_stack.push(new Type[1]);
        if (!stateMap.containsKey(phase)) {
            put(phase, Tag.NUM, () -> constant_aux(Tag.NUM));
            put(phase, Tag.LITERAL, () -> constant_aux(Tag.LITERAL));
        }
        executeOne(phase);
        return constant_stack.pop()[0];
    }

    private void constant_aux(Tag t) {
        int eatOk = eat(t);
        if (eatOk == SYNTAX_OK) {
            constant_stack.peek()[0] = t.equals(Tag.NUM) ? Type.INTEGER : Type.STRING;
        }
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
            System.out.println("DEBUG: fase " + phase + ", Tags esperadas: " + sb.toString() + ", Token/Sequencia atual: " + tokenValue(tok[0]) + ", Tag atual: " + tag(tok[0]));
        }
    }

    public void analyze() {
        program();
    }

    private boolean notNullOrErrors(Type... types) {
        boolean retVal = true;
        for (int i = 0; i < types.length && retVal; i++) {
            retVal = types[i] != null && !Type.ERROR.equals(types[i]);
        }
        return retVal;
    }

}
