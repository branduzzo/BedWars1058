package it.branduzzo.nextbedwars.blockdisplays;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import it.branduzzo.nextbedwars.blockdisplays.command.BlockDisplayCreatorSpigotCommand;
import it.branduzzo.nextbedwars.blockdisplays.command.CustomBlockGiveSpigotCommand;
import it.branduzzo.nextbedwars.blockdisplays.common.BDCDependentPluginsManager;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.BDCCustomBlockConfigStorage;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.BDCCustomBlockService;
import it.branduzzo.nextbedwars.blockdisplays.custom.block.CustomBlockService;
import it.branduzzo.nextbedwars.blockdisplays.data.manager.PersistentDataTypeManager;
import it.branduzzo.nextbedwars.blockdisplays.data.manager.PersistentDataTypes;
import it.branduzzo.nextbedwars.blockdisplays.data.manager.TypeTokens;
import it.branduzzo.nextbedwars.blockdisplays.data.persistent.PersistentData;
import it.branduzzo.nextbedwars.blockdisplays.data.yaml.YamlConfigFile;
import it.branduzzo.nextbedwars.blockdisplays.data.yaml.YamlData;
import it.branduzzo.nextbedwars.blockdisplays.file.config.value.BooleanConfigValue;
import it.branduzzo.nextbedwars.blockdisplays.file.config.value.LongConfigValue;
import it.branduzzo.nextbedwars.blockdisplays.file.config.value.StringMessagesValue;
import it.branduzzo.nextbedwars.blockdisplays.listener.block.BlockBreakListener;
import it.branduzzo.nextbedwars.blockdisplays.listener.block.BlockExplodeListener;
import it.branduzzo.nextbedwars.blockdisplays.listener.block.BlockPlaceListener;
import it.branduzzo.nextbedwars.blockdisplays.listener.block.piston.BlockPistonExtendListener;
import it.branduzzo.nextbedwars.blockdisplays.listener.block.piston.BlockPistonRetractListener;
import it.branduzzo.nextbedwars.blockdisplays.listener.entity.EntityDamageByEntityListener;
import it.branduzzo.nextbedwars.blockdisplays.listener.entity.EntityExplodeListener;
import it.branduzzo.nextbedwars.blockdisplays.listener.player.PlayerInteractEntityListener;
import it.branduzzo.nextbedwars.blockdisplays.listener.player.PlayerInteractListener;
import it.branduzzo.nextbedwars.blockdisplays.listener.skyblock.superior.SuperiorSkyblockPluginInitializeListener;
import it.branduzzo.nextbedwars.blockdisplays.service.CustomBlockServiceManager;
import it.branduzzo.nextbedwars.blockdisplays.service.ServiceManager;
import it.branduzzo.nextbedwars.blockdisplays.bdengine.BDEAnimationRegistry;
import it.branduzzo.nextbedwars.blockdisplays.compat.HorizonSupport;
import it.branduzzo.nextbedwars.blockdisplays.sound.SimplePlayableSound;
import it.branduzzo.nextbedwars.blockdisplays.util.ChatUtil;
import it.branduzzo.nextbedwars.blockdisplays.version.VersionManager;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

