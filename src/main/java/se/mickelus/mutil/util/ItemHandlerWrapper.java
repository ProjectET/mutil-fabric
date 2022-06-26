package se.mickelus.mutil.util;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ItemHandlerWrapper implements Container {

    protected final InventoryStorage inv;

    public ItemHandlerWrapper(InventoryStorage inv) {
        this.inv = inv;
    }

    /**
     * Returns the size of this inventory.
     */
    @Override
    public int getContainerSize() {
        return inv.getSlots().size();
    }

    /**
     * Returns the stack in this slot.  This stack should be a modifiable reference, not a copy of a stack in your inventory.
     */
    @Override
    public ItemStack getItem(int slot) {
        return inv.getSlot(slot).getResource().toStack();
    }

    /**
     * Attempts to remove n items from the specified slot.  Returns the split stack that was removed.  Modifies the inventory.
     */
    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack stack = getItem(slot);
        return stack.isEmpty() ? ItemStack.EMPTY : stack.split(count);
    }

    /**
     * Sets the contents of this slot to the provided stack.
     */
    @Override
    public void setItem(int slot, ItemStack stack) {
        try(Transaction transaction = Transaction.isOpen() ? Transaction.openNested(Transaction.getCurrentUnsafe()) : Transaction.openOuter()) {
            SingleSlotStorage<ItemVariant> singleSlot = inv.getSlot(slot);
            singleSlot.extract(singleSlot.getResource(), singleSlot.getAmount(), transaction);
            singleSlot.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            inv.getSlots().set(slot, singleSlot);
            transaction.commit();
        }
    }

    /**
     * Removes the stack contained in this slot from the underlying handler, and returns it.
     */
    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack s = getItem(index);
        if(s.isEmpty()) return ItemStack.EMPTY;
        setItem(index, ItemStack.EMPTY);
        return s;
    }

    @Override
    public boolean isEmpty() {
        return inv.getSlots().isEmpty();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return inv.simulateInsert(ItemVariant.of(stack), stack.getCount(), null) == stack.getCount();
    }

    @Override
    public void clearContent() {
        try(Transaction transaction = Transaction.isOpen() ? Transaction.openNested(Transaction.getCurrentUnsafe()) : Transaction.openOuter()) {
            for (StorageView<ItemVariant> storageView : inv.getSlots()) {
                storageView.extract(storageView.getResource(), storageView.getAmount(), transaction);
            }
            transaction.commit();
        }
    }

    //The following methods are never used by vanilla in crafting.  They are defunct as mods need not override them.
    @Override
    public int getMaxStackSize() { return 0; }
    @Override
    public void setChanged() {}
    @Override
    public boolean stillValid(Player player) { return false; }
    @Override
    public void startOpen(Player player) {}
    @Override
    public void stopOpen(Player player) {}
}
