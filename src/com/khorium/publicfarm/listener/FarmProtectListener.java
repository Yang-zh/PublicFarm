package com.khorium.publicfarm.listener;

import com.khorium.publicfarm.PublicFarm;
import com.khorium.publicfarm.Utils.LogUtil;
import com.khorium.publicfarm.Utils.MsgUtil;
import com.khorium.publicfarm.Utils.Utils;
import com.mysql.jdbc.Util;
import org.bukkit.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Crops;
import org.bukkit.material.NetherWarts;

import java.util.*;

/**
 * Created by Khorium on 2016/8/8.
 */
public class FarmProtectListener implements Listener {

    private PublicFarm publicFarm;
    private UUID uuid;
    private Location firstLocation;
    private Location secondLocation;
    private String farmName;
    private Material farmType;
    private boolean catchRightClick;

    private HashMap<String, Integer> harvestLimit = new HashMap<String,
            Integer>();


    public FarmProtectListener(PublicFarm publicFarm) {
        this.publicFarm = publicFarm;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    private synchronized void onPlayerClick(PlayerInteractEvent event) {

        LogUtil.logInfo(event.getEventName());

        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
                if (!catchRightClick) {
                    return;
                }

                LogUtil.logInfo("click " + event.getClickedBlock().getType()
                        .name());

                if (event.getPlayer().getUniqueId() != uuid) {
                    return;
                }

                Location clickedLocation = event.getClickedBlock()
                        .getLocation();

                if (firstLocation == null) {
                    firstLocation = clickedLocation;
                    event.getPlayer().sendMessage(MsgUtil.getMessage
                            ("set-second-location"));
                    return;
                } else if (!clickedLocation.equals(firstLocation) &&
                        secondLocation == null) {
                    if (clickedLocation.getY() != firstLocation.getY()) {
                        event.getPlayer().sendMessage(MsgUtil.getMessage
                                ("set-second-location"));
                        return;
                    }
                    catchRightClick = false;
                    secondLocation = clickedLocation;
                    publicFarm.getManager().setPublicFarm(event.getPlayer(),
                            farmName, firstLocation, secondLocation, farmType);
                    firstLocation = null;
                    secondLocation = null;
                    return;
                }
                event.setCancelled(true);
                break;
            default:
                break;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent event) {

        LogUtil.logInfo(event.getEventName());

        if (!(Utils.isCrop(event.getBlock().getType()))) {
            LogUtil.logInfo("not crop");
            if (publicFarm.protectFarm && publicFarm.getManager()
                    .isPlantInFarm(event.getBlock()) != null) {
                event.setCancelled(true);
            }
            return;
        }
        if (!event.getPlayer().hasPermission("publicfarm.use")) {
            LogUtil.logInfo("no permission");
            event.getPlayer().sendMessage(MsgUtil.getMessage
                    ("need-use-permission"));
            return;
        }
        Material blockType = publicFarm.getManager().isPlantInFarm(event
                .getBlock());
        if (blockType == null) {
            LogUtil.logInfo("not a block in farm");
            return;
        }
        LogUtil.logInfo("blockType " + blockType.name());
        NetherWarts wart = null;
        Crops crop = null;
        if (blockType == Material.NETHER_WARTS) {
            wart = (NetherWarts) event.getBlock().getState()
                    .getData();
        } else {
            crop = (Crops) event.getBlock().getState().getData();
        }

        if ((crop != null && !crop.getState().equals(CropState.RIPE)) ||
                (wart != null && !wart.getState().equals(NetherWartsState
                        .RIPE))) {
            LogUtil.logInfo("not ripe");
            event.setCancelled(true);
            event.getPlayer().sendMessage(MsgUtil.getMessage("wait-ripe"));
            return;
        }

        String uuid = event.getPlayer().getUniqueId().toString();
        if (harvestLimit.get(uuid) == null) {
            harvestLimit.put(uuid, 0);
        }

        int limit = harvestLimit.get(uuid);
        if (limit >= publicFarm.harvestLimitPerDay) {
            event.getPlayer().sendMessage(MsgUtil.getMessage
                    ("harvest-food-limit", String.valueOf(publicFarm
                            .harvestLimitPerDay)));
            event.setCancelled(true);
            return;
        } else {
            harvestLimit.put(uuid, ++limit);
            event.getPlayer().sendMessage(MsgUtil.getMessage("harvest-food",
                    String.valueOf(limit)));
        }
        switch (blockType) {
            case NETHER_WARTS:
                event.setCancelled(true);
                event.getPlayer().getWorld().dropItemNaturally(event.getBlock
                        ().getLocation(), new ItemStack(Material
                        .NETHER_STALK, Utils.random(3) + 1));
                event.getBlock().setType(Material.NETHER_WARTS);
                return;
            case CARROT:
                LogUtil.logInfo("carrot");
                event.setCancelled(true);
                event.getPlayer().getWorld().dropItemNaturally(event.getBlock
                        ().getLocation(), new ItemStack(Material.CARROT_ITEM,
                        Utils.random(4)));
                event.getBlock().setType(Material.CARROT);
                return;
            case POTATO:
                LogUtil.logInfo("potato");
                event.setCancelled(true);
                event.getPlayer().getWorld().dropItemNaturally(event.getBlock
                        ().getLocation(), new ItemStack(Material.POTATO_ITEM,
                        Utils.random(4)));
                event.getBlock().setType(Material.POTATO);
                return;
            case WHEAT:
                LogUtil.logInfo("wheat");
                event.setCancelled(true);
                event.getPlayer().getWorld().dropItemNaturally(event.getBlock
                        ().getLocation(), new ItemStack(Material.WHEAT));
                event.getPlayer().getWorld().dropItemNaturally(event.getBlock
                        ().getLocation(), new ItemStack(Material.SEEDS, Utils
                        .random(3)));
                event.getBlock().setType(Material.CROPS);
                return;
            default:
                break;
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        LogUtil.logInfo(event.getEventName());
        if (publicFarm.getManager().isBlockInFarm(event.getBlock()) != null) {
            event.setCancelled(true);
        }
    }

    public void startSetListener(String name, UUID uuid, Material farmType) {
        this.farmName = name;
        this.uuid = uuid;
        this.farmType = farmType;
        this.catchRightClick = true;

        LogUtil.logInfo("farmName： " + farmName + " farmType: " + farmType +
                " uuid: " + uuid);
    }

    public void clearLimits() {
        harvestLimit.clear();
    }
}
