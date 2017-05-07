package com.github.ShiftAC.Melodify.Util.RIFF;

import com.github.ShiftAC.Melodify.Util.BinaryIO;
import com.github.ShiftAC.Melodify.Util.CorruptedObjectException;

public class WAVfmtChunk extends AbstractRIFFChunk
{
    private final int myIdentify = getIdentifyValue("fmt ");

    public WAVfmtChunk()
    {
        identify = myIdentify;
    }

    @Override
    protected void setLength(int len)
    {
        super.setLength(len);
        ((WAVfmt)data).setLength(len);
    }

    @Override
    protected void checkIdentify()
        throws CorruptedObjectException
    {
        if (myIdentify != identify)
        {
            throw new CorruptedObjectException(
                "Invalid fmt  identifier " + identify + ", expected " + 
                    myIdentify + ".");
        }
        if (length != 16 && length != 18)
        {
            throw new CorruptedObjectException(
                "Invalid fmt length: " + length);
        }
    }
    
    @Override
    protected void initData()
    {
        data = new WAVfmt();
    }
}