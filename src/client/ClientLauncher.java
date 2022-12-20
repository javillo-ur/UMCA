package client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

import model.Party;

public class ClientLauncher {
	public static void main(String[] args) {
		Socket s = null;
		try{
			s = new Socket("localhost", 5000);
			DataOutputStream oos = new DataOutputStream(s.getOutputStream());
			SelectParty startPage = new SelectParty(s);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
