package it.branduzzo.nextbedwars.support.version.v1_21;

import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.api.arena.IArena;
import it.branduzzo.nextbedwars.api.arena.team.ITeam;
import it.branduzzo.nextbedwars.api.arena.team.TeamColor;
import it.branduzzo.nextbedwars.api.entity.Despawnable;
import it.branduzzo.nextbedwars.api.events.player.PlayerKillEvent;
import it.branduzzo.nextbedwars.api.language.Messages;
import it.branduzzo.nextbedwars.api.server.VersionSupport;
import it.branduzzo.nextbedwars.util.Sched;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.type.Bed;
import org.bukkit.command.Command;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

@SuppressWarnings("deprecation")
public class V1_21 extends VersionSupport {

    private final Plugin plugin;
    private final NamespacedKey dataKey;
    private final NamespacedKey identifierKey;

    public V1_21(Plugin plugin, String versionName) {
        super(plugin, versionName);
        this.plugin = plugin;
        this.dataKey = new NamespacedKey(plugin, "nextbedwars_data");
        this.identifierKey = new NamespacedKey(plugin, "tier_identifier");
        loadDefaultEffects();
    }

    @Override
    public void registerCommand(String name, Command clasa) {
        clasa.setLabel(name);
        Bukkit.getCommandMap().register("nextbedwars", clasa);
    }

    @Override
    public boolean isBukkitCommandRegistered(String command) {
        return Bukkit.getCommandMap().getCommand(command) != null;
    }

    @Override
    public void sendTitle(Player p, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        Component main = color(title);
        Component sub = color(subtitle);
        p.showTitle(Title.title(main, sub, Title.Times.times(
                Duration.ofMillis(Math.max(1, fadeIn) * 50L),
                Duration.ofMillis(Math.max(1, stay) * 50L),
                Duration.ofMillis(Math.max(1, fadeOut) * 50L)
        )));
    }

    @Override
    public void playAction(Player p, String text) {
        p.sendActionBar(color(text));
    }

    private static Component color(String text) {
        return net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection()
                .deserialize(ChatColor.translateAlternateColorCodes('&', text == null ? "" : text));
    }

    @Override
    public ItemStack getItemInHand(Player p) {
        return p.getInventory().getItemInMainHand();
    }

    @Override
    public void hideEntity(Entity entity, Player player) {
        player.hideEntity(plugin, entity);
    }

    @Override
    public boolean isArmor(ItemStack itemStack) {
        String type = itemStack.getType().name();
        return type.endsWith("_HELMET") || type.endsWith("_CHESTPLATE") || type.endsWith("_LEGGINGS")
                || type.endsWith("_BOOTS") || type.equals("ELYTRA");
    }

    @Override
    public boolean isTool(ItemStack itemStack) {
        String type = itemStack.getType().name();
        return type.endsWith("_PICKAXE") || type.endsWith("_AXE") || type.endsWith("_SHOVEL")
                || type.endsWith("_HOE") || type.equals("SHEARS") || type.equals("FLINT_AND_STEEL");
    }

    @Override
    public boolean isSword(ItemStack itemStack) {
        return itemStack.getType().name().endsWith("_SWORD");
    }

    @Override
    public boolean isAxe(ItemStack itemStack) {
        return itemStack.getType().name().endsWith("_AXE");
    }

    @Override
    public boolean isBow(ItemStack itemStack) {
        return itemStack.getType() == Material.BOW || itemStack.getType() == Material.CROSSBOW;
    }

    @Override
    public boolean isProjectile(ItemStack itemStack) {
        return switch (itemStack.getType()) {
            case ARROW, SPECTRAL_ARROW, SNOWBALL, EGG, ENDER_PEARL, FIRE_CHARGE, TRIDENT -> true;
            default -> false;
        };
    }

    @Override
    public boolean isInvisibilityPotion(ItemStack itemStack) {
        if (itemStack.getType() != Material.POTION && itemStack.getType() != Material.SPLASH_POTION
                && itemStack.getType() != Material.LINGERING_POTION) {
            return false;
        }
        if (!(itemStack.getItemMeta() instanceof PotionMeta meta)) {
            return false;
        }
        if (meta.hasCustomEffect(PotionEffectType.INVISIBILITY)) {
            return true;
        }
        PotionType base = meta.getBasePotionType();
        return base == PotionType.INVISIBILITY || base == PotionType.LONG_INVISIBILITY;
    }

    @Override
    public void registerEntities() {
    }

    @Override
    public void spawnShop(Location loc, String name, List<Player> players, IArena arena) {
        if (loc.getWorld() == null) {
            return;
        }
        Villager villager = loc.getWorld().spawn(loc, Villager.class);
        villager.setAI(false);
        villager.setInvulnerable(true);
        villager.setSilent(true);
        villager.setPersistent(true);
        villager.setRemoveWhenFarAway(false);
        villager.setProfession(Villager.Profession.NONE);
        villager.setCustomName(ChatColor.translateAlternateColorCodes('&', name));
        villager.setCustomNameVisible(true);
    }

