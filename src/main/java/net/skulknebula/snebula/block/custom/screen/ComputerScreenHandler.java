package net.skulknebula.snebula.block.custom.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.skulknebula.snebula.block.custom.ComputerBlockEntity;
import net.skulknebula.snebula.screen.ModScreenHandlers;

public class ComputerScreenHandler extends ScreenHandler {
    private final BlockPos computerPos;

    public ComputerScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, BlockPos.ORIGIN);
    }

    public ComputerScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos computerPos) {
        super(ModScreenHandlers.COMPUTER_SCREEN_HANDLER, syncId);
        this.computerPos = computerPos;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public void clearComputerUser(PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            var world = serverPlayer.getEntityWorld();
            if (world != null) {
                var be = world.getBlockEntity(computerPos);
                if (be instanceof ComputerBlockEntity computerEntity) {
                    if (computerEntity.getCurrentUserUUID() != null &&
                            computerEntity.getCurrentUserUUID().equals(player.getUuid())) {
                        computerEntity.clearUser();
                    }
                }
            }
        }
    }
}