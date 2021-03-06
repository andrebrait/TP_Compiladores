package com.piler.kecia;

import com.piler.kecia.datatypes.SymbolTable;
import com.piler.kecia.workers.Lexer;
import com.piler.kecia.workers.Syntatic;

public class Main {

    public static boolean DEBUG;

    public static void main(String[] args) {

        if (args.length < 2 || args.length > 3) {
            exibeErro();
            return;
        }

        Lexer lex = new Lexer(args[1]);

        DEBUG = (args.length == 3) && ("-d".equalsIgnoreCase(args[2]) || "--debug".equalsIgnoreCase(args[2]));

        switch (args[0]) {
            case "lexico":
                System.out.println("Iniciando analisador léxico");
                //Desativando modo DEBUG pois as mensagens do léxico sempre são exibidas se somente ele for executado
                DEBUG = false;
                lex.analyze();
                SymbolTable.print();
                return;
            case "sintatico":
                new Syntatic(lex, false).analyze();
                return;
            case "semantico":
                new Syntatic(lex, true).analyze();
                return;
            case "codigo":
                exibeNaoImplementado(args);
                return;
            default:
                exibeErro();
        }
    }

    private static void exibeErro() {
        System.out.println("Argumentos errados!");
        System.out.println("Uso correto: java -jar <nome_do_jar> <fase> <nome_do_arquivo> <opções>");
        System.out.println("Fases válidas:");
        System.out.println("\tlexico:\t\texecuta apenas o analisador léxico");
        System.out.println("\tsintatico:\texecuta os analisadores léxico e sintático");
        System.out.println("\tsemantico:\texecuta os analisadores léxico, sintático e semântico");
        System.out.println("\tcodigo:\t\texecuta os analisadores léxico, sintático, semântico e geração de código");
        System.out.println("Opções válidas:");
        System.out.println("\t-d, --DEBUG:\tliga as mensagens de DEBUG para os analisadores sintático e semântico");
    }

    private static void exibeNaoImplementado(String[] args) {
        System.out.println("Fase de compilação ainda não implementada!");
        System.out.println("Fase selecionada: " + args[1]);
    }
}
