package client;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.DefaultListModel;

import model.Party;

public class UpdateTask extends TimerTask {
	DefaultListModel<Party> parties;
	ObjectInputStream ois;
	ObjectOutputStream oos;
	
	public UpdateTask(DefaultListModel<Party> dlm, ObjectOutputStream os, ObjectInputStream is) {
		this.parties = dlm;
		this.oos = os;
		this.ois = is;
	}
	
	@Override
	public void run() {
		try {
			oos.writeInt(0);
			oos.flush();
			@SuppressWarnings("unchecked")
			List<Party> newParties = (List<Party>)ois.readObject();
			parties.clear();
			parties.addAll(newParties);
		}catch(IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
