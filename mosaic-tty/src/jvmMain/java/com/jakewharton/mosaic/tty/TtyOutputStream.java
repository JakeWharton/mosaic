package com.jakewharton.mosaic.tty;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;

final class TtyOutputStream extends OutputStream {
	private final Tty tty;

	TtyOutputStream(Tty tty) {
		this.tty = tty;
	}

	@Override
	public void write(int b) throws IOException {
		write(new byte[] { (byte) b }, 0, 1);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		while (len > 0) {
			int written = tty.write(b, off, len);
			if (written == -1) {
				throw new EOFException();
			}
			len -= written;
			off += written;
		}
	}
}
