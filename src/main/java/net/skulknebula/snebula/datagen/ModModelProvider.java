package net.skulknebula.snebula.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Models;
import net.skulknebula.snebula.block.ModBlocks;
import net.skulknebula.snebula.item.ModItems;

public class ModModelProvider extends FabricModelProvider {
    public ModModelProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator blockStateModelGenerator) {
        // GeckoLib блоки не нуждаются в blockstate моделях
        // Оставляем пустым
    }

    @Override
    public void generateItemModels(ItemModelGenerator itemModelGenerator) {
        // Только иконка в инвентаре
        itemModelGenerator.register(ModBlocks.SERVER_BLOCK.asItem(), Models.GENERATED);
        itemModelGenerator.register(ModBlocks.COMPUTER_BLOCK.asItem(), Models.GENERATED);
        itemModelGenerator.register(ModItems.SERVER_CASING, Models.GENERATED);
        itemModelGenerator.register(ModItems.PROCESSOR, Models.GENERATED);
        itemModelGenerator.register(ModItems.COPPER_HARD_DRIVE, Models.GENERATED);
        itemModelGenerator.register(ModItems.PRINTED_CIRCUIT_BOARD, Models.GENERATED);
        itemModelGenerator.register(ModItems.SERVER_UPGRADE, Models.GENERATED);
        itemModelGenerator.register(ModItems.ELECTRICIAN_KIT, Models.GENERATED);
    }
}