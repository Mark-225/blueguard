package de.mark225.blueguard.tasks;

import de.mark225.blueguard.Blueguard;
import de.mark225.blueguard.bluemap.BlueMapIntegration;
import de.mark225.blueguard.worldguard.RegionSnapshot;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


public class ResyncTask extends BukkitRunnable {

    private List<RegionSnapshot> synced = new ArrayList<RegionSnapshot>();

    @Override
    public void run() {
        List<UUID> worlds = Blueguard.instance.getBluemapIntegration().loadedWorldUUIDs();
        BukkitRunnable task = null;
        List<RegionSnapshot> snapshots = new ArrayList<RegionSnapshot>();
        for(int i = 0; i < worlds.size(); i++){
            UUID world = worlds.get(i);
            new FetchSnapshots(world, snapshots).runTaskLater(Blueguard.instance, i);
        }
        new BukkitRunnable(){

            @Override
            public void run() {
                if(snapshots.size() > 0){
                    List<RegionSnapshot> current = new ArrayList<RegionSnapshot>(synced);
                    List<RegionSnapshot> snapshotsCopy = new ArrayList<RegionSnapshot>(snapshots);
                    List<RegionSnapshot> unchanged = current.stream().filter(rs -> snapshots.contains(rs)).collect(Collectors.toList());
                    List<RegionSnapshot> addedOrChanged = snapshotsCopy.stream().filter(rs -> !unchanged.contains(rs)).collect(Collectors.toList());
                    List<RegionSnapshot> keep = new ArrayList<RegionSnapshot>(unchanged);
                    keep.addAll(addedOrChanged);
                    List<String> idsToKeep = keep.stream().map(RegionSnapshot::getId).collect(Collectors.toList());
                    List<RegionSnapshot> removed = current.stream().filter(rs -> !idsToKeep.contains(rs.getId())).collect(Collectors.toList());
                    BlueMapIntegration bluemap = Blueguard.instance.getBluemapIntegration();
                    synced.clear();
                    synced.addAll(unchanged);
                    synced.addAll(addedOrChanged);
                    if(addedOrChanged.size() > 0 || removed.size() > 0){
                        new BukkitRunnable(){

                            @Override
                            public void run() {
                                bluemap.updateRegions(addedOrChanged);
                                bluemap.deleteRegions(removed);
                                bluemap.save();
                            }
                        }.runTaskAsynchronously(Blueguard.instance);
                    }
                }
            }
        }.runTaskLater(Blueguard.instance, snapshots.size());
    }
}
