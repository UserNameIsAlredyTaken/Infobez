package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TEA {
    private final static int SUGAR = 0x9E3779B9;
    private final static int CUPS  = 32;
    private final static int UNSUGAR = 0xC6EF3720;

    private int[] S = new int[4];

    public TEA(byte[] key) {
        if (key == null)
            throw new RuntimeException("Invalid key: Key was null");
        if (key.length < 16)
            throw new RuntimeException("Invalid key: Length was less than 16 bytes");
        for (int off=0, i=0; i<4; i++) { //делим ключ на 4 части по 32 бита

            S[i] = ((key[off++] & 0xff)) |
                    ((key[off++] & 0xff) <<  8) |
                    ((key[off++] & 0xff) << 16) |
                    ((key[off++] & 0xff) << 24);
        }
    }


    public byte[] encrypt(byte[] clear) {
        int paddedSize = ((clear.length/8) + (clear.length % 8 == 0 ? 0 : 1)) * 2; //деление размера сообщения на количество блоков с округлением в большую сторону
        int[] buffer = new int[paddedSize + 1];
        buffer[0] = clear.length; //пишем длинну исходного сообщения в начало буфера
        pack(clear, buffer, 1);
        brew(buffer);
        return unpack(buffer, 0, buffer.length * 4);
    }


    public byte[] decrypt(byte[] crypt) {
        int[] buffer = new int[crypt.length / 4];
        pack(crypt, buffer, 0);
        unbrew(buffer);
        return unpack(buffer, 1, buffer[0]);
    }

    void brew(int[] buf) {
        int i, v0, v1, sum, n;
        i = 1;
        while (i < buf.length) {
            n = CUPS;
            v0 = buf[i];
            v1 = buf[i+1];
            sum = 0;
            while (n-- > 0) {
                sum += SUGAR;
                v0  += ((v1 << 4 ) + S[0] ^ v1) + (sum ^ (v1 >>> 5)) + S[1];
                v1  += ((v0 << 4 ) + S[2] ^ v0) + (sum ^ (v0 >>> 5)) + S[3];
            }
            buf[i] = v0;
            buf[i+1] = v1;
            i += 2;
        }
    }

    void unbrew(int[] buf) {
        int i, v0, v1, sum, n;
        i = 1;
        while (i < buf.length) {
            n = CUPS;
            v0 = buf[i];
            v1 = buf[i+1];
            sum = UNSUGAR;
            while (n--> 0) {
                v1  -= ((v0 << 4 ) + S[2] ^ v0) + (sum ^ (v0 >>> 5)) + S[3];
                v0  -= ((v1 << 4 ) + S[0] ^ v1) + (sum ^ (v1 >>> 5)) + S[1];
                sum -= SUGAR;
            }
            buf[i] = v0;
            buf[i+1] = v1;
            i += 2;
        }
    }

    void pack(byte[] src, int[] dest, int destOffset) {//в зависимости от того пакуем зашифрованное сообщение устанавливаем destOffset ведь в dest[0] лежит длинна исходного сообщения
        int i = 0, shift = 24;
        int j = destOffset;
        dest[j] = 0;
        while (i < src.length) {
            dest[j] |= ((src[i] & 0xff) << shift);
            if (shift==0) {
                shift = 24;
                j++;
                if (j < dest.length) dest[j] = 0;
            }
            else {
                shift -= 8;
            }
            i++;
        }
    }

    byte[] unpack(int[] src, int srcOffset, int destLength) {
        byte[] dest = new byte[destLength];
        int i = srcOffset;
        int count = 0;
        for (int j = 0; j < destLength; j++) {
            dest[j] = (byte) ((src[i] >> (24 - (8*count))) & 0xff);
            count++;
            if (count == 4) {
                count = 0;
                i++;
            }
        }
        return dest;
    }

    public static void main(String[] args) throws IOException {

        //check args
        if(args.length != 7 ||
                !args[0].equals("-inputFile") ||
                !args[2].equals("-outputFile") ||
                !args[4].equals("-keyWord")||
                !(args[6].equals("-encrypt") || args[6].equals("-decrypt"))){

            System.out.println("Please, write arguments properly:");
            System.out.println("-inputFile <input file path> -outputFile <output file path> -keyWord <key word> -encrypt|-decrypt");
            return;
        }
        String inPath = args[1];
        String outPath = args[3];
        String keyWord = args[5];
        String option = args[6];

        TEA tea = new TEA(keyWord.getBytes());

        byte[] message = Files.readAllBytes(Paths.get(inPath));

        //in accordance with option take result
        byte[] result;
        switch (option){
            case "-encrypt":
                result = tea.encrypt(message);
                break;
            case "-decrypt":
                result = tea.decrypt(message);
                break;
            default:
                result = new byte[]{};
        }

        //write result to file
        Files.write(Paths.get(outPath), result);




//        TEA tea = new TEA("And is there honey still for tea?".getBytes());
//
//        byte[] original = Files.readAllBytes(Paths.get("in_file.txt"));
//
//        byte[] crypt = tea.encrypt(original);
//
//        Files.write(Paths.get("out_file.txt"), crypt);
//
//        byte[] result = tea.decrypt(crypt);
//
//        Files.write(Paths.get("res_file.txt"), result);

    }
}
