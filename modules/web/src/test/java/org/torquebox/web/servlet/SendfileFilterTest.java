package org.torquebox.web.servlet;

import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.ResponseFacade;
import org.jruby.javasupport.JavaEmbedUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class SendfileFilterTest {
    private File file;
    private ResponseFacade response;
    private RequestFacade request;
    private SendfileFilter filter;
    private FilterChain chain;

    @Before
    public void setUp() throws Exception{
        file = initTestFile("0123456789");
        response = mock(ResponseFacade.class);
        when(response.getStatus()).thenReturn(HttpServletResponse.SC_OK); // 200
        request = mock(RequestFacade.class);
        when(request.hasSendfile()).thenReturn(true);
        filter = initFilter();
        chain = initChain();
    }

    @After
    public void tearDown() {
        if(file != null && file.exists()){
            file.delete();
            file = null;
        }
    }

    private File initTestFile(String content) throws IOException {
        File result = File.createTempFile("temp", ".txt");
        FileWriter out = new FileWriter(result);
        out.write(content);
        out.close();
        return result;
    }

    private SendfileFilter initFilter() throws Exception {
        SendfileFilter result = new SendfileFilter();
        ServletContext context = mock(ServletContext.class);
        when(context.getMimeType(anyString())).thenReturn("application/test");
        FilterConfig config = mock(FilterConfig.class);
        when(config.getServletContext()).thenReturn(context);
        result.init(config);
        return result;
    }

    private FilterChain initChain() throws Exception {
        FilterChain chain = mock(FilterChain.class);

        // running doFilter on the chain always places a file to send
        doAnswer(new Answer() {
            public Object answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                ((HttpServletResponse) args[1]).setHeader("X-Sendfile", file.getAbsolutePath());
                return new Object();
            }
        }).when(chain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        return chain;
    }

    @Test
    public void testSimpleSend() throws Exception {
        filter.doFilter(request, response, chain);

        SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        verify(response).setHeader("Content-Range", "bytes 0-9/10");
        verify(response).setHeader("Accept-Ranges", "bytes");
        verify(response).setHeader("Last-Modified", httpDateFormat.format(file.lastModified()));
        verify(response).setHeader("Content-Disposition", "inline; filename=" + file.getName());
        verify(response).setContentType("application/test");
        verify(response).setContentLength(10);
        verify(response).sendFile(file.getPath(), file.getAbsolutePath(), 0L, 10L);
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testRangePartial() throws Exception {
        when(request.getHeader("Range")).thenReturn("bytes=2-7");
        filter.doFilter(request, response, chain);

        SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        verify(response).setHeader("Content-Range", "bytes 2-7/10");
        verify(response).setHeader("Accept-Ranges", "bytes");
        verify(response).setHeader("Last-Modified", httpDateFormat.format(file.lastModified()));
        verify(response).setHeader("Content-Disposition", "inline; filename=" + file.getName());
        verify(response).setContentType("application/test");
        verify(response).setContentLength(6);
        verify(response).sendFile(file.getPath(), file.getAbsolutePath(), 2L, 8L);
        verify(response).setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
    }

    @Test
    public void testRangeDefaultEnd() throws Exception {
        when(request.getHeader("Range")).thenReturn("bytes=3-");
        filter.doFilter(request, response, chain);
        verify(response).sendFile(file.getPath(), file.getAbsolutePath(), 3L, 10L);
    }

    @Test
    public void testRangeDefault() throws Exception {
        when(request.getHeader("Range")).thenReturn("junk");
        filter.doFilter(request, response, chain);
        verify(response).sendFile(file.getPath(), file.getAbsolutePath(), 0L, 10L);
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testRangeSuffix() throws Exception {
        when(request.getHeader("Range")).thenReturn("bytes=-5");
        filter.doFilter(request, response, chain);
        verify(response).sendFile(file.getPath(), file.getAbsolutePath(), 5L, 10L);
    }

    @Test
    public void testRangeSyntacticallyInvalid() throws Exception {
        when(request.getHeader("Range")).thenReturn("bytes=6-2");
        filter.doFilter(request, response, chain);
        verify(response).sendFile(file.getPath(), file.getAbsolutePath(), 0L, 10L);
        verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    public void testRangeUnsatisfiable() throws Exception {
        when(request.getHeader("Range")).thenReturn("bytes=100-");
        filter.doFilter(request, response, chain);
        verify(response, never()).sendFile(anyString(), anyString(), anyLong(), anyLong());
        verify(response).setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @Test
    public void testRangeEmptyFile() throws Exception {
        file.delete();
        file = initTestFile("");
        when(request.getHeader("Range")).thenReturn("bytes=0-0");
        filter.doFilter(request, response, chain);
        verify(response, never()).sendFile(anyString(), anyString(), anyLong(), anyLong());
        verify(response).setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @Test
    public void testRangeMultiSet() throws Exception {
        when(request.getHeader("Range")).thenReturn("bytes=0-1,-1");
        filter.doFilter(request, response, chain);
        verify(response).sendFile(file.getPath(), file.getAbsolutePath(), 0L, 10L);
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setHeader("Content-Range", "bytes 0-9/10");
    }

    @Test
    public void testNotModified() throws Exception {
        when(request.getDateHeader("If-Modified-Since")).thenReturn(file.lastModified() + 1);
        filter.doFilter(request, response, chain);
        verify(response, never()).sendFile(anyString(), anyString(), anyLong(), anyLong());
        verify(response).setStatus(HttpServletResponse.SC_NOT_MODIFIED);
    }


    @Test
    public void testDisableOnWrongRequestType() throws Exception {
        // if the request if not of the ResponseFacade type, native connectors are disabled
        HttpServletRequest wrongRequestType = mock(HttpServletRequest.class);
        filter.doFilter(wrongRequestType, response, chain);
        verify(response, never()).sendFile(anyString(), anyString(), anyLong(), anyLong());
    }

    @Test
    public void testDisableSendfile() throws Exception {
        // sendfile functionality can be disabled on the connector
        when(request.hasSendfile()).thenReturn(false);
        filter.doFilter(request, response, chain);
        verify(response, never()).sendFile(anyString(), anyString(), anyLong(), anyLong());
    }

    @Test
    public void testDisableSendfileEarlier() throws Exception {
        // some earlier filter or host could be responsible for sendfile
        when(request.getHeader("X-Sendfile-Type")).thenReturn("not null");
        filter.doFilter(request, response, chain);
        verify(response, never()).sendFile(anyString(), anyString(), anyLong(), anyLong());
    }

    @Test
    public void testDoNotAnswerNon2xxCodes() throws Exception {
        when(response.getStatus()).thenReturn(HttpServletResponse.SC_MOVED_PERMANENTLY); // 301
        filter.doFilter(request, response, chain);
        verify(response, never()).sendFile(anyString(), anyString(), anyLong(), anyLong());
    }

    @Test
    public void testNonExistingFile() throws Exception {
        file.delete();
        filter.doFilter(request, response, chain);
        verify(response, never()).sendFile(anyString(), anyString(), anyLong(), anyLong());
    }
}
