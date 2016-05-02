package com.piler.kecia;

import com.piler.kecia.datatypes.token.EOFToken;
import com.piler.kecia.datatypes.token.Token;
import com.piler.kecia.workers.Lexer;

public class Main {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Argumentos errados!");
            System.out.println("Uso correto: java -jar <nome_do_jar> <nome_do_arquivo>");
            return;
        }

        System.out.println("Iniciando analisador léxico");

        Lexer lex = new Lexer(args[0]);

        Token result = null;

        while (!(result instanceof EOFToken)) {
            result = lex.scan();
            if (result == null) {
                System.out.println("ERRO: Token inválido na linha " + String.valueOf(lex.getLine()) + ". Sequência de caracteres: [ " + lex.getCharSeq().toString() + " ].");
                continue;
            }
            if (!(result instanceof EOFToken)) {
                System.out.println("Token válido encontrado na linha " + String.valueOf(lex.getLine()) + ": " + result.toString());
            }
        }

        System.out.println("Fim do arquivo atingido: " + result.toString());
    }
}
