package com.github.ShiftAC.Melodify.ScoreScript;

import com.github.ShiftAC.Melodify.Util.JSONIO;
import org.json.JSONException;
import org.json.JSONObject;

public class SoundTag implements JSONIO
{
    double startTimeInMS;
    double endTimeInMS;
    double frequency;
    boolean extendable;
    double extStartTimeInMS;
    double extEndTimeInMS;

    public SoundTag(JSONObject obj)
    {
        parseJSON(obj);
    }

    @Override
    public JSONObject toJSON()
    {
        JSONObject res = new JSONObject();

        res.put("startTimeInMS", startTimeInMS);
        res.put("endTimeInMS", endTimeInMS);
        res.put("frequency", frequency);
        res.put("extendable", extendable);
        res.put("extStartTimeInMS", extStartTimeInMS);
        res.put("extEndTimeInMS", extEndTimeInMS);

        return res;
    }

    @Override
    public void parseJSON(JSONObject obj)
        throws JSONException
    {
        startTimeInMS = obj.getDouble("startTimeInMS");
        endTimeInMS = obj.getDouble("endTimeInMS");
        frequency = obj.getDouble("frequency");
        extendable = obj.getBoolean("extendable");
        extStartTimeInMS = obj.getDouble("extStartTimeInMS");
        extEndTimeInMS = obj.getDouble("extEndTimeInMS");
        if (extendable && extEndTimeInMS <= extStartTimeInMS)
        {
            throw new IllegalArgumentException(
                "Start time(" + extStartTimeInMS + 
                "ms) > end time(" + extEndTimeInMS + "ms)");
        }
    }
}