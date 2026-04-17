package net.skulknebula.snebula.block.custom;

import net.minecraft.util.Identifier;
import net.skulknebula.snebula.SkulkNebulaMod;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ComputerBlockModel extends DefaultedBlockGeoModel<ComputerBlockEntity> {

    public ComputerBlockModel() {
        super(Identifier.of(SkulkNebulaMod.MOD_ID, "computer"));
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return buildFormattedModelPath(
                Identifier.of(SkulkNebulaMod.MOD_ID, "computer")
        );
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return buildFormattedTexturePath(
                Identifier.of(SkulkNebulaMod.MOD_ID, "computer_texture")
        );
    }

    @Override
    public Identifier getAnimationResource(ComputerBlockEntity animatable) {
        return buildFormattedAnimationPath(
                Identifier.of(SkulkNebulaMod.MOD_ID, "computer_block_entity")
        );
    }
}