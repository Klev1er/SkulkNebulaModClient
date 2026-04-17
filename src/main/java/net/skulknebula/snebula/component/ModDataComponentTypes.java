package net.skulknebula.snebula.component;

import com.mojang.serialization.Codec;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.skulknebula.snebula.SkulkNebulaMod;
import java.util.function.UnaryOperator;

public class ModDataComponentTypes {

    public static final ComponentType<Integer> UPGRADE_TIER = register(
            "upgrade_tier",
            builder -> builder.codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT)
    );

    private static <T> ComponentType<T> register(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(
                Registries.DATA_COMPONENT_TYPE,
                Identifier.of(SkulkNebulaMod.MOD_ID, id),
                ((ComponentType.Builder)builderOperator.apply(ComponentType.builder())).build()
        );
    }

    public static void initialize() {
        // Статическая инициализация
    }
}