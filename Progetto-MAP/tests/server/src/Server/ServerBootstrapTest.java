package Server;

import org.junit.Test;
import testsupport.TestSupport;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServerBootstrapTest {
    @Test
    public void multiServerReportsOccupiedPort() throws Exception {
        try (ServerSocket occupied = new ServerSocket(0)) {
            String output = TestSupport.captureOutput(() -> new MultiServer(occupied.getLocalPort()));
            assertTrue(output.contains("BindException"));
        }
    }

    @Test
    public void serverMainUsesExplicitPort() throws Exception {
        try (ServerSocket occupied = new ServerSocket(0)) {
            String output = TestSupport.captureOutput(
                    () -> Server.main(new String[] {Integer.toString(occupied.getLocalPort())}));
            assertTrue(output.contains("BindException"));
        }
    }

    @Test
    public void serverMainUsesDefaultPort() throws Exception {
        ServerSocket occupied = null;
        try {
            occupied = new ServerSocket(8080);
        } catch (java.io.IOException alreadyOccupied) {
            // An existing listener exercises the same bind-failure path.
        }
        try {
            String output = TestSupport.captureOutput(() -> Server.main(new String[0]));
            assertTrue(output.contains("Exception") || output.contains("Address already in use"));
        } finally {
            if (occupied != null) occupied.close();
        }
    }

    @Test
    public void serverReportsMalformedAndOutOfRangePortsWithoutThrowing() throws Exception {
        for (String port : new String[] {"not-a-port", "-1", "65536"}) {
            String output = TestSupport.captureOutput(() -> Server.main(new String[] {port}));
            assertTrue(output.contains("Error: invalid server port"));
        }
    }

    @Test
    public void multiServerAcceptsRealClientAfterMalformedAndIncompleteHandshakes() throws Exception {
        int port;
        try (ServerSocket reservation = new ServerSocket(0)) {
            port = reservation.getLocalPort();
        }

        final int selectedPort = port;
        Thread serverThread = new Thread(() -> new MultiServer(selectedPort), "test-multi-server");
        serverThread.setDaemon(true);
        serverThread.start();

        Socket socket = null;
        for (int attempt = 0; attempt < 50 && socket == null; attempt++) {
            try {
                socket = new Socket("127.0.0.1", port);
            } catch (java.io.IOException notReady) {
                Thread.sleep(20L);
            }
        }
        assertTrue("server did not start", socket != null);
        socket.setSoTimeout(3000);
        try (Socket malformed = socket) {
            malformed.getOutputStream().write(new byte[] {'B', 'A', 'D', '!'});
            malformed.getOutputStream().flush();
            while (malformed.getInputStream().read() != -1) {}
        }
        try (Socket incomplete = new Socket("127.0.0.1", port)) {
            incomplete.setSoTimeout(3000);
            assertEquals(0xac, incomplete.getInputStream().read());
        }
        try (Socket client = new Socket("127.0.0.1", port);
             ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(client.getInputStream())) {
            client.setSoTimeout(3000);
            output.writeObject(99);
            output.writeObject(3);
            output.flush();
            assertEquals("No tree available.", input.readObject());
        }
    }
}
