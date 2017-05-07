package com.github.ShiftAC.Melodify.Util;

public interface BinaryIO
{
    public abstract int getDataLength();

    public default byte[] toBinary()
        throws IllegalArgumentException
    {
        byte[] res = new byte[getDataLength()];
        setBinary(res, 0);
        return res;
    }

    public abstract void setBinary(byte[] arr, int start)
        throws IllegalArgumentException;

    public abstract void parseBinary(byte[] data, int start)
        throws IllegalArgumentException, CorruptedObjectException;
}