/*
 * Copyright 2008-2011 Red Hat, Inc, and individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.projectodd.polyglot.web.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Servlet response that buffers in case of undo/retry.
 * 
 * @author Nick Sieger
 * @author Bob McWhirter
 */
public class HttpServletResponseCapture extends HttpServletResponseWrapper {

    private int status;

    public HttpServletResponseCapture(HttpServletResponse response) {
        super( response );
    }

    @Override
    public void sendError(int status, String message) throws IOException {
        this.status = status;
    }

    @Override
    public void sendError(int status) throws IOException {
        this.status = status;
    }

    @Override
    public void sendRedirect(String path) throws IOException {
        this.status = 302;
        super.sendRedirect( path );
    }

    @Override
    public void setStatus(int status) {
        this.status = status;
        if (!isError()) {
            super.setStatus( status );
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setStatus(int status, String message) {
        this.status = status;
        if (!isError()) {
            super.setStatus( status, message );
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        if (!isError()) {
            super.flushBuffer();
        }
    }

    public boolean isError() {
        return status >= 400;
    }
}
