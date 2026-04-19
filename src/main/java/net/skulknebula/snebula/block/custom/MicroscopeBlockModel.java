package net.skulknebula.snebula.block.custom; // Убедись, что пакет правильный!

import net.minecraft.util.Identifier;
import net.skulknebula.snebula.SkulkNebulaMod;
// ИМПОРТИРУЕМ ПРАВИЛЬНЫЙ БЛОК ЭНТИТИ!
import net.skulknebula.snebula.block.custom.MicroscopeBlockEntity;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

// МЕНЯЕМ ComputerBlockEntity НА MicroscopeBlockEntity
public class MicroscopeBlockModel extends DefaultedBlockGeoModel<MicroscopeBlockEntity> {

    public MicroscopeBlockModel() {
        super(Identifier.of(SkulkNebulaMod.MOD_ID, "microscope"));
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        // Берет файл: geckolib/models/block/microscope.geo.json
        return buildFormattedModelPath(Identifier.of(SkulkNebulaMod.MOD_ID, "microscope"));
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        // Берет файл: textures/block/microscope_texture.png (убедись, что он так называется!)
        return buildFormattedTexturePath(Identifier.of(SkulkNebulaMod.MOD_ID, "microscope_texture"));
    }

    @Override
    public Identifier getAnimationResource(MicroscopeBlockEntity animatable) {
        // Берет файл: geckolib/animations/block/microscope_block_entity.animation.json
        return buildFormattedAnimationPath(Identifier.of(SkulkNebulaMod.MOD_ID, "microscope_block_entity"));
    }
}