package net.skulknebula.snebula.block.custom;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.Map;

public class MicroscopeBlockRenderer extends GeoBlockRenderer<MicroscopeBlockEntity, MicroscopeBlockRenderer.MicroscopeBlockRendererState> {

    public MicroscopeBlockRenderer(BlockEntityType<? extends MicroscopeBlockEntity> blockEntityType) {
        super(new MicroscopeBlockModel());  // <-- ВАЖНО! Используем кастомную модель
    }

    public static class MicroscopeBlockRendererState extends BlockEntityRenderState implements GeoRenderState {
        @Override
        public Map<DataTicket<?>, Object> getDataMap() {
            return Map.of();
        }
    }
}