/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package org.glassfish.tyrus.sample.grizzly.http.basic;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.util.Base64Utils;
import org.glassfish.grizzly.http.util.Header;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class MyAuthFilter extends BaseFilter {

    private final Logger logger = Grizzly.logger(MyAuthFilter.class);

    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        logger.log(Level.INFO, "LogFilter handleRead. Connection={0} message={1}",
                   new Object[]{ctx.getConnection(), ctx.getMessage()});

        final HttpContent httpContent = ctx.getMessage();
        if(httpContent.getHttpHeader().containsHeader("Authorization")) {

            final String authHeaderReq = httpContent.getHttpHeader().getHeader("Authorization");

            // TODO: implement some user credential store query
            // TODO: I'm little lazy here, what should be done instead is:
            // TODO:   parse Authorization header
            // TODO:   decode base64 encoded part
            // TODO:   check user/pass values and proceed (return ctx.getInvokeAction()) or end (return HTTP 403).
            
            String user = "user";
            String password = "password";
            String authHeader = user + ":" + password;

            final String authHeaderExpected = "Basic " + Base64Utils.encodeToString(authHeader.getBytes(), false);

            System.out.println("authHeaderReq: " + authHeaderReq);
            System.out.println("authHeaderExpected: " + authHeaderExpected);

            if(authHeaderExpected.equals(authHeaderReq)) {
                return ctx.getInvokeAction();
            } else {
                final HttpResponsePacket response = ((HttpRequestPacket) httpContent.getHttpHeader()).getResponse();
                response.setStatus(403);
                ctx.getConnection().write(response);

                return ctx.getStopAction();

            }

        } else {
            final HttpResponsePacket response = ((HttpRequestPacket) httpContent.getHttpHeader()).getResponse();
            response.setStatus(401);
            response.setHeader(Header.WWWAuthenticate, "Basic");
            ctx.getConnection().write(response);

            return ctx.getStopAction();
        }
    }

    @Override
    public NextAction handleWrite(FilterChainContext ctx) throws IOException {
        logger.log(Level.INFO, "LogFilter handleWrite. Connection={0} message={1}",
                   new Object[]{ctx.getConnection(), ctx.getMessage()});
        return ctx.getInvokeAction();
    }
}
