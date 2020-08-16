package de.mark225.blueguard.worldguard;

import com.flowpowered.math.vector.Vector2d;

import java.awt.*;
import java.util.List;
import java.util.UUID;

public class RegionSnapshot {
    private String id;
    private String name;
    private UUID world;
    private int height;
    private List<Vector2d> points;
    private Color color;
    private Color borderColor;

    public RegionSnapshot(String id, String name, UUID world, int height, List<Vector2d> points, Color color, Color borderColor) {
        this.id = id;
        this.name = name;
        this.world = world;
        this.height = height;
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

    public String getName() {
        return name;
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
        RegionSnapshot snap = (RegionSnapshot) other;
        return snap.getId().equals(this.getId()) &&
                snap.getBorderColor().equals(this.getBorderColor()) &&
                snap.getColor().equals(this.getColor()) &&
                snap.getWorld().equals(this.getWorld()) &&
                (snap.getName() != null ? snap.getName().equals(this.getName()) : this.getName() == null) &&
                snap.getHeight() == this.getHeight() &&
                snap.getPoints().equals(this.getPoints());
    }

}
