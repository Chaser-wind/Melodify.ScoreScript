package com.github.ShiftAC.Melodify.ScoreScript;

import com.github.ShiftAC.Melodify.Util.Sound;
import static java.lang.StrictMath.log;
import static java.lang.StrictMath.pow;

public class Note extends Sound
{
    public int extStartPos;
    public int extEndPos;
    public boolean extendable;

    public double frequency;

    public Note(int samplesPerSec, int len, double frequency)
    {
        super(true, samplesPerSec, len);
        this.extStartPos = 0;
        this.extEndPos = 0;
        this.extendable = false;
        this.frequency = frequency;
    }

    public Note(int samplesPerSec, int len, double frequency, 
        double extStartTime, double extEndTime)
        throws IllegalArgumentException
    {
        if (extStartTime > extEndTime)
        {
            throw new IllegalArgumentException("Start time(" + extStartTime + 
                "ms) > end time(" + extEndTime + "ms)");
        }
        this.samplesPerSec = samplesPerSec;
        this.extStartPos = (int)(extStartTime / 1000 * samplesPerSec);
        this.extEndPos = (int)(extEndTime / 1000 * samplesPerSec);
        this.extendable = true;
        this.isStereo = true;
        this.frequency = frequency;
        setSize(len);
    }

    public Note(Note a)
    {
        super(a);
        this.extStartPos = a.extStartPos;
        this.extEndPos = a.extEndPos;
        this.extendable = a.extendable;
        this.frequency = a.frequency;
    }

    public Note(Sound a, double frequency)
    {
        super(a);
        this.extStartPos = 0;
        this.extEndPos = 0;
        this.extendable = false;
        this.frequency = frequency;
    }

    public Note(Sound a, double frequency, 
        double extStartTime, double extEndTime)
        throws IllegalArgumentException
    {
        super(a);
        if (extStartTime > extEndTime)
        {
            throw new IllegalArgumentException("Start time(" + extStartTime + 
                "ms) > end time(" + extEndTime + "ms)");
        }
        this.extStartPos = (int)(extStartTime / 1000 * samplesPerSec);
        this.extEndPos = (int)(extEndTime / 1000 * samplesPerSec);
        this.extendable = true;
        this.frequency = frequency;
    }

    protected static final double centMulti = pow(2, 1.0 / 1200);
    protected static final double logCentMulti = log(centMulti);
    protected double freqToCent(double newFreq)
    {
        double multi = newFreq / frequency;
        return log(multi) / logCentMulti;
    }
    protected double centToFreq(int cent)
    {
        return pow(centMulti, cent) * frequency;
    }

    protected double getLeftVolumeByPos(double pos)
    {
        int i = (int)pos;
        double interval = pos - i;

        return 
            leftChannel.getSaturation(i) * (1 - interval) + 
            leftChannel.getSaturation(i + 1) * interval;
    }
    protected double getRightVolumeByPos(double pos)
    {
        int i = (int)pos;
        double interval = pos - i;

        return 
            rightChannel.getSaturation(i) * (1 - interval) + 
            rightChannel.getSaturation(i + 1) * interval;
    }

    public Note changeFrequency(int cent)
    {
        return changeFrequency(centToFreq(cent));
    }

    public Note changeFrequency(double newFreq)
        throws IllegalArgumentException
    {
        if (newFreq <= 0)
        {
            throw new IllegalArgumentException("Non-positive frequency.");
        }

        double multi = newFreq / frequency;
        int newLen = (int)(length / multi);

        Note res = new Note(this.samplesPerSec, newLen, newFreq,
            this.extStartPos, this.extEndPos);
        for (int i = 0; i < newLen; ++i)
        {
            res.leftChannel.set(i, getLeftVolumeByPos(i * multi));
            res.rightChannel.set(i, getRightVolumeByPos(i * multi));
        }

        return res;
    }

    protected double getExtLeftVolumeByPos(double pos, int newLen)
    {
        if (pos > newLen - (length - extEndPos))
        {
            return getLeftVolumeByPos(pos - newLen + length);
        }
        else if (pos < extStartPos)
        {
            return getLeftVolumeByPos(pos);
        }

        double pdelta = pos - extStartPos;
        int rec = (int)(pdelta / (extEndPos - extStartPos));

        return getLeftVolumeByPos(pos - rec * (extEndPos - extStartPos));
    }
    protected double getExtRightVolumeByPos(
        double pos, int newLen)
    {
        if (pos > newLen - (length - extEndPos))
        {
            return getRightVolumeByPos(pos - newLen + length);
        }
        else if (pos < extStartPos)
        {
            return getRightVolumeByPos(pos);
        }

        double pdelta = pos - extStartPos;
        int rec = (int)(pdelta / (extEndPos - extStartPos));

        return getRightVolumeByPos(pos - rec * (extEndPos - extStartPos));
    }

    public Note changeFrequency(int cent, double newLenInMS)
    {
        return changeFrequency(centToFreq(cent), newLenInMS);
    }

    public Note changeFrequency(double newFreq, double newLenInMS)
        throws IllegalArgumentException
    {
        if (!extendable)
        {
            return changeFrequency(newFreq);
        }

        if (newFreq <= 0)
        {
            throw new IllegalArgumentException("Non-positive frequency.");
        }
        double lenInMS = length / samplesPerSec;
        double minTime = lenInMS - (extEndPos - extStartPos);
        if (newLenInMS < minTime)
        {
            throw new IllegalArgumentException("Illegal length " + newLenInMS +
                "ms, expected at least " + minTime + "ms.");
        }

        double multi = newFreq / frequency;
        int newLen = (int)(newLenInMS / 1000 * samplesPerSec);

        Note res = new Note(this.samplesPerSec, newLen, newFreq,
            this.extStartPos, this.extEndPos);
        for (int i = 0; i < newLen; ++i)
        {
            res.leftChannel.set(i, getExtLeftVolumeByPos(i * multi, newLen));
            res.rightChannel.set(i, getExtRightVolumeByPos(i * multi, newLen));
        }

        return res;
    }
}