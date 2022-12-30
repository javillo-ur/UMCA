package graphics;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.ControlMessage;
import client.GuestHub;
import client.Message;
import client.MessageHub;
import client.OwnerHub;
import client.PartyListener;
import client.Phase;
import client.Result;
import minesweeper.Game;
import server.Server;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ClientGame extends JDialog implements Runnable{
	private static final long serialVersionUID = 1L;
	
	private final JPanel contentPanel = new JPanel();
	private PartyListener party;
	
	private Socket s;
	private ExecutorService es;
	
	private Result result = Result.Error;
	private Phase phase = Phase.Wait;
	
	private int turn = -1;
	private int numPlayers = -1;
	
	private Game board = null;
	
	private CountDownLatch waitStart = new CountDownLatch(1);
	private CountDownLatch waitGetTurns = new CountDownLatch(1);
	private CountDownLatch waitCreateBoard = new CountDownLatch(1);
	private CountDownLatch waitGetBoard = new CountDownLatch(1);
	private CountDownLatch waitEndGame = new CountDownLatch(1);
	
	private WaitingWindow waitingWindow = null;
	
	private MessageHub hub;

	public ClientGame(Socket s) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				hub.send(ControlMessage.CancelGame);
				cancelParty();
			}
		});
		this.s = s;
		es = Executors.newCachedThreadPool(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = Executors.defaultThreadFactory().newThread(r);
				thread.setDaemon(true);
				return thread;
			}
		});
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
		phase = Phase.GetTurns;
		waitingWindow.dispose();
		setVisible(true);
		if(party.isOwner()) {
			try (Socket s = new Socket(Server.serverAddress, Server.serverPort);
				ObjectOutputStream dos = new ObjectOutputStream(s.getOutputStream());
				ObjectInputStream dis = new ObjectInputStream(s.getInputStream())) {
				dos.writeBoolean(true);
				dos.writeBytes(party.getPlayerName() + "\r\n");
				dos.flush();
			} catch(IOException e) {
				e.printStackTrace();
			}
			hub.send(ControlMessage.AssignTurns);
		}
		waitStart.countDown();
	}

	@SuppressWarnings("unchecked")
	public synchronized void receiveMessage(Object message) {
		Object readObject = ((Message)message).getMessage();
		if(readObject instanceof ControlMessage) {
			switch((ControlMessage)readObject) {
				case AssignTurns:
					startParty();
				break;
				case CancelGame:
					cancelParty();
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
			updateGame((Game) readObject);
		} else if(readObject instanceof Integer) {
			switch(phase) {
				case GetTurns:
					turn = (int)readObject;
					phase = Phase.CreateBoard;
					waitGetTurns.countDown();
					break;
				case CreateBoard:
					numPlayers = (int)readObject;
					board = new Game(numPlayers);
					waitCreateBoard.countDown();
					break;
				default:
					break;
			}
		}
	}
	
	private void cancelParty() {
		dispose();
		waitStart.countDown();
		waitGetTurns.countDown();
		waitCreateBoard.countDown();
		waitGetBoard.countDown();
		waitEndGame.countDown();
	}

	private void updateGame(Game game) {
		if(this.waitGetBoard.getCount() > 0)
			waitGetBoard.countDown();
		if(turn == game.getTurn()) {
			turn();
		}
	}

	public void sendMessage(Object writeObject) {
		hub.send(writeObject);
	}

	@Override
	public void run() {
		Future<PartyListener> partyTask = es.submit(new SelectParty(s, es));
		try {
			party = partyTask.get();
			hub = party.isOwner() ? new OwnerHub(es, this, party.getServerSocket(), party.getPlayerName()) 
					: new GuestHub(es, this, party.getParty().getOwner().getAddress(), party.getOwnerPort(), 
							party.getPlayerName());
			hub.start();
			waitingWindow = new WaitingWindow(party.getPlayerName(), party.isOwner(), this);
			waitingWindow.setVisible(true);
			System.out.println("Wait to start");
			waitStart.await();
			if(party.isOwner()) {
				es.submit(new Runnable() {
					@Override
					public void run() {
						requestTurns();
					}
				});
			}
			System.out.println("Wait for turns");
			waitGetTurns.await();
			if(turn == 0) {
				waitCreateBoard.await();
				hub.send(board);
				updateGame(board);
			}
			System.out.println("We will start now (I'm turn " + (turn + 1) + ")");
			waitGetBoard.await();
			waitEndGame.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		}
	}
	
	public void turn() {
		System.out.println("My turn");
	}
	
	public void requestTurns() {
		((OwnerHub) hub).sendTurns();
	}
	
	public Result getResult() {
		dispose();
		return result;
	}
}
