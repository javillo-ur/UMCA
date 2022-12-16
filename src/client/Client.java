package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

import model.Party;

public class Client {
	public static void main(String[] args) {
		try(Socket s = new Socket("localhost", 5000)){
			ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
			ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
			System.out.println("Nombre de juegador: ");
			Scanner sc = new Scanner(System.in);
			oos.writeBytes(sc.nextLine() + "\r\n");
			oos.writeInt(0);
			oos.writeInt(-1);
			oos.flush();
			@SuppressWarnings("unchecked")
			List<Party> parties = (List<Party>)ois.readObject();
			for(Party party : parties)
				System.out.println(party);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}