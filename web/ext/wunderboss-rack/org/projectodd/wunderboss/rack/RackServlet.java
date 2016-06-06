/*
* Copyright 2014 Red Hat, Inc, and individual contributors.
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

package org.projectodd.wunderboss.rack;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.DispatcherType;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.resource.CachingResourceManager;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.resource.RangeAwareResource;
import io.undertow.server.handlers.resource.Resource;
import io.undertow.server.handlers.resource.ResourceManager;
import io.undertow.servlet.handlers.ServletRequestContext;
import io.undertow.util.ByteRange;
import io.undertow.util.CanonicalPathUtils;
import io.undertow.util.DateUtils;
import io.undertow.util.ETag;
import io.undertow.util.ETagUtils;
import io.undertow.util.Headers;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import org.jruby.runtime.builtin.IRubyObject;
import org.projectodd.wunderboss.WunderBoss;
import org.slf4j.Logger;

public class RackServlet extends GenericServlet {

    public RackServlet(final IRubyObject rackApplication, String staticPath) throws IOException {
        this.rackApplication = new RackApplication(rackApplication);
        this.staticPath = staticPath;
        if (staticPath != null) {
            setupStaticContent();
        }
    }

    @Override
    public final void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
            service((HttpServletRequest) request, (HttpServletResponse) response);
        }
    }

    public final void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            if (!doStaticContent(request, response)) {
                RackAdapter adapter = new ServletRackAdapter(request, response);
                rackApplication.call(adapter, RackEnvironment.RACK_KEY.SERVLET_REQUEST, request);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * TODO: This static content code is copied from WunderBoss and Undertow
     * Long-term we should add a way to handle static content in WunderBoss
     * itself.
     */
    protected void setupStaticContent() {
        if (!new File(staticPath).isAbsolute()) {
            staticPath = WunderBoss.options().get("root", "").toString() + File.separator + staticPath;
        }
        if (!new File(staticPath).exists()) {
            log.debug("Not serving static content for nonexistent directory {}", staticPath);
            staticPath = null;
            return;
        }
        log.debug("Serving static content for {}", staticPath);
        this.resourceManager =
                new CachingResourceManager(1000, 1L, null,
                                           new FileResourceManager(new File(staticPath), 1 * 1024 * 1024), 250);
    }
    protected boolean doStaticContent(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (staticPath == null || resourceManager == null) {
            return false;
        }
        if (!(request.getMethod().equals(Methods.GET_STRING) ||
                request.getMethod().equals(Methods.HEAD_STRING))) {
            return false;
        }
        String path = getPath(request);
        Resource resource = resourceManager.getResource(path);
        if (resource == null) {
            return false;
        }
        if (resource.isDirectory()) {
            resource = getIndexFiles(resourceManager, resource.getPath(), welcomeFileList);
            if (resource == null) {
                return false;
            }
        }
        serveFileBlocking(request, response, resource);
        return true;
    }
    private String getPath(final HttpServletRequest request) {
        String servletPath = request.getServletPath();
        String pathInfo = request.getPathInfo();

        String result = pathInfo;
        if (result == null) {
            result = servletPath;
        } else {
            result = CanonicalPathUtils.canonicalize(result);
        }
        if ((result == null) || (result.equals(""))) {
            result = "/";
        }
        return result;

    }
    protected Resource getIndexFiles(ResourceManager resourceManager, final String base, List<String> possible) throws IOException {
        String realBase;
        if (base.endsWith("/")) {
            realBase = base;
        } else {
            realBase = base + "/";
        }
        for (String possibility : possible) {
            Resource index = resourceManager.getResource(realBase + possibility);
            if (index != null) {
                return index;
            }
        }
        return null;
    }
    private void serveFileBlocking(final HttpServletRequest req, final HttpServletResponse resp, final Resource resource) throws IOException {
        final ETag etag = resource.getETag();
        final Date lastModified = resource.getLastModified();
        if(req.getDispatcherType() != DispatcherType.INCLUDE) {
            if (!ETagUtils.handleIfMatch(req.getHeader(Headers.IF_MATCH_STRING), etag, false) ||
                    !DateUtils.handleIfUnmodifiedSince(req.getHeader(Headers.IF_UNMODIFIED_SINCE_STRING), lastModified)) {
                resp.setStatus(StatusCodes.PRECONDITION_FAILED);
                return;
            }
            if (!ETagUtils.handleIfNoneMatch(req.getHeader(Headers.IF_NONE_MATCH_STRING), etag, true) ||
                    !DateUtils.handleIfModifiedSince(req.getHeader(Headers.IF_MODIFIED_SINCE_STRING), lastModified)) {
                resp.setStatus(StatusCodes.NOT_MODIFIED);
                return;
            }
        }

        //we are going to proceed. Set the appropriate headers
        if(resp.getContentType() == null) {
            final String contentType = getServletContext().getMimeType(resource.getName());
            if (contentType != null) {
                resp.setContentType(contentType);
            } else {
                resp.setContentType("application/octet-stream");
            }
        }
        if (lastModified != null) {
            resp.setHeader(Headers.LAST_MODIFIED_STRING, resource.getLastModifiedString());
        }
        if (etag != null) {
            resp.setHeader(Headers.ETAG_STRING, etag.toString());
        }
        ByteRange range = null;
        long start = -1, end = -1;
        try {
            //only set the content length if we are using a stream
            //if we are using a writer who knows what the length will end up being
            //todo: if someone installs a filter this can cause problems
            //not sure how best to deal with this
            //we also can't deal with range requests if a writer is in use
            Long contentLength = resource.getContentLength();
            if (contentLength != null) {
                resp.getOutputStream();
                if(contentLength > Integer.MAX_VALUE) {
                    resp.setContentLengthLong(contentLength);
                } else {
                    resp.setContentLength(contentLength.intValue());
                }
                if(resource instanceof RangeAwareResource && ((RangeAwareResource)resource).isRangeSupported()) {
                    //TODO: figure out what to do with the content encoded resource manager
                    range = ByteRange.parse(req.getHeader(Headers.RANGE_STRING));
                    if(range != null && range.getRanges() == 1) {
                        start = range.getStart(0);
                        end = range.getEnd(0);
                        if(start == -1 ) {
                            //suffix range
                            long toWrite = end;
                            if(toWrite >= 0) {
                                if(toWrite > Integer.MAX_VALUE) {
                                    resp.setContentLengthLong(toWrite);
                                } else {
                                    resp.setContentLength((int)toWrite);
                                }
                            } else {
                                //ignore the range request
                                range = null;
                            }
                            start = contentLength - end;
                            end = contentLength;
                        } else if(end == -1) {
                            //prefix range
                            long toWrite = contentLength - start;
                            if(toWrite >= 0) {
                                if(toWrite > Integer.MAX_VALUE) {
                                    resp.setContentLengthLong(toWrite);
                                } else {
                                    resp.setContentLength((int)toWrite);
                                }
                            } else {
                                //ignore the range request
                                range = null;
                            }
                            end = contentLength;
                        } else {
                            long toWrite = end - start + 1;
                            if(toWrite > Integer.MAX_VALUE) {
                                resp.setContentLengthLong(toWrite);
                            } else {
                                resp.setContentLength((int)toWrite);
                            }
                        }
                        if(range != null) {
                            resp.setStatus(StatusCodes.PARTIAL_CONTENT);
                            resp.setHeader(Headers.CONTENT_RANGE_STRING, range.getStart(0) + "-" + range.getEnd(0) + "/" + contentLength);
                        }
                    }
                }
            }
        } catch (IllegalStateException e) {

        }
        final boolean include = req.getDispatcherType() == DispatcherType.INCLUDE;
        if (!req.getMethod().equals(Methods.HEAD_STRING)) {
            HttpServerExchange exchange = ServletRequestContext.requireCurrent().getOriginalRequest().getExchange();
            if(range == null) {
                resource.serve(exchange.getResponseSender(), exchange, completionCallback(include));
            } else {
                ((RangeAwareResource)resource).serveRange(exchange.getResponseSender(), exchange, start, end, completionCallback(include));
            }
        }
    }
    private IoCallback completionCallback(final boolean include) {
        return new IoCallback() {

            @Override
            public void onComplete(final HttpServerExchange exchange, final Sender sender) {
                if (!include) {
                    sender.close();
                }
            }

            @Override
            public void onException(final HttpServerExchange exchange, final Sender sender, final IOException exception) {
                //not much we can do here, the connection is broken
                sender.close();
            }
        };
    }

    private RackApplication rackApplication;
    private String staticPath;
    private ResourceManager resourceManager;

    private static final Logger log = WunderBoss.logger(RackServlet.class);

    private static final String[] welcomeFiles = new String[] { "index.html", "index.html", "default.html", "default.htm" };
    private static final List<String> welcomeFileList = new CopyOnWriteArrayList<>(welcomeFiles);
}
