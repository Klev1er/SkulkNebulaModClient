package net.skulknebula.snebula.block;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.skulknebula.snebula.SkulkNebulaMod;
import net.skulknebula.snebula.block.custom.*;

import java.util.function.Function;

public class ModBlocks {
    public static Block SERVER_BLOCK;
    public static Block COMPUTER_BLOCK;
    public static Block MICROSCOPE_BLOCK;
    public static Block SERVER_EXTENSION_CENTER_BLOCK;
    public static Block SERVER_EXTENSION_UP_BLOCK;

    public static void registerModBlocks() {
        SERVER_BLOCK = registerBlock("server_block",
                properties -> new ServerBlock(properties
                        .strength(24.0f)
                        .requiresTool()
                        .nonOpaque()
                        .luminance(state -> 2)));
        COMPUTER_BLOCK = registerBlock("computer",
                properties -> new ComputerBlock(properties
                        .strength(4.0f)
                        .requiresTool()
                        .luminance(state -> 1)));
        MICROSCOPE_BLOCK = registerBlock("microscope",
                properties -> new MicroscopeBlock(properties
                        .strength(2.0f)
                        .nonOpaque()));
        SERVER_EXTENSION_CENTER_BLOCK = registerBlock("server_extension_center",
                properties -> new ServerExtensionCenterBlock(properties
                        .strength(24.0f)
                        .noBlockBreakParticles()
                        .nonOpaque()
                        .solid()));
        SERVER_EXTENSION_UP_BLOCK = registerBlock("server_extension_up",
                properties -> new ServerExtensionUpBlock(properties
                        .strength(24.0f)
                        .noBlockBreakParticles()
                        .nonOpaque()
                        .solid()));

        SkulkNebulaMod.LOGGER.info("Registering ModBlocks for " + SkulkNebulaMod.MOD_ID);
    }

    private static Block registerBlock(String name, Function<AbstractBlock.Settings, Block> factory) {
        RegistryKey<Block> key = RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(SkulkNebulaMod.MOD_ID, name));
        Block block = factory.apply(AbstractBlock.Settings.create().registryKey(key));
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, key, block);
    }

    private static void registerBlockItem(String name, Block block) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(SkulkNebulaMod.MOD_ID, name));
        Registry.register(Registries.ITEM, key, new BlockItem(block, new Item.Settings().registryKey(key)));
    }
}