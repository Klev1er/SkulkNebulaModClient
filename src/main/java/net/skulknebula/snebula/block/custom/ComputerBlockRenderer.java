package net.skulknebula.snebula.block.custom;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import software.bernie.geckolib.constant.dataticket.DataTicket;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.base.GeoRenderState;

import java.util.Map;

public class ComputerBlockRenderer extends GeoBlockRenderer<ComputerBlockEntity, ComputerBlockRenderer.ComputerBlockRendererState> {

    // Передаём НАШУ модель ServerBlockModel!
    public ComputerBlockRenderer(BlockEntityType<? extends ComputerBlockEntity> blockEntityType) {
        super(new ComputerBlockModel());  // <-- ВАЖНО! Используем кастомную модель
    }

    public static class ComputerBlockRendererState extends BlockEntityRenderState implements GeoRenderState {
        @Override
        public Map<DataTicket<?>, Object> getDataMap() {
            return Map.of();
        }
    }
}