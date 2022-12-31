package graphics;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
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
import client.MessageHub;
import client.OwnerHub;
import client.PartyListener;
import client.Phase;
import client.Result;
import minesweeper.Tile;
import model.Message;
import server.Server;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class ClientGame extends JDialog implements Runnable{
	private static final long serialVersionUID = 1L;
	private static final int width = 30;
	private static final int height = 16;
	
	private final JPanel contentPanel = new JPanel();
	private PartyListener party;
	
	private Socket s;
	private ExecutorService es;
	
	private Result result = Result.Error;
	private Phase phase = Phase.Wait;
	
	private int turn = -1;
	
	private Game board = null;
	
	private boolean stillAlive = true;
	
	private CountDownLatch waitStart = new CountDownLatch(1);
	private CountDownLatch waitGetTurns = new CountDownLatch(1);
	private CountDownLatch waitCreateBoard = new CountDownLatch(1);
	private CountDownLatch waitGetBoard = new CountDownLatch(1);
	private CountDownLatch waitEndGame = new CountDownLatch(1);
	private CountDownLatch waitAction = new CountDownLatch(1);
	
	private WaitingWindow waitingWindow = null;
	
	private JButton[][] cells = new JButton[width][height];
	
	private MessageHub hub;
	private JTextField txtNombreTurno;
	private JTextField txtNumPlayers;

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
		
		JPanel panel_1 = new JPanel();
		contentPanel.add(panel_1);
		panel_1.setLayout(new GridLayout(1, 0, 0, 0));
		
		JLabel lblTurnoDe = new JLabel("Turno de:");
		panel_1.add(lblTurnoDe);
		
		txtNombreTurno = new JTextField();
		txtNombreTurno.setEnabled(false);
		txtNombreTurno.setEditable(false);
		panel_1.add(txtNombreTurno);
		txtNombreTurno.setColumns(10);
		
		JLabel lblNDeJugadores = new JLabel("Nº de jugadores:");
		panel_1.add(lblNDeJugadores);
		
		txtNumPlayers = new JTextField();
		txtNumPlayers.setEditable(false);
		panel_1.add(txtNumPlayers);
		txtNumPlayers.setColumns(10);
		
		JLabel lblNews = new JLabel("");
		panel_1.add(lblNews);
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel);
			panel.setLayout(new GridLayout(height, width, 0, 0));
			for(int x = 0; x < width; x++) {
				for(int y = 0; y < height; y++) {
					JButton cell = new JButton();
					cells[x][y] = cell;
					panel.add(cell);
					cell.setText("X");
					cell.setEnabled(false);
					cell.addActionListener(new ButtonListener(x, y, this));
				}
			}
		}
		this.pack();
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
	public synchronized void receiveMessage(Object message) throws InterruptedException {
		Object readObject = ((Message<?>)message).getMessage();
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
				case CreateBoard:
					List<String> turnNames = (List<String>)readObject;
					board = new Game(turnNames);
					waitCreateBoard.countDown();
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
		waitAction.countDown();
		es.shutdown();
	}

	private void updateGame(Game game) {
		this.board = game;
		updateCells();
		if(this.waitGetBoard.getCount() > 0)
			waitGetBoard.countDown();
		if(turn == game.getTurn()) {
			try {
				turn();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				e.printStackTrace();
			}
		} else {
			setEnableCells(false);
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
						try {
							requestTurns();
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						}
					}
				});
			}
			System.out.println("Wait for turns");
			waitGetTurns.await();
			System.out.println("Wait for board");
			if(turn == 0) {
				System.out.println("I'll create the board");
				waitCreateBoard.await();
				hub.send(board);
				firstTurn();
			}
			else
				waitGetBoard.await();
			System.out.println("Game has started");
			waitEndGame.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		}
	}
	
	private void firstTurn() throws InterruptedException {
		setEnableCells(true);
		waitAction.await();
		if(!stillAlive) {
			result = Result.Lose;
			waitEndGame.countDown();
		}
		endTurn();
	}
	
	private void updateCells() {
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++) {
				Tile tile = board.get(x, y);
				if(tile.isDisplayed()) {
					cells[x][y].setText(tile.isHot() ? "B" : tile.getNeighbourBombs() + "");
				}
			}
	}

	public void turn() throws InterruptedException, BrokenBarrierException {
		startTurn();
		waitAction.await();
		if(!stillAlive) {
			result = Result.Lose;
		}
		endTurn();
	}
	
	public void startTurn() throws InterruptedException, BrokenBarrierException {
		waitAction = new CountDownLatch(1);
		setEnableCells(true);
	}
	
	public void setEnableCells(boolean enabled) {
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				cells[x][y].setEnabled(enabled);
	}
	
	public void endTurn() {
		if(stillAlive)
			board.nextTurn();
		else
			board.removePlayer();
		hub.send(board);
	}
	
	public void requestTurns() throws InterruptedException, ExecutionException {
		((OwnerHub) hub).sendTurns();
	}
	
	public Result getResult() {
		dispose();
		return result;
	}

	public synchronized void buttonClicked(int x, int y) throws InterruptedException, BrokenBarrierException {
		Tile tile = board.click(x, y);
		if(tile.isHot())
			board.removePlayer();
		else
			board.nextTurn();
		updateGame(board);
		setEnableCells(false);
		stillAlive = !tile.isHot();
		waitAction.countDown();
	}
}
