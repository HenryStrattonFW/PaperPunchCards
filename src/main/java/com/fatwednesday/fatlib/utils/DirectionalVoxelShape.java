package com.fatwednesday.fatlib.utils;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DirectionalVoxelShape
{
    private final Map<Direction, VoxelShape> shapes = new HashMap<>();

    public DirectionalVoxelShape(VoxelShape baseShape, Direction baseDir,  Direction... directions)
    {
        shapes.put(baseDir, baseShape);
        for(Direction direction : directions)
        {
            shapes.put(direction, rotateShape(baseDir, direction, baseShape));
        }
    }

    public VoxelShape get(Direction direction)
    {
        return shapes.get(direction);
    }
    public Optional<VoxelShape> tryGet(Direction direction)
    {
        return shapes.containsKey(direction)
                ? Optional.of(shapes.get(direction))
                : Optional.empty();
    }

    private static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape)
    {
        if (from == to)
            return shape;

        var buffer = new VoxelShape[] { shape, Shapes.empty() };
        var times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;

        for (var i = 0; i < times; ++i)
        {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    buffer[1] = Shapes.or(buffer[1], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }

        return buffer[0];
    }
}
