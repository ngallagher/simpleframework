package org.simpleframework.transport.reactor;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;

public class ServerExample {
    public static void main(String args[]) throws Exception {
        // Create the server socket channel
        ServerSocketChannel server = ServerSocketChannel.open();
        // nonblocking I/O
        server.configureBlocking(false);
        // host-port 8000
        server.socket().bind(new InetSocketAddress(8000));
        System.out.println("Server actives at port 8000");
        // Create the selector
        Selector selector = Selector.open();
        // Recording server to selector (type OP_ACCEPT)
        server.register(selector, SelectionKey.OP_ACCEPT);

        while (selector.select() > 0) {

            // Get keys
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> i = keys.iterator();

            // print 
            System.out.println("[ " + keys.size() + " ]");

            // For each keys...
            while (i.hasNext()) {
                SelectionKey key = (SelectionKey) i.next();
                // Remove the current key
                i.remove();

                // if isAccetable = true
                // then a client required a connection
                if (key.isAcceptable()) {
                   System.err.println("ACCEPTIBLE");
                    // get client socket channel
                    SocketChannel client = server.accept();
                    // Non Blocking I/O
                    client.configureBlocking(false);
                    // recording to the selector (reading)
                    client.register(selector, SelectionKey.OP_READ);
                    continue;
                }

                // if isReadable = true
                // then the server is ready to read
                if (key.isReadable()) {

                    SocketChannel client = (SocketChannel) key.channel();

                    // Read byte coming from the client
                    int BUFFER_SIZE = 1024;
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    try {
                        client.read(buffer);
                    } catch (Exception e) {
                        // client is no longer active
                        e.printStackTrace();
                    }

                    // Show bytes on the console
                    buffer.flip();
                    Charset charset = Charset.forName("ISO-8859-1");
                    CharsetDecoder decoder = charset.newDecoder();
                    CharBuffer charBuffer = decoder.decode(buffer);
                    System.out.println("[" + charBuffer.toString() + "]");
                }
            }
        }
    }
}