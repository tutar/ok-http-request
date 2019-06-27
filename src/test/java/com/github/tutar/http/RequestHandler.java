package com.github.tutar.http;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Simplified handler
 */
public abstract class RequestHandler extends AbstractHandler {

    private Request request;

    private HttpServletResponse response;

    /**
     * Handle request
     *
     * @param request
     * @param response
     */
    public abstract void handle(Request request, HttpServletResponse response);

    /**
     * Read content
     *
     * @return content
     */
    protected byte[] read() {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        final byte[] buffer = new byte[8196];
        int read;
        try {
            InputStream input = request.getInputStream();
            while ((read = input.read(buffer)) != -1)
                content.write(buffer, 0, read);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content.toByteArray();
    }

    /**
     * Write value
     *
     * @param value
     */
    protected void write(String value) {
        try {
            response.getWriter().print(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Write line
     *
     * @param value
     */
    protected void writeln(String value) {
        try {
            response.getWriter().println(value);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handle(String target, Request baseRequest,
                       HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        this.request = (Request) request;
        this.response = response;
        this.request.setHandled(true);
        handle(this.request, response);
    }

}
