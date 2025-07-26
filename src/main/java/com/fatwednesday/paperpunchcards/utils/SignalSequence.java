package com.fatwednesday.paperpunchcards.utils;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Base64;

public record SignalSequence(byte[] bytes)
{
    public static final Codec<SignalSequence> BASIC_CODEC =
            Codec.STRING.xmap(
                    SignalSequence::fromBase64,
                    SignalSequence::toBase64
            );

    public static final StreamCodec<ByteBuf, SignalSequence> BASIC_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE_ARRAY,
            SignalSequence::bytes,
            SignalSequence::new
    );

    public boolean isBlank()
    {
        for (byte b : bytes)
        {
            if (b != 0) return false;
        }
        return true;
    }

    public boolean matches(SignalSequence other)
    {
        if(other == null)
            return false;

        return matches(other.bytes);
    }

    public boolean matches(byte[] other)
    {
        if(other.length != bytes.length)
            return false;

        for(var i = 0; i < bytes.length; i++)
        {
            if(other[i] != bytes[i])
                return false;
        }
        return true;
    }

    public static SignalSequence fromBase64(String base64)
    {
        return new SignalSequence(Base64.getDecoder().decode(base64));
    }

    public static String toBase64(SignalSequence data)
    {
        return Base64.getEncoder().encodeToString(data.bytes());
    }

    public static SignalSequence fromNibbles(byte[] bytes)
    {
        return new SignalSequence(bytes);
    }
}
