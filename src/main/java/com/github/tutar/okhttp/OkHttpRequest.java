package com.github.tutar.okhttp;


import com.github.tutar.http.Exception.HttpRequestException;
import com.github.tutar.http.Utils;
import com.squareup.okhttp.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.*;

/**
 * 封装Okhttp
 * @author tutar
 */
public class OkHttpRequest {

    /**
     * 'application/json' content type header value
     */
    public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";

    /**
     * 'charset' header value parameter
     */
    public static final String PARAM_CHARSET = "charset";

    /**
     * 'Content-Type' header name
     */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * 'POST' request method
     */
    public static final String METHOD_POST = "POST";

    /**
     * 'GET' request method
     */
    public static final String METHOD_GET = "GET";

    /**
     * 'PUT' request method
     */
    public static final String METHOD_PUT = "PUT";

    /**
     * 'DELETE' request method
     */
    public static final String METHOD_DELETE = "DELETE";

    /**
     * 保证只使用一个连接池
     */
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();

    private Request.Builder builder;
    private Request request;


    private String body;
    private String message;
    private Integer code;


    private final String requestMethod;

    private final String url;

    private Call call = null;
    private int connectTimeout = 10_000;
    private int readTimeout = 10_000;
    private int writeTimeout = 10_000;

    /**
     * 从okHttpClient获取基础特性下情况下，保持当前请求配置可扩展
     */
    private final OkHttpClient currentClient;


    private FormEncodingBuilder formEncodingBuilder;

    /**
     * default content-type:application/json
     */
    private MediaType mediaType = MediaType.parse("application/json; charset=utf-8");

    //全局okHttpClient设置
    static {
        // 默认设置5min
        long defaultKeepAliveDurationMs = 5 * 60 * 1000;
        ConnectionPool connectionPool = new ConnectionPool(5, defaultKeepAliveDurationMs);
        OK_HTTP_CLIENT.setConnectionPool(connectionPool);
    }

    public OkHttpRequest(String url,final String method){
        this.url = url;
        this.requestMethod = method;
        this.builder = new Request.Builder();

        currentClient = OK_HTTP_CLIENT.clone();
    }

    protected OkHttpClient getOkHttpClient(){
        return currentClient;
    }

    /**
     * Write char sequence to request body
     * <p>
     * The charset configured via {@link #contentType(String)} will be used and
     * UTF-8 will be used if it is unset.
     *
     * @param content
     * @return this request
     * @throws HttpRequestException
     */
    public OkHttpRequest send(final String content) throws HttpRequestException {
        RequestBody requestBody = RequestBody.create(mediaType,content);
        doSend(requestBody);
        return this;
    }

    /**
     * Write contents of file to request body
     *
     * @param input
     * @return this request
     * @throws HttpRequestException
     */
    public OkHttpRequest send(final File input) throws HttpRequestException {
        RequestBody requestBody = RequestBody.create(mediaType,input);
        doSend(requestBody);
        return this;
    }

    private OkHttpRequest doSend(RequestBody requestBody){
        try {
            builder = builder.url(url).method(requestMethod, requestBody);
            request = builder.build();
            Response response = createCall(request).execute();
            message = response.message();
            code = response.code();
            body = response.body().string();
            response.body().close();
        } catch (IOException e){
            throw new HttpRequestException(e);
        }
        return this;
    }

    /**
     * 设置支持的扩展属性
     * @param request
     * @return
     */
    private Call createCall(Request request){
//        OkHttpClient currentClient = okHttpClient.clone();
        // 设置 请求参数
        currentClient.setWriteTimeout(writeTimeout,TimeUnit.MILLISECONDS);
        currentClient.setReadTimeout(readTimeout,TimeUnit.MILLISECONDS);
        currentClient.setConnectTimeout(connectTimeout,TimeUnit.MILLISECONDS);

        return currentClient.newCall(request);
    }

    /**
     * Get the status code of the response
     *
     * @return the response code
     * @throws HttpRequestException
     */
    public int code() throws HttpRequestException {
        // 兼容空请求
        if(code == null){
            RequestBody requestBody = null;
            if(METHOD_POST.equals(requestMethod)){
                requestBody = RequestBody.create(mediaType,"");
            }
            doSend(requestBody);
        }
        return code;
    }


    /**
     * Get response as {@link String} in given character set
     * <p>
     * This will fall back to using the UTF-8 character set if the given charset
     * is null
     *
     * @return string
     * @throws HttpRequestException
     */
    public String body() throws HttpRequestException {
        return body;
    }

