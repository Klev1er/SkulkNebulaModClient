package net.skulknebula.snebula.block;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.skulknebula.snebula.SkulkNebulaMod;
import net.skulknebula.snebula.block.custom.ComputerBlockEntity;
import net.skulknebula.snebula.block.custom.ServerBlockEntity;
// Добавляем импорт
import net.skulknebula.snebula.block.custom.MicroscopeBlockEntity;

public class ModBlockEntities {
    public static final BlockEntityType<ServerBlockEntity> SERVER_BLOCK_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of(SkulkNebulaMod.MOD_ID, "server_block_entity"),
                    FabricBlockEntityTypeBuilder.create(ServerBlockEntity::new, ModBlocks.SERVER_BLOCK).build(null)
            );

    public static final BlockEntityType<ComputerBlockEntity> COMPUTER_BLOCK_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of(SkulkNebulaMod.MOD_ID, "computer_block_entity"),
                    FabricBlockEntityTypeBuilder.create(ComputerBlockEntity::new, ModBlocks.COMPUTER_BLOCK).build(null)
            );

    // Добавляем регистрацию микроскопа
    public static final BlockEntityType<MicroscopeBlockEntity> MICROSCOPE_BLOCK_ENTITY =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Identifier.of(SkulkNebulaMod.MOD_ID, "microscope_block_entity"),
                    FabricBlockEntityTypeBuilder.create(MicroscopeBlockEntity::new, ModBlocks.MICROSCOPE_BLOCK).build(null)
            );

    public static void register() {
    }
}