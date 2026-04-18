package net.skulknebula.snebula.item.custom;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;

import java.util.function.Consumer;

public class ServerUpgradeItem extends Item {

    private final int tier;

    public ServerUpgradeItem(Settings settings, int tier) {
        super(settings);
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    public static int getTierFromStack(ItemStack stack) {
        if (stack.getItem() instanceof ServerUpgradeItem upgrade) {
            return upgrade.getTier();
        }
        return 1;
    }

    public static float getBreakReduction(int tier) {
        return switch (tier) {
            case 1 -> 0.05f;
            case 2 -> 0.20f;
            case 3 -> 0.60f;
            default -> 0.05f;
        };
    }

    public ServerUpgradeItem rarity(Rarity rarity) {
        // В 1.21.1 rarity задаётся через Settings
        return this;
    }

    public ServerUpgradeItem maxCount(int maxCount) {
        // В 1.21.1 maxCount задаётся через Settings
        return this;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
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