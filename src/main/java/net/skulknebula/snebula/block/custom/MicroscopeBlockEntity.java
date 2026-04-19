package net.skulknebula.snebula.block.custom;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;

public class MicroscopeBlockEntity extends BlockEntity implements GeoBlockEntity {
    // 1. Как у друга: используем SingletonAnimatableInstanceCache
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    // 2. Как у друга: выносим анимацию в статичную константу
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation");

    public MicroscopeBlockEntity(BlockPos pos, BlockState state) {
        // Указываем путь на класс друга
        super(net.skulknebula.snebula.block.ModBlockEntities.MICROSCOPE_BLOCK_ENTITY, pos, state);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        // 3. Как у друга: короткий контроллер без this
        controllerRegistrar.add(new AnimationController<>("Idle",
                state -> state.setAndContinue(IDLE)));
    }

    @Override
    public @NotNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}