package de.mark225.blueguard.bluemap;

import com.flowpowered.math.vector.Vector2d;
import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapAPIListener;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.marker.*;
import de.mark225.blueguard.BlueGuardConfig;
import de.mark225.blueguard.Blueguard;
import de.mark225.blueguard.worldguard.RegionSnapshot;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BlueMapIntegration implements BlueMapAPIListener {

    private BlueMapAPI blueMapAPI = null;
    private MarkerAPI markers = null;
    private MarkerSet markerSet;

    @Override
    public void onEnable(BlueMapAPI blueMapApi) {
        this.blueMapAPI = blueMapApi;
        try {
            this.markers = blueMapApi.getMarkerAPI();
            markerSet = markers.createMarkerSet("blueguard-markers");
            markerSet.setLabel(BlueGuardConfig.markerSetName());
            clearMarkers();
            Blueguard.instance.startTask();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable(BlueMapAPI blueMapApi) {
        blueMapAPI = null;
        if(Blueguard.instance.isEnabled()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Blueguard.instance.stopTask();
                }
            }.runTask(Blueguard.instance);
        }
    }

    private void clearMarkers(){
        List<Marker> markers = new ArrayList<Marker>(markerSet.getMarkers());
        markers.forEach(m ->{
            markerSet.removeMarker(m);
        });
    }

    public Optional<BlueMapAPI> getBlueMapAPI(){
        return Optional.ofNullable(blueMapAPI);
    }

    public List<UUID> loadedWorldUUIDs(){
        return blueMapAPI.getWorlds().stream().map(BlueMapWorld::getUuid).collect(Collectors.toList());
    }

    public void deleteRegions(List<RegionSnapshot> snapshots){
        if(markers == null)
            return;
        snapshots.forEach(s -> {
            Optional<BlueMapWorld> oWorld = blueMapAPI.getWorld(s.getWorld());
            if(oWorld.isPresent()){
                BlueMapWorld world = oWorld.get();
                world.getMaps().forEach(map ->{
                    markerSet.removeMarker("#blueguard:" + map.getId() + "_" + s.getId());
                });
            }
        });
    }

    public void updateRegions(List<RegionSnapshot> snapshots){
        if(markers == null)
            return;
        snapshots.forEach(s ->{
            Optional<BlueMapWorld> oWorld = blueMapAPI.getWorld(s.getWorld());
            if(oWorld.isPresent()){
                BlueMapWorld world = oWorld.get();
                world.getMaps().forEach(map ->{
                    Optional<Marker> oMarker = markerSet.getMarker("#blueguard:" + map.getId() + "_" + s.getId());
                    Shape shape = new Shape(s.getPoints().toArray(new Vector2d[0]));
                    Vector3d pos = new Vector3d(shape.getMin().getX() + ((shape.getMax().getX() - shape.getMin().getX())/2), s.getHeight(), shape.getMin().getY() + ((shape.getMax().getY() - shape.getMin().getY())/2));
                    String label = s.getName() == null || s.getName().equals("") ? s.getId() : s.getName();
                    ShapeMarker sm;
                    if(oMarker.isPresent()){
                        sm = (ShapeMarker) oMarker.get();
                        sm.setShape(shape, s.getHeight());
                        sm.setPosition(pos);
                    }else{
                        sm = markerSet.createShapeMarker("#blueguard:" + map.getId() + "_" + s.getId(), map, pos, shape, s.getHeight());
                    }
                    sm.setLabel(label);
                    sm.setColors(s.getBorderColor(), s.getColor());
                    sm.setDepthTestEnabled(false);
                });
            }
        });
    }

    public void save(){
        try {
            markers.save();
        }catch(IOException e){
            Blueguard.instance.getLogger().warning("Markers could not be saved!");
            e.printStackTrace();
        }
    }



}
