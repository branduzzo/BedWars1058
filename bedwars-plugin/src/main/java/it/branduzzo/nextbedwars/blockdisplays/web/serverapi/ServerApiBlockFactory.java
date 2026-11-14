package it.branduzzo.nextbedwars.blockdisplays.web.serverapi;

import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class ServerApiBlockFactory {
    private ServerApiBlockFactory() {}

    public static Path createBlockFile(Plugin plugin, String blockName, ServerApiModel model) throws IOException {
        String safeName = blockName.toLowerCase().replaceAll("[^a-z0-9_\\-]", "_");
        if (safeName.isBlank()) safeName = "model_" + model.getRawId();
        Path dir = Path.of(plugin.getDataFolder().getPath(), "custom-blocks");
        Files.createDirectories(dir);
        Path file = dir.resolve(safeName + ".yml");
        List<String> spawnCommands = model.buildSpawnCommands();
        StringBuilder sb = new StringBuilder();
        sb.append("display:\n");
        sb.append("  spawn-command:\n");
        if (spawnCommands.isEmpty()) {
            sb.append("    - 'summon block_display ~ ~ ~ {Tags:[\"bde\"]}'\n");
        } else {
            for (String cmd : spawnCommands) {
                String escaped = cmd.replace("'", "''");
                sb.append("    - '").append(escaped).append("'\n");
            }
        }
        sb.append("central-material: BARRIER\n");
        sb.append("sides-count: 4\n");
        sb.append("item:\n");
        sb.append("  material: STONE\n");
        sb.append("  name: \"&fA Block Display\"\n");
        sb.append("interactions:\n");
        sb.append("  interaction:\n");
        sb.append("    height: 1.001\n");
        sb.append("    width: 1.001\n");
        sb.append("    offset:\n");
        sb.append("      x: 0\n");
        sb.append("      y: 0\n");
        sb.append("      z: 0\n");
        sb.append("    command:\n");
        sb.append("      - 'bde:animate default'\n");
        if (model.getSounds().containsKey("default")) {
            sb.append("      - 'bde:sound default'\n");
        }
        sb.append("    command-source: PLAYER\n");
        sb.append("collisions:\n");
        sb.append("  main:\n");
        sb.append("    size: 1.0\n");
        sb.append("    offset:\n");
        sb.append("      x: 0\n");
        sb.append("      y: 0\n");
        sb.append("      z: 0\n");
        sb.append("sound:\n");
        sb.append("  break:\n");
        sb.append("    sound-type: BLOCK_STONE_BREAK\n");
        sb.append("  place:\n");
        sb.append("    sound-type: BLOCK_STONE_PLACE\n");
        if (!model.getAnimations().isEmpty() || !model.getSounds().isEmpty()) {
            sb.append("bdengine:\n");
            if (!model.getAnimations().isEmpty()) {
                sb.append("  animations:\n");
                for (Map.Entry<String, Map<Integer, List<String>>> anim : model.getAnimations().entrySet()) {
                    sb.append("    ").append(anim.getKey()).append(":\n");
                    for (Map.Entry<Integer, List<String>> frame : anim.getValue().entrySet()) {
                        sb.append("      '").append(frame.getKey()).append("':\n");
                        for (String cmd : frame.getValue()) {
                            String esc = cmd.replace("'", "''");
                            sb.append("        - '").append(esc).append("'\n");
                        }
                    }
                }
            }
            if (!model.getSounds().isEmpty()) {
                sb.append("  sounds:\n");
                for (Map.Entry<String, Map<Integer, List<String>>> snd : model.getSounds().entrySet()) {
                    sb.append("    ").append(snd.getKey()).append(":\n");
                    for (Map.Entry<Integer, List<String>> frame : snd.getValue().entrySet()) {
                        sb.append("      '").append(frame.getKey()).append("':\n");
                        for (String cmd : frame.getValue()) {
                            String esc = cmd.replace("'", "''");
                            sb.append("        - '").append(esc).append("'\n");
                        }
                    }
                }
            }
        }
        Files.writeString(file, sb.toString());
        return file;
    }

    public static String blockNameForId(String id) {
        return "bde_" + id.replaceAll("[^a-zA-Z0-9]", "");
    }
}
