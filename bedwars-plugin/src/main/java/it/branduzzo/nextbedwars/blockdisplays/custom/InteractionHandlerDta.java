package it.branduzzo.nextbedwars.blockdisplays.custom;

import lombok.*;
import lombok.experimental.FieldDefaults;
import it.branduzzo.nextbedwars.blockdisplays.common.Cooldown;
import it.branduzzo.nextbedwars.blockdisplays.common.DeepCloneable;
import it.branduzzo.nextbedwars.blockdisplays.placeholder.universal.UniversalPlaceholder;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InteractionHandlerDta implements InteractionHandler, DeepCloneable<InteractionHandlerDta> {
    CommandBundle commandBundle;
    Cooldown<UUID> cooldown;

    public InteractionHandlerDta(CommandBundle commandBundle, int cooldown) {
        this.commandBundle = commandBundle;
        this.cooldown = new Cooldown<>(cooldown);
    }

    public InteractionHandlerDta(CommandBundle commandBundle) {
        this(commandBundle, 0);
    }

    @Override
    public void handleClick(@NotNull Interaction interaction, @NotNull Player player, UniversalPlaceholder<?>... placeholders) {
        UUID uuid = player.getUniqueId();

        if (cooldown.check(uuid)) {

            if (commandBundle == null) {
                return;
            }

            commandBundle.execute(player, placeholders);
        }
    }

    @Override
    public long getCooldown() {
        return cooldown.getCooldownValue();
    }

    @Override
    public InteractionHandlerDta clone() {
        return new InteractionHandlerDta(this.commandBundle, this.cooldown);
    }
}
