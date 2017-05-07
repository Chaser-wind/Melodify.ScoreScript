package com.github.ShiftAC.Melodify.Util;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class BinaryArrayOperations
{
    public static void setBytesLen(byte[] arr, int start, long x, int len)
    {
        for (int i = start, shift = 0; i < start + len; ++i, shift += 8)
        {
            arr[i] = (byte)(x >> shift);
        }
    }

    public static long getBytesLen(byte[] arr, int start, int len)
    {
        long res = 0;
        for (int i = start, shift = 0; i < start + len; ++i, shift += 8)
        {
            res |= ((long)arr[i] << shift) & (0x00000000000000FFL << shift);
        }
        return res;
    }

    public static long readBytesLen(InputStream is, int len)
        throws IOException
    {
        byte[] buf = new byte[len];
        is.read(buf);
        return getBytesLen(buf, 0, len);
    }

    public static void writeBytesLen(OutputStream os, int x, int len)
        throws IOException
    {
        byte[] buf = new byte[len];
        setBytesLen(buf, 0, x, len);
        os.write(buf);
    }

    public static void setIntBytes(byte[] arr, int start, int x)
    {
        setBytesLen(arr, start, x, 4);
    }

    public static int getIntBytes(byte[] arr, int start)
    {
        return (int)getBytesLen(arr, start, 4);
    }

    public static void setShortBytes(byte[] arr, int start, short x)
    {
        setBytesLen(arr, start, x, 2);
    }

    public static short getShortBytes(byte[] arr, int start)
    {
        return (short)getBytesLen(arr, start, 2); 
    }

    public static void writeIntBytes(OutputStream os, int x)
        throws IOException
    {
        writeBytesLen(os, x, 4);
    }

    public static int readIntBytes(InputStream is)
        throws IOException
    {
        return (int)readBytesLen(is, 4);
    }

    public static void writeShortBytes(OutputStream os, short x)
        throws IOException
    {
        writeBytesLen(os, x, 2);
    }

    public static short readShortBytes(InputStream is)
        throws IOException
    {
        return (short)readBytesLen(is, 2); 
    }
}