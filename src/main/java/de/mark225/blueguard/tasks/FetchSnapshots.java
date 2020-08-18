package de.mark225.blueguard.tasks;

import de.mark225.blueguard.BlueGuardConfig;
import de.mark225.blueguard.Blueguard;
import de.mark225.blueguard.worldguard.RegionSnapshot;
import org.bukkit.scheduler.BukkitRunnable;

import javax.swing.plaf.synth.Region;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FetchSnapshots extends BukkitRunnable {

    private UUID world;
    private List<RegionSnapshot> snapshots;

    public FetchSnapshots(UUID world, List<RegionSnapshot> snapshots){
        this.world = world;
        this.snapshots = snapshots;
    }

    @Override
    public void run() {
        List<RegionSnapshot> fetched = Blueguard.instance.getWorldguardIntegration().getAllRegions(world);
        snapshots.addAll(fetched);
        if(BlueGuardConfig.debug()){
            Logger log = Blueguard.instance.getLogger();
            log.info("Fetched Regions in World " + world.toString() + " (" + fetched.size() + "): " + fetched.stream().map(RegionSnapshot::getId).collect(Collectors.joining(", ")));
        }
    }
}
