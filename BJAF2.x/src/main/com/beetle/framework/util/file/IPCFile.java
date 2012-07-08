/*
 * BJAF - Beetle J2EE Application Framework
 * 甲壳虫J2EE企业应用开发框架
 * 版权所有2003-2015 余浩东 (www.beetlesoft.net)
 * 
 * 这是一个免费开源的软件，您必须在
 *<http://www.apache.org/licenses/LICENSE-2.0>
 *协议下合法使用、修改或重新发布。
 *
 * 感谢您使用、推广本框架，若有建议或问题，欢迎您和我联系。
 * 邮件： <yuhaodong@gmail.com/>.
 */
package com.beetle.framework.util.file;

import com.beetle.framework.AppException;
import com.beetle.framework.util.ObjectUtil;
import com.google.inject.internal.Errors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.FileLock;
import java.util.LinkedList;
import java.util.List;

/**
 * 进程间通信共享文件封装
 * 
 * @author HenryYu
 * 
 */
public final class IPCFile {
	private File mapFile;

	private MappedByteBuffer byteBuffer;

	private int size;

	private FileChannel fileChannel;

	private FileLock fileLock;

	public File getFile() {
		return mapFile;
	}

	public int getSize() {
		return size;
	}

	public IPCFile(String fileName, int size) throws AppException {
		connect(new File(fileName), size);
	}

