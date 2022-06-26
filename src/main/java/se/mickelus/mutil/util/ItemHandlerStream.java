package se.mickelus.mutil.util;

import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class ItemHandlerStream {
    public static Stream<ItemStack> of(BlockGetter world, BlockPos pos) {
        return of(world.getBlockEntity(pos));
    }

    public static Stream<ItemStack> of(@Nullable BlockEntity tileEntity) {
        List<ItemStack> stacks = new ArrayList<>();
        Storage<ItemVariant> storage = ItemStorage.SIDED.find(tileEntity.getLevel(), tileEntity.getBlockPos(), Direction.NORTH);
        if(storage == null || storage.equals(Storage.empty()))
            return Stream.empty();
        storage.iterable(Transaction.isOpen() ? Transaction.getCurrentUnsafe() : Transaction.openOuter())
                .forEach(itemVariantStorageView -> {
                    ItemStack stack = itemVariantStorageView.getResource().toStack();
                    stack.setCount((int) itemVariantStorageView.getAmount());
                    stacks.add(stack);
                });
        return stacks.stream();
    }
}
