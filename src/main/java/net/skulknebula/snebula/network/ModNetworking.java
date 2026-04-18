package net.skulknebula.snebula.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.skulknebula.snebula.SkulkNebulaMod;
import net.skulknebula.snebula.signal.ClientDecryptionManager;

public class ModNetworking {

    public static void register() {
        // Регистрируем тип пакета для S2C (сервер -> клиент)
        PayloadTypeRegistry.playS2C().register(BrewingParticlePayload.ID, BrewingParticlePayload.CODEC);

        // Регистрируем обработчик на клиенте (только если мы на клиенте)
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            registerClientReceivers();
        }
    }

    @Environment(EnvType.CLIENT)
    public static void registerClientReceivers() {
        ClientPlayNetworking.registerGlobalReceiver(SignalSyncPayload.ID, (payload, context) -> {
            SkulkNebulaMod.LOGGER.info("CLIENT: Received SignalSyncPayload for signal: {}");
            context.client().execute(() -> {
                ClientDecryptionManager.getInstance().syncFromServer(payload.signalId(), payload.signal());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(DecryptionProgressPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientDecryptionManager.getInstance().syncProgress(
                        payload.signalId(),
                        payload.progress(),
                        payload.maxProgress(),
                        payload.imageQuality()
                );
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(DecryptionHistoryPayload.ID, (payload, context) -> {
            SkulkNebulaMod.LOGGER.info("CLIENT: Received history payload with {} entries");
            context.client().execute(() -> {
                ClientDecryptionManager.getInstance().syncHistory(payload.history());
            });
        });
    }

    // Отправка пакета с сервера
    public static void sendBrewingParticlePacket(ServerWorld world, BlockPos pos) {
        BrewingParticlePayload payload = new BrewingParticlePayload(pos);

        // Отправляем всем игрокам в радиусе 64 блока
        for (ServerPlayerEntity player : world.getPlayers()) {
            if (player.getBlockPos().isWithinDistance(pos, 64)) {
                ServerPlayNetworking.send(player, payload);
            }
        }
    }
}