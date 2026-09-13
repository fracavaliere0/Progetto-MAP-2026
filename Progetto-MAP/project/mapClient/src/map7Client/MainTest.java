package map7Client;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import utility.Keyboard;


public class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args){
		if (args.length < 2) {
			System.out.println("Usage: java map7Client.MainTest <host> <port>");
			return;
		}
		int port;
		try {
			port = Integer.parseInt(args[1]);
			if (port < 1 || port > 65535) {
				throw new IllegalArgumentException();
			}
		} catch (IllegalArgumentException e) {
			System.out.println("Error: invalid server port. Use an integer between 1 and 65535.");
			return;
		}

		try (Socket socket = new Socket(args[0], port);
				ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream()); // stream con richieste del client
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
			System.out.println(socket);

			int decision;
			do {
				System.out.println("Learn Regression Tree from data [1]");
				System.out.println("Load Regression Tree from archive [2]");
				decision = readInt();
			} while (decision != 1 && decision != 2);

			out.writeObject(decision == 1 ? 0 : 2);
			out.flush();
			String answer;
			do {
				System.out.println(decision == 1 ? "Table name:" : "File name:");
				String tableName = Keyboard.readString();
				if (tableName == null) {
					return;
				}
				out.writeObject(tableName);
				out.flush();
				answer = in.readObject().toString();
				System.out.println(answer);
				if (!answer.equals("Table found!")) {
					System.out.println("Wrong table. Try again.");
				}
			} while (!answer.equals("Table found!"));

			answer = in.readObject().toString();
			if (!answer.equals("OK")) {
				System.out.println(answer);
				return;
			}

			if (decision == 1) {
				System.out.println("Starting data acquisition phase!");
				System.out.println("Starting learning phase!");
				out.writeObject(1);
				out.flush();
				answer = in.readObject().toString();
				if (!answer.equals("OK")) {
					System.out.println(answer);
					return;
				}
			}

			// .........

			char risp;
			do {
				out.writeObject(3);
				out.flush();
				System.out.println("Starting prediction phase!");
				answer = in.readObject().toString();

				while (answer.equals("QUERY")) {
					// Formualting query, reading answer
					answer = in.readObject().toString();
					if (answer.equals("OK")) {
						break;
					}
					System.out.println(answer);
					int path = readInt();
					out.writeObject(path);
					out.flush();
					answer = in.readObject().toString();
				}

				if (answer.equals("OK")) {
					// Reading prediction
					answer = in.readObject().toString();
					System.out.println("Predicted class:" + answer);
				} else {
					//Printing error message
					System.out.println(answer);
				}

				System.out.println("Would you repeat ? (y/n)");
				do {
					risp = Character.toUpperCase(Keyboard.readChar());
					if (risp == Character.MIN_VALUE) {
						return;
					}
					if (risp != 'Y' && risp != 'N') {
						System.out.println("Please type y or n");
					}
				} while (risp != 'Y' && risp != 'N');
			} while (risp == 'Y');
		} catch (IOException | ClassNotFoundException e) {
			System.out.println(e.toString());
		}
	}

	private static int readInt() throws EOFException {
		String token = Keyboard.readWord();
		if (token == null) {
			throw new EOFException("Input closed.");
		}
		try {
			return Integer.parseInt(token);
		} catch (NumberFormatException e) {
			System.out.println("Please type an integer");
			return Integer.MIN_VALUE;
		}
	}
}
