package com.fatwednesday.paperpunchcards.utils;

import java.util.Arrays;

public class NibbleStore
{
    private final byte[] data;

    public NibbleStore(int size)
    {
        var byteCount = (size + 1) / 2;
        data = new byte[byteCount];
    }

    public NibbleStore(byte[] data)
    {
        this.data = data;
    }

    public boolean isEmpty()
    {
        for (byte b : data)
            if (b != 0)
                return false;

        return true;
    }

    public void copyFrom(NibbleStore other)
    {
        copyFrom(other.data);
    }

    public void copyFrom(byte[] other)
    {
        System.arraycopy(other, 0, data, data.length, other.length);
    }

    public void clear()
    {
        Arrays.fill(data, (byte) 0);
    }

    public void setNibble(int index, int value)
    {
        if (value < 0 || value > 0xF)
            throw new IllegalArgumentException("Nibbles must be between 0 and 15 (0xF)");

        var byteIndex = index / 2;
        var highNibble = (index & 1) == 0;

        if (highNibble)
        {
            data[byteIndex] = (byte) ((data[byteIndex] & 0x0F) | (value << 4));
        }
        else
        {
            data[byteIndex] = (byte) ((data[byteIndex] & 0xF0) | value);
        }
    }

    public int getNibble(int index)
    {
        var byteIndex = index / 2;
        var highNibble = (index & 1) == 0;

        return highNibble
                ? (data[byteIndex] >> 4) & 0x0F
                : data[byteIndex] & 0x0F;
    }

    public byte[] bytes()
    {
        return data;
    }

    public int size()
    {
        return data.length * 2;
    }

    public String toString()
    {
        var sb = new StringBuilder();
        var size = size();
        for(var i=0;i<size;i++)
        {
            sb.append(getNibble(i));
            if(i<size-1)
                sb.append(",");
        }
        return sb.toString();
    }
}

