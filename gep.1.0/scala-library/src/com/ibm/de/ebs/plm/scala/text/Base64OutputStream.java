package com.ibm.de.ebs.plm.scala.text;

/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the "License").  You may not use this file except 
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt or 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html. 
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * HEADER in each file and include the License file at 
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable, 
 * add the following below this CDDL HEADER, with the 
 * fields enclosed by brackets "[]" replaced with your 
 * own identifying information: Portions Copyright [yyyy] 
 * [name of copyright owner]
 */

/*
 * @(#)BASE64EncoderStream.java 1.9 06/06/05
 *
 * Copyright 1997-2006 Sun Microsystems, Inc. All Rights Reserved.
 */

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class implements a BASE64 encoder. It is implemented as a
 * FilterOutputStream, so one can just wrap this class around any output stream
 * and write bytes into this filter. The encoding is done as the bytes are
 * written out.
 * 
 * @author John Mani
 * @author Bill Shannon
 */

public class Base64OutputStream extends FilterOutputStream {
	private final byte[] buffer; // cache of bytes that are yet to be encoded
	private int bufsize = 0; // size of the cache
	private byte[] outbuf; // line size output buffer
	private int count = 0; // number of bytes that have been output
	private final int bytesPerLine; // number of bytes per line
	private final int lineLimit; // number of input bytes to output bytesPerLine
	private boolean noCRLF = false;

	private static byte[] newline = new byte[] { '\r', '\n' };

	/**
	 * Create a BASE64 encoder that encodes the specified output stream.
	 * 
	 * @param out
	 *            the output stream
	 * @param bytesPerLine
	 *            number of bytes per line. The encoder inserts a CRLF sequence
	 *            after the specified number of bytes, unless bytesPerLine is
	 *            Integer.MAX_VALUE, in which case no CRLF is inserted.
	 *            bytesPerLine is rounded down to a multiple of 4.
	 */
	public Base64OutputStream(final OutputStream out, int bytesPerLine) {
		super(out);
		buffer = new byte[3];
		if (bytesPerLine == Integer.MAX_VALUE || bytesPerLine < 4) {
			noCRLF = true;
			bytesPerLine = 76;
		}
		bytesPerLine = (bytesPerLine / 4) * 4; // Rounded down to 4n
		this.bytesPerLine = bytesPerLine; // save it
		lineLimit = bytesPerLine / 4 * 3;

		if (noCRLF) {
			outbuf = new byte[bytesPerLine];
		} else {
			outbuf = new byte[bytesPerLine + 2];
			outbuf[bytesPerLine] = (byte) '\r';
			outbuf[bytesPerLine + 1] = (byte) '\n';
		}
	}

	/**
	 * Create a BASE64 encoder that encodes the specified input stream. Inserts
	 * the CRLF sequence after outputting 76 bytes.
	 * 
	 * @param out
	 *            the output stream
	 */
	public Base64OutputStream(final OutputStream out) {
		this(out, 76);
	}

	/**
	 * Encodes <code>len</code> bytes from the specified <code>byte</code> array
	 * starting at offset <code>off</code> to this output stream.
	 * 
	 * @param b
	 *            the data.
	 * @param off
	 *            the start offset in the data.
	 * @param len
	 *            the number of bytes to write.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override public synchronized void write(final byte[] b, int off, final int len) throws IOException {
		final int end = off + len;

		// finish off incomplete coding unit
		while (bufsize != 0 && off < end) {
			write(b[off++]);
		}

		// finish off line
		int blen = ((bytesPerLine - count) / 4) * 3;
		if (off + blen < end) {
			// number of bytes that will be produced in outbuf
			int outlen = Base64OutputStream.encodedSize(blen);
			if (!noCRLF) {
				outbuf[outlen++] = (byte) '\r';
				outbuf[outlen++] = (byte) '\n';
			}
			out.write(Base64OutputStream.encode(b, off, blen, outbuf), 0, outlen);
			off += blen;
			count = 0;
		}

		// do bulk encoding a line at a time.
		for (; off + lineLimit < end; off += lineLimit) {
			out.write(Base64OutputStream.encode(b, off, lineLimit, outbuf));
		}

		// handle remaining partial line
		if (off + 3 < end) {
			blen = end - off;
			blen = (blen / 3) * 3; // round down
			// number of bytes that will be produced in outbuf
			final int outlen = Base64OutputStream.encodedSize(blen);
			out.write(Base64OutputStream.encode(b, off, blen, outbuf), 0, outlen);
			off += blen;
			count += outlen;
		}

		// start next coding unit
		for (; off < end; off++) {
			write(b[off]);
		}
	}

	/**
	 * Encodes <code>b.length</code> bytes to this output stream.
	 * 
	 * @param b
	 *            the data to be written.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override public void write(final byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	/**
	 * Encodes the specified <code>byte</code> to this output stream.
	 * 
	 * @param c
	 *            the <code>byte</code>.
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override public synchronized void write(final int c) throws IOException {
		buffer[bufsize++] = (byte) c;
		if (bufsize == 3) { // Encoding unit = 3 bytes
			encode();
			bufsize = 0;
		}
	}

	/**
	 * Flushes this output stream and forces any buffered output bytes to be
	 * encoded out to the stream.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	@Override public synchronized void flush() throws IOException {
		if (bufsize > 0) { // If there's unencoded characters in the buffer ..
			encode(); // .. encode them
			bufsize = 0;
		}
		out.flush();
	}

	/**
	 * Forces any buffered output bytes to be encoded out to the stream and
	 * closes this output stream
	 */
	@Override public synchronized void close() throws IOException {
		flush();
		if (count > 0 && !noCRLF) {
			out.write(Base64OutputStream.newline);
			out.flush();
		}
		out.close();
	}

