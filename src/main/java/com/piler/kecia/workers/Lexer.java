package com.piler.kecia.workers;

import com.piler.kecia.datatypes.SymbolTable;
import com.piler.kecia.datatypes.token.*;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by andre on 02/05/16.
 */
public class Lexer {

    private CharacterHandler charHandl;

    @Getter
    private int line;

    @Getter
    private StringBuilder charSeq;

    public Lexer(String filepath) {
        SymbolTable.initialize();
        this.charHandl = new CharacterHandler(filepath);
        this.line = 0;
    }

    private void readch() {
        charHandl.readch();
        if (ch() != null) {
            charSeq.append(ch());
        }
    }

    private boolean readch(String ch) {
        boolean ret = charHandl.readch(ch);
        if (ch() != null) {
            charSeq.append(ch());
        }
        return ret;
    }

    private String ch() {
        return charHandl.getCh();
    }

    public Token scan() {

        this.charSeq = new StringBuilder();

        //Não será lido o próximo char se o último retornado tiver sido um separador, limitador ou operador.
        //Este é o início de um novo token, nestes casos.
        if (ch() == null || StringUtils.isWhitespace(ch()) || StringUtils.containsAny(ch(), StringUtils.CR, StringUtils.LF)) {
            for (; ; readch()) {
                if (StringUtils.containsAny(ch(), StringUtils.CR, StringUtils.LF)) {
                    line++;
                    continue;
                } else if (StringUtils.isWhitespace(ch())) {
                    continue;
                } else if (ch() == null) {
                    //Sinaliza fim do arquivo
                    return new EofSignalToken();
                }
                break;
            }
        }

        switch (ch()) {
            //Operadores
            case "=":
                return Operator.EQ;
            case ">":
                if (readch("=")) {
                    return Operator.GE;
                }
                return Operator.GT;
            case "<":
                if (readch("=")) {
                    return Operator.LE;
                }
                if (StringUtils.equals(ch(), ">")) {
                    return Operator.NEQ;
                }
                return Operator.LT;
            case "+":
                return Operator.SUM;
            case "-":
                return Operator.SUBT;
            case "*":
                return Operator.MULT;
            case "/":
                if (readch("/")) {
                    //Comentário de uma linha

                }
                return Operator.DIV;

            // Limitadores
            case "(":
                return Limiter.OP_PAR;
            case ")":
                return Limiter.CL_PAR;
            case "{":
                return Limiter.OP_BRACKET;
            case "}":
                return Limiter.CL_BRACKET;
            case ";":
                return Limiter.SEMICOLON;
            case ",":
                return Limiter.COMMA;
        }

        if (StringUtils.isNumeric(ch())) {
            int value = 0;
            boolean lengthGtOne = false;
            boolean beginZero = false;
            do {
                beginZero = (value = 10 * value + Integer.valueOf(ch())) == 0;
                lengthGtOne = true;
                readch();
            } while (StringUtils.isNumeric(ch()));
            // 123c não é uma sequência válida
            if (beginZero && lengthGtOne) {
                return null;
            }
            if (StringUtils.isAlpha(ch())) {
                do {
                    //Lendo até o próximo separador, limitador ou operador.
                    readch();
                } while (StringUtils.isAlphanumeric(ch()));
                return null;
            }
            return new Num(value);
        }


    }
}
