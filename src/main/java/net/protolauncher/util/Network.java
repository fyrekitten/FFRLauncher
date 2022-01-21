package net.protolauncher.util;

import javax.net.ssl.HttpsURLConnection;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.function.Consumer;

/**
 * Provides various utilities regarding networking and handling connections.
 */
public class Network {

    // User-agent
    private static final String USER_AGENT = "ProtoLauncher/1.0";

    // Suppress default constructor
    private Network() { }

    /**
     * Creates a request to the given url using the given method.
     * Optionally appends the <code>application/json</code> Content-Type and Accept headers.
     *
     * @param url The url to connect to.
     * @param method The request method to use.
     * @param json Whether to append the <code>application/json</code> Content-Type and Accept headers.
     * @return A new {@link HttpsURLConnection}.
     * @throws IOException Thrown if creating the connection fails.
     */
    public static HttpsURLConnection createConnection(URL url, String method, boolean json) throws IOException {
        // Open a new connection and set the method and user-agent
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("User-Agent", USER_AGENT);

        // If JSON, then set Content-Type and Accept headers
        if (json) {
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
        }

        // Enable reading responses and return the connection
        connection.setDoOutput(true);
        return connection;
    }

    /**
     * Sends a request using the given connection.
     * Optionally will throw an IO exception if the HTTP response code is errored (>= 400).
     *
     * @param connection The connection to get the response from.
     * @param safe Whether to throw an exception if the response code is errored (>= 400).
     * @return A new {@link InputStream}.
     * @throws IOException Thrown if <code>safe</code> is true and the response code is errored (>= 400).
     */
    public static InputStream send(HttpsURLConnection connection, boolean safe) throws IOException {
        // Send the request
        int code = connection.getResponseCode();

        // Handle a "safe" request
        if (safe) {
            if (code >= 400 && code < 500) {
                throw new IOException(code + " Bad Request");
            } else if (code >= 500) {
                throw new IOException(code + " Internal Server Error");
            }
        }

        // Determine the correct stream
        InputStream stream;
        if (code >= 400) {
            stream = connection.getErrorStream();
        } else {
            stream = connection.getInputStream();
        }

        // Return the stream
        return stream;
    }

    /**
     * Sends a request using the given connection erroring if the response code is errored (>= 400).
     * @see Network#send(HttpsURLConnection, boolean)
     */
    public static InputStream send(HttpsURLConnection connection) throws IOException {
        return send(connection, true);
    }

    /**
     * Creates a connection using the given url, sends it, and returns the responding stream.
     * Useful for requesting a file.
     *
     * @param url The url to fetch.
     * @return A new {@link InputStream}.
     * @throws IOException Thrown if the response code is errored (>= 400).
     * @see Network#createConnection(URL, String, boolean)
     * @see Network#send(HttpsURLConnection, boolean)
     */
    public static InputStream fetch(URL url) throws IOException {
        return send(createConnection(url, "GET", false), true);
    }

    /**
     * Sends a HEAD request to the given URL and if the responses contain a Content-Length header,
     * returns the value of it.
     *
     * @param url The url to get the headers for.
     * @return The value of the Content-Length header, or null if there is not one.
     */
    public static Long fetchFileSize(URL url) throws IOException {
        return Long.parseLong(createConnection(url, "HEAD", false).getHeaderField("Content-Length"));
    }

    /**
     * Fetches a file from the given url and copies the stream to the given path,
     * replacing any existing files and giving a progress update during the download.
     * <br/><br/>
     * Essentially the same as {@link Network#download(URL, Path)} but with a progress update consumer.
     *
     * @param url The url of the file.
     * @param path The path to write the file to.
     * @param progressUpdate Called every time there is a progress update in downloading the file.
     * @throws IOException Thrown if the response code is errored (>= 400)
     * @see Network#fetch(URL)
     */
    public static void download(URL url, Path path, Consumer<Long> progressUpdate) throws IOException {
        // Handle "replace existing files"
        Files.deleteIfExists(path);

        // Fetch streams
        InputStream in = fetch(url);
        OutputStream out = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, LinkOption.NOFOLLOW_LINKS);

        // Perform transfer (equivalent to InputStream#transferTo)
        long transferred = 0;
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer, 0, 8192)) >= 0) {
            out.write(buffer, 0, read);
            transferred += read;
            progressUpdate.accept(transferred);
        }
    }

    /**
     * Fetches a file from the given url and copies the stream to the given path,
     * replacing any existing files.
     *
     * @param url The url of the file.
     * @param path The path to write the file to.
     * @throws IOException Thrown if the response code is errored (>= 400).
     * @see Network#fetch(URL)
     */
    public static void download(URL url, Path path) throws IOException {
        Files.copy(fetch(url), path, StandardCopyOption.REPLACE_EXISTING, LinkOption.NOFOLLOW_LINKS);
    }

    /**
     * Attempts to determine if we are connected by pinging the given address up to 5 times,
     * waiting <code>timeout</code> milliseconds between each ping.
     *
     * @param address The {@link Inet4Address} to ping.
     * @param timeout The amount of time in milliseconds between each ping.
     * @return <code>true</code> if the address is reachable, <code>false</code> if not.
     */
    public static boolean isConnected(Inet4Address address, int timeout) {
        return isConnected(address, timeout, 0);
    }

    // The internal function for isConnected.
    private static boolean isConnected(Inet4Address address, int timeout, int attempts) {
        try {
            return address.isReachable(timeout);
        } catch (IOException e) {
            if (attempts >= 5) {
                return false;
            } else {
                return isConnected(address, timeout, ++attempts);
            }
        }
    }

    /**
     * Checks if we are connected by pinging 1.1.1.1,
     * waiting <code>timeout</code> milliseconds between each ping.
     *
     * @see Network#isConnected(Inet4Address, int)
     */
    public static boolean isConnected(int timeout) {
        try {
            return isConnected((Inet4Address) Inet4Address.getByAddress(new byte[] { 1, 1, 1, 1 }), timeout);
        } catch (UnknownHostException e) {
            // If this happens then somebody screwed up.
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if we are connected to the internet by pinging 1.1.1.1 waiting 250 milliseconds between pings.
     * This method will take approximately one second to return if there is no connection.
     *
     * @see Network#isConnected(Inet4Address, int)
     */
    public static boolean isConnected() {
        return isConnected(250);
    }

    /**
     * Converts the given {@link InputStream} to a string.
     *
     * @param stream The {@link InputStream} to convert to a string.
     * @return The given {@link InputStream} as a string.
     * @throws IOException Thrown if there is an error reading the stream.
     */
    public static String stringify(InputStream stream) throws IOException {
        ByteArrayOutputStream string = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = stream.read(buffer)) != -1) {
            string.write(buffer, 0, length);
        }
        return string.toString(StandardCharsets.UTF_8);
    }

}
