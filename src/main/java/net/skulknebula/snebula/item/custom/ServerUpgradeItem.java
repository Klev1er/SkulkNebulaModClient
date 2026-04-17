package net.skulknebula.snebula.item.custom;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.skulknebula.snebula.component.ModDataComponentTypes;
import net.skulknebula.snebula.item.ModItems;

import java.util.List;
import java.util.function.Consumer;

public class ServerUpgradeItem extends Item {

    public ServerUpgradeItem(Settings settings) {
        super(settings);
    }

    public static int getTier(ItemStack stack) {
        return stack.getOrDefault(ModDataComponentTypes.UPGRADE_TIER, 1);
    }

    public static void setTier(ItemStack stack, int tier) {
        stack.set(ModDataComponentTypes.UPGRADE_TIER, Math.min(3, Math.max(1, tier)));
    }

    public static ItemStack create(int tier) {
        ItemStack stack = new ItemStack(ModItems.SERVER_UPGRADE);
        setTier(stack, tier);
        return stack;
    }

    public static float getBreakReduction(int tier) {
        return switch (tier) {
            case 1 -> 0.05f;
            case 2 -> 0.20f;
            case 3 -> 0.60f;
            default -> 0.05f;
        };
    }


    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        int tier = getTier(stack);

        textConsumer.accept(Text.translatable("item.snebula.server_upgrade.tier", tier)
                .formatted(Formatting.GOLD));

        textConsumer.accept(Text.translatable("item.snebula.server_upgrade.reduction",
                        (int)(getBreakReduction(tier) * 100))
                .formatted(Formatting.GREEN));

        if (tier < 3) {
            textConsumer.accept(Text.translatable("item.snebula.server_upgrade.upgrade_hint")
                    .formatted(Formatting.GRAY, Formatting.ITALIC));
        }
    }
}