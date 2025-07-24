package com.fatwednesday.fatlib.gui.utils;

import net.minecraft.world.item.context.BlockPlaceContext;

public class PlacementUtils
{
    public static final int ALLOW_NORTH = 1;
    public static final int ALLOW_SOUTH = 1 << 1;
    public static final int ALLOW_EAST = 1 << 2;
    public static final int ALLOW_WEST = 1 << 3;
    public static final int ALLOW_UP = 1 << 4;
    public static final int ALLOW_DOWN = 1 << 5;
    public static final int ALLOW_HORIZONTAL = ALLOW_NORTH | ALLOW_SOUTH | ALLOW_EAST | ALLOW_WEST;
    public static final int ALLOW_VERTICAL = ALLOW_UP | ALLOW_DOWN;
    public static final int ANY_FACING = ALLOW_HORIZONTAL | ALLOW_VERTICAL;
    public static final int REQUIRE_STURDY = 1 << 6;

    public static boolean validatePlacement(BlockPlaceContext context, int flags)
    {
        var face = context.getClickedFace();
        switch (face){
            case DOWN :
                if((flags & ALLOW_DOWN) == 0)
                    return false;

            case UP :
                if((flags & ALLOW_UP) == 0)
                    return false;

            case NORTH:
                if((flags & ALLOW_NORTH) == 0)
                    return false;

            case SOUTH :
                if((flags & ALLOW_SOUTH) == 0)
                    return false;

            case WEST:
                if((flags & ALLOW_WEST) == 0)
                    return false;

            case EAST:
                if((flags & ALLOW_EAST) == 0)
                    return false;
        }

        if((flags & REQUIRE_STURDY) != 0)
        {
            var level = context.getLevel();
            var pos = context.getClickedPos();
            var attachPos = pos.relative(face.getOpposite());
            var attachState = level.getBlockState(attachPos);
            return attachState.isFaceSturdy(level, attachPos, face);
        }

        return true;
    }

    public static boolean isTargetFaceSturdy(BlockPlaceContext context)
    {
        var face = context.getClickedFace();
        var level = context.getLevel();
        var pos = context.getClickedPos();
        var attachPos = pos.relative(face.getOpposite());
        var attachState = level.getBlockState(attachPos);
        return attachState.isFaceSturdy(level, attachPos, face);
    }
}
