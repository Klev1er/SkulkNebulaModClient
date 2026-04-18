package net.skulknebula.snebula.block.custom;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.skulknebula.snebula.SkulkNebulaMod;
import net.skulknebula.snebula.block.ModBlockEntities;
import net.skulknebula.snebula.item.ModItems;
import net.skulknebula.snebula.item.custom.ServerUpgradeItem;
import net.skulknebula.snebula.signal.DecryptionManager;
import net.skulknebula.snebula.sound.ModSounds;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animatable.manager.AnimatableManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ServerBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    public static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation");

    private boolean isBroken = false;
    private int repairProgress = 0;
    private static final int MAX_REPAIR_PROGRESS = 100;
    private int tickCounter = 0;
    private static final Random RANDOM = new Random();

    private int upgradeTier = 0;

    // Флаги для звуков
    private int diskSoundCooldown = 0;
    private static final int DISK_SOUND_DURATION = 37 * 20;
    private int brokenSoundTimer = 0;
    private int mainSoundTimer = 0;
    private int hardworkSoundTimer = 0;

    // Выбранный звук поломки (фиксируется при поломке)
    private SoundEvent currentBrokenSound = null;

    private static final List<ServerBlockEntity> ALL_SERVERS = new ArrayList<>();

    public ServerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SERVER_BLOCK_ENTITY, pos, state);
        synchronized (ALL_SERVERS) {
            ALL_SERVERS.add(this);
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, ServerBlockEntity entity) {
        if (world.isClient()) return;

        entity.tickCounter++;

        // Main sound
        if (entity.mainSoundTimer <= 0) {
            world.playSound(null, pos, ModSounds.SERVER_SOUND_MAIN, SoundCategory.BLOCKS, 0.5f, 1.0f);
            entity.mainSoundTimer = 120;
        } else {
            entity.mainSoundTimer--;
        }

        // Hardwork sound
        boolean processingSignal = DecryptionManager.getInstance().hasActiveSignal();
        if (!entity.isBroken && processingSignal) {
            if (entity.hardworkSoundTimer <= 0) {
                world.playSound(null, pos, ModSounds.SERVER_SOUND_HARDWORK, SoundCategory.BLOCKS, 0.4f, 1.0f);
                entity.hardworkSoundTimer = 80;
            } else {
                entity.hardworkSoundTimer--;
            }
        } else {
            entity.hardworkSoundTimer = 0;
        }

        // Disk sound
        if (!entity.isBroken) {
            float diskChance = processingSignal ? 0.01f : 0.002f;
            if (entity.diskSoundCooldown <= 0) {
                if (RANDOM.nextFloat() < diskChance) {
                    world.playSound(null, pos, ModSounds.SERVER_DISK_WORKING, SoundCategory.BLOCKS, 0.6f, 1.0f);
                    entity.diskSoundCooldown = DISK_SOUND_DURATION + RANDOM.nextInt(200);
                }
            } else {
                entity.diskSoundCooldown--;
            }
        }

        // Broken sound - используем фиксированный звук
        if (entity.isBroken && entity.currentBrokenSound != null) {
            if (entity.brokenSoundTimer <= 0) {
                world.playSound(null, pos, entity.currentBrokenSound, SoundCategory.BLOCKS, 0.7f, 1.0f);
                entity.brokenSoundTimer = 160;
            } else {
                entity.brokenSoundTimer--;
            }
        }

        // Логика поломки
        if (entity.tickCounter >= 10000) {
            entity.tickCounter = 0;
            if (!entity.isBroken) {
                float breakChance = 0.5f;
                if (entity.upgradeTier > 0) {
                    breakChance -= ServerUpgradeItem.getBreakReduction(entity.upgradeTier);
                }
                breakChance = Math.max(0.01f, breakChance);
                if (RANDOM.nextFloat() < breakChance) {
                    entity.setBroken(true);
                }
            }
        }
    }

    public ActionResult onUse(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        Item item = stack.getItem();

        // Установка улучшения
        if (item == ModItems.SERVER_UPGRADE_TIER_1 ||
                item == ModItems.SERVER_UPGRADE_TIER_2 ||
                item == ModItems.SERVER_UPGRADE_TIER_3) {
            if (installUpgrade(player, stack)) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.FAIL;
        }

        // Ручной ремонт УБРАН - только набор электрика!

        return ActionResult.PASS;
    }

    public boolean installUpgrade(PlayerEntity player, ItemStack upgradeStack) {
        Item item = upgradeStack.getItem();
        int newTier = 0;

        if (item == ModItems.SERVER_UPGRADE_TIER_1) newTier = 1;
        else if (item == ModItems.SERVER_UPGRADE_TIER_2) newTier = 2;
        else if (item == ModItems.SERVER_UPGRADE_TIER_3) newTier = 3;
        else return false;

        if (newTier != upgradeTier + 1) {
            if (newTier <= upgradeTier) {
                player.sendMessage(Text.literal("§cУлучшение этого уровня или выше уже установлено!"), true);
            } else {
                player.sendMessage(Text.literal("§cНужно сначала установить улучшение уровня " + (upgradeTier + 1) + "!"), true);
            }
            return false;
        }

        this.upgradeTier = newTier;
        upgradeStack.decrement(1);

        if (world != null && world.getBlockState(pos).getBlock() instanceof ServerBlock) {
            BlockState currentState = world.getBlockState(pos);
            world.setBlockState(pos, currentState.with(ServerBlock.TIER, newTier), 3);
        }

        markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }

        String status = isBroken ? " §7(сломан)" : "";
        player.sendMessage(Text.literal("§a✓ Улучшение уровня " + newTier + " установлено!" + status), true);
        player.sendMessage(Text.literal("§7Шанс поломки снижен на " +
                (int)(ServerUpgradeItem.getBreakReduction(newTier) * 100) + "%"), true);

        if (player instanceof ServerPlayerEntity serverPlayer) {
            grantAdvancement(serverPlayer, newTier);
        }

        return true;
    }

    private void grantAdvancement(ServerPlayerEntity player, int tier) {
        Identifier advancementId = null;
        String criterion = null;

        if (tier == 2) {
            advancementId = Identifier.of(SkulkNebulaMod.MOD_ID, "server_upgrade");
            criterion = "upgrade_tier_2";
        } else if (tier == 3) {
            advancementId = Identifier.of(SkulkNebulaMod.MOD_ID, "server_max_upgrade");
            criterion = "max_upgrade";
        }

        if (advancementId != null) {
            AdvancementEntry advancement = player.getEntityWorld().getServer()
                    .getAdvancementLoader()
                    .get(advancementId);
            if (advancement != null) {
                player.getAdvancementTracker().grantCriterion(advancement, criterion);
            }
        }
    }

    public int getUpgradeTier() {
        return upgradeTier;
    }

    public boolean hasUpgrade() {
        return upgradeTier > 0;
    }

    public void setBroken(boolean broken) {
        boolean wasBroken = this.isBroken;
        this.isBroken = broken;

        if (wasBroken != broken) {
            this.brokenSoundTimer = 0;
            this.hardworkSoundTimer = 0;

            if (broken) {
                // Выбираем звук поломки ОДИН раз и фиксируем
                this.currentBrokenSound = RANDOM.nextBoolean() ?
                        ModSounds.SERVER_SOUND_BROKEN :
                        ModSounds.SERVER_SOUND_BROKEN_2;
            } else {
                this.currentBrokenSound = null;
            }

            if (world != null && !world.isClient()) {
                world.playSound(null, pos, ModSounds.SERVER_SOUND_MAIN, SoundCategory.BLOCKS, 0f, 1.0f);
            }
        }

        if (world != null && !world.isClient()) {
            BlockState currentState = world.getBlockState(pos);
            if (currentState.getBlock() instanceof ServerBlock) {
                world.setBlockState(pos, currentState.with(ServerBlock.BROKEN, broken), 3);
            }
        }

        if (!broken) {
            this.repairProgress = 0;
            // Достижение за починку
            if (world != null) {
                for (PlayerEntity player : world.getPlayers()) {
                    if (player instanceof ServerPlayerEntity serverPlayer &&
                            player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < 25) {
                        AdvancementEntry advancement = serverPlayer.getEntityWorld().getServer()
                                .getAdvancementLoader()
                                .get(Identifier.of(SkulkNebulaMod.MOD_ID, "server_repair"));
                        if (advancement != null) {
                            serverPlayer.getAdvancementTracker().grantCriterion(advancement, "server_repaired");
                        }
                    }
                }
            }
        } else {
            // Достижение за поломку
            if (world != null) {
                for (PlayerEntity player : world.getPlayers()) {
                    if (player instanceof ServerPlayerEntity serverPlayer &&
                            player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) < 25) {
                        AdvancementEntry advancement = serverPlayer.getEntityWorld().getServer()
                                .getAdvancementLoader()
                                .get(Identifier.of(SkulkNebulaMod.MOD_ID, "server_break"));
                        if (advancement != null) {
                            serverPlayer.getAdvancementTracker().grantCriterion(advancement, "server_broken");
                        }
                    }
                }
            }
        }

        markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    public boolean isBroken() {
        return isBroken;
    }

    public static List<ServerBlockEntity> getAllServers() {
        synchronized (ALL_SERVERS) {
            return new ArrayList<>(ALL_SERVERS);
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        synchronized (ALL_SERVERS) {
            ALL_SERVERS.remove(this);
        }
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.isBroken = view.getBoolean("IsBroken", false);
        this.upgradeTier = view.getInt("UpgradeTier", 0);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putBoolean("IsBroken", this.isBroken);
        view.putInt("UpgradeTier", this.upgradeTier);
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>("Idle",
                state -> state.setAndContinue(IDLE)));
    }

    @Override
    public @NotNull AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}