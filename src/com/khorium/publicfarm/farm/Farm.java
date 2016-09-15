package com.khorium.publicfarm.farm;

import org.bukkit.Location;

import java.util.UUID;

/**
 *
 * Created by Khorium on 2016/8/8.
 */
public class Farm {

    public Farm(String name, String farmType, String id, Location firstLocation,
                Location secondLocation) {
        this.name = name;
        this.type = farmType;
        this.id = id;
        world = firstLocation.getWorld().getName();

        if (firstLocation.getBlockX() > secondLocation.getBlockX()) {
            x1 = firstLocation.getBlockX();
            x2 = secondLocation.getBlockX();
        } else {
            x1 = secondLocation.getBlockX();
            x2 = firstLocation.getBlockX();
        }
        if (firstLocation.getBlockY() > secondLocation.getBlockY()) {
            y1 = firstLocation.getBlockY();
            y2 = secondLocation.getBlockY();
        } else {
            y1 = secondLocation.getBlockY();
            y2 = firstLocation.getBlockY();
        }
        if (firstLocation.getBlockZ() > secondLocation.getBlockZ()) {
            z1 = firstLocation.getBlockZ();
            z2 = secondLocation.getBlockZ();
        } else {
            z1 = secondLocation.getBlockZ();
            z2 = firstLocation.getBlockZ();
        }

    }

    private String name;
    private String type;
    private String id;
    private int x1;
    private int y1;
    private int z1;
    private int x2;
    private int y2;
    private int z2;
    private String world;
    private int area;


    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public int getX1() {
        return x1;
    }

    public int getY1() {
        return y1;
    }

    public int getZ1() {
        return z1;
    }

    public int getX2() {
        return x2;
    }

    public int getY2() {
        return y2;
    }

    public int getZ2() {
        return z2;
    }

    public String getWorld() {
        return world;
    }

    public int getArea() {
        return (x1 - x2) * (z1 - z2);
    }

    public String getType() {
        return type;
    }
}
