package de.mark225.blueguard.worldguard;

import com.flowpowered.math.vector.Vector2d;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
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
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import de.mark225.blueguard.BlueGuardConfig;
import de.mark225.blueguard.Blueguard;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.StringSubstitutor;
import org.bukkit.Bukkit;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class WorldGuardIntegration {
    public static StateFlag RENDER_FLAG;
    public static IntegerFlag HEIGHT_FLAG;
    public static StringFlag COLOR_FLAG;
    public static StringFlag OUTLINE_FLAG;
    public static StringFlag DISPLAY_FLAG;

    private static final Pattern hexPatternRGBA = Pattern.compile("[0-9a-f]{8}");
    private static final Pattern hexPatternRGB = Pattern.compile("[0-9a-f]{6}");

    /**
     * Initializes the Worldguard integration and registers the custom flags. Can only be called once during {@link org.bukkit.plugin.java.JavaPlugin#onLoad()}
     * @return true if the registration of custom flags was successful
     */
    public boolean init(){
        //register custom flags
        FlagRegistry flags = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag renderFlag = new StateFlag("render-on-bluemap", true);
            IntegerFlag heightFlag = new IntegerFlag("bluemap-render-height");
            StringFlag colorFlag = new StringFlag("bluemap-color", Integer.toHexString(BlueGuardConfig.defaultColor().getRGB()));
            StringFlag outlineFlag = new StringFlag("bluemap-color-outline", Integer.toHexString(BlueGuardConfig.defaultBorderColor().getRGB()).substring(2));
            StringFlag displayFlag = new StringFlag("bluemap-display");
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

    /**
     * Iterates through all Worldguard regions of a given world, filters out regions that can't be rendered or
     * are configured to not be rendered on Bluemap and returns them as seperate {@link RegionSnapshot} objects.
     * @param worldUUID
     * @return The List of region snapshots
     */
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
                return new RegionSnapshot(pr.getId(), BlueGuardConfig.useHtml() ? parseHtmlDisplay(pr) : pr.getFlag(DISPLAY_FLAG), worldUUID, pr.getFlag(HEIGHT_FLAG) != null ? pr.getFlag(HEIGHT_FLAG):BlueGuardConfig.defaultHeight(), points, colorRGBA, colorRGB);
            }).collect(Collectors.toList());
    }

    private String parseHtmlDisplay(ProtectedRegion region){
        String name = region.getFlag(DISPLAY_FLAG) != null ? region.getFlag(DISPLAY_FLAG) : region.getId();
        String owners = region.getOwners().getUniqueIds().stream().limit(10).map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.joining("<br>"));
        String members = region.getMembers().getUniqueIds().stream().limit(10).map(uuid -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.joining("<br>"));
        int size3d = 0;
        int size2d = 0;
        if(region instanceof ProtectedCuboidRegion){
            ProtectedCuboidRegion cuboidRegion = (ProtectedCuboidRegion) region;
            size3d = cuboidRegion.volume();
            BlockVector3 min = cuboidRegion.getMinimumPoint();
            BlockVector3 max = cuboidRegion.getMaximumPoint();
            size2d = (max.getX() - min.getX()) * (max.getZ() - min.getZ());
        }else if(region instanceof ProtectedPolygonalRegion){
            ProtectedPolygonalRegion polyRegion = (ProtectedPolygonalRegion) region;
            size2d = polygonArea(region.getPoints());
            BlockVector3 min = polyRegion.getMinimumPoint();
            BlockVector3 max = polyRegion.getMaximumPoint();
            int height = max.getY() - min.getY();
            size3d = height * size2d;
        }
        HashMap<String, String> values = new HashMap<String, String>();
        values.put("name", name);
        values.put("owners", owners);
        values.put("members", members);
        values.put("size:2d", ""+ size2d);
        values.put("size:3d", ""+ size3d);
        FlagRegistry flagR = WorldGuard.getInstance().getFlagRegistry();
        List<Flag<?>> flags = flagR.getAll();
        for(Flag<?> flag : flags){
            Object obj = region.getFlag(flag);
            if(obj == null){
                obj = flag.getDefault();
            }
            values.put("flag:" + flag.getName(), obj != null ? obj.toString() : "");
        }
        sanitize(values);
        StringSubstitutor sub = new StringSubstitutor(values);
        return sub.replace(BlueGuardConfig.htmlPreset());
    }

    private void sanitize(HashMap<String, String> values){
        for(String key : new ArrayList<String>(values.keySet())){
            values.put(key, StringEscapeUtils.escapeHtml4(values.get(key)));
        }
    }

    private int polygonArea(List<BlockVector2> coordinates){
        int size = coordinates.size();
        int sum = 0;
        for(int i = 0; i < size; i++){
            sum += (coordinates.get(i).getX() * coordinates.get((i + 1) % size).getZ()) - (coordinates.get(i).getZ() * coordinates.get((i + 1) % size).getX());
        }
        return Math.abs((int) ((double) sum / 2d));
    }



}
