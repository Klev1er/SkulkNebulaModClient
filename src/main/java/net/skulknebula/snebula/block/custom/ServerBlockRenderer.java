package net.skulknebula.snebula.block.custom;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;
import software.bernie.geckolib.model.GeoModel;

import java.util.Map;

public class ServerBlockRenderer extends GeoBlockRenderer<ServerBlockEntity, ServerBlockRenderer.ServerBlockRenderState> {

    // Передаём НАШУ модель ServerBlockModel!
    public ServerBlockRenderer(BlockEntityType<? extends ServerBlockEntity> blockEntityType) {
        super(new ServerBlockModel());  // <-- ВАЖНО! Используем кастомную модель
    }

    public static class ServerBlockRenderState extends BlockEntityRenderState implements GeoRenderState {
        @Override
        public Map<DataTicket<?>, Object> getDataMap() {
            return Map.of();
        }
    }
}