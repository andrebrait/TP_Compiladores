package com.piler.kecia.workers;

import com.piler.kecia.datatypes.SymbolTable;
import com.piler.kecia.datatypes.token.*;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.io.EOFException;

/**
 * Created by andre on 02/05/16.
 */
public class Lexer {

    private CharacterHandler charHandl;

    private boolean eof;

    private boolean refeed;

    @Getter
    private int line;

    @Getter
    private StringBuilder charSeq;

    public Lexer(String filepath) {
        SymbolTable.initialize();
        this.charHandl = new CharacterHandler(filepath);
        this.line = 1;
        this.eof = false;
        this.refeed = false;
    }

    private void readch() throws EOFException {
        charHandl.readch();
        charSeq.append(ch());
    }

    private boolean readch(String ch) throws EOFException {
        boolean ret = charHandl.readch(ch);
        charSeq.append(ch());
        return ret;
    }

    private String ch() {
        return charHandl.getCh();
    }

    private boolean isLineBreak(String ch) {
        boolean result = StringUtils.equals(ch, StringUtils.CR) || StringUtils.equals(ch(), StringUtils.LF);
        if (result) {
            line++;
        }
        return result;
    }

    private void readchSetEOF() {
        try {
            readch();
        } catch (EOFException e) {
            eof = true;
        }
    }

    private void removeLastFromSeqAndRefeed() {
        charSeq.deleteCharAt(charSeq.length() - 1);
        refeed = true;
    }

    public Token scan() {

        if (eof) {
            return new EOFToken();
        }

        this.charSeq = new StringBuilder();

        if (refeed) {
            this.charSeq.append(ch());
            refeed = false;
        } else {
            readchSetEOF();
        }

        if (eof) {
            return new EOFToken();
        }

        //Não será lido o próximo char se o último retornado tiver sido um separador, limitador ou operador.
        //Este é o início de um novo token, nestes casos.
        if (ch() == null || isLineBreak(ch()) || StringUtils.isBlank(ch())) {
            try {
                do {
                    readch();
                    if (isLineBreak(ch())) {
                        continue;
                    } else if (ch() == null || StringUtils.isBlank(ch())) {
                        continue;
                    }
                    break;
                }
                while (true);
            } catch (EOFException e) {
                return new EOFToken();
            }
        }

        switch (ch()) {
            //Operadores
            case ":":
                try {
                    if (readch("=")) {
                        return Operator.ASSIGN;
                    }
                } catch (EOFException e) {
                    eof = true;
                }
                removeLastFromSeqAndRefeed();
                return null;
            case "=":
                return Operator.EQ;
            case ">":
                try {
                    if (readch("=")) {
                        return Operator.GE;
                    }
                } catch (EOFException e) {
                    eof = true;
                }
                removeLastFromSeqAndRefeed();
                return Operator.GT;
            case "<":
                try {
                    if (readch("=")) {
                        return Operator.LE;
                    }
                } catch (EOFException e) {
                    eof = true;
                }
                if (StringUtils.equals(ch(), ">")) {
                    return Operator.NEQ;
                }
                removeLastFromSeqAndRefeed();
                return Operator.LT;
            case "+":
                return Operator.SUM;
            case "-":
                return Operator.SUBT;
            case "*":
                return Operator.MULT;
            case "/":
                try {
                    if (readch("/")) {
                        //Comentário de uma linha
                        try {
                            do {
                                readch();
                            } while (!isLineBreak(ch()));
                            return scan();
                        } catch (EOFException e) {
                            return new EOFToken();
                        }
                    }
                } catch (EOFException e) {
                    eof = true;
                }
                removeLastFromSeqAndRefeed();
                return Operator.DIV;

            // Limitadores
            case "(":
                return Limiter.OP_PAR;
            case ")":
                return Limiter.CL_PAR;
            case "{":
                Literal lit = null;
                try {
                    do {
                        readch();
                        if (StringUtils.equals(ch(), "{") || isLineBreak(ch())) {
                            return null;
                        }
                    } while (!StringUtils.equals(ch(), "}"));
                    lit = new Literal(StringUtils.replaceEach(charSeq.toString(), new String[]{"{", "}"}, new String[]{"", ""}));
                } catch (EOFException e) {
                    eof = true;
                }
                return lit;
            case ";":
                return Limiter.SEMICOLON;
            case ",":
                return Limiter.COMMA;
            case "%":
                try {
                    do {
                        readch();
                        isLineBreak(ch());
                    } while (!StringUtils.equals(ch(), "%"));
                } catch (EOFException e) {
                    return new EOFToken();
                }
                return scan();
        }

        if (StringUtils.isNumeric(ch())) {
            int value = 0;
            boolean lengthGtOne = false;
            boolean beginZero = false;
            try {
                do {
                    beginZero = (value = 10 * value + Integer.valueOf(ch())) == 0;
                    lengthGtOne = true;
                    readch();
                } while (StringUtils.isNumeric(ch()));
                // 123c não é uma sequência válida
                if (beginZero && lengthGtOne) {
                    removeLastFromSeqAndRefeed();
                    return null;
                }
                if (StringUtils.isAlpha(ch())) {
                    try {
                        do {
                            //Lendo até o próximo separador, limitador ou operador.
                            readch();
                        } while (StringUtils.isAlphanumeric(ch()));
                    } catch (EOFException e) {
                        eof = true;
                    }
                    removeLastFromSeqAndRefeed();
                    return null;
                }
            } catch (EOFException e) {
                eof = true;
            }
            removeLastFromSeqAndRefeed();
            return new Num(value);
        }

        if (StringUtils.isAlpha(ch()) || StringUtils.equals(ch(), "_")) {
            try {
                do {
                    readch();
                } while (StringUtils.isAlphanumeric(ch()) || StringUtils.equals(ch(), "_"));
            } catch (EOFException e) {
                eof = true;
            }
            removeLastFromSeqAndRefeed();
            String value = charSeq.toString().trim();
            if (value.length() > 15 || StringUtils.countMatches(value, "_") == value.length()) {
                return null;
            }
            if (SymbolTable.hasToken(value)) {
                return SymbolTable.getToken(value);
            }
            Identifier id = new Identifier(value);
            SymbolTable.putToken(id);
            return id;
        }

        return null;
    }

}