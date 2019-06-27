package com.github.tutar.okhttp;

import com.github.tutar.http.RequestHandler;
import com.github.tutar.http.ServerTestCase;
import com.squareup.okhttp.ConnectionPool;
import org.eclipse.jetty.server.Request;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.tutar.http.Utils.encode;
import static com.github.tutar.okhttp.OkHttpRequest.delete;
import static com.github.tutar.okhttp.OkHttpRequest.get;
import static java.net.HttpURLConnection.*;
import static org.junit.Assert.*;


/**
 * unit tests of {@link OkHttpRequest}
 */
public class OkHttpRequestTest extends ServerTestCase {

    private static String url;

    private static RequestHandler handler;

    /**
     * Set up server
     *
     * @throws Exception
     */
    @BeforeClass
    public static void startServer() throws Exception {
        url = setUp(new RequestHandler() {

            @Override
            public void handle(String target, Request baseRequest,
                               HttpServletRequest request, HttpServletResponse response)
                    throws IOException, ServletException {
                if (handler != null)
                    handler.handle(target, baseRequest, request, response);
            }

            @Override
            public void handle(Request request, HttpServletResponse response) {
                if (handler != null)
                    handler.handle(request, response);
            }
        });
    }

    /******************************* Post Unit Tests ***********************************/
    /**
     * Clear handler
     */
    @After
    public void clearHandler() {
        handler = null;
    }


    /**
     * Make a POST request with an empty request body
     *
     * @throws Exception
     */
    @Test
    public void postEmpty() throws Exception {
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                method.set(request.getMethod());
                response.setStatus(HTTP_CREATED);
            }
        };
        OkHttpRequest request = OkHttpRequest.post(url);
        int code = request.code();
        assertEquals("POST", method.get());
        assertFalse(request.ok());
        assertTrue(request.created());
        assertEquals(HTTP_CREATED, code);
    }

    /**
     * Make a POST request with an empty request body
     *
     * @throws Exception
     */
    @Test
    public void postUrlEmpty() throws Exception {
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                method.set(request.getMethod());
                response.setStatus(HTTP_CREATED);
            }
        };
        OkHttpRequest request = OkHttpRequest.post(new URL(url));
        int code = request.code();
        assertEquals("POST", method.get());
        assertFalse(request.ok());
        assertTrue(request.created());
        assertEquals(HTTP_CREATED, code);
    }

    /**
     * Make a POST request with a non-empty request body
     *
     * @throws Exception
     */
    @Test
    public void postNonEmptyString() throws Exception {
        final AtomicReference<String> body = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                body.set(new String(read()));
                response.setStatus(HTTP_OK);
            }
        };
        int code = OkHttpRequest.post(url).send("hello").code();
        assertEquals(HTTP_OK, code);
        assertEquals("hello", body.get());
    }

    /**
     * Make a POST request with a non-empty request body
     *
     * @throws Exception
     */
    @Test
    public void postNonEmptyFile() throws Exception {
        final AtomicReference<String> body = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                body.set(new String(read()));
                response.setStatus(HTTP_OK);
            }
        };
        File file = File.createTempFile("post", ".txt");
        new FileWriter(file).append("hello").close();
        int code = OkHttpRequest.post(url).send(file).code();
        assertEquals(HTTP_OK, code);
        assertEquals("hello", body.get());
    }

    /**
     * Make a POST request with multiple files in the body
     *
     * @throws Exception
     */
    @Test
    @Ignore
    public void postMultipleFiles() throws Exception {
        final AtomicReference<String> body = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                body.set(new String(read()));
                response.setStatus(HTTP_OK);
            }
        };

        File file1 = File.createTempFile("post", ".txt");
        new FileWriter(file1).append("hello").close();

        File file2 = File.createTempFile("post", ".txt");
        new FileWriter(file2).append(" world").close();

        int code = OkHttpRequest.post(url).send(file1).send(file2).code();
        assertEquals(HTTP_OK, code);
        assertEquals("hello world", body.get());
    }


    /******************************* Get Unit Tests ***********************************/
    /**
     * Make a GET request with an empty body response
     *
     * @throws Exception
     */
    @Test
    public void getEmpty() throws Exception {
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                method.set(request.getMethod());
                response.setStatus(HTTP_OK);
            }
        };
        OkHttpRequest request = get(url);
