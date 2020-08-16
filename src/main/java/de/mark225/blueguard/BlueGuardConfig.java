package de.mark225.blueguard;

import org.bukkit.configuration.file.FileConfiguration;

import java.awt.*;
import java.util.regex.Pattern;

public class BlueGuardConfig {

    private static FileConfiguration config;

    public static void setConfig(FileConfiguration config){
        BlueGuardConfig.config = config;
    }

    public static boolean renderDefault(){
        return config.getBoolean("render-flag-default", true);
    }

    public static int defaultHeight(){
        return config.getInt("render-height", 63);
    }

    private static Pattern rgbpattern = Pattern.compile("[0-9a-f]{8}");

    public static Color defaultColor(){
        String string = config.getString("region-color", "960087ff");
        int a,r,g,b;
        if(rgbpattern.matcher(string).matches()){
            a = Integer.parseInt(string.substring(0,2), 16);
            r = Integer.parseInt(string.substring(2,4), 16);
            g = Integer.parseInt(string.substring(4,6), 16);
            b = Integer.parseInt(string.substring(6,8), 16);
        }else{
            a = 0x96;
            r = 0x00;
            g = 0x87;
            b = 0xff;
        }
        int rgba = (((((a << 8) + r) << 8) + g) << 8) + b;
        return new java.awt.Color(rgba, true);
    }

    public static Color defaultBorderColor(){
        int rgba = Integer.parseInt(config.getString("outline-color", "0060ff"),16);
        return new java.awt.Color(rgba, false);
    }

    public static String markerSetName(){
        return config.getString("markerset-name", "WorldGuard Regions");
    }

    public static int syncInterval(){
        return config.getInt("update-interval", 200);
    }


}
