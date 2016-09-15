package com.khorium.publicfarm.farm;

import com.khorium.publicfarm.PublicFarm;
import com.khorium.publicfarm.Utils.LogUtil;
import com.khorium.publicfarm.database.DatabaseHelper;
import com.khorium.publicfarm.Utils.MsgUtil;
import com.sun.scenario.effect.Crop;
import net.minecraft.server.v1_10_R1.BlockCrops;
import org.bukkit.CropState;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.Crops;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/**
 *
 * Created by Khorium on 2016/8/8.
 */
public class PFManager {

    private PublicFarm publicFarm;

    public PFManager(PublicFarm publicFarm) {
        this.publicFarm = publicFarm;
    }

    public void listPublicFarm(CommandSender sender) {
        HashMap<String, Farm> farms = DatabaseHelper.getAllFarms();
        if (farms.size() == 0) {
            sender.sendMessage(MsgUtil.getMessage("no-farm-found"));
            return;
        }
        Iterator iterator = farms.entrySet().iterator();
        String farmName;
        String farmArea;
        String farmType;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            farmName = ((Farm) entry.getValue()).getName();
            farmArea = String.valueOf(((Farm) entry.getValue()).getArea());
            farmType = ((Farm) entry.getValue()).getType();
            sender.sendMessage(MsgUtil.getMessage("list-farms", farmName,
                    farmArea, farmType));
        }
    }

    public void setPublicFarm(Player player, String name, Location
            firstLocation, Location secondLocation, Material farmType) {

        if (firstLocation == null || secondLocation == null || name == null
                || farmType == null) {
            player.sendMessage(MsgUtil.getMessage("set-farm-fail"));
            return;
        }
        LogUtil.logInfo("farmName: " + name + "  firstLocation: " +
                firstLocation.getX() + "," + firstLocation.getY() + "," +
                firstLocation.getZ() + "  secondLocation:" + secondLocation
                .getX() + "," + secondLocation.getY() + "," + secondLocation
                .getZ() + "   farmType:" + farmType.toString());


        if (!firstLocation.getWorld().getName().equalsIgnoreCase
                (secondLocation.getWorld().getName())) {
            player.sendMessage(MsgUtil.getMessage("world-wrong"));
            return;
        }

        Farm farm = new Farm(name, farmType.name(), UUID.randomUUID()
                .toString(), firstLocation, secondLocation);

        World world = publicFarm.getServer().getWorld(farm.getWorld());
        Block highBlock = world.getBlockAt(farm.getX1(), farm.getY1(), farm
                .getZ1());
        Block lowBlock = world.getBlockAt(farm.getX2(), farm.getY2(), farm
                .getZ2());

        if (isDuplicateFarm(highBlock.getLocation(), lowBlock.getLocation
                ()) != null) {
            player.sendMessage(MsgUtil.getMessage("duplicate-location"));
            return;
        }

        Block temp;
        Material tempType;
        Block upBlock;

        for (int i = lowBlock.getX(); i <= highBlock.getX(); i++) {
            for (int j = lowBlock.getZ(); j <= highBlock.getZ(); j++) {

                temp = world.getBlockAt(i, lowBlock.getY(), j);
                tempType = temp.getType();
                LogUtil.logInfo(i + "," + j + "," + lowBlock.getY() + "  this" +
                        " is a " + tempType.name());
                switch (farmType) {
                    case NETHER_WARTS:
                        if (tempType.equals(Material.SOUL_SAND)) {
                            LogUtil.logInfo("found plantable block in" + i +
                                    "," + j);
                            upBlock = temp.getRelative(BlockFace.UP);
                            if (upBlock.getType().equals(Material.AIR) ||
                                    upBlock
                                            .getType().equals(Material
                                            .LONG_GRASS)) {

                                LogUtil.logInfo("found magicable upblock in"
                                        + i + "," +
                                        "" + j);
                                upBlock.setType(farmType);
                            }
                        }
                        break;
                    case WHEAT:
                        if (tempType.equals(Material.DIRT) || tempType.equals
                                (Material.GRASS) || tempType.equals(Material
                                .SOIL)) {
                            LogUtil.logInfo("found plantable block " + "in" +
                                    i + "," + j);
                            temp.setType(Material.SOIL);
                            upBlock = temp.getRelative(BlockFace.UP);
                            if (upBlock.getType().equals(Material.AIR) ||
                                    upBlock.getType().equals(Material
                                            .LONG_GRASS)) {

                                LogUtil.logInfo("found magicable " + "upblock" +
                                        " in" + i + "," + "" + j + ", " +
                                        farmType.name());
                                upBlock.setType(Material.CROPS);
                            }
                        }
                        break;
                    case POTATO:
                    case CARROT:
                        if (tempType.equals(Material.DIRT) || tempType.equals
                                (Material.GRASS) || tempType.equals(Material
                                .SOIL)) {
                            LogUtil.logInfo("found plantable block " + "in" +
                                    i + "," + j);
                            temp.setType(Material.SOIL);
                            upBlock = temp.getRelative(BlockFace.UP);
                            if (upBlock.getType().equals(Material.AIR) ||
                                    upBlock.getType().equals(Material
                                            .LONG_GRASS)) {

                                LogUtil.logInfo("found magicable " + "upblock" +
                                        " in" + i + "," + "" + j + ", " +
                                        farmType.name());
                                upBlock.setType(farmType);
                            }
                        }
                        break;
                }
            }
        }


        if (!DatabaseHelper.addFarm(farm)) {
            player.sendMessage(MsgUtil.getMessage
                    ("set-farm-fail"));
            return;
        }

        player.sendMessage(MsgUtil.getMessage
                ("set-farm-success"));
    }

    public void deletePublicFarm(CommandSender commandSender, String name) {

        if (!DatabaseHelper.deleteFarm(name)) {
            commandSender.sendMessage(MsgUtil.getMessage("delete-farm-fail"));
            return;
        }

        commandSender.sendMessage(MsgUtil.getMessage("delete-farm-success"));
    }

    public Material isBlockInFarm(Block block) {
        block.getLocation().setY(block.getLocation().getY());
        Farm farm = isDuplicateFarm(block.getLocation(), block.getLocation());
        return farm != null ?
                Material.getMaterial(farm.getType()) : null;
    }

    public Material isPlantInFarm(Block block) {
        block.getLocation().setY(block.getLocation().getY() + 1);
        Farm farm = isDuplicateFarm(block.getLocation(), block.getLocation());
        return farm != null ?
                Material.getMaterial(farm.getType()) : null;
    }

    private static Farm isDuplicateFarm(Location highLocation, Location
            lowLocation) {
        for (Object o : DatabaseHelper.getAllFarms().entrySet()) {
            Farm farm = (Farm) ((Map.Entry) o).getValue();
            if (highLocation.getX() >= farm.getX2() && highLocation.getZ() >=
                    farm.getZ2() && lowLocation.getX() <= farm.getX1() &&
                    lowLocation.getZ() <= farm.getZ1()) {
                return farm;
            }
        }
        return null;
    }
}
