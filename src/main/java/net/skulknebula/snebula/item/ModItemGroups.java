package net.skulknebula.snebula.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.skulknebula.snebula.SkulkNebulaMod;
import net.skulknebula.snebula.block.ModBlocks;
import net.skulknebula.snebula.client.SkulkNebulaModClient;
import net.skulknebula.snebula.item.custom.ServerUpgradeItem;

public class ModItemGroups {
    public static final ItemGroup ML1_GROUP = Registry.register(Registries.ITEM_GROUP,
            Identifier.of(SkulkNebulaMod.MOD_ID, "snebula_group"),
            FabricItemGroup.builder().icon(() -> new ItemStack(Items.ECHO_SHARD))
                    .displayName(Text.translatable("itemgroup.snebula.snebula_group"))
                    .entries(((displayContext, entries) -> {
                        entries.add(ModBlocks.SERVER_BLOCK);
                        entries.add(ModBlocks.COMPUTER_BLOCK);
                        entries.add(ModBlocks.MICROSCOPE_BLOCK);

                        entries.add(ModItems.SERVER_UPGRADE_TIER_1);
                        entries.add(ModItems.SERVER_UPGRADE_TIER_2);
                        entries.add(ModItems.SERVER_UPGRADE_TIER_3);

                        entries.add(ModItems.PROCESSOR);
                        entries.add(ModItems.PRINTED_CIRCUIT_BOARD);
                        entries.add(ModItems.COPPER_HARD_DRIVE);
                        entries.add(ModItems.SERVER_CASING);
                        entries.add(ModItems.ELECTRICIAN_KIT);
                    })).build());

    public static void registerItemGroups() {
        SkulkNebulaMod.LOGGER.info("Registering Item Groups for " + SkulkNebulaMod.MOD_ID);
    }
}