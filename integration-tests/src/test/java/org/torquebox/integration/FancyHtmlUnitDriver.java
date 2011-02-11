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

package org.torquebox.integration;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.CredentialsNotAvailableException;
import org.apache.commons.httpclient.auth.CredentialsProvider;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.gargoylesoftware.htmlunit.DefaultCredentialsProvider;
import com.gargoylesoftware.htmlunit.WebClient;

public class FancyHtmlUnitDriver extends HtmlUnitDriver implements CredentialsProvider {

    DefaultCredentialsProvider credentialsProvider;

    public FancyHtmlUnitDriver() {
        this.credentialsProvider = new DefaultCredentialsProvider();
    }

    @Override
    protected WebClient modifyWebClient(WebClient client) {
        client.setCredentialsProvider( this );
        return client;
    }

    public void setCredentials(String username, String password) {
        resetCredentials();
        this.credentialsProvider.addCredentials( username, password );
    }

    public void resetCredentials() {
        this.credentialsProvider = new DefaultCredentialsProvider();
    }

    @Override
    public Credentials getCredentials(AuthScheme scheme, String host, int port, boolean proxy) throws CredentialsNotAvailableException {
        return this.credentialsProvider.getCredentials( scheme, host, port, proxy );
    }

}
