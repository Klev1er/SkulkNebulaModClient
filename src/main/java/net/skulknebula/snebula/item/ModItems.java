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
    public static final Item PROCESSOR = registerItem("processor", Item::new);
    public static final Item PRINTED_CIRCUIT_BOARD = registerItem("printed_circuit_board", Item::new);
    public static final Item COPPER_HARD_DRIVE = registerItem("copper_hard_drive", Item::new);
    public static final Item SERVER_CASING = registerItem("server_casing", Item::new);

    // ========== УЛУЧШЕНИЯ СЕРВЕРА (ТРИ УРОВНЯ) ==========
    public static final Item SERVER_UPGRADE_TIER_1 = registerItem("server_upgrade_tier_1",
            settings -> new ServerUpgradeItem(settings, 1)
                    .rarity(Rarity.UNCOMMON)
                    .maxCount(16));

    public static final Item SERVER_UPGRADE_TIER_2 = registerItem("server_upgrade_tier_2",
            settings -> new ServerUpgradeItem(settings, 2)
                    .rarity(Rarity.RARE)
                    .maxCount(16));

    public static final Item SERVER_UPGRADE_TIER_3 = registerItem("server_upgrade_tier_3",
            settings -> new ServerUpgradeItem(settings, 3)
                    .rarity(Rarity.EPIC)
                    .maxCount(16));

    // Для обратной совместимости (если нужно)
    public static final Item SERVER_UPGRADE = SERVER_UPGRADE_TIER_1;

    // ========== ИНСТРУМЕНТЫ ==========
    public static final Item ELECTRICIAN_KIT = registerItem("electrician_kit",
            settings -> new ElectricianKitItem(settings.maxDamage(4)));

    private static Item registerItem(String name, Function<Item.Settings, Item> function) {
        Item.Settings settings = new Item.Settings()
                .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(SkulkNebulaMod.MOD_ID, name)));
        return Registry.register(Registries.ITEM,
                Identifier.of(SkulkNebulaMod.MOD_ID, name),
                function.apply(settings));
    }

    public static void register() {
        // Статическая инициализация
    }
}