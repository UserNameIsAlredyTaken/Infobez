package com.company;

import javafx.util.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import static java.util.stream.Collectors.toList;

public class Main {

    public static void main(String[] args) {

        //check args
        if(args.length != 7 ||
                !args[0].equals("-inputFile") ||
                !args[2].equals("-outputFile") ||
                !args[4].equals("-keyWord")){

            System.out.println("Please, write arguments properly:");
            System.out.println("-inputFile <input file path> -outputFile <output file path> -keyWord <key word> -encrypt|-decrypt");
            return;
        }
        String inPath = args[1];
        String outPath = args[3];
        String keyWord1 = args[5];
        String keyWord2 = args[6];

        String message;
        try {
            message = new String(Files.readAllBytes(Paths.get(inPath))); //read message
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        //in accordance with option take result
        String result = Encrypt(message, keyWord1, keyWord2);

        //write result to file
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outPath));
            writer.write(result);

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static ArrayList<Character> alphabet = new ArrayList<>(Arrays.asList(
            'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','r','s','t','u','v','w','x','y','z'
    ));

    //НЕ ДОЛЖНО БЫТЬ Q В СООБЩЕНИИ ИЛИ КЛЮЧЕ
    public static ArrayList<ArrayList<Character>> GenerateSquare(String key){
        key = key.toLowerCase();
        StringBuilder sb = new StringBuilder();
        key = key.chars().distinct().collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();//удалить повторяющиеся буквы

        ArrayList<Character> localAlphabet = new ArrayList<>(alphabet);
        ArrayList<ArrayList<Character>> square = new ArrayList<ArrayList<Character>>();

        int key_i = 0;//итератор ключа
        int alpha_i = 0;//итератор алфавита
        for(int i = 0; i < 5; i++){
            ArrayList<Character> line = new ArrayList<>();
            for(int j = 0; j < 5; j++){
                if(key_i != key.length()){
                    line.add(key.charAt(key_i));
                    localAlphabet.remove((Character) key.charAt(key_i));
                    key_i++;
                }else{
                    line.add(localAlphabet.get(alpha_i));
                    alpha_i++;
                }
            }
            square.add(line);
        }
        System.out.println();
        return square;
    }

    public static Pair<Integer, Integer> GetIndexes(ArrayList<ArrayList<Character>> sqr, Character c){
        ArrayList<Character> lineContainingChar = sqr.stream().filter(line -> line.contains(c)).collect(toList()).get(0);
        int y = sqr.indexOf(lineContainingChar);
        int x = lineContainingChar.indexOf(c);
        return new Pair<>(y,x);
    }

    public static ArrayList<Pair<Character,Character>> SplitToBigramms(String str){
        if(str.length() %2 != 0)//если не чётное число символов, то добавляем в конец 'x'
            str = str + 'x';

        String[] doubleChars = str.split("(?<=\\G.{2})"); //рахбиваем на короткие строки по 2 буквы

        ArrayList<Pair<Character,Character>> pairs = new ArrayList<Pair<Character,Character>>();
        for (String doubleChar: doubleChars) {
            pairs.add(new Pair<>(doubleChar.charAt(0), doubleChar.charAt(1)));
        }
        return pairs;
    }

    public static Character GetCharByIndex(ArrayList<ArrayList<Character>> sqr, Pair<Integer, Integer> indexes){
        return sqr.get(indexes.getValue()).get(indexes.getKey());
    }



    public static String Encrypt(String message, String key1, String key2){
        ArrayList<ArrayList<Character>> sqr1 = GenerateSquare(key1);
        ArrayList<ArrayList<Character>> sqr2 = GenerateSquare(key2);

        message = message.replaceAll(" ", ""); //удаляем все пробелы

        ArrayList<Pair<Character,Character>> bigramms = SplitToBigramms(message);

        StringBuilder decrypted = new StringBuilder();
        for (Pair<Character,Character> bigramm : bigramms) {
            Pair<Integer, Integer> indexes1 = GetIndexes(sqr1, bigramm.getKey());
            Pair<Integer, Integer> indexes2 = GetIndexes(sqr2, bigramm.getValue());

            Pair<Integer, Integer> newIndexes1;
            Pair<Integer, Integer> newIndexes2;

            if(!indexes1.getValue().equals(indexes2.getValue())){ //если расположнеы на разных столбах
                newIndexes1 = new Pair<>(indexes1.getKey(), indexes2.getValue());
                newIndexes2 = new Pair<>(indexes2.getKey(), indexes1.getValue());
            }else{
                newIndexes1 = new Pair<>(indexes2.getKey(), indexes1.getValue());
                newIndexes2 = new Pair<>(indexes1.getKey(), indexes2.getValue());
            }

            decrypted.append(sqr1.get(newIndexes1.getKey()).get(newIndexes1.getValue()));
            decrypted.append(sqr2.get(newIndexes2.getKey()).get(newIndexes2.getValue()));
        }

        return decrypted.toString();
    }
}
