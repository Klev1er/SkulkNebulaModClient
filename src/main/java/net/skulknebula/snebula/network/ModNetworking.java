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

public class ModNetworking {

    public static void register() {
        // Регистрируем тип пакета для S2C (сервер -> клиент)
        PayloadTypeRegistry.playS2C().register(BrewingParticlePayload.ID, BrewingParticlePayload.CODEC);

        // Регистрируем обработчик на клиенте (только если мы на клиенте)
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            registerClientReceiver();
        }
    }

    @Environment(EnvType.CLIENT)
    private static void registerClientReceiver() {
        ClientPlayNetworking.registerGlobalReceiver(BrewingParticlePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.player() != null) {
                    BlockPos pos = payload.pos();
                    context.player().getEntityWorld().addImportantParticleClient(
                            ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                            pos.getX() + 0.5f,
                            pos.getY() + 1.0f,
                            pos.getZ() + 0.5f,
                            0f, 0.03f, 0f
                    );
                }
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