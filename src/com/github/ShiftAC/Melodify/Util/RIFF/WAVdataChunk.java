package com.github.ShiftAC.Melodify.Util.RIFF;

import com.github.ShiftAC.Melodify.Util.BinaryIO;
import com.github.ShiftAC.Melodify.Util.CorruptedObjectException;

public class WAVdataChunk extends AbstractRIFFChunk
{
    private final int myIdentify = getIdentifyValue("data");

    public WAVdataChunk()
    {
        identify = myIdentify;
    }

    @Override
    protected void setLength(int len)
    {
        super.setLength(len);
        ((WAVdata)data).setLength(len);
    }

    @Override
    protected void checkIdentify()
        throws CorruptedObjectException
    {
        if (myIdentify != identify)
        {
            throw new CorruptedObjectException("Invalid data identifier " + 
                identify + ", expected " + myIdentify + ".");
        }
    }
    
    @Override
    protected void initData()
    {
        data = new WAVdata();
    }
}