package com.github.ShiftAC.Melodify.Util;

import java.util.ArrayList;
import java.util.HashMap;

public class CommandArgumentParser
{
    ArrayList<String> standAlone = new ArrayList<String>();
    HashMap<Character, String> shortArg = new HashMap<Character, String>();
    HashMap<String, String> longArg = new HashMap<String, String>();

    public String DULL_PARAM = new String("-");

    protected static final int ARG_DULL = -1;
    protected static final int ARG_ALONE = 0;
    protected static final int ARG_SHORT = 1;
    protected static final int ARG_LONG = 2;

    public CommandArgumentParser(String[] args) 
    {
        parse(args);
    }

    public int argumentType(String arg)
    {
        if (arg.indexOf("--") == 0)
        {
            return arg.length() > 2 ? ARG_LONG : ARG_DULL;
        }
        else if (arg.indexOf('-') == 0)
        {
            return arg.length() == 2 ? ARG_SHORT : ARG_DULL;
        }

        return ARG_ALONE;
    }

    public void parse(String[] args)
    {
        for (int i = 0; i < args.length; ++i)
        {
            String param = DULL_PARAM;
            switch (argumentType(args[i]))
            {
            case ARG_LONG:
                if (i + 1 < args.length && 
                    argumentType(args[i + 1]) == ARG_ALONE)
                {
                    param = args[++i];
                }
                longArg.put(args[i].substring(2), param);
                break;
            case ARG_SHORT:
                if (i + 1 < args.length && 
                    argumentType(args[i + 1]) == ARG_ALONE)
                {
                    param = args[++i];
                }
                shortArg.put(new Character(args[i].charAt(1)), param);
                break;
            case ARG_ALONE:
                standAlone.add(args[i]);
                break;
            case ARG_DULL:
                break;
            }
        }
    }

    public String getShortArg(char c)
    {
        String param = shortArg.get(new Character(c));
        return param == DULL_PARAM ? null : param;
    }

    public String getLongArg(String str)
    {
        String param = longArg.get(str);
        return param == DULL_PARAM ? null : param;
    }

    public ArrayList<String> getStandAlone()
    {
        return standAlone;
    }
}