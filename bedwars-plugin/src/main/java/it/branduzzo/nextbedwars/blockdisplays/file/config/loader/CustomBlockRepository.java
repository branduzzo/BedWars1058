package it.branduzzo.nextbedwars.blockdisplays.file.config.loader;

import it.branduzzo.nextbedwars.blockdisplays.custom.block.AbstractCustomBlock;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public interface CustomBlockRepository {
    List<AbstractCustomBlock> getAbstractCustomBlocks();

    @Nullable
    CustomBlockConfigurationFile getFile(String name);

    void addFile(CustomBlockConfigurationFile configurationFile);

    boolean hasFile(String name);

    List<CustomBlockConfigurationFile> getFiles();

    Path getPath();
}
