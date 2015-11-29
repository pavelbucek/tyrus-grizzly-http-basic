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
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import javax.websocket.DeploymentException;
import javax.websocket.server.ServerEndpointConfig;

import org.glassfish.tyrus.container.grizzly.server.WebSocketAddOnFactory;
import org.glassfish.tyrus.core.TyrusWebSocketEngine;
import org.glassfish.tyrus.server.TyrusServerContainer;
import org.glassfish.tyrus.spi.WebSocketEngine;

import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.http.server.AddOn;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.HttpServerFilter;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.strategies.WorkerThreadIOStrategy;

/**
 * @author Pavel Bucek (pavel.bucek at oracle.com)
 */
public class Main {

    public static void main(String[] args) throws DeploymentException, IOException {


        TyrusServerContainer container = new TyrusServerContainer((Set<Class<?>>) null) {

            private final WebSocketEngine engine =
                    TyrusWebSocketEngine.builder(this)
                            // TODO: Config
//                                        .incomingBufferSize(incomingBufferSize)
//                                        .clusterContext(clusterContext)
//                                        .applicationEventListener(applicationEventListener)
//                                        .maxSessionsPerApp(maxSessionsPerApp)
//                                        .maxSessionsPerRemoteAddr(maxSessionsPerRemoteAddr)
//                                        .parallelBroadcastEnabled(parallelBroadcastEnabled)
//                                        .tracingType(tracingType)
//                                        .tracingThreshold(tracingThreshold)
                            .build();

            private HttpServer server;
            private String contextPath;

            @Override
            public void register(Class<?> endpointClass) throws DeploymentException {
                engine.register(endpointClass, contextPath);
            }

            @Override
            public void register(ServerEndpointConfig serverEndpointConfig) throws DeploymentException {
                engine.register(serverEndpointConfig, contextPath);
            }


            public WebSocketEngine getWebSocketEngine() {
                return engine;
            }

            @Override
            public void start(final String rootPath, int port) throws IOException, DeploymentException {
                contextPath = rootPath;
                server = new HttpServer();
                // TODO
//                final ServerConfiguration config = server.getServerConfiguration();

                final NetworkListener listener = new NetworkListener("grizzly", "0.0.0.0", port);
                server.addListener(listener);

//                // TODO
//                // server = HttpServer.createSimpleServer(rootPath, port);
//                ThreadPoolConfig workerThreadPoolConfig =
//                ThreadPoolConfig selectorThreadPoolConfig =
//
//                // TYRUS-287: configurable server thread pools
//                if (workerThreadPoolConfig != null || selectorThreadPoolConfig != null) {
//                    TCPNIOTransportBuilder transportBuilder = TCPNIOTransportBuilder.newInstance();
//                    if (workerThreadPoolConfig != null) {
//                        transportBuilder.setWorkerThreadPoolConfig(workerThreadPoolConfig);
//                    }
//                    if (selectorThreadPoolConfig != null) {
//                        transportBuilder.setSelectorThreadPoolConfig(selectorThreadPoolConfig);
//                    }
//                    transportBuilder.setIOStrategy(WorkerThreadIOStrategy.getInstance());
//                    server.getListener("grizzly").setTransport(transportBuilder.build());
//                } else {
                // if no configuration is set, just update IO Strategy to worker thread strat.
                server.getListener("grizzly").getTransport().setIOStrategy(WorkerThreadIOStrategy.getInstance());
//                }

                // idle timeout set to indefinite.
                server.getListener("grizzly").getKeepAlive().setIdleTimeoutInSeconds(-1);
                server.getListener("grizzly").registerAddOn(WebSocketAddOnFactory.create(this, contextPath));
                server.getListener("grizzly").registerAddOn(new AddOn() {
                    public void setup(NetworkListener networkListener, FilterChainBuilder filterChainBuilder) {
                        // Get the index of HttpServerFilter in the HttpServer filter chain
                        final int httpServerFilterIdx = filterChainBuilder.indexOfType(HttpServerFilter.class);

                        if (httpServerFilterIdx >= 0) {

                            // GrizzlyWebSocketFilter is at httpServerFilterIdx - 1, we wan't to have this filter
                            // before that one.
                            filterChainBuilder.add(httpServerFilterIdx - 1, new MyAuthFilter());
                        }
                    }
                });

                final WebSocketEngine webSocketEngine = getWebSocketEngine();

                // TODO
//                final Object staticContentPath = localProperties.get(Server.STATIC_CONTENT_ROOT);
//                HttpHandler staticHandler = null;
//                if (staticContentPath != null && !staticContentPath.toString().isEmpty()) {
//                    staticHandler = new StaticHttpHandler(staticContentPath.toString());
//                }

                // TODO
//                final Object wsadl = localProperties.get(TyrusWebSocketEngine.WSADL_SUPPORT);
//
//                if (wsadl != null && wsadl.toString().equalsIgnoreCase("true")) { // wsadl enabled
//                    config.addHttpHandler(new WsadlHttpHandler((TyrusWebSocketEngine) webSocketEngine,
// staticHandler));
//                } else if (staticHandler != null) { // wsadl disabled
//                    config.addHttpHandler(staticHandler);
//                }

                server.start();
                super.start(rootPath, port);
            }

            @Override
            public void stop() {
                super.stop();
                server.shutdownNow();
            }
        };

        container.addEndpoint(EchoEndpoint.class);

        container.start("/ws/", 8080);


        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }
}
