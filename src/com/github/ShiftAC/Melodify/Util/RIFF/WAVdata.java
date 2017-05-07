package com.github.ShiftAC.Melodify.Util.RIFF;

import com.github.ShiftAC.Melodify.Util.BinaryIO;
import com.github.ShiftAC.Melodify.Util.CorruptedObjectException;
import com.github.ShiftAC.Melodify.Util.Sound;

class WAVdata implements BinaryIO
{
    int length;
    byte[] samples;

    public void setLength(int len)
    {
        length = len;
        samples = new byte[len];
    }

    @Override
    public int getDataLength()
    {
        return length;
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

        for (int i = 0; i < length; ++i)
        {
            arr[start + i] = samples[i];
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

        for (int i = 0; i < length; ++i)
        {
            samples[i] = arr[start + i];
        }
    }
}
