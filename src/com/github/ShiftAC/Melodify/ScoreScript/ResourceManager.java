package com.github.ShiftAC.Melodify.ScoreScript;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class ResourceManager
{
    public static final int PRIO_CMDARG = 0;
    public static final int PRIO_USER = 1;
    public static final int PRIO_DEFAULT = 2;
    public static final int PRIO_COUNT = 3;

    ArrayList<HashMap<String, Object>> soundSources = 
        new ArrayList<HashMap<String, Object>>();
    ArrayList<HashMap<String, Temperament>> temperaments = 
        new ArrayList<HashMap<String, Temperament>>();

    public ResourceManager()
    {
        for (int i = 0; i < PRIO_COUNT; ++i)
        {
            soundSources.add(new HashMap<String, Object>());
            temperaments.add(new HashMap<String, Temperament>());
        }
    }

    public void loadSoundSource(String dir, int prio)
        throws JSONException, IOException
    {
        FileInputStream is = new FileInputStream(dir + "/SoundSources.json");
        JSONArray arr = new JSONArray(new JSONTokener(is));
        for (int i = 0; i < arr.length(); ++i)
        {
            JSONObject obj = arr.getJSONObject(i);
            String name = obj.getString("name");
            String path;
            try
            {
                path = obj.getString("path");
            }
            catch (JSONException e)
            {
                path = name.substring(0, name.length());
            }

            if (soundSources.get(prio).get(name) == null)
            {
                soundSources.get(prio).put(name, dir + "/" + path);
            }
        }
        is.close();
    }

    public void loadTemperament(String fileName, int prio)
        throws JSONException, IOException
    {
        FileInputStream is = new FileInputStream(fileName);
        loadTemperament(is, prio);
        is.close();
    }

    public void loadTemperament(InputStream is, int prio)
        throws JSONException, IOException
    {
        JSONArray arr = new JSONArray(new JSONTokener(is));

        for (int i = 0; i < arr.length(); ++i)
        {
            Temperament temp = new Temperament(arr.getJSONObject(i));
            String name = temp.name.substring(0, temp.name.length());

            if (temperaments.get(prio).get(name) == null)
            {
                temperaments.get(prio).put(name, temp);
            }
        }
    }

    private SoundSource findSoundSourceWithPrio(String name, int prio)
        throws JSONException, IOException
    {
        Object obj = soundSources.get(prio).get(name);
        if (obj == null)
        {
            return null;
        }
        
        if (obj instanceof String)
        {
            SoundSource src = new SoundSource((String)obj);
            soundSources.get(prio).put(name, src);
            return src;
        }
        else
        {
            return (SoundSource)obj;
        }
    }

    public SoundSource findSoundSource(String name)
        throws JSONException, IOException
    {
        for (int i = 0; i < PRIO_COUNT; ++i)
        {
            SoundSource src = findSoundSourceWithPrio(name, i);
            if (src != null)
            {
                return src;
            }
        }

        return null;
    }

    public Temperament findTemperament(String name)
    {
        for (int i = 0; i < PRIO_COUNT; ++i)
        {
            Temperament temp = temperaments.get(i).get(name);
            if (temp != null)
            {
                return temp;
            }
        }
        return null;
    }
}