    /**
     * Get status message of the response
     *
     * @return message
     * @throws HttpRequestException
     */
    public String message() throws HttpRequestException {
        return message;
    }

    /**
     * Set the 'Content-Type' request header to the given value and charset
     *
     * @param contentType
     * @return this request
     */
    public OkHttpRequest contentType(final String contentType) {
        mediaType = MediaType.parse(contentType);
        return this;
    }


    /**
     * Start a 'POST' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpRequestException
     */
    public static OkHttpRequest post(final String url)
            throws HttpRequestException {
        return new OkHttpRequest(url,METHOD_POST);
    }

    /**
     * Start a 'POST' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpRequestException
     */
    public static OkHttpRequest post(final URL url) throws HttpRequestException {
        return post(url.toString());
    }

    /**
     * Start a 'POST' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param params
     *          the query parameters to include as part of the baseUrl
     * @param encode
     *          true to encode the full URL
     *
     * @return request
     */
    public static OkHttpRequest post(final CharSequence baseUrl,
                                   final Map<?, ?> params, final boolean encode) {
        String url = Utils.append(baseUrl, params);
        return post(encode ? Utils.encode(url) : url);
    }

    /**
     * Start a 'POST' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param encode
     *          true to encode the full URL
     * @param params
     *          the name/value query parameter pairs to include as part of the
     *          baseUrl
     * @return request
     */
    public static OkHttpRequest post(final CharSequence baseUrl,
                                   final boolean encode, final Object... params) {
        String url = Utils.append(baseUrl, params);
        return post(encode ? Utils.encode(url) : url);
    }


    /**
     * Start a 'DELETE' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpRequestException
     */
    public static OkHttpRequest delete(final CharSequence url)
            throws HttpRequestException {
        return new OkHttpRequest(url.toString(), METHOD_DELETE);
    }

    /**
     * Start a 'DELETE' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpRequestException
     */
    public static OkHttpRequest delete(final URL url) throws HttpRequestException {
        return new OkHttpRequest(url.toString(), METHOD_DELETE);
    }

    /**
     * Start a 'DELETE' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param params
     *          The query parameters to include as part of the baseUrl
     * @param encode
     *          true to encode the full URL
     *
     * @see Utils#append(CharSequence, Map)
     * @see Utils#encode(CharSequence)
     *
     * @return request
     */
    public static OkHttpRequest delete(final CharSequence baseUrl,
                                     final Map<?, ?> params, final boolean encode) {
        String url = Utils.append(baseUrl, params);
        return delete(encode ? Utils.encode(url) : url);
    }

    /**
     * Start a 'DELETE' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param encode
     *          true to encode the full URL
     * @param params
     *          the name/value query parameter pairs to include as part of the
     *          baseUrl
     *
     * @see Utils#append(CharSequence, Object...)
     * @see Utils#encode(CharSequence)
     *
     * @return request
     */
    public static OkHttpRequest delete(final CharSequence baseUrl,
                                     final boolean encode, final Object... params) {
        String url = Utils.append(baseUrl, params);
        return delete(encode ? Utils.encode(url) : url);
    }

    /**
     * Start a 'PUT' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpRequestException
     */
    public static OkHttpRequest put(final String url)
            throws HttpRequestException {
        return new OkHttpRequest(url, METHOD_PUT);
    }

    /**
     * Start a 'PUT' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpRequestException
     */
    public static OkHttpRequest put(final URL url) throws HttpRequestException {
        return new OkHttpRequest(url.toString(), METHOD_PUT);
    }

    /**
     * Start a 'PUT' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param params
     *          the query parameters to include as part of the baseUrl
     * @param encode
     *          true to encode the full URL
     *
     * @see Utils#append(CharSequence, Map)
     * @see Utils#encode(CharSequence)
     *
     * @return request
     */
    public static OkHttpRequest put(final CharSequence baseUrl,
                                  final Map<?, ?> params, final boolean encode) {
        String url = Utils.append(baseUrl, params);
        return put(encode ? Utils.encode(url) : url);
    }

    /**
     * Start a 'PUT' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param encode
     *          true to encode the full URL
     * @param params
     *          the name/value query parameter pairs to include as part of the
     *          baseUrl
     *
     * @see Utils#append(CharSequence, Object...)
     * @see Utils#encode(CharSequence)
     *
     * @return request
     */
    public static OkHttpRequest put(final CharSequence baseUrl,
                                  final boolean encode, final Object... params) {
        String url = Utils.append(baseUrl, params);
        return put(encode ? Utils.encode(url) : url);
    }

