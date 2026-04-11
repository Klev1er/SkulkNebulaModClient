package net.skulknebula.snebula.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.skulknebula.snebula.SkulkNebulaMod;

public record BrewingParticlePayload(BlockPos pos) implements CustomPayload {
    public static final CustomPayload.Id<BrewingParticlePayload> ID =
            new CustomPayload.Id<>(Identifier.of(SkulkNebulaMod.MOD_ID, "brewing_particle"));

    public static final PacketCodec<RegistryByteBuf, BrewingParticlePayload> CODEC =
            PacketCodec.tuple(BlockPos.PACKET_CODEC, BrewingParticlePayload::pos, BrewingParticlePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}