	private void connect(File file, int size) throws AppException {
		if (null != this.byteBuffer) {
			throw new AppException("AlreadyConnected");
		}
		checkFile(file);
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "rw");
		} catch (FileNotFoundException e) {
			throw new AppException("ErrorOpeningMappingFile", e);
		}
		fileChannel = raf.getChannel();
		try {
			this.byteBuffer = fileChannel.map(MapMode.READ_WRITE, 0, size);
		} catch (IOException e) {
			throw new AppException("ErrorCreatingMap", e);
		}
		this.mapFile = file;
		this.size = size;
	}

	public void write(Object obj) throws AppException {
		this.empty();
		write(ObjectUtil.objToBytes(obj));
	}

	void write_bak(Object obj) throws AppException {
		byte[] bts = ObjectUtil.objToBytes(obj);
		byte[] bs = new byte[bts.length + 1];
		System.arraycopy(bts, 0, bs, 0, bts.length);
		bs[bts.length + 1] = '\n';
		write(bs);
	}

	private static void checkFile(File f) throws AppException {
		if (!f.exists()) {
			try {
				f.createNewFile();
			} catch (IOException e) {
				throw new AppException("ExceptionCreatingFile", e);
			}
		}
	}

	/**
	 * Write an array of bytes into a specified location in the segment,
	 * starting with a particular byte in the buffer and continuing for a
	 * specified number of bytes.
	 * 
	 * @param buf
	 *            The data to write.
	 * @param buffOffset
	 *            The location, relative to the start of the buffer, where the
	 *            data should be obtained.
	 * @param bufLength
	 *            The number of bytes to write.
	 * @param segmentOffset
	 *            The location in the buffer where the data should be written.
	 * @throws AppException
	 *             The method will throw this exception if the segment is not
	 *             actually connected to the underlying shared memory block.
	 */
	public void write(byte[] buf, int buffOffset, int bufLength,
			int segmentOffset) throws AppException {
		try {
			if (null == this.byteBuffer) {
				throw new AppException("NotConnected");
			}
			this.byteBuffer.position(segmentOffset);
			this.byteBuffer.put(buf, buffOffset, bufLength);
		} catch (java.nio.BufferOverflowException e) {
			throw e;
		}
	}

	public void write(byte value, int offset) {
		this.byteBuffer.position(offset);
		this.byteBuffer.put(value);
	}

	public void write(byte[] buf) throws AppException {
		write(buf, 0, buf.length, 0);
	}

	public void write(int value, int offset) {
		byte b = (byte) value;
		write(b, offset);
	}

	public void write(byte[] buf, int segmentOffset) throws AppException {
		write(buf, 0, buf.length, segmentOffset);
	}

	/**
	 * <P>
	 * Read bytes up to a specified number of bytes into the provided buffer.
	 * </P>
	 * <P>
	 * This method returns the number of bytes that were actually read. If fewer
	 * bytes are left in the segment than requested, then the method returns the
	 * number of bytes read.
	 * </P>
	 * 
	 * @param buf
	 *            The buffer where the bytes read should be stored.
	 * @param boffset
	 *            The offset into buf where the bytes should be placed.
	 * @param length
	 *            The number of bytes to try to read.
	 * @param soffset
	 *            The offset within the segment where the read should start.
	 * @return The number of bytes actually read. See description for details.
	 * @throws AppException
	 *             This method throws this exception with the error set to
	 *             {@link Errors#NotConnected} if the instance has not been
	 *             connected to a segment yet.
	 */
	public int read(int soffset, byte[] buf, int boffset, int length)
			throws AppException {
		if (null == byteBuffer) {
			throw new AppException("NotConnected");
		}
		if (length > byteBuffer.remaining())
			length = byteBuffer.remaining();
		byteBuffer.position(soffset);
		byteBuffer.get(buf, boffset, length);
		return length;
	}

	/**
	 * <P>
	 * Read bytes from the segment into a buffer, storing them in a location
	 * offset from the start of the buffer.
	 * </P>
	 * <P>
	 * This method is equivalent to calling:
	 * </P>
	 * <CODE>
	 * <PRE>
	 *     read(0, buf, bufferOffset, buf.length);
	 * </PRE>
	 * </CODE>
	 * 
	 * @param buf
	 *            The buffer where the bytes read should be placed.
	 * @param bufferOffset
	 *            The location in the buffer where the bytes read should start.
	 * @return The number of bytes actually read.
	 * @throws AppException
	 *             {@link #read(int, byte[], int, int)}.
	 * @see #read(int, byte[], int, int)
	 */
	public int read(byte[] buf, int bufferOffset) throws AppException {
		return read(0, buf, bufferOffset, buf.length);
	}

	/**
	 * Read bytes from the start of the segment.
	 * <P>
	 * This method is equivalent to calling
	 * </P>
	 * <CODE>
	 * <PRE>
	 *     read(0, buf, 0, buf.length);
	 * </PRE>
	 * </CODE>
	 * <P>
	 * See that method for details.
	 * </P>
	 * 
	 * @param buf
	 *            The buffer where bytes read should be placed.
	 * @return The number of bytes read.
	 * @throws AppException
	 *             see {@link #read(int, byte[], int, int)}.
	 * @see {@link #read(int, byte[], int, int)}
	 */
	public int read(byte[] buf) throws AppException {
		return read(0, buf, 0, buf.length);
	}

	/**
	 * Read in data from the segment, starting at a particular location in the
	 * segment.
	 * <P>
	 * This method is equivalent to calling
	 * </P>
	 * <CODE>
	 * <PRE>
	 *     read(segmentOffset, buf, 0, buf.length);
	 * </PRE>
	 * </CODE>
	 * <P>
	 * See that method for details.
	 * </P>
	 * 
	 * @param segmentOffset
	 *            The byte index into the segment where reading should start.
	 * @param buf
	 *            Where the bytes read should be placed.
	 * @return The number of bytes actually read.
	 * @throws AppException
	 *             see {@link #read(int, byte[], int, int)}
	 * @see #read(int, byte[], int, int)
	 */
	public int read(int segmentOffset, byte[] buf) throws AppException {
		return read(segmentOffset, buf, 0, buf.length);
	}

	Object read_bak() throws AppException {
		List<byte[]> ls = new LinkedList<byte[]>();
		int count = 0;
		while (true) {
			byte b[] = new byte[2048];
			int i = read(b);
			if (i <= 0) {
				break;
			} else {
				count = count + b.length;
				ls.add(b);
			}
		}
		if (ls.isEmpty()) {
			return null;
		}
		byte bb[] = new byte[count];
		int pt = 0;
		for (int i = 0; i < ls.size(); i++) {
			byte b[] = (byte[]) ls.get(i);
			if (i == 0) {
				pt = -1;
			} else {
				pt = pt + b.length;
			}
			System.arraycopy(b, 0, bb, pt + 1, b.length);
		}
		return ObjectUtil.bytesToObj(bb);
	}

	public Object read() {
		byte b[] = new byte[this.size];
		byte bb[];
		try {
			bb = new byte[read(b)];
			System.arraycopy(b, 0, bb, 0, bb.length);
			Object o = ObjectUtil.bytesToObj(bb);
			return o;
		} catch (Exception e) {
			return null;
		}
	}

	public void empty() throws AppException {
		byte b[] = new byte[this.size];
		this.write(b);
	}

	/**
	 * Return the byte at a particular location in the segment.
	 * 
	 * @param offset
	 *            The location of the byte in the segment.
	 * @return The value of the byte.
	 * @throws IndexOutOfBoundsException
	 *             If offset is negative or larger than the size of the segment
	 *             minus 1.
	 */
	public byte getByte(int offset) throws AppException {
		return byteBuffer.get(offset);
	}

	/**
	 * Reserve the shared memory segment.
	 * <P>
	 * It is not clear from the JRE documentation whether or not locking the
	 * segment will stop modifications to the segment or not.
	 * {@linkplain FileChannel#lock() for details.}
	 * 
	 * @throws IOException
	 */
	public void lock() throws IOException {
		fileLock = fileChannel.lock();
	}

	public boolean tryLock() throws IOException {
		fileLock = fileChannel.tryLock();
		if (fileLock == null) {
			return false;
		} else {
			return true;
		}
	}

	public void unlock() throws IOException {
		if (fileLock != null) {
			fileLock.release();
		}
	}

	public void flush() {
		if (byteBuffer != null) {
			if (byteBuffer.isLoaded()) {
				byteBuffer.force();
			}
		}
	}

	public void close() throws IOException {
		if (fileChannel != null) {
			if (byteBuffer != null) {
				byteBuffer.force();
			}
			fileChannel.close();
		}
	}
}
