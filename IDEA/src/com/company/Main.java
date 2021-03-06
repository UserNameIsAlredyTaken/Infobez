package com.company;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    private final static int CBC_BLOCK_SIZE = 8; //8 байта
    private final static int IDEA_BLOCK_SIZE = 2; //8 байта
    private final static int ROUNDS_COUNT = 8;
    private final static int KEYS_IN_ONE_ROUND = 6;
    private final static int BYTES_IN_KEY_TABLE = 16;

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


        ArrayList<Byte> pair1 = new ArrayList<>();
        pair1.add((byte) 0x11); pair1.add((byte) 0xfb);
        ArrayList<Byte> pair2 = new ArrayList<>();
        pair2.add((byte) 0xed); pair2.add((byte) 0x2b);
        ArrayList<Byte> pair3 = new ArrayList<>();
        pair3.add((byte) 0x01); pair3.add((byte) 0x98);
        ArrayList<Byte> pair4 = new ArrayList<>();
        pair4.add((byte) 0x6d); pair4.add((byte) 0xe5);
        ArrayList<ArrayList<Byte>> initVector = new ArrayList<>();
        initVector.add(pair1);
        initVector.add(pair2);
        initVector.add(pair3);
        initVector.add(pair4);

        byte[] message = Files.readAllBytes(Paths.get(inPath));

        //in accordance with option take result
        byte[] result;
        switch (option){
            case "-encrypt":
                ArrayList<ArrayList<ArrayList<Byte>>> table = GetKeys(KeyToBytes(keyWord));
                ArrayList<ArrayList<ArrayList<Byte>>> blocksForEncryption = ToBlocks(message);
                ArrayList<ArrayList<ArrayList<Byte>>> encryptedBlocks = CbcEncrypt(blocksForEncryption, initVector, table);
                result = BlocksToBytes(encryptedBlocks);
                break;
            case "-decrypt":
                table = GetOppositeKeys(KeyToBytes(keyWord));
                ArrayList<ArrayList<ArrayList<Byte>>> blocksForDecryption = ToBlocks(message);
                ArrayList<ArrayList<ArrayList<Byte>>> decryptedBlocks = CbcDecrypt(blocksForDecryption, initVector, table);
                result = BlocksToBytes(decryptedBlocks);
                System.out.println(new String(result));
                break;
            default:
                result = new byte[]{};
        }

        Files.write(Paths.get(outPath), result);
    }

    public static byte[] KeyToBytes(String str){
        String[] keyNums = str.split(",");
        byte[] nums = new byte[16];
        for (int i = 0; i < 16; i++) {
            nums[i] = (byte)Integer.parseInt(keyNums[i]);
        }
        return nums;
    }


    public static ArrayList<ArrayList<ArrayList<Byte>>> CbcEncrypt(ArrayList<ArrayList<ArrayList<Byte>>> blocks , ArrayList<ArrayList<Byte>> initVector, ArrayList<ArrayList<ArrayList<Byte>>> keyTable){
        ArrayList<ArrayList<ArrayList<Byte>>> resultBlocks = new ArrayList<>();
        ArrayList<ArrayList<Byte>> resultBlock;

        for (ArrayList<ArrayList<Byte>> block : blocks) {
            ArrayList<ArrayList<Byte>> xorResult = XorBlocks(initVector, block);
            resultBlock = EncryptBlock(xorResult, keyTable);
            initVector = new ArrayList<>(resultBlock);
            resultBlocks.add(resultBlock);
        }

        return resultBlocks;
    }

    public static ArrayList<ArrayList<ArrayList<Byte>>> CbcDecrypt(ArrayList<ArrayList<ArrayList<Byte>>> blocks , ArrayList<ArrayList<Byte>> initVector, ArrayList<ArrayList<ArrayList<Byte>>> keyTable){
        ArrayList<ArrayList<ArrayList<Byte>>> resultBlocks = new ArrayList<>();
        ArrayList<ArrayList<Byte>> resultBlock;

        for (ArrayList<ArrayList<Byte>> block : blocks) {
            ArrayList<ArrayList<Byte>> decryptResult = EncryptBlock(block, keyTable);
            resultBlock = XorBlocks(initVector, decryptResult);
            initVector = new ArrayList<>(block);
            resultBlocks.add(resultBlock);
        }

        return resultBlocks;
    }

    public static ArrayList<ArrayList<Byte>> XorBlocks(ArrayList<ArrayList<Byte>> block1, ArrayList<ArrayList<Byte>> block2){
        ArrayList<ArrayList<Byte>> resultBlock = new ArrayList<>();

        //4 - пары в блоке
        for (int i = 0; i < 4; i++) {
            ArrayList<Byte> resultPair = new ArrayList<>();
            ArrayList<Byte> pair1 = block1.get(i);
            ArrayList<Byte> pair2 = block2.get(i);
            //2 байта в паре
            for (int j = 0; j < 2; j++) {
                Byte resultByte = (byte)(pair1.get(j) ^ pair2.get(j));
                resultPair.add(resultByte);
            }
            resultBlock.add(resultPair);
        }

        return resultBlock;
    }


    public static String BlocksToString(ArrayList<ArrayList<ArrayList<Byte>>> blocks){
        byte[] bytes = new byte[blocks.size() * CBC_BLOCK_SIZE];
        int i = 0;
        for (ArrayList<ArrayList<Byte>> block:blocks) {
            for (ArrayList<Byte> pair : block) {
                for (Byte b : pair) {
                    bytes[i] = b;
                    i++;
                }
            }
        }
        return new String(bytes);
    }

    public static byte[] BlocksToBytes(ArrayList<ArrayList<ArrayList<Byte>>> blocks){
        byte[] bytes = new byte[blocks.size() * CBC_BLOCK_SIZE];
        int i = 0;
        for (ArrayList<ArrayList<Byte>> block:blocks) {
            for (ArrayList<Byte> pair : block) {
                for (Byte b : pair) {
                    bytes[i] = b;
                    i++;
                }
            }
        }
        return bytes;
    }

    public static ArrayList<ArrayList<ArrayList<Byte>>> ToBlocks(byte[] bytes) throws IOException {
        ArrayList<ArrayList<ArrayList<Byte>>> blocks = new ArrayList<>();

        double length = bytes.length; //конвертируем bytes.length в double чтобы приделении использовалась арифметика с плавующим числом
        int blocksCount = (int) Math.ceil(length / CBC_BLOCK_SIZE); //делим с округлением в большую сторону

        //i - итератор по всем байтам, j - по блокам
        for (int j = 0, i = 0; j < blocksCount; j++) {
            ArrayList<ArrayList<Byte>> block = new ArrayList<>();

            ArrayList<Byte> pair = new ArrayList<>(); ; //упаковываем по 4 байтовых пары в один блок (8 бит * 2 бита в паре * 4 = 64)
            for (int k = 0; k < CBC_BLOCK_SIZE; k++, i++) {

                byte b;
                if(i < bytes.length){
                    b = bytes[i];
                }else{
                    b = 0; //заполняем нулями оставшуюся часть
                }

                if(k % 2 == 0){
                    pair = new ArrayList<>(); //начинаем новую пару
                    pair.add(b);
                }else{
                    pair.add(b);
                    block.add(pair); //заканчиваем пару, добавляем в блок
                }
            }
            blocks.add(block);
        }
        return blocks;
    }

    public static ArrayList<ArrayList<ArrayList<Byte>>> ToBlocks(String str) throws IOException {

        byte[] bytes = str.getBytes();
        ArrayList<ArrayList<ArrayList<Byte>>> blocks = new ArrayList<>();

        double length = bytes.length; //конвертируем bytes.length в double чтобы приделении использовалась арифметика с плавующим числом
        int blocksCount = (int) Math.ceil(length / CBC_BLOCK_SIZE); //делим с округлением в большую сторону

        //i - итератор по всем байтам, j - по блокам
        for (int j = 0, i = 0; j < blocksCount; j++) {
            ArrayList<ArrayList<Byte>> block = new ArrayList<>();

            ArrayList<Byte> pair = new ArrayList<>(); ; //упаковываем по 4 байтовых пары в один блок (8 бит * 2 бита в паре * 4 = 64)
            for (int k = 0; k < CBC_BLOCK_SIZE; k++, i++) {

                byte b;
                if(i < bytes.length){
                    b = bytes[i];
                }else{
                    b = 0; //заполняем нулями оставшуюся часть
                }

                if(k % 2 == 0){
                    pair = new ArrayList<>(); //начинаем новую пару
                    pair.add(b);
                }else{
                    pair.add(b);
                    block.add(pair); //заканчиваем пару, добавляем в блок
                }
            }
            blocks.add(block);
        }
        return blocks;
    }


    //должно быть 4 блока по 2 байта в каждом
    public static ArrayList<ArrayList<Byte>> EncryptBlock(ArrayList<ArrayList<Byte>> blocks, ArrayList<ArrayList<ArrayList<Byte>>> keyTable){

        int d1 = TwoBytesToInt(blocks.get(0));
        int d2 = TwoBytesToInt(blocks.get(1));
        int d3 = TwoBytesToInt(blocks.get(2));
        int d4 = TwoBytesToInt(blocks.get(3));

        ArrayList<Byte> D1 = new ArrayList<>();
        ArrayList<Byte> D2 = new ArrayList<>();
        ArrayList<Byte> D3 = new ArrayList<>();
        ArrayList<Byte> D4 = new ArrayList<>();

        for (int i = 0; i < ROUNDS_COUNT; i++) {
            int k1 = TwoBytesToInt(keyTable.get(i).get(0));
            int k2 = TwoBytesToInt(keyTable.get(i).get(1));
            int k3 = TwoBytesToInt(keyTable.get(i).get(2));
            int k4 = TwoBytesToInt(keyTable.get(i).get(3));
            int k5 = TwoBytesToInt(keyTable.get(i).get(4));
            int k6 = TwoBytesToInt(keyTable.get(i).get(5));

            int A = mult(d1, k1);//(d1 * k1) % 65537; //mod(d1 * k1, 65537);
            int B = (d2 + k2) % 65536; //mod(d2 + k2, 65536);
            int C = (d3 + k3) % 65536; //mod(d3 + k3, 65536);
            int D = mult(d4, k4);//(d4 * k4) % 65537; //mod(d4 * k4, 65537);
            int E = A ^ C;
            int F = B ^ D;

            int G = mult(k5, E);//(k5 * E) % 65537; //mod(k5 * E, 65537);
            int H = (G + F) % 65536; //mod(G + F, 65536);
            int L = mult(H, k6);//(H * k6) % 65537; //mod(H * k6, 65537);
            int M = (G + L) % 65536; //mod(G + L, 65536);

            d1 = A ^ L;
            d2 = C ^ L;
            d3 = B ^ M;
            d4 = D ^ M;
        }

        //выходное преобразование
        int k1 = TwoBytesToInt(keyTable.get(ROUNDS_COUNT).get(0));
        int k2 = TwoBytesToInt(keyTable.get(ROUNDS_COUNT).get(1));
        int k3 = TwoBytesToInt(keyTable.get(ROUNDS_COUNT).get(2));
        int k4 = TwoBytesToInt(keyTable.get(ROUNDS_COUNT).get(3));

        int d2_prev = d2;

        d1 = mult(d1, k1);
        d2 = (d3 + k2) % 65536;
        d3 = (d2_prev + k3) % 65536;
        d4 = mult(d4, k4);

        D1.add((byte) (d1/256)); D1.add((byte)(d1%256));
        D2.add((byte) (d2/256)); D2.add((byte)(d2%256));
        D3.add((byte) (d3/256)); D3.add((byte)(d3%256));
        D4.add((byte) (d4/256)); D4.add((byte)(d4%256));

        blocks = new ArrayList<>();
        blocks.add(D1);
        blocks.add(D2);
        blocks.add(D3);
        blocks.add(D4);
        return blocks;
    }

    //находит мультипликативную инверсию числа
    public static ArrayList<Byte> MultInversion(ArrayList<Byte> bytes){
        int x = TwoBytesToInt(bytes);
        int y = 0;
        int z;
        do{
            y++;
            z = x * y;
            z--;

        }while (z % 65537 != 0);

        ArrayList<Byte> result = new ArrayList<>();
        result.add((byte) (y/256)); result.add((byte)(y%256));

        return result;
    }

    private static int mult(long x, long y)
    {
        if(x == 0){
            x = 65536;
        }
        if(y == 0){
            y = 65536;
        }

        long res = ((x * y) % 65537);

        if(res == 65536) {
            return 0;
        } else {
            return (int)res;
        }
    }

    private static int mod(int x, int y)
    {
        int result = x % y;
        if (result < 0)
        {
            result += y;
        }
        return result;
    }

    //возвращает тот же байт но отрицательный
    public static ArrayList<Byte> ArifInversion(ArrayList<Byte> bytes){
        short x = (short)-TwoBytesToInt(bytes);

        ArrayList<Byte> result = new ArrayList<>();
        result.add((byte)((x >> 8)& 0xff));
        result.add((byte) (x & 0xff));
        return result;
    }

    public static int TwoBytesToInt(ArrayList<Byte> bytes){
        int i0 = bytes.get(0) & 0xff;
        int i1 = bytes.get(1) & 0xff;
        return i0 * 256 + i1; //соединяем байты
    }

    public static ArrayList<ArrayList<Byte>> SplitIntoBlocks(byte[] message, int blockSize){
        ArrayList<ArrayList<Byte>> blocks = new ArrayList<>();
        for (int i = 0; i < message.length; i += blockSize) {
            ArrayList<Byte> block = new ArrayList<>();
            for (int j = i; j < i + blockSize; j++) {
                if(j < message.length){
                    block.add(message[j]);
                }else{
                    break;
                }
            }
            blocks.add(block);
        }
        return blocks;
    }

