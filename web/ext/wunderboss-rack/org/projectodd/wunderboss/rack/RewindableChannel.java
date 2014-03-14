package org.projectodd.wunderboss.rack;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;

public class RewindableChannel extends FileChannel {

    public RewindableChannel(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    protected FileChannel getTempFileChannel() throws java.io.IOException {
        if (tempFileChannel == null) {
            tempFile = Files.createTempFile("TorqueBoxRewindableChannel", null).toFile();
            tempFileRandom = new RandomAccessFile(tempFile, "rw");
            tempFileChannel = tempFileRandom.getChannel();

            // Transfer all the input data to the temporary file
            ReadableByteChannel inputChannel = Channels.newChannel(inputStream);
            long bytesRead = 0;
            long transferPosition = 0;
            while ((bytesRead = tempFileChannel.transferFrom(inputChannel, transferPosition, 1024 * 4)) > 0) {
                transferPosition += bytesRead;
            }
            inputChannel.close();
        }
        return tempFileChannel;
    }

    @Override
    public void force(boolean metaData) throws IOException {
        getTempFileChannel().force(metaData);
    }

    @Override
    public FileLock lock(long position, long size, boolean shared) throws IOException {
        return getTempFileChannel().lock(position, size, shared);
    }

    @Override
    public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
        return getTempFileChannel().map(mode, position, size);
    }

    @Override
    public long position() throws IOException {
        return getTempFileChannel().position();
    }

    @Override
    public FileChannel position(long newPosition) throws IOException {
        return getTempFileChannel().position(newPosition);
    }

    @Override
    public int read(ByteBuffer dst) throws IOException {
        return getTempFileChannel().read(dst);
    }

    @Override
    public int read(ByteBuffer dst, long position) throws IOException {
        return getTempFileChannel().read(dst, position);
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) throws IOException {
        return getTempFileChannel().read(dsts, offset, length);
    }

    @Override
    public long size() throws IOException {
        return getTempFileChannel().size();
    }

    @Override
    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        return getTempFileChannel().transferFrom(src, position, count);
    }

    @Override
    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        return getTempFileChannel().transferTo(position, count, target);
    }

    @Override
    public FileChannel truncate(long size) throws IOException {
        return getTempFileChannel().truncate(size);
    }

    @Override
    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        return getTempFileChannel().tryLock(position, size, shared);
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
        return getTempFileChannel().write(src);
    }

    @Override
    public int write(ByteBuffer src, long position) throws IOException {
        return getTempFileChannel().write(src, position);
    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) throws IOException {
        return getTempFileChannel().write(srcs, offset, length);
    }

    @Override
    protected void implCloseChannel() throws IOException {
        if (tempFileChannel != null) {
            tempFileChannel.close();
        }
        if (tempFileRandom != null) {
            tempFileRandom.close();
        }
        if (tempFile != null) {
            tempFile.delete();
        }
        if (inputStream != null) {
            inputStream.close();
        }
    }

    private InputStream inputStream;
    private File tempFile;
    private RandomAccessFile tempFileRandom;
    private FileChannel tempFileChannel;
}