//        assertNotNull(request.getConnection());
//        assertEquals(30000, request.getReadTimeout());
//        assertEquals(50000, request.connectTimeout(50000).getConnection()
//                .getConnectTimeout());
//        assertEquals(2500, request.bufferSize(2500).bufferSize());
//        assertFalse(request.ignoreCloseExceptions(false).ignoreCloseExceptions());
//        assertFalse(request.useCaches(false).getConnection().getUseCaches());
        int code = request.code();
        assertTrue(request.ok());
        assertFalse(request.created());
        assertFalse(request.badRequest());
        assertFalse(request.serverError());
        assertFalse(request.notFound());
        assertFalse(request.notModified());
        assertEquals("GET", method.get());
        assertEquals("OK", request.message());
        assertEquals(HTTP_OK, code);
        assertEquals("", request.body());
        assertNotNull(request.toString());
        assertFalse(request.toString().length() == 0);
//        assertEquals(request, request.disconnect());
        assertTrue(request.isBodyEmpty());
        String exceptUrl = request.url().toString();
        if(exceptUrl.endsWith("/")){
            exceptUrl = exceptUrl.substring(0,exceptUrl.length()-1);
        }
        assertEquals(exceptUrl, url);
        assertEquals("GET", request.method());
    }

    /**
     * Make a GET request with an empty body response
     *
     * @throws Exception
     */
    @Test
    public void getUrlEmpty() throws Exception {
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                method.set(request.getMethod());
                response.setStatus(HTTP_OK);
            }
        };
        OkHttpRequest request = get(new URL(url));
//        assertNotNull(request.getConnection());
        assertNotNull(request.getOkHttpClient());
        int code = request.code();
        assertTrue(request.ok());
        assertFalse(request.created());
        assertFalse(request.noContent());
        assertFalse(request.badRequest());
        assertFalse(request.serverError());
        assertFalse(request.notFound());
        assertEquals("GET", method.get());
        assertEquals("OK", request.message());
        assertEquals(HTTP_OK, code);
        assertEquals("", request.body());
    }

    /**
     * Make a GET request with an empty body response
     *
     * @throws Exception
     */
    @Test
    public void getNoContent() throws Exception {
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                method.set(request.getMethod());
                response.setStatus(HTTP_NO_CONTENT);
            }
        };
        OkHttpRequest request = get(new URL(url));
