package com.piler.kecia;

import com.piler.kecia.datatypes.SymbolTable;
import com.piler.kecia.datatypes.token.EOFToken;
import com.piler.kecia.datatypes.token.Token;
import com.piler.kecia.workers.Lexer;

public class Main {

    public static void main(String[] args) {

        if (args.length != 2) {
            exibeErro();
            return;
        }

        Lexer lex = new Lexer(args[1]);

        Token result = null;

        switch (args[0]) {
            case "lexico":
                System.out.println("Iniciando analisador léxico");
                while (!(result instanceof EOFToken)) {
                    result = lex.scan();
                    if (result == null) {
                        System.out.println("ERRO: Token inválido na linha " + String.valueOf(lex.getLine()) + ". Sequência de caracteres: [" + lex.getCharSeq().toString() + "].");
                        continue;
                    }
                    if (!(result instanceof EOFToken)) {
                        System.out.println("Token válido encontrado na linha " + String.valueOf(lex.getLine()) + ": " + result.toString());
                    }
                }

                System.out.println("Fim do arquivo atingido: " + result.toString() + System.lineSeparator());

                System.out.println(SymbolTable.strValue());
                return;
            case "sintatico":
                exibeNaoImplementado(args);
            default:
                exibeErro();
        }
    }

    private static void exibeErro() {
        System.out.println("Argumentos errados!");
        System.out.println("Uso correto: java -jar <nome_do_jar> <fase> <nome_do_arquivo>");
        return;
    }

    private static void exibeNaoImplementado(String[] args) {
        System.out.println("Fase de compilação ainda não implementada!");
        System.out.println("Fase selecionada: " + args[1]);
        return;
    }
}
