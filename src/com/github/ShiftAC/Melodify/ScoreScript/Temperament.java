package com.github.ShiftAC.Melodify.ScoreScript;

import com.github.ShiftAC.Melodify.Util.JSONIO;
import java.util.ArrayList;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import static java.lang.StrictMath.pow;

public class Temperament implements JSONIO
{
    static class Pitch implements JSONIO
    {
        String name;
        int multiInCents;
        
        public Pitch(JSONObject obj)
        {
            parseJSON(obj);
        }

        @Override
        public JSONObject toJSON()
        {
            JSONObject res = new JSONObject();

            res.put("name", name);
            res.put("multiInCents", 0);

            return res;
        }

        @Override
        public void parseJSON(JSONObject obj)
            throws JSONException
        {
            name = obj.getString("name");
            multiInCents = obj.getInt("multiInCents");
        }
    }

    public String name;
    public int refNoteIndex;
    public int refNotePeriod;
    public double refNoteFreq;
    public ArrayList<Pitch> notesInPeriod;
    public int periodMultiInCents;
    public int baseNoteIndex;

    public int getNoteIndex(String name)
        throws IllegalArgumentException
    {
        for (int i = 0; i < notesInPeriod.size(); ++i)
        {
            Pitch pitch = notesInPeriod.get(i);
            if (pitch.name.equals(name))
            {
                return i; 
            }
        }
        throw new IllegalArgumentException("No note named " + name + " in" +
            name + ".");
    }

    public double getFrequency(int period, int index)
    {
        int centDelta = 
            (period - refNotePeriod) * periodMultiInCents +
            notesInPeriod.get(index).multiInCents - 
            notesInPeriod.get(refNoteIndex).multiInCents;
        
        return pow(2, centDelta / 1200.0) * refNoteFreq;

    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject res = new JSONObject();

        res.put("name", name);
        res.put("refNoteName", notesInPeriod.get(refNoteIndex).name);
        res.put("refNotePeriod",refNotePeriod);
        res.put("refFrequency", refNoteFreq);
        JSONArray arr = new JSONArray();
        for (int i = 0; i < notesInPeriod.size(); ++i)
        {
            arr.put(notesInPeriod.get(i).toJSON());
        }
        res.put("notesInPeriod", arr);
        res.put("periodMultiInCents", periodMultiInCents);
        res.put("baseNoteName", notesInPeriod.get(baseNoteIndex).name);

        return res;
    }

    @Override
    public void parseJSON(JSONObject obj)
        throws JSONException
    {
        name = obj.getString("name");
        refNotePeriod = obj.getInt("refNotePeriod");
        refNoteFreq = obj.getDouble("refFrequency");
        periodMultiInCents = obj.getInt("periodMultiInCents");
        
        JSONArray arr = obj.getJSONArray("notesInPeriod");
        notesInPeriod = new ArrayList<Pitch>();
        for (int i = 0; i < arr.length(); ++i)
        {
            notesInPeriod.add(new Pitch(arr.getJSONObject(i)));
        }
        Collections.sort(notesInPeriod, (x, y) -> x.name.compareTo(y.name));

        String baseNoteName = obj.getString("baseNoteName");
        String refNoteName = obj.getString("refNoteName");
        boolean baseHit = false;
        boolean refHit = false;
        for (int i = 0; i < arr.length(); ++i)
        {
            if (notesInPeriod.get(i).name.equals(baseNoteName))
            {
                baseNoteIndex = i;
                baseHit = true;
            }
            if (notesInPeriod.get(i).name.equals(refNoteName))
            {
                refNoteIndex = i;
                refHit = true;
            }
            if (i > 0 && 
                notesInPeriod.get(i).name.equals(notesInPeriod.get(i - 1).name))
            {
                throw new JSONException("Multiple notes with same name");
            }
        }
        if (!baseHit)
        {
            throw new JSONException("Illegal base note name " + baseNoteName);
        }
        if (!refHit)
        {
            throw new JSONException(
                "Illegal reference note name " + refNoteName);
        }
    }

    public Temperament(JSONObject obj)
    {
        parseJSON(obj);
    }
}