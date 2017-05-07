package com.github.ShiftAC.Melodify.Util;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONIO
{
    public JSONObject toJSON();

    public void parseJSON(JSONObject obj)
        throws JSONException;
}