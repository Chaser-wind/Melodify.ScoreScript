package com.github.ShiftAC.Melodify.Util;

public class Sound
{
    public boolean isStereo;
    public int length;
    public int samplesPerSec;

    public SoundChannel leftChannel = new SoundChannel();
    public SoundChannel rightChannel = new SoundChannel();

    protected Sound() {}

    public Sound(Sound a)
    {
        cloneAndResize(a, a.length);
    }

    public Sound(Sound a, int len)
    {
        cloneAndResize(a, len);
    }

    public Sound(boolean isStereo, int samplesPerSec, int len)
    {
        this.samplesPerSec = samplesPerSec;
        this.isStereo = isStereo;
        setSize(len);
    }

    public void changeSPS(int samplesPerSec)
    {

    }

    private void cloneAndResize(Sound a, int len)
    {
        isStereo = a.isStereo;
        samplesPerSec = a.samplesPerSec;
        length = len;
        
        int limit = len < a.length ? len : a.length;

        leftChannel.cloneAndResize(a.leftChannel, len);
        
        if (isStereo)
        {
            rightChannel.cloneAndResize(a.rightChannel, len);
        }
    }

    public void setSize(int len)
    {
        length = len;
        leftChannel.setSize(len);
        if (isStereo)
        {
            rightChannel.setSize(len);
        }
    }

    public void extendChannel(boolean copy)
    {
        isStereo = true;
        if (rightChannel == null)
        {
            rightChannel = new SoundChannel();
        }
        rightChannel.setSize(length);
        if (copy)
        {
            for (int i = 0; i < length; ++i)
            {
                rightChannel.set(i, leftChannel.get(i));
            }
        }
    }

    public static Sound mix(Sound... src)
        throws IllegalArgumentException
    {
        if (src == null)
        {
            throw new IllegalArgumentException("Empty argument array.");
        }

        double[] volume = new double[src.length];
        double val = (double)1 / src.length;
        for (int i = 0; i < src.length; ++i)
        {
            volume[i] = val;
        }
        return mix(src, volume);
    }

    public static Sound mix(Sound[] src, double volume[])
        throws IllegalArgumentException
    {
        if (src == null || volume == null || src.length == 0)
        {
            throw new IllegalArgumentException("Empty argument array.");
        }
        
        if (volume.length != src.length)
        {
            throw new IllegalArgumentException(
                "Argument array length not equal");
        }

        for (int i = 0; i < src.length; ++i)
        {
            if (src[i] == null)
            {
                throw new IllegalArgumentException("Null sound #" + i);
            }
            if (src[i].length == 0)
            {
                throw new IllegalArgumentException("Empty sound #" + i);
            }
        }

        int len = src[0].length;
        int sps = src[0].samplesPerSec;
        for (int i = 1; i < src.length; ++i)
        {
            if (src[i].length < len)
            {
                len = src[i].length;
            }
            if (src[i].samplesPerSec != sps)
            {
                throw new IllegalArgumentException(
                    "Mixing sound with different sampling rate.");
            }
        }

        Sound res = new Sound();
        res.length = len;
        res.isStereo = true;
        res.samplesPerSec = sps;

        SoundChannel[] channels = new SoundChannel[src.length];
        for (int i = 0; i < src.length; ++i)
        {
            channels[i] = src[i].leftChannel;
        }
        SoundChannel.mixChannels(res.leftChannel, channels, volume);
        
        for (int i = 0; i < src.length; ++i)
        {
            channels[i] = 
                src[i].isStereo ? src[i].rightChannel : src[i].leftChannel;
        }
        SoundChannel.mixChannels(res.rightChannel, channels, volume);

        return res;
    }

    public void normalize()
    {
        double maxAmplitude = leftChannel.maxAmplitude();
        if (isStereo)
        {
            double tmp = rightChannel.maxAmplitude();
            maxAmplitude = maxAmplitude > tmp ? maxAmplitude: tmp;
        }
        if (maxAmplitude > 1e-10)
        {
            leftChannel.multiAmplitude(1 / maxAmplitude);
            if (isStereo)
            {
                rightChannel.multiAmplitude(1 / maxAmplitude);
            }
        }
    }

    public void overlay(Sound src, int pos)
        throws IllegalArgumentException
    {
        Sound stereoSrc = src;
        if (!src.isStereo)
        {
            stereoSrc = new Sound(src);
            stereoSrc.extendChannel(true);
        }
        if (!this.isStereo)
        {
            extendChannel(true);
        }

        int len = stereoSrc.leftChannel.size() + pos;
        int tlen = stereoSrc.rightChannel.size() + pos;
        len = tlen > len ? tlen : len;
        tlen = this.leftChannel.size();
        len = tlen > len ? tlen : len;
        tlen = this.rightChannel.size();
        len = tlen > len ? tlen : len;

        this.setSize(len);

        this.leftChannel.overlay(stereoSrc.leftChannel, pos);
        this.rightChannel.overlay(stereoSrc.rightChannel, pos);
    }
}