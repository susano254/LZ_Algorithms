package com.susano.LZ_Algorithms;

public class Tag {
    private int offset;
    private int length;
    private char nextChar;

    Tag(int offset, int length, char nextChar){
        this.offset = offset;
        this.length = length;
        this.nextChar = nextChar;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public char getNextChar() {
        return nextChar;
    }

    public void setNextChar(char nextChar) {
        this.nextChar = nextChar;
    }
}
