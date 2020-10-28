package de.mark225.blueguard.worldguard;

import com.flowpowered.math.vector.Vector2d;

import java.awt.*;
import java.util.List;
import java.util.UUID;

/**
 * Class that stores all needed Information about a Worldguard region, so it can be accessed asynchronously
 */
public class RegionSnapshot {
    private String id;
    private String htmlDisplay;
    private UUID world;
    private int height;
    private boolean depthCheck;
    private List<Vector2d> points;
    private Color color;
    private Color borderColor;

    public RegionSnapshot(String id, String htmlDisplay, UUID world, int height, boolean depthCheck, List<Vector2d> points, Color color, Color borderColor) {
        this.id = id;
        this.htmlDisplay = htmlDisplay;
        this.world = world;
        this.height = height;
        this.depthCheck = depthCheck;
        this.points = points;
        this.color = color;
        this.borderColor = borderColor;
    }

    public String getId() {
        return id;
    }

    public int getHeight() {
        return height;
    }

    public boolean getDepthCheck() {
        return depthCheck;
    }

    public String getName() {
        return htmlDisplay;
    }

    public UUID getWorld() {
        return world;
    }

    public List<Vector2d> getPoints() {
        return points;
    }

    public Color getColor() {
        return color;
    }

    public Color getBorderColor() {
        return borderColor;
    }

    @Override
    public boolean equals(Object other){
        if(other == this)
            return true;
        if(other == null)
            return false;
        if(!(other instanceof RegionSnapshot))
            return false;
        RegionSnapshot snap = (RegionSnapshot) other;
        return snap.getId().equals(this.getId()) &&
                snap.getBorderColor().equals(this.getBorderColor()) &&
                snap.getColor().equals(this.getColor()) &&
                snap.getWorld().equals(this.getWorld()) &&
                (snap.getName() != null ? snap.getName().equals(this.getName()) : this.getName() == null) &&
                snap.getHeight() == this.getHeight() &&
                snap.getDepthCheck() == this.getDepthCheck() &&
                snap.getPoints().equals(this.getPoints());
    }

}
