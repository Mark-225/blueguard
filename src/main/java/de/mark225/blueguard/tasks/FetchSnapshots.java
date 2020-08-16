package de.mark225.blueguard.tasks;

import de.mark225.blueguard.Blueguard;
import de.mark225.blueguard.worldguard.RegionSnapshot;
import org.bukkit.scheduler.BukkitRunnable;

import javax.swing.plaf.synth.Region;
import java.util.List;
import java.util.UUID;

public class FetchSnapshots extends BukkitRunnable {

    private UUID world;
    private List<RegionSnapshot> snapshots;

    public FetchSnapshots(UUID world, List<RegionSnapshot> snapshots){
        this.world = world;
        this.snapshots = snapshots;
    }

    @Override
    public void run() {
        snapshots.addAll(Blueguard.instance.getWorldguardIntegration().getAllRegions(world));
    }
}