public static ArrayList<ArrayList<ArrayList<Byte>>> GetKeys(byte[] key)
{
    key = Arrays.copyOfRange(key, 0, BYTES_IN_KEY_TABLE); //обрезаем лишние байты из ключа

    ArrayList<ArrayList<Byte>> allKeys = GetAllKeysSequence(key);

    ArrayList<ArrayList<ArrayList<Byte>>> table = new ArrayList<>();
    for (int i = 0; i < ROUNDS_COUNT; i++) {
        table.add(new ArrayList<>(allKeys.subList(i * KEYS_IN_ONE_ROUND, (i+1) * KEYS_IN_ONE_ROUND)));
    }
    table.add(new ArrayList<>(allKeys.subList(ROUNDS_COUNT * KEYS_IN_ONE_ROUND, allKeys.size() - 4)));
    return table;
}

public static ArrayList<ArrayList<ArrayList<Byte>>> GetOppositeKeys(byte[] key){

    key = Arrays.copyOfRange(key, 0, BYTES_IN_KEY_TABLE); //обрезаем лишние байты из ключа
    ArrayList<ArrayList<ArrayList<Byte>>> keyTable = GetKeys(key);
    ArrayList<ArrayList<ArrayList<Byte>>> table = new ArrayList<>();

    ArrayList<ArrayList<Byte>> line = new ArrayList<>();

    int j = ROUNDS_COUNT;
    line.add(MultInversion(keyTable.get(j).get(0)));
    line.add(ArifInversion(keyTable.get(j).get(1)));
    line.add(ArifInversion(keyTable.get(j).get(2)));
    line.add(MultInversion(keyTable.get(j).get(3)));
    line.add(keyTable.get(j - 1).get(4));
    line.add(keyTable.get(j - 1).get(5));

    table.add(line);
    line = new ArrayList<>();

    j--;

    for (; j > 0; j--) {


        line.add(MultInversion(keyTable.get(j).get(0)));
        line.add(ArifInversion(keyTable.get(j).get(2))); //этот странный порядок индексов - не ошибка
        line.add(ArifInversion(keyTable.get(j).get(1)));
        line.add(MultInversion(keyTable.get(j).get(3)));
        line.add(keyTable.get(j - 1).get(4));
        line.add(keyTable.get(j - 1).get(5));

        table.add(line);
        line = new ArrayList<>();
    }

    line.add(MultInversion(keyTable.get(0).get(0)));
    line.add(ArifInversion(keyTable.get(0).get(1)));
    line.add(ArifInversion(keyTable.get(0).get(2)));
    line.add(MultInversion(keyTable.get(0).get(3)));
    table.add(line);
    return table;
}

public static ArrayList<ArrayList<Byte>> GetAllKeysSequence(byte[] key){
    ArrayList<ArrayList<Byte>> allKeys = new ArrayList<>();
    ArrayList<ArrayList<Byte>> keyBlocks;
    for (int i = 0; i < 7; i++) {
        keyBlocks = SplitIntoBlocks(key, IDEA_BLOCK_SIZE);
        allKeys.addAll(keyBlocks);
        ShiftKey(key, 25);//сдвигаем все ключи
    }
    return allKeys;
}


    private static void ShiftKey(byte[] key, int positions){
        for (int i = 0; i < positions; i++) {

            byte carryOld = (byte)(key[0] & 0b10000000);//получаем кери бит в конце байта
            byte carryNew;

            for (int j = key.length - 1; j >= 0; j--) {
                carryOld = (byte)(carryOld == -128 ? 0b00000001 : 0b00000000);//и сдвигаем его в начало байта(так как подставлять будем туда)
                carryNew = (byte)(key[j] & 0b10000000); //получаем новый керри бит из конца
                key[j] <<= 1; //сдвигаем байт

                key[j] |= carryOld; //вставляем старый керри бит в начало
                carryOld = carryNew;
            }
        }
    }
}
