package de.mark225.blueguard.worldguard;

import com.flowpowered.math.vector.Vector2d;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.internal.platform.WorldGuardPlatform;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.mark225.blueguard.BlueGuardConfig;
import de.mark225.blueguard.Blueguard;
import org.bukkit.Bukkit;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WorldGuardIntegration {
    public static StateFlag RENDER_FLAG;
    public static IntegerFlag HEIGHT_FLAG;
    public static StringFlag COLOR_FLAG;
    public static StringFlag OUTLINE_FLAG;
    public static StringFlag DISPLAY_FLAG;

    private static Pattern hexPatternRGBA = Pattern.compile("[0-9a-f]{8}");
    private static Pattern hexPatternRGB = Pattern.compile("[0-9a-f]{6}");


    public boolean init(){
        //register custom flags
        FlagRegistry flags = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag renderFlag = new StateFlag("render-on-bluemap", true);
            IntegerFlag heightFlag = new IntegerFlag("bluemap-render-height");
            StringFlag colorFlag = new StringFlag("bluemap-color", "0087ffff");
            StringFlag outlineFlag = new StringFlag("bluemap-color-outline", "0060ff");
            StringFlag displayFlag = new StringFlag("bluemap-display", "");
            flags.registerAll(Arrays.asList(new Flag[]{renderFlag, heightFlag, colorFlag, outlineFlag, displayFlag}));
            RENDER_FLAG = renderFlag;
            HEIGHT_FLAG = heightFlag;
            COLOR_FLAG = colorFlag;
            OUTLINE_FLAG = outlineFlag;
            DISPLAY_FLAG = displayFlag;
            return true;
        }catch(FlagConflictException e){
            Blueguard.instance.getLogger().severe("Custom Worldguard flags are conflicting with another plugin!");
        }
        return false;
    }

    public List<RegionSnapshot> getAllRegions(UUID worldUUID){
        org.bukkit.World bukkitWorld = Bukkit.getWorld(worldUUID);
        if(bukkitWorld == null){
            Blueguard.instance.getLogger().warning("World " + worldUUID.toString() + " not found! Please check your Bluemap config for invalid worlds!");
            return new ArrayList<RegionSnapshot>();
        }
        World w = BukkitAdapter.adapt(bukkitWorld);
        RegionContainer regions = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager rm = regions.get(w);
        return rm.getRegions().values().stream().filter(pr -> {
                //filter all regions that shall not be rendered
                StateFlag.State state = pr.getFlag(RENDER_FLAG);
                return pr.getPoints().size() >= 3 && (((state == null) && BlueGuardConfig.renderDefault()) || (state == StateFlag.State.ALLOW));
            }).map(pr ->{
                //Convert polygon points to Verctor2d List
                List<Vector2d> points = pr.getPoints().stream().map(vector -> new Vector2d(vector.getX(),vector.getZ())).collect(Collectors.toList());
                //Convert color flags to Color Objects, if applicable
                String color = pr.getFlag(COLOR_FLAG);
                Color colorRGBA = null;
                if(color != null && hexPatternRGBA.matcher(color).matches()){
                    int a,r,g,b;
                    a = Integer.parseInt(color.substring(0,2), 16);
                    r = Integer.parseInt(color.substring(2,4), 16);
                    g = Integer.parseInt(color.substring(4,6), 16);
                    b = Integer.parseInt(color.substring(6,8), 16);
                    int rgba = (((((a << 8) + r) << 8) + g) << 8) + b;
                    colorRGBA = new Color(rgba, true);
                }else{
                    colorRGBA = BlueGuardConfig.defaultColor();
                }
                String bordercolor = pr.getFlag(OUTLINE_FLAG);
                Color colorRGB = null;
                if(bordercolor != null && hexPatternRGB.matcher(bordercolor).matches()){
                    colorRGB = new Color(Integer.parseInt(bordercolor, 16), false);
                }else{
                    colorRGB = BlueGuardConfig.defaultBorderColor();
                }
                //create and return new RegionSnapshot
                return new RegionSnapshot(pr.getId(), pr.getFlag(DISPLAY_FLAG), worldUUID, pr.getFlag(HEIGHT_FLAG) != null ? pr.getFlag(HEIGHT_FLAG):BlueGuardConfig.defaultHeight(), points, colorRGBA, colorRGB);
            }).collect(Collectors.toList());
    }

}
