package client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import minesweeper.Game;
import server.Server;

public class ClientGame extends JDialog implements Callable<Result>{
	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();
	private PartyListener party;
	
	private Socket s;
	private ExecutorService es;
	
	private Result result = Result.Error;
	private Phase phase = Phase.Wait;
	private CountDownLatch cd = new CountDownLatch(1);
	
	private WaitingWindow waitingWindow = null;
	
	private MessageHub hub;

	public ClientGame(Socket s, ExecutorService es) {
		this.s = s;
		this.es = es;
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setLayout(new FlowLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	public void startParty() {
		phase = Phase.Start;
		setVisible(true);
		if(party.isOwner()) {
			try (Socket s = new Socket(Server.serverAddress, Server.serverPort);
				DataOutputStream dos = new DataOutputStream(s.getOutputStream())) {
				dos.writeBoolean(true);
				dos.writeBytes(party.getPlayerName());
				dos.flush();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		cd.countDown();
	}

	@SuppressWarnings("unchecked")
	public void receiveMessage(Object message) {
		Object readObject = ((Message)message).getMessage();
		if(readObject instanceof ControlMessage) {
			switch((ControlMessage)readObject) {
				case StartGame:
					startParty();
				break;
				default:
					break;
			}
		} else if(readObject instanceof List) {
			switch(phase) {
				case Wait:
					if(waitingWindow != null)
						waitingWindow.addPlayer((List<String>) readObject);
					break;
				case Start:
					break;
				default:
					break;
			}
		} else if(readObject instanceof Game) {
			updateGame();
		}
	}
	
	private void updateGame() {
		
	}

	public void sendMessage(Object writeObject) {
		hub.send(writeObject);
	}

	@Override
	public Result call() throws Exception {
		Future<PartyListener> partyTask = es.submit(new SelectParty(s, es));
		try {
			party = partyTask.get();
			hub = party.isOwner() ? new OwnerHub(es, this, party.getServerSocket(), party.getPlayerName()) 
					: new GuestHub(es, this, party.getParty().getOwner().getAddress(), party.getOwnerPort(), 
							party.getPlayerName());
			hub.start();
			waitingWindow = new WaitingWindow();
			waitingWindow.setVisible(true);
			party();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		}
		return result;
	}
	
	public void party() throws InterruptedException {
		cd.await();
	}
}
