package it.branduzzo.nextbedwars.blockdisplays.custom.block;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import it.branduzzo.nextbedwars.blockdisplays.BlockDisplayCreator;
import it.branduzzo.nextbedwars.blockdisplays.file.config.loader.CustomBlockConfigurationFile;
import it.branduzzo.nextbedwars.blockdisplays.file.config.loader.CustomBlockFileRepository;
import it.branduzzo.nextbedwars.blockdisplays.file.config.loader.CustomBlockRepository;
import it.branduzzo.nextbedwars.blockdisplays.file.exception.CustomBlockLoadException;
import it.branduzzo.nextbedwars.blockdisplays.bdengine.BDEAnimationRegistry;
import it.branduzzo.nextbedwars.blockdisplays.util.ChatUtil;
import it.branduzzo.nextbedwars.blockdisplays.util.FoliaScheduler;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BDCCustomBlockConfigStorage implements CustomBlockStorage {
    volatile List<AbstractCustomBlock> abstractCustomBlocks;
    final Map<String, AbstractCustomBlock> blockCache = new ConcurrentHashMap<>();
    CustomBlockRepository customBlockRepository;
    final JavaPlugin plugin;

    public BDCCustomBlockConfigStorage(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.abstractCustomBlocks = new ArrayList<>();
    }

    @Override
    public void reloadAll(@Nullable Runnable onComplete) {
        FoliaScheduler.runAsync(this.plugin, () -> {
            ChatUtil.log("&6[NextBedWars blocks] &eInitializing blocks...");

            CustomBlockRepository tempCustomBlockRepository = new CustomBlockFileRepository(this.plugin);
            List<AbstractCustomBlock> tempAbstractCustomBlocks = java.util.Collections.synchronizedList(new ArrayList<>());

            tempCustomBlockRepository.getFiles().parallelStream().forEach(configurationFile -> {
                try {
                    tempAbstractCustomBlocks.add(configurationFile.getAbstractCustomBlock());
                } catch (CustomBlockLoadException e) {
                    ChatUtil.log(e.getMessage());
                }
            });

            ChatUtil.log("&6[NextBedWars blocks] &eInitialization complete, %d blocks initialized", tempAbstractCustomBlocks.size());

            FoliaScheduler.runGlobal(this.plugin, () -> {
                this.customBlockRepository = tempCustomBlockRepository;
                this.abstractCustomBlocks = List.copyOf(tempAbstractCustomBlocks);
                this.blockCache.clear();
                for (AbstractCustomBlock b : tempAbstractCustomBlocks) this.blockCache.put(b.getName(), b);
                BDEAnimationRegistry.load(this.plugin);

                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
    }

    @Override
    public void reload(@NotNull String name, @Nullable Runnable onComplete) throws NoSuchElementException {
        if (!this.blockCache.containsKey(name) && !containsAbstractCustomBlock(name)) {
            throw new NoSuchElementException("Block \"" + name + "\" is not in storage");
        }

        FoliaScheduler.runAsync(this.plugin, () -> {
            AbstractCustomBlock reloaded = null;
            try {
                CustomBlockConfigurationFile file = this.customBlockRepository.getFile(name);
                file.reload();
                reloaded = file.getAbstractCustomBlock();
            } catch (CustomBlockLoadException | IllegalArgumentException e) {
                ChatUtil.log("&c" + e.getMessage());
            }
            AbstractCustomBlock finalReloaded = reloaded;
            FoliaScheduler.runGlobal(this.plugin, () -> {
                if (finalReloaded != null) {
                    this.blockCache.put(name, finalReloaded);
                    List<AbstractCustomBlock> copy = new ArrayList<>(this.abstractCustomBlocks);
                    copy.removeIf(b -> b.getName().equals(name));
                    copy.add(finalReloaded);
                    this.abstractCustomBlocks = List.copyOf(copy);
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        });
    }

    @Override
    public void addAbstractCustomBlock(AbstractCustomBlock abstractCustomBlock) {
        if (this.blockCache.containsKey(abstractCustomBlock.getName())) {
            throw new IllegalStateException("Block named " + abstractCustomBlock.getName() + " already exists in storage");
        }
        this.blockCache.put(abstractCustomBlock.getName(), abstractCustomBlock);
        List<AbstractCustomBlock> copy = new ArrayList<>(this.abstractCustomBlocks);
        copy.add(abstractCustomBlock);
        this.abstractCustomBlocks = List.copyOf(copy);
    }

    @Override
    public void removeAbstractCustomBlock(String name) throws NoSuchElementException {
        if (this.blockCache.remove(name) == null) {
            throw new NoSuchElementException("Block named " + name + " does not exists in storage");
        }
        List<AbstractCustomBlock> copy = new ArrayList<>(this.abstractCustomBlocks);
        copy.removeIf(b -> b.getName().equals(name));
        this.abstractCustomBlocks = List.copyOf(copy);
    }

    @Override
    @Nullable
    public AbstractCustomBlock getAbstractCustomBlock(@NotNull String name, Object... data) {
        AbstractCustomBlock cached = this.blockCache.get(name);
        if (cached == null) return null;
        AbstractCustomBlock cloned = cached.clone();
        cloned.getItem().setAmount(1);
        return cloned;
    }

    @Override
    public boolean containsAbstractCustomBlock(@NotNull String name) {
        return this.blockCache.containsKey(name);
    }

    @NotNull
    public List<AbstractCustomBlock> getAbstractCustomBlocks() {
        return abstractCustomBlocks.stream()
                .map(AbstractCustomBlock::clone)
                .toList();
    }
}
