package com.github.ShiftAC.Melodify.ScoreScript;

import com.github.ShiftAC.Melodify.Util.JSONIO;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Score implements JSONIO
{
    static class ScoreNote implements JSONIO
    {
        String name;
        int index;
        int period;
        double position;
        double length;
        double frequency;

        public ScoreNote(JSONObject obj)
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
            name = obj.getString("name");
            period = obj.getInt("period");
            position = obj.getDouble("position");
            try
            {
                length = obj.getDouble("length");
            }
            catch (JSONException e)
            {
                length = -1;
            }
        }

        public void introduceTemperament(Temperament temp)
            throws IllegalArgumentException
        {
            index = temp.getNoteIndex(name);
            frequency = temp.getFrequency(period, index);
        }
    }

    Object soundSource;
    ArrayList<ScoreNote> notes;

    public Score(JSONObject obj)
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
        soundSource = obj.getString("soundSource");
        JSONArray arr = obj.getJSONArray("notes");
        
        notes = new ArrayList<ScoreNote>();
        for (int i = 0; i < arr.length(); ++i)
        {
            notes.add(new ScoreNote(arr.getJSONObject(i)));
        }
    }

    public void requireResource(ResourceManager rm)
        throws IllegalArgumentException
    {
        if (soundSource instanceof String)
        {
            String name = (String)soundSource;
            try
            {
                soundSource = rm.findSoundSource(name);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                throw new IllegalArgumentException(
                    "Can't get soundSource \"" + name + "\" due to " + 
                    e.getClass().getName() + "(" + e.getMessage() + ").");
            }
            if (soundSource == null)
            {
                throw new IllegalArgumentException(
                    "SoundSource \"" + name + "\" not found.");
            }
        }
    }

    public void introduceTemperament(Temperament temp)
        throws IllegalArgumentException
    {
        for (int i = 0; i < notes.size(); ++i)
        {
            notes.get(i).introduceTemperament(temp);
        }
    }
}