package com.jakewharton.mosaic.tty;

import java.io.IOException;
import java.io.InputStream;

final class TtyInputStream extends InputStream {
	private final Tty tty;

	TtyInputStream(Tty tty) {
		this.tty = tty;
	}

	@Override
	public int read() throws IOException {
		byte[] b = new byte[1];

		int read;
		do {
			read = read(b, 0, 1);
		} while (read == 0);

		return read == -1
			? -1
			: b[0] & 0xff;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return tty.read(b, off, len);
	}
}
