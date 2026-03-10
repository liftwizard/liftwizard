/*
 * Copyright 2025 Craig Motlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.liftwizard.servlet.logging.filter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * An {@link HttpServletResponseWrapper} that streams the response through to the client
 * immediately while capturing up to {@code maxCapture} bytes in a side buffer for logging.
 *
 * <p>Unlike {@link org.springframework.web.util.ContentCachingResponseWrapper}, this wrapper
 * does not buffer the entire response in memory. The response is written to the real output
 * stream as it arrives, so there is no need to call {@code copyBodyToResponse()}.
 */
public class BodyCaptureResponseWrapper extends HttpServletResponseWrapper {

	private final int maxCapture;
	private final byte[] captureBuffer;
	private int capturedBytes;
	private ServletOutputStream wrappedStream;
	private PrintWriter wrappedWriter;

	public BodyCaptureResponseWrapper(HttpServletResponse response, int maxCapture) {
		super(response);
		this.maxCapture = maxCapture;
		this.captureBuffer = new byte[maxCapture];
	}

	@Override
	public ServletOutputStream getOutputStream() throws IOException {
		if (this.wrappedStream == null) {
			ServletOutputStream delegate = super.getOutputStream();
			this.wrappedStream = new TeeServletOutputStream(delegate);
		}
		return this.wrappedStream;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		if (this.wrappedWriter == null) {
			String encoding = this.getCharacterEncoding();
			this.wrappedWriter = new PrintWriter(new OutputStreamWriter(this.getOutputStream(), encoding));
		}
		return this.wrappedWriter;
	}

	@Override
	public void flushBuffer() throws IOException {
		if (this.wrappedWriter != null) {
			this.wrappedWriter.flush();
		}
		if (this.wrappedStream != null) {
			this.wrappedStream.flush();
		}
		super.flushBuffer();
	}

	public byte[] getCapturedBody() {
		byte[] result = new byte[this.capturedBytes];
		System.arraycopy(this.captureBuffer, 0, result, 0, this.capturedBytes);
		return result;
	}

	public int getCapturedSize() {
		return this.capturedBytes;
	}

	public boolean isTruncated() {
		return this.capturedBytes >= this.maxCapture;
	}

	private void capture(byte[] data, int offset, int length) {
		int remaining = this.maxCapture - this.capturedBytes;
		if (remaining <= 0) {
			return;
		}
		int toCopy = Math.min(length, remaining);
		System.arraycopy(data, offset, this.captureBuffer, this.capturedBytes, toCopy);
		this.capturedBytes += toCopy;
	}

	private void capture(int b) {
		if (this.capturedBytes < this.maxCapture) {
			this.captureBuffer[this.capturedBytes] = (byte) b;
			this.capturedBytes++;
		}
	}

	private class TeeServletOutputStream extends ServletOutputStream {

		private final ServletOutputStream delegate;

		TeeServletOutputStream(ServletOutputStream delegate) {
			this.delegate = delegate;
		}

		@Override
		public void write(int b) throws IOException {
			this.delegate.write(b);
			BodyCaptureResponseWrapper.this.capture(b);
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			this.delegate.write(b, off, len);
			BodyCaptureResponseWrapper.this.capture(b, off, len);
		}

		@Override
		public boolean isReady() {
			return this.delegate.isReady();
		}

		@Override
		public void setWriteListener(WriteListener writeListener) {
			this.delegate.setWriteListener(writeListener);
		}

		@Override
		public void flush() throws IOException {
			this.delegate.flush();
		}

		@Override
		public void close() throws IOException {
			this.delegate.close();
		}
	}
}