/**
 * Main entry point of the NextBedWars blocks plugin.
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class BlockDisplayCreator {
    /**
     * Singleton instance of the plugin.
     */
    @Getter
    private static BlockDisplayCreator instance;

    private static JavaPlugin host;

    public static JavaPlugin getHost() {
        return host;
    }

    public static void bootstrap(JavaPlugin plugin) {
        host = plugin;
    }

    public static void init(JavaPlugin plugin) {
        host = plugin;
        instance = new BlockDisplayCreator();
        instance.load();
        instance.enable();
    }

    public static void shutdown() {
        if (instance != null) {
            instance.disable();
            instance = null;
        }
    }

    public JavaPlugin getPlugin() {
        return host;
    }

    public java.io.File getDataFolder() {
        return host.getDataFolder();
    }

    public org.bukkit.Server getServer() {
        return host.getServer();
    }

    public String getName() {
        return host.getName();
    }

    public java.io.InputStream getResource(String path) {
        return host.getResource(path);
    }

    public org.bukkit.plugin.PluginDescriptionFile getDescription() {
        return host.getDescription();
    }

    public java.util.logging.Logger getLogger() {
        return host.getLogger();
    }

    /**
     * Handles integration with external plugins and libraries.
     */
    BDCDependentPluginsManager dependentPluginsManager;

    /**
     * Manages registered custom block services.
     */
    ServiceManager<String, CustomBlockService> servicesManager;
    /**
     * Provides operations with custom blocks.
     */
    CustomBlockService customBlockService;

    /**
     * Main configuration file (config.yml).
     */
    YamlConfigFile yamlConfiguration;
    /**
     * Messages configuration file (messages.yml).
     */
    YamlConfigFile messagesFile;

    /**
     * Root command (/bdc or /blockdisplaycreator).
     */
    /**
     * Simplified specialized command for giving custom blocks (/cb or /custom-block).
     */

    /**
     * Detects and validates the running Minecraft server version.
     */
    VersionManager versionManager;
    /**
     * Indicates if CommandAPI is supported on the current server version.
     */

    /**
     * Collects anonymous plugin usage statistics.
     */


    /**
     * Invoked during the plugin load phase, before the server has fully started.
     * <p>
     * Responsibilities:
     * <ul>
     *   <li>Initializes the {@link BDCDependentPluginsManager} for handling
     *       integrations with external plugins and libraries.</li>
     *   <li>Sets up {@link VersionManager} and determines whether CommandAPI
     *       can be used based on the server version.</li>
     *   <li>Prepares CommandAPI if supported, otherwise logs a fallback
     *       to legacy command handling.</li>
     *   <li>Registers WorldGuard flags and loads PacketEvents.</li>
     * </ul>
     * </p>
     */
    public void load() {
        this.dependentPluginsManager = new BDCDependentPluginsManager(getHost());

        this.versionManager = new VersionManager(this.getServer());

        ChatUtil.log("&6[NextBedWars] &eCustom blocks attivi (comandi legacy).");

        this.dependentPluginsManager.loadPacketEvents();

        this.dependentPluginsManager.registerWorldGuardFlags();
    }

    /**
     * Invoked when the plugin is enabled and ready to operate.
     * <p>
     * Responsibilities:
     * <ul>
     *   <li>Sets the static plugin instance reference.</li>
     *   <li>Initializes and registers the {@link CustomBlockService} via
     *       {@link ServiceManager}.</li>
     *   <li>Registers commands either via CommandAPI (modern versions)
     *       or via Bukkit's legacy executor (older versions).</li>
     *   <li>Registers all data adapters and event listeners.</li>
     *   <li>Loads and reloads configuration files (config.yml, messages.yml,
     *       and default custom block - barrel.yml).</li>
     *   <li>Initializes plugin metrics and performs an update check
     *       against SpigotMC.</li>
     * </ul>
     * </p>
     */
    public void enable() {
        instance = this;
        this.dependentPluginsManager.initPacketEvents();

        this.customBlockService = new BDCCustomBlockService(new BDCCustomBlockConfigStorage(getHost()));
        this.servicesManager = new CustomBlockServiceManager();
        this.servicesManager.registerService(this.customBlockService);

        getHost().getCommand("blockdisplaycreator").setExecutor(new BlockDisplayCreatorSpigotCommand(this, this.customBlockService));
        getHost().getCommand("cb").setExecutor(new CustomBlockGiveSpigotCommand(this.customBlockService));

        registerDataAdapters();

        this.yamlConfiguration = new YamlConfigFile(
                Path.of(getDataFolder().getPath(), "displays-config.yml"),
                true,
                this.getResource("displays-config.yml")
        );
        this.messagesFile = new YamlConfigFile(
                Path.of(getDataFolder().getPath(), "displays-messages.yml"),
                true,
                this.getResource("displays-messages.yml")
        );
        new YamlConfigFile(
                Path.of(getDataFolder().getPath(), "custom-blocks", "barrel.yml"),
                true,
                this.getResource("custom-blocks/barrel.yml")
        );

        reloadConfig();
        registerEvents();
        getServer().getMessenger().registerOutgoingPluginChannel(getHost(), "bungeecord:main");
        getServer().getMessenger().registerOutgoingPluginChannel(getHost(), "velocity:main");
        getServer().getMessenger().registerOutgoingPluginChannel(getHost(), "velocity:player_command");
        getServer().getMessenger().registerOutgoingPluginChannel(getHost(), "bblockdisplays:proxy");
        BDEAnimationRegistry.load(getHost());
        if (HorizonSupport.isHorizon()) ChatUtil.log("&a[Horizon] Detected Horizon " + HorizonSupport.getHorizonVersion() + " - mixin support enabled");

    }

    /**
     * Registers all plugin listeners to the Bukkit {@link PluginManager}.
     * <p>
     * Listeners cover player interactions, block placement and destruction,
     * entity damage, piston mechanics, and compatibility hooks with
     * other plugins (e.g. SuperiorSkyblock).
     * </p>
     */
    private void registerEvents() {
        PluginManager pluginManager = getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerInteractListener(this.servicesManager), getHost());
        pluginManager.registerEvents(new EntityDamageByEntityListener(this.servicesManager), getHost());
        pluginManager.registerEvents(new BlockPlaceListener(), getHost());
        pluginManager.registerEvents(new PlayerInteractEntityListener(this.servicesManager), getHost());
        pluginManager.registerEvents(new BlockBreakListener(this.servicesManager), getHost());
        pluginManager.registerEvents(new BlockExplodeListener(), getHost());
        pluginManager.registerEvents(new EntityExplodeListener(), getHost());
        pluginManager.registerEvents(new BlockPistonExtendListener(), getHost());
        pluginManager.registerEvents(new BlockPistonRetractListener(), getHost());
        if (this.dependentPluginsManager.isSuperiorSkyblockAvailable()) {
            pluginManager.registerEvents(new SuperiorSkyblockPluginInitializeListener(), getHost());
        }
        //pluginManager.registerEvents(new PrepareItemCraftListener(), getHost());
    }

    /**
     * Registers data adapters for both Persistent Data Container (PDC)
     * and YAML-based storage.
     */
    private void registerDataAdapters() {
        ConfigurationSerialization.registerClass(SimplePlayableSound.class);

        PersistentDataTypeManager persistentStore = PersistentData.getPersistentDataTypeStore();
        persistentStore.register(TypeTokens.UUID, PersistentDataTypes.UUID);
        persistentStore.register(TypeTokens.CLASS, PersistentDataTypes.CLASS);
        persistentStore.register(TypeTokens.UUID_ARRAY, PersistentDataTypes.UUID_ARRAY);
        persistentStore.register(TypeTokens.LOCATION, PersistentDataTypes.LOCATION);
        persistentStore.register(TypeTokens.CUSTOM_BLOCK_ROTATION, PersistentDataTypes.CUSTOM_BLOCK_ROTATION);
        persistentStore.register(TypeTokens.COMMAND_ARRAY, PersistentDataTypes.COMMAND_ARRAY);


        PersistentDataTypeManager yamlStore = YamlData.getPersistentDataTypeStore();
        yamlStore.register(TypeTokens.ITEM_FLAG, PersistentDataTypes.ITEM_FLAG);
        yamlStore.register(TypeTokens.ITEM_FLAG_LIST, PersistentDataTypes.ITEM_FLAG_LIST);
        yamlStore.register(TypeTokens.PLAYABLE_SOUND, PersistentDataTypes.PLAYABLE_SOUND);
        yamlStore.register(TypeTokens.COMMAND_LIST, PersistentDataTypes.COMMAND_LIST);
        yamlStore.register(TypeTokens.ENCHANT_MAP, PersistentDataTypes.ENCHANT);
        yamlStore.register(TypeTokens.BILLBOARD, PersistentDataTypes.BILLBOARD);
        yamlStore.register(TypeTokens.BRIGHTNESS, PersistentDataTypes.BRIGHTNESS);
        yamlStore.register(TypeTokens.MATERIAL, PersistentDataTypes.MATERIAL);
        yamlStore.register(TypeTokens.SOUND, PersistentDataTypes.SOUND);
        yamlStore.register(TypeTokens.COMMAND_SOURCE, PersistentDataTypes.COMMAND_SOURCE);
        yamlStore.register(TypeTokens.COMMAND_BUNDLE, PersistentDataTypes.COMMAND_BUNDLE);
        yamlStore.register(TypeTokens.DYE_COLOR, PersistentDataTypes.DYE_COLOR);
        yamlStore.register(TypeTokens.DIRECTED_VECTOR, PersistentDataTypes.DIRECTED_VECTOR);
        yamlStore.register(TypeTokens.CUSTOM_BLOCK_PLACEMENT_MODE, PersistentDataTypes.CUSTOM_BLOCK_PLACEMENT_MODE);
        yamlStore.register(TypeTokens.CUSTOM_BLOCK_DROP_MODE, PersistentDataTypes.CUSTOM_BLOCK_DROP_MODE);
    }

    /**
     * Reloads all configuration files used by the plugin.
     * <p>
     * This includes the primary {@code config.yml}, the
     * {@code messages.yml}, and all custom block configurations.
     * It also re-initializes configuration-dependent values in memory.
     * </p>
     *
     * @see #initializeConfigValues()
     */
    public void reloadConfig() {
        this.yamlConfiguration.reload();
        this.messagesFile.reload();
        this.customBlockService.getStorage().reloadAll();
        this.initializeConfigValues();
        BDEAnimationRegistry.load(getHost());
    }

    /**
     * Initializes static, strongly typed configuration values.
     * <p>
     * These values (boolean, long, and message strings) are exposed
     * via static fields, allowing global access to immutable
     * configuration data throughout the plugin.
     * </p>
     */
    public void initializeConfigValues() {
        new BooleanConfigValue().initialize(new YamlData<>(this.getYamlConfiguration(), TypeTokens.BOOLEAN));
        new LongConfigValue().initialize(new YamlData<>(this.getYamlConfiguration(), TypeTokens.LONG));
        new StringMessagesValue().initialize(new YamlData<>(this.getMessagesFile(), TypeTokens.STRING));
    }

    /**
     * Checks whether a newer version of the plugin is available on <a href="https://www.spigotmc.org/resources/blockdisplaycreator.114877/">SpigotMC</a>.
     * <p>
     * If a newer release is found, a notification message is sent to
     * the console.
     * </p>
     */
    /**
     * Called when the plugin is disabled.
     * <p>
     * Performs clean shutdown operations, such as disabling
     * CommandAPI (if active) and terminating packet event handlers.
     * </p>
     */
    public void disable() {
        this.dependentPluginsManager.terminatePacketEvents();
        try { it.branduzzo.nextbedwars.blockdisplays.bdengine.BDEAnimationPlayer.clearAll(); } catch (Exception ignored) {}
        try { if (this.customBlockService instanceof it.branduzzo.nextbedwars.blockdisplays.custom.block.BDCCustomBlockService s) { s.clearCache(); } } catch (Exception ignored) {}
    }
}
