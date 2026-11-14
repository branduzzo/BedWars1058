package it.branduzzo.nextbedwars.blockdisplays.web.serverapi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ServerApiModel {
    private final String rawId;
    private final List<String> passengers = new ArrayList<>();
    private final List<String> hitbox = new ArrayList<>();
    private String version;
    private String type;
    private JsonObject datapack;
    private String projectId;
    private final Map<String, Map<Integer, List<String>>> animations = new HashMap<>();
    private final Map<String, Map<Integer, List<String>>> sounds = new HashMap<>();

    public ServerApiModel(String rawId, JsonObject content) {
        this.rawId = rawId;
        if (content.has("version")) this.version = content.get("version").getAsString();
        if (content.has("type")) this.type = content.get("type").getAsString();
        if (content.has("project_id") && !content.get("project_id").isJsonNull()) this.projectId = content.get("project_id").getAsString();
        if (content.has("datapack") && content.get("datapack").isJsonObject()) {
            this.datapack = content.getAsJsonObject("datapack");
            parseAnimations();
            parseSounds();
        }
        if (content.has("passengers") && content.get("passengers").isJsonArray()) {
            JsonArray arr = content.getAsJsonArray("passengers");
            for (JsonElement el : arr) {
                if (el.isJsonPrimitive()) passengers.add(el.getAsString());
                else passengers.add(el.toString());
            }
        }
        if (content.has("hitbox") && content.get("hitbox").isJsonArray()) {
            JsonArray arr = content.getAsJsonArray("hitbox");
            for (JsonElement el : arr) {
                String s = el.isJsonPrimitive() ? el.getAsString() : el.toString();
                hitbox.add(s);
            }
        }
    }

    private void parseAnimations() {
        if (!datapack.has("anim_keyframes")) return;
        JsonElement el = datapack.get("anim_keyframes");
        if (!el.isJsonObject()) return;
        JsonObject obj = el.getAsJsonObject();
        for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
            String animName = e.getKey();
            if (!e.getValue().isJsonObject()) continue;
            JsonObject frames = e.getValue().getAsJsonObject();
            Map<Integer, List<String>> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> f : frames.entrySet()) {
                try {
                    int tick = Integer.parseInt(f.getKey());
                    List<String> cmds = new ArrayList<>();
                    if (f.getValue().isJsonArray()) {
                        for (JsonElement ce : f.getValue().getAsJsonArray()) cmds.add(ce.getAsString());
                    }
                    map.put(tick, cmds);
                } catch (NumberFormatException ignored) {}
            }
            animations.put(animName, map);
        }
    }

    private void parseSounds() {
        if (!datapack.has("sound_keyframes")) return;
        JsonElement el = datapack.get("sound_keyframes");
        if (!el.isJsonObject()) return;
        JsonObject obj = el.getAsJsonObject();
        for (Map.Entry<String, JsonElement> e : obj.entrySet()) {
            String animName = e.getKey();
            if (!e.getValue().isJsonObject()) continue;
            JsonObject frames = e.getValue().getAsJsonObject();
            Map<Integer, List<String>> map = new HashMap<>();
            for (Map.Entry<String, JsonElement> f : frames.entrySet()) {
                try {
                    int tick = Integer.parseInt(f.getKey());
                    List<String> cmds = new ArrayList<>();
                    if (f.getValue().isJsonArray()) {
                        for (JsonElement ce : f.getValue().getAsJsonArray()) cmds.add(ce.getAsString());
                    }
                    map.put(tick, cmds);
                } catch (NumberFormatException ignored) {}
            }
            sounds.put(animName, map);
        }
    }

    public String buildSpawnCommand() {
        if (passengers.isEmpty()) return null;
        String joined = String.join(",", passengers);
        return "summon block_display ~ ~ ~ {Tags:[\"bde\"],Passengers:[" + joined + "]}";
    }

    public List<String> buildSpawnCommands() {
        String cmd = buildSpawnCommand();
        if (cmd == null) return List.of();
        return List.of(cmd);
    }
}
