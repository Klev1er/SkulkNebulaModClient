package net.skulknebula.snebula.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.skulknebula.snebula.SkulkNebulaMod;

@Environment(EnvType.CLIENT)
public class ClientNetworking {

    public static void register() {
        // Регистрируем обработчик пакета на клиенте
        ClientPlayNetworking.registerGlobalReceiver(BrewingParticlePayload.ID, (payload, context) -> {
            // Выполняем на клиентском потоке рендеринга
            context.client().execute(() -> {
                // Спавним частицы
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
}