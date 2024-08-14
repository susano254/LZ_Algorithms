package com.susano.LZ_Algorithms;

import java.util.ArrayList;

public class Compressor {
    String stream;
    int windowMaxSize;
    StringBuffer searchBuffer;
    StringBuilder match;
    char nextChar;
    ArrayList<Tag> tags;

    public Compressor(String stream, ArrayList tags){
        this.stream = stream;
        this.tags = tags;
        this.tags.clear();
        searchBuffer = new StringBuffer();
    }

    public void lz77(String stream, int windowMaxSize){
        this.windowMaxSize = windowMaxSize;
        for(int i = 0; i < stream.length(); i += match.length() + 1) {
            //find longest match return a tag
            Tag tag = findLongestMatch(stream, i, searchBuffer);
            //add the tag
            tags.add(tag);
            //modify buffer ( add new matched and trim if necessary )
            searchBuffer.append(match.toString() + nextChar);
            if(searchBuffer.length() > windowMaxSize) {
                searchBuffer = searchBuffer.delete(0, searchBuffer.length() - windowMaxSize);
            }
        }
        //repeat
    }

    private Tag findLongestMatch(String stream, int index, StringBuffer searchBuffer) {
        match = new StringBuilder();
        int tempIndex;
        int matchIndex = searchBuffer.length();

        for(int i = index; i < index + windowMaxSize && i < stream.length(); i++) {
            nextChar = stream.charAt(i);
            tempIndex = searchBuffer.lastIndexOf(match.toString() + nextChar);

            if(tempIndex != -1){
                matchIndex = tempIndex;
                match.append(nextChar);
            }
            else {
                int offset = searchBuffer.length() - matchIndex;
                int length = match.length();
                return new Tag(offset, length, nextChar);
            }
        }



        return new Tag(0,0,'\0');
    }

    public void lz78(String stream){

    }
    public void lzw(String stream){

    }

}
