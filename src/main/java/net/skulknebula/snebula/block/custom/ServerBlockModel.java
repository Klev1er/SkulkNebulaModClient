package net.skulknebula.snebula.block.custom;

import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.skulknebula.snebula.SkulkNebulaMod;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.DefaultedBlockGeoModel;
import software.bernie.geckolib.renderer.base.GeoRenderState;

public class ServerBlockModel extends DefaultedBlockGeoModel<ServerBlockEntity> {

    public ServerBlockModel() {
        super(Identifier.of(SkulkNebulaMod.MOD_ID, "server_block_entity"));
    }

    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        if (renderState != null) {
            BlockState blockState = renderState.getGeckolibData(DataTickets.BLOCKSTATE);
            if (blockState != null) {
                int tier = blockState.get(ServerBlock.TIER);
                return buildFormattedModelPath(
                        Identifier.of(SkulkNebulaMod.MOD_ID, "server_block_" + tier)
                );
            }
        }
        return buildFormattedModelPath(
                Identifier.of(SkulkNebulaMod.MOD_ID, "server_block_0")
        );
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        if (renderState != null) {
            BlockState blockState = renderState.getGeckolibData(DataTickets.BLOCKSTATE);
            if (blockState != null && blockState.contains(ServerBlock.BROKEN)) {
                boolean isBroken = blockState.get(ServerBlock.BROKEN);

                if (isBroken) {
                    return buildFormattedTexturePath(
                            Identifier.of(SkulkNebulaMod.MOD_ID, "server_block_texture_broken")
                    );
                }
            }
        }

        return buildFormattedTexturePath(
                Identifier.of(SkulkNebulaMod.MOD_ID, "server_block_texture")
        );
    }

    @Override
    public Identifier getAnimationResource(ServerBlockEntity animatable) {
        return buildFormattedAnimationPath(
                Identifier.of(SkulkNebulaMod.MOD_ID, "server_block_entity")
        );
    }
}