    @Override
    public double getDamage(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable) {
            return damageable.getDamage();
        }
        return 0;
    }

    @Override
    public void spawnSilverfish(Location loc, ITeam team, double speed, double health, int despawn, double damage) {
        if (loc.getWorld() == null) {
            return;
        }
        Silverfish entity = loc.getWorld().spawn(loc, Silverfish.class);
        entity.setMaxHealth(Math.max(1, health));
        entity.setHealth(Math.max(1, health));
        entity.setCustomNameVisible(true);
        new Despawnable(entity, team, despawn, Messages.SHOP_UTILITY_NPC_SILVERFISH_NAME,
                PlayerKillEvent.PlayerKillCause.SILVERFISH_FINAL_KILL,
                PlayerKillEvent.PlayerKillCause.SILVERFISH);
    }

    @Override
    public void spawnIronGolem(Location loc, ITeam team, double speed, double health, int despawn) {
        if (loc.getWorld() == null) {
            return;
        }
        IronGolem entity = loc.getWorld().spawn(loc, IronGolem.class);
        entity.setMaxHealth(Math.max(1, health));
        entity.setHealth(Math.max(1, health));
        entity.setPlayerCreated(true);
        entity.setCustomNameVisible(true);
        new Despawnable(entity, team, despawn, Messages.SHOP_UTILITY_NPC_IRON_GOLEM_NAME,
                PlayerKillEvent.PlayerKillCause.IRON_GOLEM_FINAL_KILL,
                PlayerKillEvent.PlayerKillCause.IRON_GOLEM);
    }

    @Override
    public void minusAmount(Player p, ItemStack i, int amount) {
        if (i == null) {
            return;
        }
        int newAmount = i.getAmount() - amount;
        if (newAmount <= 0) {
            p.getInventory().removeItem(i);
        } else {
            i.setAmount(newAmount);
        }
    }

    @Override
    public void setSource(TNTPrimed tnt, Player owner) {
        tnt.setSource(owner);
    }

    @Override
    public void voidKill(Player p) {
        p.setHealth(0);
    }

    @Override
    public void hideArmor(Player victim, Player receiver) {
    }

    @Override
    public void showArmor(Player victim, Player receiver) {
    }

    @Override
    public void spawnDragon(Location l, ITeam team) {
        if (l.getWorld() == null) {
            return;
        }
        EnderDragon dragon = l.getWorld().spawn(l, EnderDragon.class);
        dragon.setPhase(EnderDragon.Phase.CIRCLING);
        dragon.setCustomNameVisible(true);
        new Despawnable(dragon, team, 0, Messages.SHOP_UTILITY_NPC_IRON_GOLEM_NAME,
                PlayerKillEvent.PlayerKillCause.UNKNOWN, PlayerKillEvent.PlayerKillCause.UNKNOWN);
    }

    @Override
    public void colorBed(ITeam team) {
        if (team == null || team.getBed() == null || team.getBed().getWorld() == null) {
            return;
        }
        Block block = team.getBed().getBlock();
        block.setType(team.getColor().bedMaterial());
    }

    @Override
    public void registerTntWhitelist(float endStoneBlast, float glassBlast) {
    }

    @Override
    public void setBlockTeamColor(Block block, TeamColor teamColor) {
        String type = block.getType().name();
        if (type.endsWith("_WOOL")) {
            block.setType(teamColor.woolMaterial());
        } else if (type.endsWith("_BED")) {
            block.setType(teamColor.bedMaterial());
        } else if (type.contains("STAINED_GLASS")) {
            block.setType(teamColor.glassMaterial());
        } else if (type.endsWith("_TERRACOTTA")) {
            block.setType(teamColor.glazedTerracottaMaterial());
        }
    }

    @Override
    public void setCollide(Player p, IArena a, boolean value) {
        p.setCollidable(value);
    }

    @Override
    public ItemStack addCustomData(ItemStack i, String data) {
        ItemMeta meta = i.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(dataKey, PersistentDataType.STRING, data);
            i.setItemMeta(meta);
        }
        return i;
    }

    @Override
    public ItemStack setTag(ItemStack itemStack, String key, String value) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(new NamespacedKey(plugin, normalize(key)), PersistentDataType.STRING, value);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    @Nullable
    @Override
    public String getTag(ItemStack itemStack, String key) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(new NamespacedKey(plugin, normalize(key)), PersistentDataType.STRING);
    }

    private static String normalize(String key) {
        return key.toLowerCase().replaceAll("[^a-z0-9_.-]", "_");
    }

    @Override
    public boolean isCustomBedWarsItem(ItemStack i) {
        ItemMeta meta = i.getItemMeta();
        return meta != null && meta.getPersistentDataContainer().has(dataKey, PersistentDataType.STRING);
    }

    @Override
    public String getCustomData(ItemStack i) {
        ItemMeta meta = i.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(dataKey, PersistentDataType.STRING);
    }

    @Override
    public ItemStack colourItem(ItemStack itemStack, ITeam team) {
        if (itemStack == null || team == null) {
            return itemStack;
        }
        if (itemStack.getItemMeta() instanceof LeatherArmorMeta meta) {
            meta.setColor(team.getColor().bukkitColor());
            itemStack.setItemMeta(meta);
            return itemStack;
        }
        String type = itemStack.getType().name();
        if (type.endsWith("_WOOL")) {
            itemStack.setType(team.getColor().woolMaterial());
        } else if (type.endsWith("_BED")) {
            itemStack.setType(team.getColor().bedMaterial());
        } else if (type.contains("STAINED_GLASS")) {
            itemStack.setType(team.getColor().glassMaterial());
        }
        return itemStack;
    }

    @Override
    public ItemStack createItemStack(String material, int amount, short data) {
        Material mat = Material.matchMaterial(material == null ? "" : material.toUpperCase());
        if (mat == null) {
            BedWars.plugin.getLogger().severe("Invalid material: " + material);
            return null;
        }
        return new ItemStack(mat, Math.max(1, amount));
    }

    @Override
    public Material materialFireball() {
        return Material.FIRE_CHARGE;
    }

    @Override
    public Material materialPlayerHead() {
        return Material.PLAYER_HEAD;
    }

    @Override
    public Material materialSnowball() {
        return Material.SNOWBALL;
    }

    @Override
    public Material materialGoldenHelmet() {
        return Material.GOLDEN_HELMET;
    }

    @Override
    public Material materialGoldenChestPlate() {
        return Material.GOLDEN_CHESTPLATE;
    }

    @Override
    public Material materialGoldenLeggings() {
        return Material.GOLDEN_LEGGINGS;
    }

    @Override
    public Material materialNetheriteHelmet() {
        return Material.NETHERITE_HELMET;
    }

    @Override
    public Material materialNetheriteChestPlate() {
        return Material.NETHERITE_CHESTPLATE;
    }

    @Override
    public Material materialNetheriteLeggings() {
        return Material.NETHERITE_LEGGINGS;
    }

    @Override
    public Material materialElytra() {
        return Material.ELYTRA;
    }

    @Override
    public Material materialCake() {
        return Material.CAKE;
    }

    @Override
    public Material materialCraftingTable() {
        return Material.CRAFTING_TABLE;
    }

    @Override
    public Material materialEnchantingTable() {
        return Material.ENCHANTING_TABLE;
    }

    @Override
    public void setJoinSignBackground(BlockState b, Material material) {
        b.getBlock().setType(material);
    }

    @Override
    public Material woolMaterial() {
        return Material.WHITE_WOOL;
    }

    @Override
    public String getShopUpgradeIdentifier(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(identifierKey, PersistentDataType.STRING);
    }

    @Override
    public ItemStack setShopUpgradeIdentifier(ItemStack itemStack, String identifier) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(identifierKey, PersistentDataType.STRING, identifier);
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    @Override
    public ItemStack getPlayerHead(Player player, @Nullable ItemStack copyTagFrom) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (head.getItemMeta() instanceof SkullMeta meta) {
            meta.setOwningPlayer(player);
            head.setItemMeta(meta);
        }
        return head;
    }

    @Override
    public void sendPlayerSpawnPackets(Player player, IArena arena) {
    }

    @Override
    public String getInventoryName(InventoryEvent e) {
        return e.getView().getTitle();
    }

    @Override
    public void setUnbreakable(ItemMeta itemMeta) {
        itemMeta.setUnbreakable(true);
    }

    @Override
    public int getVersion() {
        return 21;
    }

    @Override
    public void registerVersionListeners() {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onSwap(PlayerSwapHandItemsEvent event) {
                ItemStack main = event.getMainHandItem();
                ItemStack off = event.getOffHandItem();
                if ((main != null && isCustomBedWarsItem(main)) || (off != null && isCustomBedWarsItem(off))) {
                    event.setCancelled(true);
                }
            }
        }, plugin);
    }

    @Override
    public String getMainLevel() {
        return Bukkit.getWorlds().get(0).getName();
    }

    @Override
    public Fireball setFireballDirection(Fireball fireball, Vector vector) {
        fireball.setDirection(vector);
        return fireball;
    }

    @Override
    public void playRedStoneDot(Player player) {
        player.spawnParticle(Particle.DUST, player.getLocation().add(0, 1, 0), 1,
                new Particle.DustOptions(org.bukkit.Color.RED, 1.0f));
    }

    @Override
    public void clearArrowsFromPlayerBody(Player player) {
        player.setArrowsInBody(0);
    }

    @Override
    public void placeTowerBlocks(Block b, IArena a, TeamColor color, int x, int y, int z) {
        b.setType(color.woolMaterial());
    }

    @Override
    public void placeLadder(Block b, int x, int y, int z, IArena a, int ladderdata) {
        b.setType(Material.LADDER);
    }

    @Override
    public void playVillagerEffect(Player player, Location location) {
        player.spawnParticle(Particle.HAPPY_VILLAGER, location, 1);
    }
}