    /**
     * Start a 'GET' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpRequestException
     */
    public static OkHttpRequest get(final String url)
            throws HttpRequestException {

        return new OkHttpRequest(url,METHOD_GET);
    }

    /**
     * Start a 'GET' request to the given URL
     *
     * @param url
     * @return request
     * @throws HttpRequestException
     */
    public static OkHttpRequest get(final URL url) throws HttpRequestException {
        return get(url.toString());
    }

    /**
     * Start a 'GET' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param params
     *          The query parameters to include as part of the baseUrl
     * @param encode
     *          true to encode the full URL
     *
     * @return request
     */
    public static OkHttpRequest get(final CharSequence baseUrl,
                                  final Map<?, ?> params, final boolean encode) {
        String url = Utils.append(baseUrl, params);
        return get(encode ? Utils.encode(url) : url);
    }

    /**
     * Start a 'GET' request to the given URL along with the query params
     *
     * @param baseUrl
     * @param encode
     *          true to encode the full URL
     * @param params
     *          the name/value query parameter pairs to include as part of the
     *          baseUrl
     *
     * @return request
     */
    public static OkHttpRequest get(final CharSequence baseUrl,
                                  final boolean encode, final Object... params) {
        String url = Utils.append(baseUrl, params);
        return get(encode ? Utils.encode(url) : url);
    }


    /**
     * Is the response code a 200 OK?
     *
     * @return true if 200, false otherwise
     * @throws HttpRequestException
     */
    public boolean ok() throws HttpRequestException {
        return HTTP_OK == code();
    }

    /**
     * Is the response code a 201 Created?
     *
     * @return true if 201, false otherwise
     * @throws HttpRequestException
     */
    public boolean created() throws HttpRequestException {
        return HTTP_CREATED == code();
    }

    /**
     * Is the response code a 204 No Content?
     *
     * @return true if 204, false otherwise
     * @throws HttpRequestException
     */
    public boolean noContent() throws HttpRequestException {
        return HTTP_NO_CONTENT == code();
    }

    /**
     * Is the response code a 500 Internal Server Error?
     *
     * @return true if 500, false otherwise
     * @throws HttpRequestException
     */
    public boolean serverError() throws HttpRequestException {
        return HTTP_INTERNAL_ERROR == code();
    }

    /**
     * Is the response code a 400 Bad Request?
     *
     * @return true if 400, false otherwise
     * @throws HttpRequestException
     */
    public boolean badRequest() throws HttpRequestException {
        return HTTP_BAD_REQUEST == code();
    }

    /**
     * Is the response code a 404 Not Found?
     *
     * @return true if 404, false otherwise
     * @throws HttpRequestException
     */
    public boolean notFound() throws HttpRequestException {
        return HTTP_NOT_FOUND == code();
    }

    /**
     * Is the response code a 304 Not Modified?
     *
     * @return true if 304, false otherwise
     * @throws HttpRequestException
     */
    public boolean notModified() throws HttpRequestException {
        return HTTP_NOT_MODIFIED == code();
    }

    public static String encode(final CharSequence url){
        return Utils.encode(url);
    }


    /**
     * Set read timeout on connection to given value
     *
     * @param timeout Time unit representing one thousandth of a second
     * @return this request
     */
    public OkHttpRequest readTimeout(final int timeout) {
        this.readTimeout = timeout;
        return this;
    }

    /**
     * Set connect timeout on connection to given value
     *
     * @param timeout
     * @return this request
     */
    public OkHttpRequest connectTimeout(final int timeout) {
        this.connectTimeout = timeout;
        return this;
    }

    /**
     * Set connect timeout on connection to given value
     *
     * @param timeout
     * @return this request
     */
    public OkHttpRequest writeTimeout(final int timeout) {
        this.writeTimeout = timeout;
        return this;
    }

    /**
     * Get the {@link URL} of this request's connection
     *
     * @return request URL
     */
    public URL url() {
        return request.url();
    }

    /**
     * Get the HTTP method of this request
     *
     * @return method
     */
    public String method() {
        return request.method();
    }

    /**
     * Is the response body empty?
     *
     * @return true if the Content-Length response header is 0, false otherwise
     * @throws HttpRequestException
     */
    public boolean isBodyEmpty() throws HttpRequestException {
        try {
            return request.body()==null || request.body().contentLength() == 0;
        }catch (IOException e){
            throw  new HttpRequestException(e);
        }
    }

}
