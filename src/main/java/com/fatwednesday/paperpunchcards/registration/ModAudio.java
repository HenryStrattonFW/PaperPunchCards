package com.fatwednesday.paperpunchcards.registration;

import com.fatwednesday.paperpunchcards.PaperPunchCards;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ModAudio
{
    private static final int TICK_THRESHOLD = 2;
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, PaperPunchCards.MOD_ID);

    public static final Supplier<SoundEvent> GUILLOTINE_CUT = registerSoundEvent("guillotine_cut");
    public static final Supplier<SoundEvent> HOLE_PUNCH = registerSoundEvent("hole_punch");
    //public static final Supplier<SoundEvent> READER_CONFIGURED = registerSoundEvent("guillotine_cut");
    public static final Supplier<SoundEvent> READER_BAD = registerSoundEvent("reader_bad");
    public static final Supplier<SoundEvent> READER_GOOD = registerSoundEvent("reader_good");
    //public static final Supplier<SoundEvent> JAMMED = registerSoundEvent("guillotine_cut");

    private static Map<SoundEvent, Long> tickWatch = new HashMap<>();


    public static void register(IEventBus modBus)
    {
        SOUND_EVENTS.register(modBus);
    }

    private static Supplier<SoundEvent> registerSoundEvent(String name)
    {
        var id = PaperPunchCards.getResource(name);
        return SOUND_EVENTS.register(name, ()->SoundEvent.createVariableRangeEvent(id));
    }

    public static void tryPlaySound(SoundEvent sound, BlockPos pos, ServerLevel serverLevel, SoundSource source)
    {
        if(serverLevel == null || sound == null || source == null)
            return;

        var now = serverLevel.getGameTime();
        if(tickWatch.containsKey(sound))
        {
            var lastTick = tickWatch.get(sound);
            if(now - lastTick <= TICK_THRESHOLD)
                return;
        }

        serverLevel.playSound(null, pos, sound, source, 1f, 1f);
        tickWatch.put(sound, now);
    }
}
