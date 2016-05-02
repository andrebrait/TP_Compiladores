package com.piler.kecia.workers;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

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
        Charset encoding = Charset.defaultCharset();
        File file = new File(filename);
        try {
            InputStream in = new FileInputStream(file);
            Reader reader = new InputStreamReader(in, encoding);
            this.buffer = new BufferedReader(reader);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Arquivo não encontrado: " + filename);
        }
    }

    public void readch() {
        int r;
        try {
            if ((r = buffer.read()) != -1) {
                ch = String.valueOf((char) r);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Erro na leitura do arquivo");
        }
        ch = null;
    }

    public boolean readch(String ch) {
        readch();
        return StringUtils.equals(this.ch, ch);
    }
}
