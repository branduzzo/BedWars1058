package it.branduzzo.nextbedwars.blockdisplays.listener.inventory;

import it.branduzzo.nextbedwars.blockdisplays.custom.block.CustomBlockKey;
import it.branduzzo.nextbedwars.blockdisplays.data.manager.TypeTokens;
import it.branduzzo.nextbedwars.blockdisplays.data.persistent.PersistentData;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public class PrepareItemCraftListener implements Listener {

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();

        if (result == null)
            return;

        for (ItemStack itemStack : event.getInventory().getMatrix()) {

            if (itemStack == null)
                continue;

            if (itemStack.getItemMeta() == null)
                continue;

            PersistentData<String> persistentData = new PersistentData<>(itemStack.getItemMeta(), TypeTokens.STRING);

            if (persistentData.has(CustomBlockKey.NAME))
                continue;

            event.getInventory().setResult(new ItemStack(Material.AIR));

            return;
        }
    }
}
