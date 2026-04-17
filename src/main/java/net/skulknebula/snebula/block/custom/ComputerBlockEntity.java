package net.skulknebula.snebula.block.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.skulknebula.snebula.block.ModBlockEntities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animatable.manager.AnimatableManager;

import java.util.UUID;

public class ComputerBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation");

    @Nullable
    private UUID currentUserUUID = null;

    public ComputerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.COMPUTER_BLOCK_ENTITY, pos, state);
    }

    public boolean isOccupied() {
        return currentUserUUID != null;
    }

    public void setCurrentUser(@Nullable PlayerEntity player) {
        if (player != null) {
            this.currentUserUUID = player.getUuid();
        } else {
            this.currentUserUUID = null;
        }
        markDirty();
    }

    public void clearUser() {
        this.currentUserUUID = null;
        markDirty();
    }

    @Nullable
    public UUID getCurrentUserUUID() {
        return currentUserUUID;
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        view.getOptionalString("CurrentUser").ifPresent(uuidString -> {
            try {
                this.currentUserUUID = UUID.fromString(uuidString);
            } catch (IllegalArgumentException e) {
                this.currentUserUUID = null;
            }
        });
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        if (currentUserUUID != null) {
            view.putString("CurrentUser", currentUserUUID.toString());
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>("Idle",
                state -> state.setAndContinue(IDLE)));
    }

    @Override
    public @NotNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}