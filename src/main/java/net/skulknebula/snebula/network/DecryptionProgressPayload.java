package net.skulknebula.snebula.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.skulknebula.snebula.SkulkNebulaMod;

public record DecryptionProgressPayload(String signalId, int progress, int maxProgress, int imageQuality) implements CustomPayload {
    public static final CustomPayload.Id<DecryptionProgressPayload> ID =
            new CustomPayload.Id<>(Identifier.of(SkulkNebulaMod.MOD_ID, "decryption_progress"));

    public static final PacketCodec<PacketByteBuf, DecryptionProgressPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeString(value.signalId);
                buf.writeInt(value.progress);
                buf.writeInt(value.maxProgress);
                buf.writeInt(value.imageQuality);
            },
            buf -> new DecryptionProgressPayload(buf.readString(), buf.readInt(), buf.readInt(), buf.readInt())
    );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}