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

        System.out.println("Введитеключ");
        String key = reader.readLine();

        System.out.println("Введите строку");
        String message = reader.readLine();

        byte[] result = Crypt(message.getBytes(), key);
        System.out.println(new String(result));

        byte[] ss = Crypt(result, key);
        System.out.println(new String(ss));
    }

    //шифрует не защифрованное, рассшифровывает зашифрованное
    public static byte[] Crypt(byte[] message, String key) { //ключи должны быть не короче 11-ти латинских букв

        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);

        byte[] bits1 = SplitBytesIntoBits(bytes, 64);
        byte[] bits2 = SplitBytesIntoBits(bytes, 67);
        byte[] bits3 = SplitBytesIntoBits(bytes, 79);

        LRS lrs1 = new LRS(bits1, new int[]{0, 1, 3, 4, 63});
        LRS lrs2 = new LRS(bits2, new int[]{0, 1, 2, 5, 66});
        LRS lrs3 = new LRS(bits3, new int[]{0, 2, 3, 4, 78});

        Gamma gamma = new Gamma(lrs1, lrs2, lrs3);


        byte[] result = new byte[message.length];
        for (int i = 0; i < message.length; i++) {
            byte gm = gamma.GetNextByte();
            result[i] = (byte)(gm ^ message[i]);
        }

        return result;
    }

    public static byte[] SplitBytesIntoBits(byte[] bytes, int n){
        byte[] bits = new byte[n];
        byte[] bytesBuffer = bytes.clone();
        //i - итератор bits, j - итератор bytes
        for(int i = 0, j = 0; i < n; i++){
            j = i / BITS_IN_BYTE;

            bits[i] = (byte)(bytesBuffer[j] & 0x1); //записываем самый прайвый бит
            bytesBuffer[j] = (byte)(bytesBuffer[j] >>> 1); //сдвигаем все биты вправо на 1
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
