package dev.sapphic.armorsoundtweak;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.TickEvent;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

abstract class EquipmentTicker implements Consumer<TickEvent.ClientTickEvent> {
  private List<ItemStack> oldEquipment = Collections.emptyList();

  @Override
  public void accept(final TickEvent.ClientTickEvent event) {
    final Minecraft client = Minecraft.getInstance();

    if ((event.phase == TickEvent.Phase.START) && (client.player != null)) {
      if ((client.player.world != null) && client.player.world.isRemote) {
        final List<ItemStack> equipment = this.getEquipment(client.player);

        if (client.currentScreen instanceof ContainerScreen<?>) {
          final Iterator<ItemStack> newEquipment = equipment.iterator();
          final Iterator<ItemStack> oldEquipment = this.oldEquipment.iterator();

          while (oldEquipment.hasNext() && newEquipment.hasNext()) {
            final ItemStack newItem = newEquipment.next();
            final ItemStack oldItem = oldEquipment.next();

            if (!ItemStack.areItemStacksEqual(newItem, oldItem)) {
              this.playEquipSound(client.player, (newItem.isEmpty() ? oldItem : newItem).getItem());
            }
          }
        }

        this.oldEquipment = equipment;
      }
    }
  }

  protected abstract List<ItemStack> getEquipment(final PlayerEntity player);

  protected abstract void playEquipSound(final PlayerEntity player, final Item item);
}
