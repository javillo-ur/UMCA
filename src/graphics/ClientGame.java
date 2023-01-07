package graphics;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang3.time.StopWatch;

import client.ClientLauncher;
import client.ControlMessage;
import client.GuestHub;
import client.MessageHub;
import client.OwnerHub;
import client.PartyListener;
import client.Phase;
import client.Result;
import minesweeper.Tile;
import model.Turn;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class ClientGame extends JFrame implements Runnable{
	private static final long serialVersionUID = 1L;
	
	private String serverAddress;
	private int port;
	
	private final JPanel contentPanel = new JPanel();
	private PartyListener party;
	
	private Socket s;
	private ExecutorService es;
	
	private Result result = Result.Error;
	private Phase phase = Phase.Wait;
	
	private int turn = -1;
	
	private Game board = null;
	
	private CountDownLatch waitStart = new CountDownLatch(1);
	private CountDownLatch waitGetTurns = new CountDownLatch(1);
	private CountDownLatch waitCreateBoard = new CountDownLatch(1);
	private CountDownLatch waitGetBoard = new CountDownLatch(1);
	private CountDownLatch waitEndGame = new CountDownLatch(1);
	
	private WaitingWindow waitingWindow = null;
	
	private JButton[][] cells;
	
	private MessageHub hub;
	private JTextField txtNombreTurno;
	private JTextField txtNumPlayers;
	private JLabel lblTimer;
	
	private Timer timer;
	private boolean defuse = false;
	
	private int width;
	private int height;

	public ClientGame(Socket s) {
		this.width = ClientLauncher.getBoardWidth();
		this.height = ClientLauncher.getBoardHeight();
		cells = new JButton[width][height];
		this.serverAddress = ClientLauncher.getServerAddress();
		this.port = ClientLauncher.getServerPort();
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				forcedEndParty();
			}

			private void forcedEndParty() {
				waitStart.countDown();
				waitGetTurns.countDown();
				waitGetBoard.countDown();
				waitCreateBoard.countDown();
				waitEndGame.countDown();
			}
		});
		this.s = s;
		es = Executors.newCachedThreadPool(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
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
		txtNombreTurno.setEditable(false);
		panel_1.add(txtNombreTurno);
		txtNombreTurno.setColumns(10);
		
		JLabel lblNDeJugadores = new JLabel("Nº de jugadores:");
		panel_1.add(lblNDeJugadores);
		
		txtNumPlayers = new JTextField();
		txtNumPlayers.setEditable(false);
		panel_1.add(txtNumPlayers);
		txtNumPlayers.setColumns(10);
		
		lblTimer = new JLabel("");
		panel_1.add(lblTimer);
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel);
			panel.setLayout(new GridLayout(height, width, 0, 0));
			for(int y = 0; y < height; y++) {
				for(int x = 0; x < width; x++) {
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
		this.setExtendedState(MAXIMIZED_BOTH);
	}

	public synchronized void startParty() throws InterruptedException {
		phase = Phase.GetTurns;
		waitingWindow.dispose();
		setVisible(true);
		if(party.isOwner()) {
			try (Socket s = new Socket(serverAddress, port);
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
	
	public void printTime(long l) throws InterruptedException, BrokenBarrierException {
		long seconds = Math.floorDiv(l, 1000);
		long millis = l - (seconds * 1000);
		lblTimer.setText(seconds + ":" + millis);
		if(l <= 0 && !defuse) {
			synchronized(this) {
				result = Result.Lose;
				hub.signalEndGame();
				board.click(-1, -1);
				hub.send(board.lastTurnSummary());
				updateCells();
				setEnableCells(false);
				eraseTime();
			}
		}
	}
	
	private void eraseTime() {
		timer.cancel();
		lblTimer.setText("");
	}

	@SuppressWarnings("unchecked")
	public synchronized void receiveMessage(Object message) throws InterruptedException {
		if(message instanceof ControlMessage) {
			receiveControl((ControlMessage) message);
		} else if(message instanceof List<?>) {
			receiveList((List<String>) message);
		} else if(message instanceof Turn) {
			receiveTurn((Turn) message);
		} else if(message instanceof Integer) {
			receiveInteger((int) message);
		} else if(message instanceof Game) {
			setGame((Game) message);
		}
	}
	
	private void receiveInteger(int message) {
		switch(phase) {
			case GetTurns:
				turn = message;
				phase = Phase.CreateBoard;
				waitGetTurns.countDown();
				break;
			default:
				break;
		}
	}

	private void receiveTurn(Turn action) {
		try {
			board.setRectification(action.getRectification());
			board.click(action.getX(), action.getY());
			if(this.turn == board.getTurn()) {
				turn();
			} else {
				updateCells();
				setEnableCells(false);
			}
		} catch (InterruptedException | BrokenBarrierException e1) {
			e1.printStackTrace();
		}
	}

	private void receiveControl(ControlMessage message) throws InterruptedException {
		switch(message) {
			case AssignTurns:
				if(phase == Phase.Wait)
					startParty();
				break;
			case CancelGame:
				cancelParty();
				break;
			default:
				break;
		}
	}
	
	private void receiveList(List<String> message) {
		switch(phase) {
		case Wait:
			if(waitingWindow != null)
				waitingWindow.addPlayer(message);
			break;
		case CreateBoard:
			Game newGame = new Game(message, ClientLauncher.getBoardHeight(), ClientLauncher.getBoardWidth(), ClientLauncher.getBombNumber());
			setGame(newGame);
			waitCreateBoard.countDown();
			break;
		default:
			break;
	}
	}
	
	private void cancelParty() {
		dispose();
		waitStart.countDown();
		waitGetTurns.countDown();
		waitCreateBoard.countDown();
		waitGetBoard.countDown();
		waitEndGame.countDown();
		es.shutdown();
	}

	private void setGame(Game game) {
		this.board = game;
		this.txtNombreTurno.setText(board.getTurnName());
		this.txtNumPlayers.setText(board.getNumPlayers() + "");
		if(turn != game.getTurn())
			setEnableCells(false);
		if(this.waitGetBoard.getCount() > 0)
			waitGetBoard.countDown();
	}

	public void waitStart() throws InterruptedException {
		waitingWindow = new WaitingWindow(party.getPlayerName(), party.isOwner(), this);
		waitingWindow.setVisible(true);
		waitStart.await();
	}
	
	@Override
	public void run() {
		Future<PartyListener> partyTask = es.submit(new SelectParty(s, es));
		try {
			party = partyTask.get();
			if(party == null) {
				result = Result.Cancelled;
				return;
			}
			Thread.currentThread().setName(party.getPlayerName());
			hub = party.isOwner() ? new OwnerHub(es, this, party) 
					: new GuestHub(es, this, party);
			hub.start();
			
			waitStart();
			
			if(party.isOwner()) {
				es.submit(new Runnable() {
					@Override
					public void run() {
						Thread.currentThread().setName("Establecer hub");
						try {
							((OwnerHub) hub).setGame();
						} catch (InterruptedException | ExecutionException e) {
							e.printStackTrace();
						} catch (BrokenBarrierException e) {
							e.printStackTrace();
						}
					}
				});
			}
			
			waitGetTurns.await();
			
			if(turn == 0) {
				firstTurn();
			}
			else {
				waitGetBoard.await();
			}
			
			waitEndGame.await();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		}
	}
	
	private void firstTurn() throws InterruptedException {
		waitCreateBoard.await();
		hub.send(board);
		es.submit(new Runnable() {
			@Override
			public void run() {
				Thread.currentThread().setName("Comenzar turno");
				try {
					turn();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (BrokenBarrierException e) {
					e.printStackTrace();
				}	
			}
		});
	}
	
	private void updateCells() {
		if(board.isInitialised()) {
			for(Tile update : board.getUpdates()) {
				cells[update.getX()][update.getY()].setText(update.isHot() ? "B" : update.getNeighbourBombs() + "");
			}
		}
		this.txtNombreTurno.setText(board.getTurnName());
		this.txtNumPlayers.setText("" + board.getNumPlayers());
	}

	public void turn() throws InterruptedException, BrokenBarrierException {
		if(board.isEnded()) {
			if(result == Result.Error)
				result = Result.Win;
			hub.signalEndGame();
		} else {
			if(board.isInitialised())
				updateCells();
			setTimer(board.getMillis());
			setEnableCells(true);
		}
	}
	
	private void setTimer(int millis) {
		StopWatch sw = new StopWatch();
		sw.start();
		timer = new Timer();
		timer.schedule(new PrintTime(sw, this, millis), 0, 50);
		defuse = false;
	}

	public void setEnableCells(boolean enabled) {
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)
				cells[x][y].setEnabled(enabled && cells[x][y].getText().equals("X"));
	}
	
	public Result getResult() {
		dispose();
		return result;
	}

	public synchronized void buttonClicked(int x, int y) throws InterruptedException, BrokenBarrierException {
		defuse = true;
		Tile tile = board.click(x, y);
		if(tile.isHot()) {
			result = Result.Lose;
			hub.signalEndGame();
		}
		hub.send(board.lastTurnSummary());
		updateCells();
		setEnableCells(false);
		eraseTime();
	}
	
	public void endParty() {
		this.waitEndGame.countDown();
	}
}
