package com.onarandombox.MultiverseCore.utils.webpaste;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * HTTP API-client.
 */
public abstract class HttpAPIClient {
    /**
     * The URL for this API-request.
     */
    protected final String urlFormat;

    public HttpAPIClient(final String urlFormat) {
        this.urlFormat = urlFormat;
    }

    /**
     * Executes this API-Request.
     *
     * @param args Format-args.
     *
     * @return The result (as text).
     * @throws IOException When the I/O-operation failed.
     */
    protected final String exec(final Object... args) throws IOException {

        final URLConnection conn = new URL(String.format(urlFormat, args)).openConnection();
        conn.connect();
        final StringBuilder ret = new StringBuilder();
        try (final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            while (!reader.ready())
                ; // wait until reader is ready, may not be necessary, SUPPRESS CHECKSTYLE: EmptyStatement

            while (reader.ready()) {
                ret.append(reader.readLine()).append('\n');
            }
        }
        return ret.toString();
    }
}
