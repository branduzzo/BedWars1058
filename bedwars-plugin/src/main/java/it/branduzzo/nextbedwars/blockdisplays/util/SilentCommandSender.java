package it.branduzzo.nextbedwars.blockdisplays.util;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public final class SilentCommandSender implements CommandSender {

    private static final SilentCommandSender INSTANCE = new SilentCommandSender();

    public static CommandSender get() {
        return INSTANCE;
    }

    private final CommandSender delegate = Bukkit.getConsoleSender();

    @Override
    public void sendMessage(@NotNull String message) {
    }

    @Override
    public void sendMessage(@NotNull String... messages) {
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String message) {
    }

    @Override
    public void sendMessage(@Nullable UUID sender, @NotNull String... messages) {
    }

    @Override
    public void sendMessage(@NotNull Component message) {
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull net.kyori.adventure.audience.MessageType type) {
    }

    @Override
    public @NotNull String getName() {
        return "NextBedWars";
    }

    @Override
    public @NotNull Component name() {
        return Component.text("NextBedWars");
    }

    @Override
    public @NotNull org.bukkit.Server getServer() {
        return delegate.getServer();
    }

    @Override
    public boolean isPermissionSet(@NotNull String name) {
        return delegate.isPermissionSet(name);
    }

    @Override
    public boolean isPermissionSet(@NotNull Permission perm) {
        return delegate.isPermissionSet(perm);
    }

    @Override
    public boolean hasPermission(@NotNull String name) {
        return delegate.hasPermission(name);
    }

    @Override
    public boolean hasPermission(@NotNull Permission perm) {
        return delegate.hasPermission(perm);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        return delegate.addAttachment(plugin, name, value);
    }

    @Override
    public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return delegate.addAttachment(plugin);
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        return delegate.addAttachment(plugin, name, value, ticks);
    }

    @Override
    public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return delegate.addAttachment(plugin, ticks);
    }

    @Override
    public void removeAttachment(@NotNull PermissionAttachment attachment) {
        delegate.removeAttachment(attachment);
    }

    @Override
    public void recalculatePermissions() {
        delegate.recalculatePermissions();
    }

    @Override
    public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return delegate.getEffectivePermissions();
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
    }

    @Override
    public @NotNull CommandSender.Spigot spigot() {
        return delegate.spigot();
    }

}
