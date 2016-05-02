package com.piler.kecia.workers;

import lombok.Getter;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by andre on 02/05/16.
 */
public class CharacterHandler {

    private Reader buffer;

    @Getter
    private String ch;

    public CharacterHandler(String filename) {
        File file = new File(filename);
        try {
            InputStream in = new FileInputStream(file);
            Reader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
            this.buffer = new BufferedReader(reader);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Arquivo não encontrado: " + filename);
        }
    }

    public void readch() throws EOFException {
        int r;
        try {
            if ((r = buffer.read()) != -1) {
                ch = String.valueOf((char) r);
                return;
            }
            throw new EOFException();
        } catch (EOFException e) {
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro na leitura do arquivo");
        }
    }

    public boolean readch(String ch) throws EOFException {
        readch();
        return this.ch == null && ch == null || this.ch != null && ch != null && this.ch.equals(ch);
    }
}
