package com.github.tutar.http.Exception;

import com.github.tutar.http.HttpRequest;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * Request output stream
 */
public class RequestOutputStream extends BufferedOutputStream {

    private final CharsetEncoder encoder;

    /**
     * Create request output stream
     *
     * @param stream
     * @param charset
     * @param bufferSize
     */
    public RequestOutputStream(final OutputStream stream, final String charset,
                               final int bufferSize) {
        super(stream, bufferSize);

        encoder = Charset.forName(HttpRequest.getValidCharset(charset)).newEncoder();
    }

    /**
     * Write string to stream
     *
     * @param value
     * @return this stream
     * @throws IOException
     */
    public RequestOutputStream write(final String value) throws IOException {
        final ByteBuffer bytes = encoder.encode(CharBuffer.wrap(value));

        super.write(bytes.array(), 0, bytes.limit());

        return this;
    }

    public CharsetEncoder getEncoder(){
       return encoder;
    }
}
