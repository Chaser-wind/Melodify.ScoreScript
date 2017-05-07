package com.github.ShiftAC.Melodify.Util.RIFF;

import com.github.ShiftAC.Melodify.Util.BinaryArrayOperations;
import com.github.ShiftAC.Melodify.Util.CorruptedObjectException;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public abstract class AbstractRIFFFile
{
    private static final int myIdentify = 
        AbstractRIFFChunk.getIdentifyValue("RIFF");

    int length;
    int type;
    int chunkCount = 1;
    AbstractRIFFChunk[] chunks;

    protected void setChunkCount(int count)
        throws IllegalArgumentException
    {
        if (chunkCount <= 0)
        {
            throw new IllegalArgumentException("Invalid chunk count " + 
                count + ", expected positive integer.");
        }
        chunkCount = count;
        chunks = new AbstractRIFFChunk[count];
        initChunks();
    }

    protected abstract void checkType(int type)
        throws CorruptedObjectException;

    private void checkFormat(int identify)
        throws CorruptedObjectException
    {
        if (identify != myIdentify)
        {
            throw new CorruptedObjectException("Invalid RIFF identifier " + 
                identify + ", expected " + myIdentify);
        }
        checkType(type);
    }

    protected abstract void initChunks();

    public void read(InputStream is)
        throws IOException, CorruptedObjectException
    {
        int identify;
        identify = BinaryArrayOperations.readIntBytes(is);
        length = BinaryArrayOperations.readIntBytes(is);
        type = BinaryArrayOperations.readIntBytes(is);

        checkFormat(identify);

        for (int i = 0; i < chunkCount; ++i)
        {
            chunks[i].read(is);
        }
    }

    public void write(OutputStream os)
        throws IOException
    {
        BinaryArrayOperations.writeIntBytes(os, myIdentify);
        BinaryArrayOperations.writeIntBytes(os, length);
        BinaryArrayOperations.writeIntBytes(os, type);
        for (int i = 0; i < chunkCount; ++i)
        {
            chunks[i].write(os);
        }
    }
}