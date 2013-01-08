package org.torquebox.web.servlet;

import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.ResponseFacade;
import org.jboss.logging.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collections;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

public class SendfileFilter implements Filter {
    public void init(FilterConfig filterConfig) throws ServletException {
        this.servletContext = filterConfig.getServletContext();
    }

    public void destroy() { }

    public void doFilter(ServletRequest request, ServletResponse response, javax.servlet.FilterChain chain)
            throws java.io.IOException, javax.servlet.ServletException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            doFilter( (HttpServletRequest) request, (HttpServletResponse) response, chain );
        }
        else{
            chain.doFilter(request, response);
        }
    }

    protected void doFilter(HttpServletRequest request, HttpServletResponse response, javax.servlet.FilterChain chain)
            throws java.io.IOException, javax.servlet.ServletException {
        // only activate the sendfile filter if connectors are capable and sendfile was not somehow enabled earlier
        boolean doSendfile = isEnabled(request, response) && request.getHeader(HEADER_NAME_SENDFILE_TYPE) == null;
        if(doSendfile){
            request = new SendfileRequest(request);
            response = new SendfileResponse(response);
        }

        chain.doFilter(request, response);

        if(doSendfile && 200 <= response.getStatus() && response.getStatus() < 300){
            File file = getResponseFile(response);
            if(file != null){
                sendFile((SendfileRequest) request, (SendfileResponse) response, file);
                response.flushBuffer();
            }
        }
    }

    protected void sendFile(SendfileRequest request, SendfileResponse response, File file){
        boolean modified = true;
        long fileSize = file.length();
        long mTime = file.lastModified();
        try {
            long since = request.getDateHeader("If-Modified-Since");
            modified = (since < mTime - 1000);     // 1 sec to compensate rounding error
        } catch (IllegalArgumentException e) {
            // silent catch
        }

        if (modified) {
            ByteRange range = new ByteRange(request, fileSize);

            if(range.isSatisfiable()){
                if(range.isPartial()){
                    response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206
                }
                else{
                    response.setStatus(HttpServletResponse.SC_OK);
                }

                response.setHeader("Content-Range",
                        "bytes " + range.getRangeStart() + "-" + range.getRangeEnd() + "/" + fileSize);

                response.setHeader("Accept-Ranges", "bytes");
                if(response.getContentType() == null){
                    String mimeType = servletContext.getMimeType(file.getName().toLowerCase());
                    if (mimeType == null) {
                        mimeType = "application/octet-stream";
                    }
                    response.setContentType(mimeType);
                }
                response.setContentLength(range.getLength());

                if(response.getHeader("Content-Disposition") == null){
                    response.setHeader("Content-Disposition", "inline; filename=" + file.getName());
                }

                if(response.getHeader("Last-Modified") == null){
                    response.setHeader("Last-Modified", httpDateFormat.format(mTime));
                }

                log.debug("Using APR native sendfile to send " +
                        ", " + file.getAbsolutePath() + " | " + range.getFileStartPos() + "-" + range.getFileEndPos());
                ResponseFacade responseFacade = (ResponseFacade) response.getOriginalResponse();
                responseFacade.sendFile(file.getPath(), file.getAbsolutePath(), range.getFileStartPos(), range.getFileEndPos());
            }
            else{
                response.setStatus(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE); // 416
            }
        }
        else {
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED); // 304
        }

    }

    private File getResponseFile(HttpServletResponse response){
        File result = null;
        String filePath = response.getHeader("X-Sendfile");
        if(filePath != null){
            File file = new File(filePath);
            if(file.exists()){
                result = file;
            }
        }
        return result;
    }

    private boolean isEnabled(HttpServletRequest request, HttpServletResponse response){
        return request instanceof RequestFacade &&
               response instanceof ResponseFacade &&
               ((RequestFacade) request).hasSendfile();
    }

    // Util class implementing (single) range functionality described in
    // 14.35.1 Byte Ranges at http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
    private class ByteRange{
        ByteRange(HttpServletRequest request, long fileSize){
            this.fileSize = fileSize;

            String rangeHeader = request.getHeader("Range");
            if(rangeHeader == null){
                rangeHeader = "";
            }
            determineRange(rangeHeader);
        }

        /*
         The following method implements parsing and ignoring of syntactically invalid ranges.
         Multiple ranges are unsupported in the APR connector, which is inherited by this class.

         Examples of valid ranges are

         - The first 500 bytes (byte offsets 0-499, inclusive):  bytes=0-499
         - The second 500 bytes (byte offsets 500-999, inclusive): bytes=500-999
         - The final 500 bytes (byte offsets 9500-9999, inclusive): bytes=-500 Or bytes=9500-

         If the method encounters a range set with multiple ranges, for instance
         - The first and last bytes only (bytes 0 and 9999):  bytes=0-0,-1
         it ignores the ranges as if one of them were syntactically invalid.
        */
        private void determineRange(String range){
            this.start = 0L;
            this.end = this.fileSize - 1;

            Matcher match = rangeHeaderPattern.matcher(range.replace("\\s", ""));
            if(match.find()){
                boolean isSingleRange = match.group(4) == null || "".equals(match.group(4)),
                        hasRangeStart = !"".equals(match.group(2)),
                        hasRangeEnd = !"".equals(match.group(3));
                Long rangeStart = getLongOrNull(match.group(2)),
                        rangeEnd = getLongOrNull(match.group(3));

                if(isSingleRange){
                    if(hasRangeStart && rangeStart != null){
                        // assume normal range
                        if(hasRangeEnd && rangeEnd != null){
                            rangeEnd = Math.min(rangeEnd, Math.max(fileSize - 1, 0L));
                            if(rangeStart <= rangeEnd){
                                // the range is syntactically valid
                                this.start = rangeStart;
                                this.end = rangeEnd;
                            }
                        }
                        else{
                            // no end specified, use default
                            this.start = rangeStart;
                        }
                    }
                    else if (hasRangeEnd && rangeEnd != null){
                        // suffix range, meaning rangeEnd specifies the length of the desired suffix bytes in the file
                        this.start = Math.max(0L, this.fileSize - rangeEnd);
                    }
                }
                else{
                    log.debug("Failed to satisfy range request, multiple HTTP byte ranges requested but only single " +
                            "range supported. Ignoring HTTP Range header.");
                }
            }
        }

        private Long getLongOrNull(String number){
            Long result = null;

            try {
                result = Long.parseLong(number);
            }
            catch(NumberFormatException e){
                // silent catch
            }

            return result;
        }

        public boolean isSatisfiable(){
            return this.start < fileSize && this.start <= this.end;
        }

        public boolean isPartial(){
            return this.start < 0L || this.end < this.fileSize - 1;
        }

        public long getRangeStart(){
            return this.start;
        }

        public long getRangeEnd(){
            return this.end;
        }

        public int getLength(){
            return (int) (getFileEndPos() - getFileStartPos());
        }

        public long getFileStartPos(){
            return Math.max(0L, this.start);
        }

        public long getFileEndPos(){
            return Math.min(this.end + 1, fileSize);
        }

        private Long start, end, fileSize;
    }

    // Request wrapper for adding the sendfile type header
    private class SendfileRequest extends HttpServletRequestWrapper {
        public SendfileRequest(HttpServletRequest request){
            super(request);
        }

        @Override
        public String getHeader(String name) {
            String result = super.getHeader(name);
            if(HEADER_NAME_SENDFILE_TYPE.equals(name)){
                result = HEADER_NAME_SENDFILE;
            }
            return result;
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> list = Collections.list(super.getHeaderNames());
            list.add(HEADER_NAME_SENDFILE_TYPE);
            return Collections.enumeration(list);
        }
    }

    // Response wrapper so the X-Sendfile header isn't sent to the client
    private class SendfileResponse extends HttpServletResponseWrapper {
        private String value = null;
        private HttpServletResponse originalResponse;

        public SendfileResponse(HttpServletResponse response){
            super(response);
            originalResponse = response;
        }

        public HttpServletResponse getOriginalResponse(){
            return this.originalResponse;
        }

        @Override
        public String getHeader(String name) {
            String result = super.getHeader(name);
            if(HEADER_NAME_SENDFILE.equals(name)){
                result = value;
            }
            return result;
        }

        @Override
        public Collection<String> getHeaderNames() {
            List<String> list = new ArrayList<String>(super.getHeaderNames());
            list.add(HEADER_NAME_SENDFILE);
            return list;
        }

        @Override
        public void setHeader(String name, String value) {
            if(HEADER_NAME_SENDFILE.equals(name)){
                this.value = value;
            }
            else {
                super.setHeader(name, value);
            }
        }

        @Override
        public void addHeader(String name, String value) {
            if(HEADER_NAME_SENDFILE.equals(name)){
                this.value = value;
            }
            else {
                super.addHeader(name, value);
            }
        }
    }

    private static final String HEADER_NAME_SENDFILE_TYPE = "X-Sendfile-Type";
    private static final String HEADER_NAME_SENDFILE = "X-Sendfile";
    private static final Pattern rangeHeaderPattern = Pattern.compile("^bytes=((\\d*)-(\\d*))((,\\d*-\\d*)*)?$");
    private static final SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
    private static final Logger log = Logger.getLogger( SendfileFilter.class );
    private ServletContext servletContext;
}
