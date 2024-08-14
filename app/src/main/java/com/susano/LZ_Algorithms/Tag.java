package com.susano.LZ_Algorithms;

public class Tag {
    int offset;
    int length;
    char nextChar;

    Tag(int offset, int length, char nextChar){
        this.offset = offset;
        this.length = length;
        this.nextChar = nextChar;
    }
}
