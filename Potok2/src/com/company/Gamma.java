package com.company;

public class Gamma {

    static final int BITS_IN_BYTE = 8;

    private LRS lrs1;
    private LRS lrs2;
    private LRS lrs3;

    private int emptyCellCount = 2;

    public Gamma(LRS lrs1, LRS lrs2, LRS lrs3){
        this.lrs1 = lrs1;
        this.lrs2 = lrs2;
        this.lrs3 = lrs3;
    }

    public byte GetNextByte(){
        byte result = 0;
        for(int i = 0; i < BITS_IN_BYTE; i++){
            result = (byte)(result << 1);
            result = (byte)(result | (GetNextBit() & 0x1));
        }
        return result;
    }

    private byte GetNextBit(){
        byte lrs1Bit = lrs1.GetNextBit();
        byte lrs2Bit = lrs2.GetNextBit();
        byte lrs3Bit = lrs3.GetNextBit();

        int onesCount = 0;

        if(lrs1Bit == 1)
            onesCount++;
        if(lrs2Bit == 1)
            onesCount++;
        if(lrs3Bit == 1)
            onesCount++;

        if(onesCount >= 2){
            return 0x01;
        }else{
            return 0x00;
        }
    }
}
