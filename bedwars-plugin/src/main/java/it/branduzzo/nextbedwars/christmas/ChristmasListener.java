package it.branduzzo.nextbedwars.christmas;

import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.api.arena.IArena;
import it.branduzzo.nextbedwars.api.arena.team.ITeam;
import it.branduzzo.nextbedwars.api.events.player.PlayerBaseEnterEvent;
import it.branduzzo.nextbedwars.api.events.shop.ShopBuyEvent;
import it.branduzzo.nextbedwars.api.language.Language;
import it.branduzzo.nextbedwars.api.language.Messages;
import it.branduzzo.nextbedwars.arena.Arena;
import it.branduzzo.nextbedwars.util.Sched;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class ChristmasListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Interaction)) {
            return;
        }
        PresentDrop drop = ChristmasManager.present(entity.getUniqueId());
        if (drop != null) {
            event.setCancelled(true);
            ChristmasManager.openPresent(entity.getUniqueId(), event.getPlayer());
            return;
        }
        ChristmasTree tree = ChristmasManager.tree(entity.getUniqueId());
        if (tree != null) {
            event.setCancelled(true);
            tree.hit(event.getPlayer(), 5);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamageEntity(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (SnowmanGuard.isSnowman(entity)) {
            Player damager = resolvePlayer(event.getDamager());
            if (damager == null) {
                event.setCancelled(true);
            }
            return;
        }
        if (!(entity instanceof Interaction interaction)) {
            return;
        }
        ChristmasTree tree = ChristmasManager.tree(interaction.getUniqueId());
        if (tree == null) {
            return;
        }
        event.setCancelled(true);
        Player damager = resolvePlayer(event.getDamager());
        if (damager != null) {
            tree.hit(damager, event.getDamage());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageEvent event) {
        if (SnowmanGuard.isSnowman(event.getEntity())
                && !(event instanceof EntityDamageByEntityEvent)
                && event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }
        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) {
            return;
        }
        String id = ChristmasManager.itemId(item);
        if (id == null) {
            return;
        }
        Player player = event.getPlayer();
        IArena arena = Arena.getArenaByPlayer(player);
        if (arena == null) {
            return;
        }
        switch (id) {
            case "ice-bridge":
                if (event.getAction().name().contains("RIGHT_CLICK")) {
                    event.setCancelled(true);
                    IceBridge.build(player);
                    consume(player, item);
                }
                break;
            case "sleigh":
                event.setCancelled(true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 20 * 8, 0, false, true, true));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 8, 1, false, true, true));
                player.playSound(player.getLocation(), Sound.ENTITY_HORSE_SADDLE, 1f, 1f);
                consume(player, item);
                break;
            case "gift-bomb":
                event.setCancelled(true);
                Location location = player.getLocation().add(player.getLocation().getDirection().multiply(1.5));
                TNTPrimed tnt = player.getWorld().spawn(location, TNTPrimed.class);
                tnt.setFuseTicks(40);
                tnt.setSource(player);
                consume(player, item);
                break;
            default:
                break;
        }
    }

    private static void consume(Player player, ItemStack item) {
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().removeItem(item);
        }
        player.updateInventory();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onBaseEnter(PlayerBaseEnterEvent event) {
        if (!ChristmasManager.enabled() || !ChristmasManager.config().getBoolean(ChristmasConfig.TRAP_ENABLED)) {
            return;
        }
        ITeam team = event.getTeam();
        Player player = event.getPlayer();
        if (team.isMember(player) || !ChristmasManager.consumeTrap(team)) {
            return;
        }
        int seconds = Math.max(1, ChristmasManager.config().getInt(ChristmasConfig.TRAP_DARKNESS));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, seconds * 20, 0, false, true, true));
        IArena arena = Arena.getArenaByPlayer(player);
        ITeam enemyTeam = arena == null ? null : arena.getTeam(player);
        Location spawn = enemyTeam != null ? enemyTeam.getSpawn() : null;
        if (spawn != null) {
            player.teleport(spawn);
        }
        String message = ChristmasManager.config().getString(ChristmasConfig.TRAP_MESSAGE);
        player.sendMessage(it.branduzzo.nextbedwars.api.util.Theme.apply(message));
        for (Player member : team.getMembers()) {
            member.sendMessage(Language.getMsg(member, Messages.PREFIX)
                    + it.branduzzo.nextbedwars.api.util.Theme.apply(message));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBuy(ShopBuyEvent event) {
        if (event.isCancelled() || !ChristmasManager.enabled()) {
            return;
        }
        Player player = event.getBuyer();
        IArena arena = event.getArena();
        if (arena == null) {
            return;
        }
        ITeam team = arena.getTeam(player);
        String identifier = event.getCategoryContent().getIdentifier();
        if (identifier == null) {
            return;
        }
        switch (identifier) {
            case "snowman":
                SnowmanGuard.spawn(player);
                ChristmasManager.removeOneLater(player, "CARVED_PUMPKIN");
                player.playSound(player.getLocation(), Sound.ENTITY_SNOW_GOLEM_AMBIENT, 1f, 1f);
                break;
            case "christmas-tree":
                if (team != null) {
                    ChristmasManager.spawnTree(arena, team);
                    ChristmasManager.removeOneLater(player, "SPRUCE_SAPLING");
                    player.playSound(player.getLocation(), Sound.BLOCK_SNOW_PLACE, 1f, 0.8f);
                }
                break;
            case "dream-trap":
                if (team != null) {
                    ChristmasManager.armTrap(team);
                    ChristmasManager.removeOneLater(player, "TRIPWIRE_HOOK");
                    for (Player member : team.getMembers()) {
                        member.sendMessage(Language.getMsg(member, Messages.PREFIX)
                                + it.branduzzo.nextbedwars.api.util.Theme.apply(
                                "{accent}\u2744 {secondary}La trappola natalizia \u00e8 attiva!"));
                    }
                }
                break;
            case "ice-bridge":
                ChristmasManager.tagLater(player, "ICE", "ice-bridge");
                break;
            case "sleigh":
                ChristmasManager.tagLater(player, "OAK_BOAT", "sleigh");
                break;
            case "gift-bomb":
                ChristmasManager.tagLater(player, "TNT", "gift-bomb");
                break;
            default:
                break;
        }
    }

    private static Player resolvePlayer(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }
}
