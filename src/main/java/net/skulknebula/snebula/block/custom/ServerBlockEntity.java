package net.skulknebula.snebula.block.custom;

import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
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
import net.skulknebula.snebula.block.ModBlockEntities;
import net.skulknebula.snebula.item.ModItems;
import net.skulknebula.snebula.item.custom.ServerUpgradeItem;
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
import java.util.Objects;
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

    // Для ручного ремонта (Shift+ПКМ)
    private PlayerEntity repairingPlayer = null;

    // Флаги для звуков
    private int diskSoundCooldown = 0;
    private static final int DISK_SOUND_DURATION = 37 * 20; // 37 секунд в тиках
    private int brokenSoundTimer = 0;
    private int mainSoundTimer = 0;
    private int hardworkSoundTimer = 0;

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

        // ========== ЗВУКИ ==========

        // Main sound - всегда играет (каждые 6 секунд)
        if (entity.mainSoundTimer <= 0) {
            world.playSound(null, pos, ModSounds.SERVER_SOUND_MAIN, SoundCategory.BLOCKS, 0.5f, 1.0f);
            entity.mainSoundTimer = 120; // 6 секунд
        } else {
            entity.mainSoundTimer--;
        }

        // Hardwork sound - когда усиленно работает (тикКунтер > 5000 или ремонт)
        if (!entity.isBroken && (entity.tickCounter > 5000 || entity.repairingPlayer != null)) {
            if (entity.hardworkSoundTimer <= 0) {
                world.playSound(null, pos, ModSounds.SERVER_SOUND_HARDWORK, SoundCategory.BLOCKS, 0.4f, 1.0f);
                entity.hardworkSoundTimer = 80; // 4 секунды
            } else {
                entity.hardworkSoundTimer--;
            }
        }

        // Звук диска (рандомно, только если не сломан)
        if (!entity.isBroken) {
            if (entity.diskSoundCooldown <= 0) {
                if (RANDOM.nextFloat() < 0.005f) { // 0.5% шанс каждый тик
                    world.playSound(null, pos, ModSounds.SERVER_DISK_WORKING, SoundCategory.BLOCKS, 0.6f, 1.0f);
                    entity.diskSoundCooldown = DISK_SOUND_DURATION + RANDOM.nextInt(200);
                }
            } else {
                entity.diskSoundCooldown--;
            }
        }

        // Звуки поломки (ИЛИ один ИЛИ другой)
        if (entity.isBroken) {
            if (entity.brokenSoundTimer <= 0) {
                // Выбираем ОДИН из двух звуков
                SoundEvent brokenSound = RANDOM.nextBoolean() ?
                        ModSounds.SERVER_SOUND_BROKEN :
                        ModSounds.SERVER_SOUND_BROKEN_2;
                world.playSound(null, pos, brokenSound, SoundCategory.BLOCKS, 0.7f, 1.0f);
                entity.brokenSoundTimer = 160; // 8 секунд
            } else {
                entity.brokenSoundTimer--;
            }
        }

        // ========== ЛОГИКА ПОЛОМКИ ==========
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

        // Ручной ремонт (Shift+ПКМ)
        if (entity.repairingPlayer != null) {
            if (!entity.repairingPlayer.isSneaking() ||
                    entity.repairingPlayer.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) > 25) {
                if (entity.repairProgress > 0) {
                    entity.repairingPlayer.sendMessage(
                            Text.literal("§cРемонт прерван!"), true);
                }
                entity.repairingPlayer = null;
                entity.repairProgress = 0;
            } else {
                entity.addRepairProgress(2);

                if (entity.repairProgress % 20 == 0 && entity.repairingPlayer != null) {
                    entity.repairingPlayer.sendMessage(
                            Text.literal("§7Ремонт: §e" + entity.repairProgress + "%"), true);
                }
            }
            entity.markDirty();
        }
    }

    // Взаимодействие с сервером
    public ActionResult onUse(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);

        // Установка улучшения (можно в любом состоянии)
        if (stack.isOf(ModItems.SERVER_UPGRADE)) {
            if (installUpgrade(player, stack)) {
                return ActionResult.SUCCESS;
            }
            return ActionResult.FAIL;
        }

        // Ручной ремонт (Shift+ПКМ пустой рукой)
        if (player.isSneaking() && stack.isEmpty() && isBroken) {
            if (world.isClient()) {
                return ActionResult.SUCCESS;
            }

            repairingPlayer = player;
            player.sendMessage(Text.literal("§7Начат ремонт сервера..."), true);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public boolean installUpgrade(PlayerEntity player, ItemStack upgradeStack) {
        if (!upgradeStack.isOf(ModItems.SERVER_UPGRADE)) {
            return false;
        }

        int newTier = ServerUpgradeItem.getTier(upgradeStack);

        if (newTier <= upgradeTier) {
            player.sendMessage(Text.literal("§cУлучшение этого уровня или выше уже установлено!"), true);
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
        player.sendMessage(Text.literal("§a✓ Улучшение тира " + newTier + " установлено!" + status), true);
        player.sendMessage(Text.literal("§7Шанс поломки снижен на " +
                (int)(ServerUpgradeItem.getBreakReduction(newTier) * 100) + "%"), true);

        if (upgradeTier >= 3) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                AdvancementEntry advancement = Objects.requireNonNull(serverPlayer.getEntityWorld().getServer())
                        .getAdvancementLoader()
                        .get(Identifier.of("snebula", "server_max_upgrade"));
                if (advancement != null) {
                    serverPlayer.getAdvancementTracker()
                            .grantCriterion(advancement, "max_upgrade");
                }
            }
        } else if (upgradeTier >= 2) {
            if (player instanceof ServerPlayerEntity serverPlayer) {
                AdvancementEntry advancement = Objects.requireNonNull(serverPlayer.getEntityWorld().getServer())
                        .getAdvancementLoader()
                        .get(Identifier.of("snebula", "server_upgrade"));
                if (advancement != null) {
                    serverPlayer.getAdvancementTracker()
                            .grantCriterion(advancement, "upgrade_tier_2");
                }
            }
        }

        return true;
    }

    public int getUpgradeTier() {
        return upgradeTier;
    }

    public boolean hasUpgrade() {
        return upgradeTier > 0;
    }

    public void setBroken(boolean broken) {
        this.isBroken = broken;

        if (world != null && !world.isClient()) {
            BlockState currentState = world.getBlockState(pos);
            if (currentState.getBlock() instanceof ServerBlock) {
                world.setBlockState(pos, currentState.with(ServerBlock.BROKEN, broken), 3);
            }
        }

        if (!broken) {
            this.repairProgress = 0;
            this.repairingPlayer = null;
            if (!broken && repairingPlayer instanceof ServerPlayerEntity serverPlayer) {
                AdvancementEntry advancement = serverPlayer.getEntityWorld().getServer()
                        .getAdvancementLoader()
                        .get(Identifier.of("snebula", "server_repair"));
                if (advancement != null) {
                    serverPlayer.getAdvancementTracker()
                            .grantCriterion(advancement, "server_repaired");
                }
            }
        } else {
            if (broken && repairingPlayer instanceof ServerPlayerEntity serverPlayer) {
                AdvancementEntry advancement = serverPlayer.getEntityWorld().getServer()
                        .getAdvancementLoader()
                        .get(Identifier.of("snebula", "server_break"));
                if (advancement != null) {
                    serverPlayer.getAdvancementTracker()
                            .grantCriterion(advancement, "server_broken");
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

    public void addRepairProgress(int amount) {
        if (!isBroken) return;

        repairProgress = Math.min(MAX_REPAIR_PROGRESS, repairProgress + amount);

        if (repairProgress >= MAX_REPAIR_PROGRESS) {
            setBroken(false);
            if (repairingPlayer != null) {
                repairingPlayer.sendMessage(Text.literal("§a✓ Сервер починен!"), true);
                repairingPlayer = null;
            }
        }

        markDirty();
        if (world != null) {
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    public int getRepairProgress() {
        return repairProgress;
    }

    public float getRepairPercentage() {
        return repairProgress / (float) MAX_REPAIR_PROGRESS;
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
        this.repairProgress = view.getInt("RepairProgress", 0);
        this.upgradeTier = view.getInt("UpgradeTier", 0);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putBoolean("IsBroken", this.isBroken);
        view.putInt("RepairProgress", this.repairProgress);
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