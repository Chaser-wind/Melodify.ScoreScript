package com.github.ShiftAC.Melodify.Util.RIFF;

import com.github.ShiftAC.Melodify.Util.BinaryArrayOperations;
import com.github.ShiftAC.Melodify.Util.BinaryIO;
import com.github.ShiftAC.Melodify.Util.CorruptedObjectException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractRIFFChunk implements BinaryIO
{
    protected int identify;
    int length;
    BinaryIO data;

    public AbstractRIFFChunk()
    {
        initData();
    }

    protected abstract void initData();

    public static int getIdentifyValue(String str)
    {
        if (str.length() < 4)
        {
            throw new IllegalArgumentException(
                "Illegal length, at least 4 is requested.");
        }

        int res = 0;
        res |= (int)str.charAt(0) & 0x000000FF;
        res |= ((int)str.charAt(1) << 8) & 0x0000FF00;
        res |= ((int)str.charAt(2) << 16) & 0x00FF0000;
        res |= ((int)str.charAt(3) << 24) & 0xFF000000;
        return res;
    }

    protected abstract void checkIdentify()
        throws CorruptedObjectException;

    protected void setLength(int len)
        throws IllegalArgumentException
    {
        if (len <= 0)
        {
            throw new IllegalArgumentException("Illegal length: " + len);
        }
        length = len;
    }

    public void read(InputStream is)
        throws IOException, CorruptedObjectException
    {
        identify = BinaryArrayOperations.readIntBytes(is);
        setLength(BinaryArrayOperations.readIntBytes(is));
        checkIdentify();
        byte[] buf = new byte[length];
        is.read(buf);
        data.parseBinary(buf, 0);
    }

    public void write(OutputStream os)
        throws IOException
    {
        os.write(toBinary());
    }

    @Override
    public void setBinary(byte[] arr, int start)
        throws IllegalArgumentException
    {
        if (arr.length < start + length + 8)
        {
            throw new IllegalArgumentException("Array size is " + arr.length + 
                ", expected at least " + (start + length + 8) + ".");
        }
        BinaryArrayOperations.setIntBytes(arr, start, identify);
        BinaryArrayOperations.setIntBytes(arr, start + 4, length);
        data.setBinary(arr, start + 8);
    }

    @Override
    public void parseBinary(byte[] arr, int start)
        throws IllegalArgumentException, CorruptedObjectException
    {
        if (arr.length < 8)
        {
            throw new IllegalArgumentException("Array size is " + arr.length + 
                ", expected at least " + (start + 8) + ".");
        }
        identify = BinaryArrayOperations.getIntBytes(arr, start);
        checkIdentify();
        setLength(BinaryArrayOperations.getIntBytes(arr, start + 4));
        if (arr.length < 8 + start + length)
        {
            throw new IllegalArgumentException("Array size is " + arr.length + 
                ", expected at least " + (start + length + 8) + ".");
        }
        data.parseBinary(arr, start + 8);
    }

    @Override
    public int getDataLength()
    {
        return length + 8;
    }
}