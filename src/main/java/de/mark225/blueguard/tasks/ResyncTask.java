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
        List<RegionSnapshot> snapshots = new ArrayList<RegionSnapshot>();

        //Create one delayed task per world to fetch all Regions on the server
        for(int i = 0; i < worlds.size(); i++){
            UUID world = worlds.get(i);
            new FetchSnapshots(world, snapshots).runTaskLater(Blueguard.instance, i);
        }

        //Handle the result in a delayed task one tick after the fetch tasks are completed
        new BukkitRunnable(){
            @Override
            public void run() {
                if(snapshots.size() > 0){

                    /*
                    Categorize Worldguard regions:
                        unchanged: Nothing will happen to these regions
                        addedOrChanged: Will be sent to Bluemap for a marker update
                        removed: Will be sent to Bluemap for marker deletion
                     */
                    List<RegionSnapshot> unchanged = synced.stream().filter(rs -> snapshots.contains(rs)).collect(Collectors.toList());
                    List<RegionSnapshot> addedOrChanged = snapshots.stream().filter(rs -> !unchanged.contains(rs)).collect(Collectors.toList());
                    List<RegionSnapshot> keep = new ArrayList<RegionSnapshot>();
                    keep.addAll(unchanged);
                    keep.addAll(addedOrChanged);
                    List<String> idsToKeep = keep.stream().map(RegionSnapshot::getId).collect(Collectors.toList());
                    List<RegionSnapshot> removed = synced.stream().filter(rs -> !idsToKeep.contains(rs.getId())).collect(Collectors.toList());
                    BlueMapIntegration bluemap = Blueguard.instance.getBluemapIntegration();

                    //Set regions in synced list to the ones that currently exist
                    synced.clear();
                    synced.addAll(unchanged);
                    synced.addAll(addedOrChanged);

                    //If any regions were changed or removed, start an async task to edit the markers on Bluemap
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
        }.runTaskLater(Blueguard.instance, worlds.size());
    }
}
