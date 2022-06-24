package com.xenon.glfw;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.lwjgl.system.MemoryUtil.*;

public class ResourceUtils {

    /**
     * Creates a new ByteBuffer object with the new capacity using <code>MemoryUtils.memAlloc()</code>.
     * Also reclaims <code>old</code> memory, so after calling this function,
     * <code>old</code> is inconsistent without reassigning it to the new buffer.
     * @param old the old buffer pointer
     * @param new_cap the capacity for the new buffer
     * @return the new buffer
     */
    public static ByteBuffer transferBuffer(ByteBuffer old, int new_cap) {
        ByteBuffer newBuffer = memAlloc(new_cap);
        int pos = old.position();
        newBuffer.put(old).rewind();
        memFree(old);
        return newBuffer.position(pos);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static ByteBuffer bytes(Path resource) throws IOException {
        ByteBuffer buffer;
        if (Files.isReadable(resource)) try (SeekableByteChannel channel = Files.newByteChannel(resource)) {
            buffer = memAlloc((int) channel.size() + 1);    // + 1 is for null terminator
            while (channel.read(buffer) != -1) ;
        }
        else try (InputStream stream = ResourceUtils.class.getResourceAsStream(resource.toString());
                    ReadableByteChannel rbc = Channels.newChannel(stream)
        ) {
            buffer = memAlloc(128);
            while (rbc.read(buffer) != -1) if (buffer.remaining() == 0)
                transferBuffer(buffer, buffer.capacity() * 2);
            buffer = memSlice(buffer);
        }
        buffer.flip();
        return buffer;
    }

}
