package com.github.ShiftAC.Melodify.ScoreScript;

import com.github.ShiftAC.Melodify.Util.CommandArgumentParser;
import com.github.ShiftAC.Melodify.Util.Sound;
import com.github.ShiftAC.Melodify.Util.RIFF.WAVFile;
import com.github.ShiftAC.Melodify.Util.RIFF.WAVfmt;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.json.JSONException;

public class Main
{
    static String helpMsg = "";
    static ResourceManager rm = new ResourceManager();

    public static void loadSoundSource(CommandArgumentParser parser)
        throws IOException, JSONException
    {
        String param = "";
        String tmp;
        if ((tmp = parser.getShortArg('s')) != null)
        {
            param += tmp; 
        }
        if ((tmp = parser.getLongArg("sound")) != null)
        {
            param += ";";
            param += tmp;
        }

        String[] src = param.split(";");
        for (int i = 0; i < src.length; ++i)
        {
            if (src[i] != null && src[i].length() != 0)
            {
                rm.loadSoundSource(src[i], ResourceManager.PRIO_CMDARG);
            }
        }

        String scoreDir = parser.getStandAlone().get(0);
        if (scoreDir.indexOf("/") == -1)
        {
            scoreDir = "./" + scoreDir;
        }
        scoreDir = scoreDir.substring(0, scoreDir.indexOf("/"));
        try
        {
            rm.loadSoundSource(scoreDir, ResourceManager.PRIO_USER);
        }
        catch (IOException e) {}

        String defPos = "data/sounds";
        try
        {
            rm.loadSoundSource(defPos, ResourceManager.PRIO_DEFAULT);
        }
        catch (IOException e) {}
    }

    public static void loadTemperament(CommandArgumentParser parser)
        throws IOException
    {
        String param = "";
        String tmp;
        if ((tmp = parser.getShortArg('t')) != null)
        {
            param += tmp; 
        }
        if ((tmp = parser.getLongArg("temperament")) != null)
        {
            param += ";";
            param += tmp;
        }

        String[] src = param.split(";");
        for (int i = 0; i < src.length; ++i)
        {
            if (src[i] != null && src[i].length() != 0)
            {
                rm.loadTemperament(src[i], ResourceManager.PRIO_CMDARG);
            }
        }

        String scoreDir = parser.getStandAlone().get(0);
        if (scoreDir.indexOf("/") == -1)
        {
            scoreDir = "./" + scoreDir;
        }
        scoreDir = scoreDir.substring(0, scoreDir.indexOf("/"));
        try
        {
            rm.loadTemperament(scoreDir + "/Temperament.json", 
                ResourceManager.PRIO_USER);
        }
        catch (IOException e) {}

        String defPos = "data/Temperament.json";
        try
        {
            rm.loadTemperament(defPos, ResourceManager.PRIO_DEFAULT);
        }
        catch (IOException e) {}
    }

    public static void loadResources(CommandArgumentParser parser)
        throws IOException
    {
        loadSoundSource(parser);
        loadTemperament(parser);
    }

    public static String getOutName(CommandArgumentParser parser,
        JSONScriptParser jsonParser)
    {
        String res;
        if ((res = parser.getShortArg('o')) != null)
        {
            return res;
        }
        return jsonParser.getName();
    }

    public static void main(String[] args)
    {
        try
        {
            CommandArgumentParser parser = new CommandArgumentParser(args);
            
            if (parser.getStandAlone().size() == 0)
            {
                System.err.println(helpMsg);
                return;
            }

            loadResources(parser);

            //TextScriptParser textParser = new TextScriptParser(); 
            JSONScriptParser jsonParser = new JSONScriptParser(rm);
            Sound res = jsonParser.parse(parser.getStandAlone().get(0));

            OutputStream os = new FileOutputStream(
                getOutName(parser, jsonParser) + ".wav");

            WAVFile file = new WAVFile();
            file.parseSound(res, WAVfmt.PCM16);
            file.write(os);
            os.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}