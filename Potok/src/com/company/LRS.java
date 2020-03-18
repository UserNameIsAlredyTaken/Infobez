package com.company;

public class LRS {
    private byte[] content;
    private int[] h;

    public LRS(byte[] content, int[] h){
        this.content = content;
        this.h = h;
    }

    public byte GetNext(){
        byte tail = content[0];
        byte head = CalculateHead();

        ShiftContent();
        content[content.length-1] = head;

        return tail;
    }

    private void ShiftContent(){
        for(int i = 0; i < content.length - 1; i++){
            content[i] = content[i+1];
        }
    }

    private byte CalculateHead(){
        byte head = (byte) (content[h[0]] ^ content[h[1]]);

        for(int i = 2; i < h.length; i++){
            head = (byte) (head ^ content[h[i]]);
        }

        return head;
    }

}
