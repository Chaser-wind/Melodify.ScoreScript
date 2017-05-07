package com.github.ShiftAC.Melodify.ScoreScript;

import com.github.ShiftAC.Melodify.Util.Sound;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JSONScriptParser
{
    ResourceManager resourceManager;
    ScriptObject obj;
    public JSONScriptParser(ResourceManager resourceManager) 
    {
        this.resourceManager = resourceManager;
    }

    public Sound parse(String fileName)
        throws IOException, JSONException, IllegalArgumentException
    {   
        FileInputStream is = new FileInputStream(fileName);
        Sound res = parse(is);
        is.close();
        return res;
    }

    public Sound parse(InputStream is)
        throws IOException, JSONException, IllegalArgumentException
    {
        obj = new ScriptObject(new JSONObject(new JSONTokener(is)));
        obj.requireResource(resourceManager);
        return obj.toSound();
    }

    public String getName()
    {
        return new String(obj.name);
    }
}