package se.mickelus.mutil.gui;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class ToggleableSlot extends Slot {

    private boolean isEnabled = true;
    private int realX, realY;

    public ToggleableSlot(Container container, int index, int xPosition, int yPosition) {
        super(container, index, xPosition, yPosition);

        realX = xPosition;
        realY = yPosition;
    }

    public void toggle(boolean enabled) {
        isEnabled = enabled;
    }

    @Override
    public boolean isActive() {
        return isEnabled;
    }

    @Override
    public boolean mayPickup(Player playerIn) {
        return isEnabled;
    }

    @Override
    public boolean mayPlace(@Nullable ItemStack stack) {
        return isEnabled;
    }
}
