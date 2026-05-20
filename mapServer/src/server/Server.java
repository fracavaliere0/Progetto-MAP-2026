package server;

public class Server {

	public static void main(String[] args) {
		int port = 8080;
		if (args.length > 0) {
			port = new Integer(args[0]).intValue();
		}
		new MultiServer(port);
	}
}
