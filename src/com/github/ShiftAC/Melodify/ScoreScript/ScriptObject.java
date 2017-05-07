package com.github.ShiftAC.Melodify.ScoreScript;

import com.github.ShiftAC.Melodify.Util.JSONIO;
import com.github.ShiftAC.Melodify.Util.Sound;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ScriptObject implements JSONIO
{
    String name;
    int bpm;
    Object temperament;
    ArrayList<Score> scores;

    public ScriptObject(JSONObject obj)
        throws JSONException
    {
        parseJSON(obj);
    }

    @Override
    public JSONObject toJSON()
    {
        return null;
    }

    @Override
    public void parseJSON(JSONObject obj)
        throws JSONException
    {
        bpm = obj.getInt("bpm");
        name = obj.getString("name");
        temperament = obj.getString("temperament");
        
        JSONArray arr = obj.getJSONArray("scores");
        scores = new ArrayList<Score>();
        for (int i = 0; i < arr.length(); ++i)
        {
            scores.add(new Score(arr.getJSONObject(i)));
        }
    }

    public void requireResource(ResourceManager rm)
        throws IllegalArgumentException
    {
        if (temperament instanceof String)
        {
            String name = (String)temperament;
            temperament = rm.findTemperament(name);
            if (temperament == null)
            {
                throw new IllegalArgumentException(
                    "Temperament \"" + name + "\" not found.");
            }
        }

        for (int i = 0; i < scores.size(); ++i)
        {
            scores.get(i).requireResource(rm);
        }

        introduceTemperament();
        checkSoundSource();
    }

    private void introduceTemperament()
        throws IllegalArgumentException
    {
        for (int i = 0; i < scores.size(); ++i)
        {
            scores.get(i).introduceTemperament((Temperament)temperament);
        }
    }

    public void checkSoundSource()
        throws IllegalArgumentException
    {
        int samplesPerSec = getSamplesPerSecond();
        for (int i = 1; i < scores.size(); ++i)
        {
            if (((SoundSource)scores.get(i).soundSource).getSamplesPerSecond()
                != samplesPerSec)
            {
                throw new IllegalArgumentException("SamplesPerSecond of sound"
                    + " sources not consistent!");
            }
        }
    }

    public int getSamplesPerSecond()
    {
        return ((SoundSource)(scores.get(0).soundSource)).getSamplesPerSecond();
    }

    public int getPos(double posInBeat, int samplesPerSec)
    {
        return (int)(posInBeat / bpm * 60 * samplesPerSec);
    }

    public Sound generateScoreSound(Score score)
    {
        SoundSource src = ((SoundSource)score.soundSource);
        Sound res = new Sound(true, src.getSamplesPerSecond(), 1);
        for (int i = 0; i < score.notes.size(); ++i)
        {
            int pos = getPos(
                score.notes.get(i).position, src.getSamplesPerSecond());
            double len = score.notes.get(i).length;
            double freq = score.notes.get(i).frequency;
            Sound sound;
            if (len == -1)
            {
                sound = src.generateSound(freq);
            } 
            else
            {
                sound = src.generateSound(freq, len / bpm * 60000);
            }
            res.overlay(sound, pos);
        }

        return res;
    }

    public Sound toSound()
    {
        Sound[] scoreSound = new Sound[scores.size()];
        for (int i = 0; i < scores.size(); ++i)
        {
            scoreSound[i] = generateScoreSound(scores.get(i));
        }
        for (int i = 1; i < scores.size(); ++i)
        {
            scoreSound[0].overlay(scoreSound[i], 0);
        }
        scoreSound[0].normalize();
        return scoreSound[0];
    }
}