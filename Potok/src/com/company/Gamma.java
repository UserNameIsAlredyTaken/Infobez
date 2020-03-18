package com.company;




public class Gamma {

    static final int BITS_IN_BYTE = 8;

    private LRS lrs1;
    private LRS lrs2;
    private byte cell1;
    private byte cell2;

    private int emptyCellCount = 2;

    public Gamma(LRS lrs1, LRS lrs2){
        this.lrs1 = lrs1;
        this.lrs2 = lrs2;
    }

    public byte GetNextByte(){
        byte result = 0;
        for(int i = 0; i < BITS_IN_BYTE; i++){
            result = (byte)(result << 1);
            result = (byte)(result | (GetNext() & 0x1));
        }
        return result;
    }

    private byte GetNext(){
        byte result = (byte) (lrs1.GetNext() ^ lrs2.GetNext());

        if(emptyCellCount != 0){
            cell2 = cell1;
            cell1 = result;
            emptyCellCount--;
        }else{
            result = (byte) (result ^ (cell1 & cell2));
        }

        return result;
    }
}
