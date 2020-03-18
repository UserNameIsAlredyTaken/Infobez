package com.company;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {


    public static void main(String[] args) {
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

        String message;
        try {
            message = new String(Files.readAllBytes(Paths.get(inPath))); //read message
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        //in accordance with option take result
        String result = new String();
        switch (option){
            case "-encrypt":
                result = Encrypt(message, keyWord);
                break;
            case "-decrypt":
                result = Decrypt(message, keyWord);
                break;
        }

        //write result to file
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(outPath));
            writer.write(result);

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        String encrypted = Encrypt(message, keyWord);
//        System.out.println(encrypted);
//
//        String decrypted = Decrypt(encrypted, keyWord);
//        System.out.println(decrypted);
    }

    private static String Encrypt(String message, String keyWord){
        ArrayList<Integer> keyNums = KeyWordToKeyNums(keyWord);

        ArrayList<ArrayList<Character>> table = new ArrayList<>();

        int numOfLines = message.length() / keyNums.size() + //number of lines equals  meessage's length mod key word length
                (message.length() % keyNums.size() == 0 ? 0 : 1); //and + 1 if  meessage's length has a remainder of division

        for(int i = 0; i < numOfLines; i++){
            ArrayList<Character> lineInTable = new ArrayList<>(); //create new line

            for(int j = 0; j < keyNums.size(); j++){
                int charNum = i * keyNums.size() + j;

                if(charNum < message.length())
                    lineInTable.add(message.charAt(charNum)); //full fill this line

            }

            table.add(lineInTable); //add the line to  the table
        }

//        for (ArrayList<Character> line : table){
//            for(Character character : line){
//                System.out.print(character + " ");
//            }
//            System.out.println();
//        }

        StringBuilder encryptedMessage = new StringBuilder();

        //reading the table
        for (Integer i : keyNums) { //in accordance with key numbers
            for (ArrayList<Character> line : table){ //line by line
                if(line.size() > i)
                    encryptedMessage.append(line.get(i));
            }
        }
        return encryptedMessage.toString();
    }

    private static String Decrypt(String message, String keyWord){
        ArrayList<Integer> keyNums = KeyWordToKeyNums(keyWord);

        HashMap<Integer,char[]> columnsWithNumbers = new HashMap<>();
        int columnLength = message.length() / keyNums.size(); //number of lines equals  meessage's length mod key word length
        int lastLIneLength = message.length() % keyNums.size();

        //read encrypted message dividing it ti columns and writing columns in columnsWithNumbers in accordance with key numbers
        for(Integer num : keyNums){
            int thisColumnLength = columnLength + (num < lastLIneLength ? 1 : 0);
            char[] column = message.substring(0, thisColumnLength).toCharArray();
            columnsWithNumbers.put(num, column);

            message = message.substring(thisColumnLength); //cut readed column of the message
        }

//        System.out.println();
//        for (Map.Entry<Integer,char[]> entry : columnsWithNumbers.entrySet()) {
//            System.out.println(entry.getKey() + " " + new String(entry.getValue()));
//        }

        int numOfLines = columnLength + //number of lines equals  meessage's length mod key word length
        (lastLIneLength == 0 ? 0 : 1); //and + 1 if  meessage's length has a remainder of division

        StringBuilder decrypted = new StringBuilder();

        //read columnWithNumbers
        for(int i = 0; i < numOfLines; i++){ //line by line
            for(int j = 0; j < keyNums.size(); j++){ //in accordance with key numbers
                if(columnsWithNumbers.get(j).length > i)
                    decrypted.append(columnsWithNumbers.get(j)[i]);
            }
        }

        return decrypted.toString();
    }

    //"Шифр" -> 1,3,2,0
    private static ArrayList<Integer> KeyWordToKeyNums(String word){
        word = word.toLowerCase();
        char[] charArr = word.toCharArray();
        Arrays.sort(charArr);

        ArrayList<Integer> keyNums = new ArrayList<>();
        for (char c : charArr) {
            keyNums.add(word.indexOf(c));
        }

        return keyNums;
    }
}
