package com.piler.kecia.workers;

import com.piler.kecia.Main;
import com.piler.kecia.datatypes.SymbolTable;
import com.piler.kecia.datatypes.token.*;
import lombok.Getter;

import java.io.EOFException;

/**
 * Created by andre on 02/05/16.
 */
public class Lexer {

    private final CharacterHandler charHandl;

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

    private static boolean isAlpha(String ch) {
        return ch.matches("[a-zA-Z]");
    }

    private static boolean isNumeric(String ch) {
        return ch.matches("[0-9]");
    }

    private static boolean isAlphanumeric(String ch) {
        return isNumeric(ch) || isAlpha(ch);
    }

    private static boolean equals(String ch1, String ch2) {
        return ch1 == null && ch2 == null || ch1 != null && ch2 != null && ch1.equals(ch2);
    }

    private static boolean isBlank(String ch) {
        if (ch == null) {
            return true;
        }
        for (char c : ch.toCharArray()) {
            if (!Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    private static int countMatches(String str, char ch) {
        int result = 0;
        if (str != null) {
            for (char c : str.toCharArray()) {
                if (c == ch) {
                    result++;
                }
            }
        }
        return result;
    }

    private static boolean beginsWith(String str, char ch) {
        return str != null && str.toCharArray()[0] == ch;
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
        boolean result = equals(ch, "\n") || equals(ch(), "\r");
        if (result) {
            line++;
        }
        return result;
    }

    private void removeLastFromSeqAndRefeed() {
        charSeq.deleteCharAt(charSeq.length() - 1);
        refeed = true;
    }

    public void analyze() {
        Token result;
        do {
            result = scan(true);
        } while (!(result instanceof EOFToken));
    }

    public Token scan() {
        return scan(Main.DEBUG);
    }

    private Token scan(boolean print) {
        Token tok = next();
        if (print) {
            printMessage(tok);
        }
        return tok;
    }

    private void printMessage(Token result) {
        if (result == null) {
            System.out.println("ERRO LÉXICO (Linha " + String.valueOf(getLine()) + "): Token inválido. Sequência de caracteres: [" + getCharSeq().toString() + "].");
        } else if (!(result instanceof EOFToken)) {
            System.out.println((Main.DEBUG ? "DEBUG " : "") + "(Linha " + String.valueOf(getLine()) + "): Token válido encontrado " + result.toString());
        } else {
            System.out.println((Main.DEBUG ? "DEBUG " : "") + "(Linha " + String.valueOf(getLine()) + "): Fim do arquivo atingido: " + result.toString() + System.lineSeparator());
        }
    }

    private Token next() {

        if (eof) {
            return new EOFToken();
        }

        this.charSeq = new StringBuilder();

        if (refeed) {
            this.charSeq.append(ch());
            refeed = false;
        } else {
            try {
                readch();
            } catch (EOFException e) {
                eof = true;
            }
        }

        if (eof) {
            return new EOFToken();
        }

        //Não será lido o próximo char se o último retornado tiver sido um separador, limitador ou operador.
        //Este é o início de um novo token, nestes casos.
        if (isLineBreak(ch()) || isBlank(ch())) {
            try {
                do {
                    readch();
                    if (isLineBreak(ch())) {
                        continue;
                    } else if (isBlank(ch())) {
                        continue;
                    }
                    String str = charSeq.toString();
                    charSeq = new StringBuilder();
                    charSeq.append(str.substring(str.length() - 1));
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
                if (equals(ch(), ">")) {
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
                        if (equals(ch(), "{") || isLineBreak(ch())) {
                            return null;
                        }
                    } while (!equals(ch(), "}"));
                    String value = charSeq.toString();
                    lit = new Literal(value.substring(1, value.length() - 1));
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
                    } while (!equals(ch(), "%"));
                } catch (EOFException e) {
                    return new EOFToken();
                }
                return scan();
        }

        if (isNumeric(ch())) {
            int value = 0;
            int len = 0;
            boolean beginZero = false;
            try {
                do {
                    int charValue = Integer.valueOf(ch());
                    if (len == 0 && charValue == 0) {
                        beginZero = true;
                    }
                    value = 10 * value + charValue;
                    len++;
                    readch();
                } while (isNumeric(ch()));
                if (beginZero && len > 1) {
                    removeLastFromSeqAndRefeed();
                    return null;
                }
                // 123c não é uma sequência válida
                if (isAlpha(ch())) {
                    try {
                        do {
                            //Lendo até o próximo separador, limitador ou operador.
                            readch();
                        } while (isAlphanumeric(ch()));
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

        if (isAlpha(ch()) || equals(ch(), "_")) {
            try {
                do {
                    readch();
                } while (isAlphanumeric(ch()) || equals(ch(), "_"));
            } catch (EOFException e) {
                eof = true;
            }
            removeLastFromSeqAndRefeed();
            String value = charSeq.toString().trim();
            int numUnderscore = countMatches(value, '_');
            if (numUnderscore > 1 || equals(value, "_") || numUnderscore == 1 && !beginsWith(value, '_') || value.length() > 15) {
                return null;
            }
            if (SymbolTable.hasToken(value)) {
                return SymbolTable.getToken(value);
            }
            return new Identifier(value);
        }

        return null;
    }

}
