/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
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
 *
 */

package com.sun.grizzly.http;

import com.sun.grizzly.Connection;
import com.sun.grizzly.ThreadCache;
import com.sun.grizzly.http.HttpCodecFilter.ContentParsingState;

/**
 *
 * @author oleksiys
 */
class HttpRequestPacketImpl extends HttpRequestPacket implements HttpPacketParsing {
    private static final ThreadCache.CachedTypeIndex<HttpRequestPacketImpl> CACHE_IDX =
            ThreadCache.obtainIndex(HttpRequestPacketImpl.class, 2);

    public static HttpRequestPacketImpl create() {
        final HttpRequestPacketImpl httpRequestImpl =
                ThreadCache.takeFromCache(CACHE_IDX);
        if (httpRequestImpl != null) {
            return httpRequestImpl;
        }

        return new HttpRequestPacketImpl();
    }

    private boolean isHeaderParsed;
    
    private final HttpCodecFilter.ParsingState headerParsingState;
    private final HttpCodecFilter.ContentParsingState contentParsingState;
    private final ProcessingState processingState;

    private HttpRequestPacketImpl() {
        this.headerParsingState = new HttpCodecFilter.ParsingState();
        this.contentParsingState = new HttpCodecFilter.ContentParsingState();
        this.processingState = new ProcessingState();
    }

    public void initialize(Connection connection, int initialOffset,
            int maxHeaderSize) {
        headerParsingState.initialize(initialOffset, maxHeaderSize);
        setConnection(connection);
    }

    public ProcessingState getProcessingState() {
        return processingState;
    }

    @Override
    public HttpCodecFilter.ParsingState getHeaderParsingState() {
        return headerParsingState;
    }

    @Override
    public ContentParsingState getContentParsingState() {
        return contentParsingState;
    }

    @Override
    public boolean isHeaderParsed() {
        return isHeaderParsed;
    }

    @Override
    public void setHeaderParsed(boolean isHeaderParsed) {
        if (isHeaderParsed && !isChunked) {
            contentParsingState.chunkRemainder = getContentLength();
        }
        
        this.isHeaderParsed = isHeaderParsed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        headerParsingState.recycle();
        contentParsingState.recycle();
        processingState.recycle();
        isHeaderParsed = false;
        super.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recycle() {
        reset();
        ThreadCache.putToCache(CACHE_IDX, this);
    }
}