	/** This array maps the characters to their 6 bit values */
	private final static char pem_array[] = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', // 0
			'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', // 1
			'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', // 2
			'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', // 3
			'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', // 4
			'o', 'p', 'q', 'r', 's', 't', 'u', 'v', // 5
			'w', 'x', 'y', 'z', '0', '1', '2', '3', // 6
			'4', '5', '6', '7', '8', '9', '+', '/' // 7
	};

	/**
	 * Encode the data stored in <code>buffer</code>. Uses <code>outbuf</code>
	 * to store the encoded data before writing.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	private void encode() throws IOException {
		final int osize = Base64OutputStream.encodedSize(bufsize);
		out.write(Base64OutputStream.encode(buffer, 0, bufsize, outbuf), 0, osize);
		// increment count
		count += osize;
		// If writing out this encoded unit caused overflow,
		// start a new line.
		if (count >= bytesPerLine) {
			if (!noCRLF) {
				out.write(Base64OutputStream.newline);
			}
			count = 0;
		}
	}

	/**
	 * Base64 encode a byte array. No line breaks are inserted. This method is
	 * suitable for short strings, such as those in the IMAP AUTHENTICATE
	 * protocol, but not to encode the entire content of a MIME part.
	 */
	public static byte[] encode(final byte[] inbuf) {
		if (inbuf.length == 0) {
			return inbuf;
		}
		return Base64OutputStream.encode(inbuf, 0, inbuf.length, null);
	}

	/**
	 * Internal use only version of encode. Allow specifying which part of the
	 * input buffer to encode. If outbuf is non-null, it's used as is.
	 * Otherwise, a new output buffer is allocated.
	 */
	private static byte[] encode(final byte[] inbuf, final int off, int size, byte[] outbuf) {
		if (outbuf == null) {
			outbuf = new byte[Base64OutputStream.encodedSize(size)];
		}
		int inpos, outpos;
		int val;
		for (inpos = off, outpos = 0; size >= 3; size -= 3, outpos += 4) {
			val = inbuf[inpos++] & 0xff;
			val <<= 8;
			val |= inbuf[inpos++] & 0xff;
			val <<= 8;
			val |= inbuf[inpos++] & 0xff;
			outbuf[outpos + 3] = (byte) Base64OutputStream.pem_array[val & 0x3f];
			val >>= 6;
			outbuf[outpos + 2] = (byte) Base64OutputStream.pem_array[val & 0x3f];
			val >>= 6;
			outbuf[outpos + 1] = (byte) Base64OutputStream.pem_array[val & 0x3f];
			val >>= 6;
			outbuf[outpos + 0] = (byte) Base64OutputStream.pem_array[val & 0x3f];
		}
		// done with groups of three, finish up any odd bytes left
		if (size == 1) {
			val = inbuf[inpos++] & 0xff;
			val <<= 4;
			outbuf[outpos + 3] = (byte) '='; // pad character;
			outbuf[outpos + 2] = (byte) '='; // pad character;
			outbuf[outpos + 1] = (byte) Base64OutputStream.pem_array[val & 0x3f];
			val >>= 6;
			outbuf[outpos + 0] = (byte) Base64OutputStream.pem_array[val & 0x3f];
		} else if (size == 2) {
			val = inbuf[inpos++] & 0xff;
			val <<= 8;
			val |= inbuf[inpos++] & 0xff;
			val <<= 2;
			outbuf[outpos + 3] = (byte) '='; // pad character;
			outbuf[outpos + 2] = (byte) Base64OutputStream.pem_array[val & 0x3f];
			val >>= 6;
			outbuf[outpos + 1] = (byte) Base64OutputStream.pem_array[val & 0x3f];
			val >>= 6;
			outbuf[outpos + 0] = (byte) Base64OutputStream.pem_array[val & 0x3f];
		}
		return outbuf;
	}

	/**
	 * Return the corresponding encoded size for the given number of bytes, not
	 * including any CRLF.
	 */
	private static int encodedSize(final int size) {
		return ((size + 2) / 3) * 4;
	}

}
