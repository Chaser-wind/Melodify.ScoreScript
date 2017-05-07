package com.github.ShiftAC.Melodify.Util;

public class SoundChannel
{
    private static final int DEFAULT_SIZE = 1;

    private double[] arr;

    public SoundChannel(int size)
    {
        arr = new double[size];
    }

    public SoundChannel()
    {
        arr = new double[DEFAULT_SIZE];
    }

    public int size()
    {
        return arr.length;
    }

    private boolean isInvalidIndex(int index)
    {
        return index < 0 || index >= arr.length;
    }

    public void set(int index, double e)
        throws IllegalArgumentException
    {
        if (isInvalidIndex(index))
        {
            throw new IllegalArgumentException("Invalid index " + index +
                ", expected [" + 0 + ", " + arr.length + ").");
        }
        arr[index] = e;
    }

    public double get(int index)
        throws IllegalArgumentException
    {
        if (isInvalidIndex(index))
        {
            throw new IllegalArgumentException("Invalid index " + index +
                ", expected [" + 0 + ", " + arr.length + ").");
        }
        return arr[index];
    }

    public double getSaturation(int index)
    {
        index = index < 0 ? 0 : (index < arr.length ? index : arr.length - 1);
        return arr[index];
    }

    public void setSize(int size)
        throws IllegalArgumentException
    {
        if (size <= 0)
        {
            throw new IllegalArgumentException("Invalid size " + size);
        }
        double[] old = arr;
        arr = new double[size];
        int len = size < old.length ? size : old.length;
        for (int i = 0; i < len; ++i)
        {
            arr[i] = old[i];
        }
    }

    void cloneAndResize(SoundChannel channel, int len)
    {
        this.setSize(len);
        for (int i = 0; i < len; ++i)
        {
            this.arr[i] = channel.arr[i];
        }
    }

    public static void mixChannels(
        SoundChannel dest, SoundChannel[] src, double[] volume)
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

        int len = (1 << 31) - 1;
        for (int i = 0; i < src.length; ++i)
        {
            if (src[i] == null)
            {
                throw new IllegalArgumentException("Null channel #" + i);
            }
            if (src[i].size() == 0)
            {
                throw new IllegalArgumentException("Empty channel #" + i);
            }
            if (src[i].size() < len)
            {
                len = src[i].size();
            }
        }

        dest.setSize(len);
        for (int i = 0; i < len; ++i)
        {
            double x = 0;
            for (int j = 0; j < src.length; ++j)
            {
                x += src[j].get(i) * volume[j];
            }
            dest.set(i, x);
        }
    }

    double maxAmplitude()
    {
        double amp = arr[0];
        for (int i = 0; i < arr.length; ++i)
        {
            if (arr[i] > amp)
            {
                amp = arr[i];
            }
        }
        return amp;
    }

    void multiAmplitude(double multi)
    {
        for (int i = 0; i < arr.length; ++i)
        {
            arr[i] *= multi;
        }
    }

    void overlay(SoundChannel src, int pos)
        throws IllegalArgumentException
    {
        if (pos < 0)
        {
            throw new IllegalArgumentException("negative position " + pos);
        }
        if (src.arr.length + pos > arr.length)
        {
            setSize(src.arr.length + pos);;
        }

        for (int i = 0; i < src.arr.length; ++i)
        {
            arr[i + pos] += src.arr[i];
        }
    }
}