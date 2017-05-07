package com.github.ShiftAC.Melodify.Util.RIFF;

import com.github.ShiftAC.Melodify.Util.BinaryArrayOperations;
import com.github.ShiftAC.Melodify.Util.BinaryIO;
import com.github.ShiftAC.Melodify.Util.CorruptedObjectException;

public class WAVfmt implements BinaryIO
{
    int length;
    
    short codeFormat;   //编码格式
    public static final short WAVE_FORMAT_PCM = 1;

    short channel;     //声道数
    public static final short MONO = 1;
    public static final short STEREO = 2;

    int samplesPerSec;  //采样频率

    int avgBytesPerSec; //每秒的数据量
    
    short blockAlign;   //块对齐
    
    short bitsPerSample;//WAVE文件的采样大小
    public static final int PCM8 = 8;
    public static final int PCM16 = 16;

    short extra;

    public boolean isStereo()
    {
        return channel == STEREO;
    }

    private boolean hasExtra()
    {
        return length == 18;
    }

    public void setLength(int len)
    {
        length = len;
    }

    @Override
    public int getDataLength()
    {
        return length;
    }

    public void checkFormat()
        throws CorruptedObjectException
    {
        if (bitsPerSample != PCM8 && bitsPerSample != PCM16)
        {
            throw new CorruptedObjectException("Invalid bits/sample value " +
                bitsPerSample);
        }
        if (channel != MONO && channel != STEREO)
        {
            throw new CorruptedObjectException("Invalid channel value " +
                channel);
        }
        if (codeFormat != WAVE_FORMAT_PCM)
        {
            throw new CorruptedObjectException("Invalid format value " +
                codeFormat);
        }
        int expect = bitsPerSample / 8 * samplesPerSec * channel;
        if (avgBytesPerSec != expect)
        {
            throw new CorruptedObjectException("Invalid byte rate value " +
                avgBytesPerSec + ", expected " + expect + ".");
        }
        expect = bitsPerSample / 8 * channel;
        if (blockAlign != expect)
        {
            throw new CorruptedObjectException("Invalid block align value " +
                blockAlign + ", expected " + expect +".");
        }
    }

    @Override
    public void setBinary(byte[] arr, int start)
        throws IllegalArgumentException
    {
        int expectLen = start + getDataLength();
        if (arr.length < expectLen)
        {
            throw new IllegalArgumentException("Array size is " + 
                arr.length + ", expected at least " + expectLen + ".");
        }

        BinaryArrayOperations.setShortBytes(arr, start, codeFormat);
        BinaryArrayOperations.setShortBytes(arr, start + 2, channel);
        BinaryArrayOperations.setIntBytes(arr, start + 4, samplesPerSec);
        BinaryArrayOperations.setIntBytes(arr, start + 8, avgBytesPerSec);
        BinaryArrayOperations.setShortBytes(arr, start + 12, blockAlign);
        BinaryArrayOperations.setShortBytes(arr, start + 14, bitsPerSample);
        if (hasExtra())
        {
            BinaryArrayOperations.setShortBytes(arr, start + 16, extra);
        }
    }

    @Override
    public void parseBinary(byte[] arr, int start)
        throws IllegalArgumentException, CorruptedObjectException
    {
        int expectLen = start + getDataLength();
        if (arr.length < expectLen)
        {
            throw new IllegalArgumentException("Array size is " + 
                arr.length + ", expected at least " + expectLen + ".");
        }

        codeFormat = BinaryArrayOperations.getShortBytes(arr, start);
        channel = BinaryArrayOperations.getShortBytes(arr, start + 2);
        samplesPerSec = BinaryArrayOperations.getIntBytes(arr, start + 4);
        avgBytesPerSec = BinaryArrayOperations.getIntBytes(arr, start + 8);
        blockAlign = BinaryArrayOperations.getShortBytes(arr, start + 12);
        bitsPerSample = BinaryArrayOperations.getShortBytes(arr, start + 14);
        if (hasExtra())
        {
            extra = BinaryArrayOperations.getShortBytes(arr, start + 16);
        }

        checkFormat();
    }
}
