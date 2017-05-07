package com.github.ShiftAC.Melodify.ScoreScript;

import com.github.ShiftAC.Melodify.Util.JSONIO;
import com.github.ShiftAC.Melodify.Util.Sound;
import com.github.ShiftAC.Melodify.Util.RIFF.WAVFile;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.FileOutputStream;

public class SoundSource
{
    public ArrayList<Note> notes = new ArrayList<Note>();

    public SoundSource(String dir)
        throws IOException, JSONException
    {
        readDir(dir);
    }

    public void readDir(String name)
        throws IOException, JSONException
    {
        WAVFile wavFile = new WAVFile(name + "/sound.wav");
        FileInputStream is = new FileInputStream(name + "/Tags.json");
        JSONArray tags = new JSONArray(new JSONTokener(is));
        
        for (int i = 0; i < tags.length(); ++i)
        {
            SoundTag tag = new SoundTag(tags.getJSONObject(i));
            notes.add(buildNote(wavFile, tag));
        }

        Collections.sort(notes, (x, y) -> (x.frequency < y.frequency ? -1 : 1));
        is.close();
    }

    protected Note buildNote(WAVFile wavFile, SoundTag tag)
        throws IllegalArgumentException
    {
        Sound sound = wavFile.toSound(tag.startTimeInMS, tag.endTimeInMS);
        Note res;
        if (!tag.extendable)
        {
            res = new Note(sound, tag.frequency);
        }
        else
        {
            res = new Note(sound, tag.frequency, 
                tag.extStartTimeInMS, tag.extEndTimeInMS);
        }

        return res;
    }

    public int getSamplesPerSecond()
    {
        return notes.get(0).samplesPerSec;
    }

    public Note fitFrequency(double freq)
    {
        int st = 0;
        int ed = notes.size();
        int pos;
        while (st + 1 < ed)
        {
            pos = (st + ed) / 2;
            if (notes.get(pos).frequency > freq)
            {
                ed = pos;
            }
            else
            {
                st = pos;
            }
        }
        if (ed == notes.size())
        {
            ed--;
        }

        double stDiff = freq - notes.get(st).frequency;
        double edDiff = notes.get(ed).frequency - freq;

        return stDiff < edDiff ? notes.get(st) : notes.get(ed);
    }

    public Sound generateSound(double freq, double lenInMS)
    {
         return fitFrequency(freq).changeFrequency(freq, lenInMS);
    }

    public Sound generateSound(double freq)
    {
        return fitFrequency(freq).changeFrequency(freq);
    }
}