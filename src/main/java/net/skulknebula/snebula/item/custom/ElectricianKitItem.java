package net.skulknebula.snebula.item.custom;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.skulknebula.snebula.block.custom.ServerBlockEntity;

import java.util.List;
import java.util.function.Consumer;

public class ElectricianKitItem extends Item {
    private static final int MAX_USES = 4;
    private static final int REPAIR_AMOUNT = 100; // Полный ремонт

    public ElectricianKitItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        Hand hand = context.getHand();

        if (player == null) return ActionResult.PASS;

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof ServerBlockEntity server)) return ActionResult.PASS;

        if (!server.isBroken()) {
            if (world.isClient()) {
                player.sendMessage(Text.literal("§eСервер не нуждается в ремонте").formatted(Formatting.YELLOW), true);
            }
            return ActionResult.PASS;
        }

        if (!world.isClient()) {
            server.setBroken(false);
            ItemStack stack = player.getStackInHand(hand);

            int newDamage = stack.getDamage() + 1;
            if (newDamage >= MAX_USES) {
                stack.decrement(1); // ЛОМАЕТСЯ!
                player.sendMessage(Text.literal("§cНабор электрика сломался!").formatted(Formatting.RED), true);
            } else {
                stack.setDamage(newDamage);
            }
            player.sendMessage(Text.literal("§a✓ Сервер починен!").formatted(Formatting.GREEN), true);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return stack.getDamage() > 0;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13.0F - (float)stack.getDamage() * 13.0F / (float)MAX_USES);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        float f = Math.max(0.0F, ((float)MAX_USES - (float)stack.getDamage()) / (float)MAX_USES);
        return net.minecraft.util.math.MathHelper.hsvToRgb(f / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        int usesLeft = MAX_USES - stack.getDamage();

        textConsumer.accept(Text.translatable("item.snebula.electrician_kit.uses", usesLeft, MAX_USES)
                .formatted(Formatting.AQUA));

        textConsumer.accept(Text.empty());
        textConsumer.accept(Text.translatable("item.snebula.electrician_kit.desc1")
                .formatted(Formatting.GRAY));
        textConsumer.accept(Text.translatable("item.snebula.electrician_kit.desc2")
                .formatted(Formatting.DARK_GRAY, Formatting.ITALIC));

        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
    }
}