//        assertNotNull(request.getConnection());
        assertNotNull(request.getOkHttpClient());
        int code = request.code();
        assertFalse(request.ok());
        assertFalse(request.created());
        assertTrue(request.noContent());
        assertFalse(request.badRequest());
        assertFalse(request.serverError());
        assertFalse(request.notFound());
        assertEquals("GET", method.get());
        assertEquals("No Content", request.message());
        assertEquals(HTTP_NO_CONTENT, code);
        assertEquals("", request.body());
    }

    /**
     * Make a GET request with a URL that needs encoding
     *
     * @throws Exception
     */
    @Test
    public void getUrlEncodedWithSpace() throws Exception {
        String unencoded = "/a resource";
        final AtomicReference<String> path = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                path.set(request.getPathInfo());
                response.setStatus(HTTP_OK);
            }
        };
        OkHttpRequest request = get(encode(url + unencoded));
        assertTrue(request.ok());
        assertEquals(unencoded, path.get());
    }

    /**
     * Make a GET request with a URL that needs encoding
     *
     * @throws Exception
     */
    @Test
    public void getUrlEncodedWithUnicode() throws Exception {
        String unencoded = "/\u00DF";
        final AtomicReference<String> path = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                path.set(request.getPathInfo());
                response.setStatus(HTTP_OK);
            }
        };
        OkHttpRequest request = get(encode(url + unencoded));
        assertTrue(request.ok());
        assertEquals(unencoded, path.get());
    }

    /**
     * Make a GET request with a URL that needs encoding
     *
     * @throws Exception
     */
    @Test
    public void getUrlEncodedWithPercent() throws Exception {
        String unencoded = "/%";
        final AtomicReference<String> path = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                path.set(request.getPathInfo());
                response.setStatus(HTTP_OK);
            }
        };
        OkHttpRequest request = get(encode(url + unencoded));
        assertTrue(request.ok());
        assertEquals(unencoded, path.get());
    }


    /**
     * Verify GET with query parameters
     *
     * @throws Exception
     */
    @Test
    public void getWithMappedQueryParams() throws Exception {
        Map<String, String> inputParams = new HashMap<String, String>();
        inputParams.put("name", "user");
        inputParams.put("number", "100");
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        OkHttpRequest request = get(url, inputParams, false);
        assertTrue(request.ok());
        assertEquals("GET", method.get());
        assertEquals("user", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify GET with query parameters
     *
     * @throws Exception
     */
    @Test
    public void getWithVarargsQueryParams() throws Exception {
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        OkHttpRequest request = get(url, false, "name", "user", "number", "100");
        assertTrue(request.ok());
        assertEquals("GET", method.get());
        assertEquals("user", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify GET with escaped query parameters
     *
     * @throws Exception
     */
    @Test
    public void getWithEscapedMappedQueryParams() throws Exception {
        Map<String, String> inputParams = new HashMap<String, String>();
        inputParams.put("name", "us er");
        inputParams.put("number", "100");
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        OkHttpRequest request = get(url, inputParams, true);
        assertTrue(request.ok());
        assertEquals("GET", method.get());
        assertEquals("us er", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify GET with escaped query parameters
     *
     * @throws Exception
     */
    @Test
    public void getWithEscapedVarargsQueryParams() throws Exception {
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        OkHttpRequest request = get(url, true, "name", "us er", "number", "100");
        assertTrue(request.ok());
        assertEquals("GET", method.get());
        assertEquals("us er", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }


    /******************************* Connection pool Tests ***********************************/
    @Test
    public void getConnectionPool() throws Exception {
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                response.setStatus(HTTP_OK);
            }
        };

        for(int i=0;i<100;i++){
            Thread thread = new Thread(new SimpleRequestThread());
            thread.start();
        }

        Thread.sleep(1000*60*3);
    }

    static class SimpleRequestThread implements Runnable{

        @Override
        public void run(){
            for(int i=0 ;i<100;i++){
                OkHttpRequest request = get(url, true, "name", "us er", "number", "100");
                assertTrue(request.ok());
                ConnectionPool connectionPool = request.getOkHttpClient().getConnectionPool();
                System.out.println(
                        "poolInstance:"+connectionPool.toString()
                                +"ConnectionCount:"+connectionPool.getConnectionCount()
                                +",HttpConnectionCount:"+connectionPool.getHttpConnectionCount()
                                +",IdleConnectionCount:"+connectionPool.getIdleConnectionCount()
                                +",MultiplexedConnectionCount:"+connectionPool.getMultiplexedConnectionCount()
                                +",SpdyConnectionCount:"+connectionPool.getSpdyConnectionCount()
                );

            }
        }
    }

    /******************************* Get Delete Tests ***********************************/

    /**
     * Verify DELETE with query parameters
     *
     * @throws Exception
     */
    @Test
    public void deleteWithMappedQueryParams() throws Exception {
        Map<String, String> inputParams = new HashMap<String, String>();
        inputParams.put("name", "user");
        inputParams.put("number", "100");
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        OkHttpRequest request = delete(url, inputParams, false);
        assertTrue(request.ok());
        assertEquals("DELETE", method.get());
        assertEquals("user", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify DELETE with query parameters
     *
     * @throws Exception
     */
    @Test
    public void deleteWithVarargsQueryParams() throws Exception {
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        OkHttpRequest request = delete(url, false, "name", "user", "number", "100");
        assertTrue(request.ok());
        assertEquals("DELETE", method.get());
        assertEquals("user", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify DELETE with escaped query parameters
     *
     * @throws Exception
     */
    @Test
    public void deleteWithEscapedMappedQueryParams() throws Exception {
        Map<String, String> inputParams = new HashMap<String, String>();
        inputParams.put("name", "us er");
        inputParams.put("number", "100");
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        OkHttpRequest request = delete(url, inputParams, true);
        assertTrue(request.ok());
        assertEquals("DELETE", method.get());
        assertEquals("us er", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

    /**
     * Verify DELETE with escaped query parameters
     *
     * @throws Exception
     */
    @Test
    public void deleteWithEscapedVarargsQueryParams() throws Exception {
        final Map<String, String> outputParams = new HashMap<String, String>();
        final AtomicReference<String> method = new AtomicReference<String>();
        handler = new RequestHandler() {

            @Override
            public void handle(Request request, HttpServletResponse response) {
                method.set(request.getMethod());
                outputParams.put("name", request.getParameter("name"));
                outputParams.put("number", request.getParameter("number"));
                response.setStatus(HTTP_OK);
            }
        };
        OkHttpRequest request = delete(url, true, "name", "us er", "number", "100");
        assertTrue(request.ok());
        assertEquals("DELETE", method.get());
        assertEquals("us er", outputParams.get("name"));
        assertEquals("100", outputParams.get("number"));
    }

}
