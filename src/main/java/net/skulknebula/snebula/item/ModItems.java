package net.skulknebula.snebula.item;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.skulknebula.snebula.SkulkNebulaMod;
import net.skulknebula.snebula.item.custom.ElectricianKitItem;
import net.skulknebula.snebula.item.custom.ServerUpgradeItem;

import java.util.function.Function;

public class ModItems {

    // ========== БАЗОВЫЕ КОМПОНЕНТЫ ==========
    public static final Item PROCESSOR = registerItem("processor", settings ->
            new Item(settings
                    .rarity(Rarity.UNCOMMON)
                    .maxCount(64)));

    public static final Item PRINTED_CIRCUIT_BOARD = registerItem("printed_circuit_board", settings ->
            new Item(settings
                    .rarity(Rarity.COMMON)
                    .maxCount(64)));

    public static final Item COPPER_HARD_DRIVE = registerItem("copper_hard_drive", settings ->
            new Item(settings
                    .rarity(Rarity.COMMON)
                    .maxCount(64)));

    public static final Item SERVER_CASING = registerItem("server_casing", settings ->
            new Item(settings
                    .rarity(Rarity.COMMON)
                    .maxCount(64)));

    // ========== УЛУЧШЕНИЕ СЕРВЕРА ==========
    public static final Item SERVER_UPGRADE = registerItem("server_upgrade", settings ->
            new ServerUpgradeItem(settings
                    .rarity(Rarity.UNCOMMON)
                    .maxCount(1)));

    public static final Item ELECTRICIAN_KIT = registerItem("electrician_kit", settings ->
            new ElectricianKitItem(settings
                    .maxDamage(4)
                    .rarity(Rarity.UNCOMMON)));

    private static Item registerItem(String name, Function<Item.Settings, Item> function) {
        return Registry.register(Registries.ITEM, Identifier.of(SkulkNebulaMod.MOD_ID, name),
                function.apply(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(SkulkNebulaMod.MOD_ID, name)))));
    }

    public static void register() {
        // Статическая инициализация
    }
}