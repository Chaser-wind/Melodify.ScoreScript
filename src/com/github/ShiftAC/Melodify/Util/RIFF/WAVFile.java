package com.github.ShiftAC.Melodify.Util.RIFF;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import com.github.ShiftAC.Melodify.Util.BinaryArrayOperations;
import com.github.ShiftAC.Melodify.Util.BinaryIO;
import com.github.ShiftAC.Melodify.Util.CorruptedObjectException;
import com.github.ShiftAC.Melodify.Util.Sound;

public class WAVFile extends AbstractRIFFFile
{
    public static final int myType = 
        AbstractRIFFChunk.getIdentifyValue("WAVE");

    public WAVFile()
    {
        type = myType;
        setChunkCount(2);
    }

    public WAVFile(String fileName)
        throws IOException, CorruptedObjectException
    {
        type = myType;
        setChunkCount(2);
        FileInputStream is = new FileInputStream(fileName);
        read(is);
        is.close();
    }

    public WAVFile(InputStream is)
        throws IOException, CorruptedObjectException
    {
        type = myType;
        setChunkCount(2);
        read(is);
    }

    @Override
    protected void checkType(int type)
        throws CorruptedObjectException
    {
        if (type != myType)
        {
            throw new CorruptedObjectException("Invalid WAVE identifier " + 
                type + ", expected " + myType);
        }
    }

    @Override
    protected void initChunks()
    {
        chunks[0] = new WAVfmtChunk();
        chunks[1] = new WAVdataChunk();
    }

    protected static long saturation(long x, long floor, long ceil)
    {
        return x < floor ? floor : (x > ceil ? ceil : x);
    }

    protected static double toVolume(int raw, int bitsPerSample)
    {
        if (bitsPerSample == WAVfmt.PCM8)
        {
            return (double)(raw - 128) / 128;
        }
        else if (bitsPerSample == WAVfmt.PCM16)
        {
            return (double)((short)raw) / 32768;
        }

        return 0;
    }

    protected static long toRaw(double volume, int bitsPerSample)
    {
        if (bitsPerSample == WAVfmt.PCM8)
        {
            return saturation((long)(volume * 128 + 128), 0, 255);
        }
        else if (bitsPerSample == WAVfmt.PCM16)
        {
            return saturation((long)(volume * 32768), -32768, 32767);
        }

        return 0;
    }

    public Sound toSound()
        throws CorruptedObjectException
    {
        try
        {
            WAVdataChunk dataChunk = (WAVdataChunk)chunks[1];
            WAVdata data = (WAVdata)dataChunk.data;
            WAVfmt fmt = (WAVfmt)(chunks[0].data);
            int length = dataChunk.length / 
                (fmt.channel * fmt.bitsPerSample / 8);
            return toSound(0, length);
        }
        catch (IllegalArgumentException e) {}

        // control never reaches here, we use this just to make javac happy.
        return null;
    }

    public Sound toSound(double startTimeInMS, double endTimeInMS)
        throws CorruptedObjectException, IllegalArgumentException
    {
        int samplesPerSec = ((WAVfmt)(chunks[0].data)).samplesPerSec;
        int startPos = (int)(startTimeInMS * samplesPerSec / 1000);
        int endPos = (int)(endTimeInMS * samplesPerSec / 1000);
    
        return toSound(startPos, endPos);
    }

    public Sound toSound(int startPos, int endPos)
        throws CorruptedObjectException, IllegalArgumentException
    {
        WAVdataChunk dataChunk = (WAVdataChunk)chunks[1];
        WAVdata data = (WAVdata)dataChunk.data;
        WAVfmt fmt = (WAVfmt)(chunks[0].data);
        int length = dataChunk.length / 
            (fmt.channel * fmt.bitsPerSample / 8);
        if (startPos < 0 || endPos > length || startPos >= endPos)
        {
            throw new IllegalArgumentException("Illegal interval [" + startPos
                + ", " + endPos + ")" + ", expected subset of [0, " + 
                length + ")");
        }

        Sound sound = new Sound(fmt.isStereo(), fmt.samplesPerSec, 
            endPos - startPos);
        
        // process Linear PCM data
        int bytesPerSample = fmt.bitsPerSample / 8;
        if (!fmt.isStereo())
        {
            for (int i = startPos; i < endPos; ++i)
            {
                int raw = (int)BinaryArrayOperations.getBytesLen(data.samples, 
                    i * bytesPerSample, bytesPerSample);
                sound.leftChannel.set(
                    i - startPos, toVolume(raw, fmt.bitsPerSample));
            }
        }
        else
        {
            for (int i = startPos; i < endPos; ++i)
            {
                int raw;
                raw = (int)BinaryArrayOperations.getBytesLen(data.samples,
                    i * 2 * bytesPerSample, bytesPerSample);
                sound.leftChannel.set(
                    i - startPos, toVolume(raw, fmt.bitsPerSample));
                raw = (int)BinaryArrayOperations.getBytesLen(data.samples,
                    (i * 2 + 1) * bytesPerSample, bytesPerSample);
                sound.rightChannel.set(
                    i - startPos, toVolume(raw, fmt.bitsPerSample));
            }
        }

        return sound;
    }

    public void parseSound(Sound sound, int bitsPerSample)
        throws IllegalArgumentException
    {
        WAVdataChunk dataChunk = (WAVdataChunk)chunks[1];
        WAVdata data = (WAVdata)dataChunk.data;
        WAVfmtChunk fmtChunk = (WAVfmtChunk)chunks[0];
        WAVfmt fmt = (WAVfmt)(chunks[0].data); 
        
        fmtChunk.setLength(16);
        fmt.codeFormat = WAVfmt.WAVE_FORMAT_PCM;
        fmt.channel = sound.isStereo ? WAVfmt.STEREO : WAVfmt.MONO;
        fmt.samplesPerSec = sound.samplesPerSec;
        fmt.avgBytesPerSec = 
            fmt.channel * sound.samplesPerSec * bitsPerSample / 8;
        fmt.blockAlign = (short)(fmt.channel * bitsPerSample / 8);
        fmt.bitsPerSample = (short)bitsPerSample;

        try
        {
            fmt.checkFormat();
        }
        catch (CorruptedObjectException e)
        {
            throw new IllegalArgumentException(e.getMessage());
        }

        dataChunk.setLength(sound.length * fmt.channel * bitsPerSample / 8);
        
        int bytesPerSample = bitsPerSample / 8;
        byte[] arr = data.samples;
        if (!fmt.isStereo())
        {
            for (int i = 0; i < sound.length; ++i)
            {
                long raw = toRaw(sound.leftChannel.get(i), bitsPerSample);
                BinaryArrayOperations.setBytesLen(arr, i * bytesPerSample, raw, 
                    bytesPerSample);
            }
        }
        else
        {
            for (int i = 0; i < sound.length; ++i)
            {
                long raw;
                raw = toRaw(sound.leftChannel.get(i), bitsPerSample);
                BinaryArrayOperations.setBytesLen(arr, i * 2 * bytesPerSample, 
                    raw, bytesPerSample);
                raw = toRaw(sound.rightChannel.get(i), bitsPerSample);
                BinaryArrayOperations.setBytesLen(arr, (i * 2 + 1) * bytesPerSample, 
                    raw, bytesPerSample);
            }
        }

        length = chunks[0].getDataLength() + 
            chunks[1].getDataLength() + 4;
    }
}