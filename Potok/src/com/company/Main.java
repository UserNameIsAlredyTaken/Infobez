package com.company;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

public class Main {
    static final int BITS_IN_BYTE = 8;

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Введите первый ключ");
        String key1 = reader.readLine();

        System.out.println("Введите второй ключ");
        String key2 = reader.readLine();

        System.out.println("Введите строку");
        String message = reader.readLine();

        byte[] result = Crypt(message.getBytes(), key1, key2);
        System.out.println(new String(result));

        byte[] ss = Crypt(result, key1, key2);
        System.out.println(new String(ss));
    }

    //шифрует не защифрованное, рассшифровывает зашифрованное
    public static byte[] Crypt(byte[] message, String key1, String key2) { //ключи должны быть не короче 11-ти латинских букв

        byte[] bytes1 = key1.getBytes(StandardCharsets.UTF_8);
        byte[] bytes2 = key2.getBytes(StandardCharsets.UTF_8);

        byte[] bits1 = SplitBytesIntoBits(bytes1, 81);
        byte[] bits2 = SplitBytesIntoBits(bytes2, 81);

        LRS lrs1 = new LRS(bits1, new int[]{0, 2, 4, 9, 80});
        LRS lrs2 = new LRS(bits2, new int[]{0, 1, 2, 3, 5, 80});

        Gamma gamma = new Gamma(lrs1, lrs2);


        byte[] result = new byte[message.length];
        for (int i = 0; i < message.length; i++) {
            byte gm = gamma.GetNextByte();
            result[i] = (byte)(gm ^ message[i]);
        }

        return result;
    }

    public static byte[] SplitBytesIntoBits(byte[] bytes, int n){
        byte[] bits = new byte[n];

        //i - итератор bits, j - итератор bytes
        for(int i = 0, j = 0; i < n; i++){
            j = i / BITS_IN_BYTE;

            bits[i] = (byte)(bytes[j] & 0x1); //записываем самый прайвый бит
            bytes[j] = (byte)(bytes[j] >>> 1); //сдвигаем все биты вправо на 1
        }
        return bits;
    }

    public static void StrFromByte(byte[] b){
        for (byte by : b) {
            System.out.print(by + ",");
        }
        System.out.println();
    }
}
