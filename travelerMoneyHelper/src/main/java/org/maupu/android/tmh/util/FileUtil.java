package org.maupu.android.tmh.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * Source: https://stackoverflow.com/questions/6540906/simple-export-and-import-of-a-sqlite-database-on-android
 */
public class FileUtil {
    private static final int BYTE_BUFFER_SIZE = 1024;
    public static void copyFile(InputStream from, FileOutputStream toFile) throws IOException {
        ReadableByteChannel fromChannel = null;
        FileChannel toChannel = null;

        try {
            fromChannel = Channels.newChannel(from);
            toChannel = toFile.getChannel();

            ByteBuffer readBuffer = ByteBuffer.allocate(BYTE_BUFFER_SIZE);
            while( fromChannel.read(readBuffer) >= 0) {
                readBuffer.flip();
                toChannel.write(readBuffer);
                readBuffer.clear();
            }
        } finally {
            try {
                if (fromChannel != null) {
                    fromChannel.close();
                }
            } finally {
                if (toChannel != null) {
                    toChannel.close();
                }
            }
        }
    }
}
