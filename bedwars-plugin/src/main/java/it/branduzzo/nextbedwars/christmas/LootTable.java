package it.branduzzo.nextbedwars.christmas;

import it.branduzzo.nextbedwars.BedWars;
import it.branduzzo.nextbedwars.api.arena.IArena;
import it.branduzzo.nextbedwars.shop.ShopManager;
import it.branduzzo.nextbedwars.shop.main.CategoryContent;
import it.branduzzo.nextbedwars.shop.main.ShopCategory;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class LootTable {

    private static final Random RANDOM = new Random();

    private LootTable() {
    }

    public static void open(Player player, IArena arena, Location location) {
        ChristmasConfig config = ChristmasManager.config();
        if (config == null) {
            return;
        }
        int rolls = Math.max(1, config.getInt("loot.max-rolls"));

        List<String> materials = config.lootMaterials();
        List<String> shopItems = config.lootShopItems();
        List<String> potions = config.lootPotions();
        List<String> effects = config.lootEffects();

        for (int i = 0; i < rolls; i++) {
            int kind = RANDOM.nextInt(4);
            if (kind == 0 && !materials.isEmpty()) {
                dropMaterial(location, materials.get(RANDOM.nextInt(materials.size())));
            } else if (kind == 1 && !shopItems.isEmpty()) {
                dropShopItem(location, player, shopItems.get(RANDOM.nextInt(shopItems.size())));
            } else if (kind == 2 && !potions.isEmpty()) {
                applyEffect(player, potions.get(RANDOM.nextInt(potions.size())));
            } else if (!effects.isEmpty()) {
                applyEffect(player, effects.get(RANDOM.nextInt(effects.size())));
            }
        }
        location.getWorld().spawnParticle(ChristmasConfig.particle("SNOWFLAKE"), location.clone().add(0, 1, 0), 40, 1, 1, 1, 0.05);
    }

    private static void dropMaterial(Location location, String entry) {
        String[] parts = entry.split(":");
        Material material = ChristmasConfig.material(parts[0]);
        int amount = parts.length > 1 ? parseInt(parts[1], 1) : 1;
        ItemStack stack = new ItemStack(material, Math.max(1, amount));
        location.getWorld().dropItemNaturally(location.clone().add(0, 0.5, 0), stack);
    }

    private static void dropShopItem(Location location, Player player, String identifier) {
        if (ShopManager.shop == null) {
            return;
        }
        for (ShopCategory category : ShopManager.shop.getCategoryList()) {
            for (CategoryContent content : category.getCategoryContentList()) {
                if (!content.getIdentifier().equalsIgnoreCase(identifier)) {
                    continue;
                }
                ItemStack preview = content.getItemStack(player);
                if (preview != null) {
                    ItemStack clone = preview.clone();
                    location.getWorld().dropItemNaturally(location.clone().add(0, 0.5, 0), clone);
                }
                return;
            }
        }
    }

    private static void applyEffect(Player player, String entry) {
        String[] parts = entry.split(":");
        PotionEffectType type = effect(parts[0]);
        int duration = parts.length > 1 ? parseInt(parts[1], 100) : 100;
        int amplifier = parts.length > 2 ? parseInt(parts[2], 0) : 0;
        player.addPotionEffect(new PotionEffect(type, Math.max(20, duration), Math.max(0, amplifier), false, true, true));
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static PotionEffectType effect(String name) {
        String normalized = name.toUpperCase();
        switch (normalized) {
            case "INCREASE_DAMAGE":
                normalized = "STRENGTH";
                break;
            case "DAMAGE_RESISTANCE":
                normalized = "RESISTANCE";
                break;
            case "JUMP":
                normalized = "JUMP_BOOST";
                break;
            case "SLOW":
                normalized = "SLOWNESS";
                break;
            case "FAST_DIGGING":
                normalized = "HASTE";
                break;
            default:
                break;
        }
        PotionEffectType type = PotionEffectType.getByName(normalized);
        return type == null ? PotionEffectType.SPEED : type;
    }

    public static List<String> identifiers() {
        List<String> ids = new ArrayList<>();
        if (ShopManager.shop != null) {
            for (ShopCategory category : ShopManager.shop.getCategoryList()) {
                for (CategoryContent content : category.getCategoryContentList()) {
                    ids.add(content.getIdentifier());
                }
            }
        }
        return ids;
    }

    public static int countShopItems() {
        return identifiers().size();
    }
}
