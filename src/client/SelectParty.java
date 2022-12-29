package client;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import model.Party;
import javax.swing.DefaultListModel;
import java.awt.GridLayout;
import javax.swing.JList;
import java.awt.FlowLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.awt.event.ActionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class SelectParty extends JFrame implements Callable<Party>{
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private SelectParty own = this;
	private JButton join;
	private JButton details;
	private DefaultListModel<Party> dlm;
	private Socket s;
	private Timer timer = new Timer();
	
	private ObjectOutputStream oos;
	private ObjectInputStream ois;
	
	private CountDownLatch cd = new CountDownLatch(1);
	
	public Party result = null;
	
	public void setParties(List<Party> parties) {
		dlm.removeAllElements();
		for(int i = 0; i < parties.size(); i++)
			dlm.add(i, parties.get(i));
	}

	public SelectParty(Socket s, ExecutorService es) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				joinParty(null);
			}
		});
		this.s = s;
		try {
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		SelectPlayerName task = new SelectPlayerName();
		Future<String> nameTask = es.submit(task);
		try {
			String name = nameTask.get();
			name = (name == null || name.isEmpty()) ? "alguien" : name;
			oos.writeBytes(name + "\r\n");
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			e1.printStackTrace();
		}
		task.dispose();
		
		setTitle("Salas de juego");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 411, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 2, 0, 0));
		
		dlm = new DefaultListModel<Party>();
		JList<Party> list = new JList<Party>(dlm);
		list.setSelectedIndex(-1);
		contentPane.add(list);
		
		JPanel panel = new JPanel();
		contentPane.add(panel);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		JButton btnNewButton = new JButton("Nueva sala");
		panel.add(btnNewButton);
		
		details = new JButton("Ver detalles");
		details.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Party selectedParty = list.getSelectedValue();
				DetallesSala dialog = new DetallesSala(selectedParty, own);
				dialog.setVisible(true);
			}
		});
		details.setEnabled(false);
		panel.add(details);
		
		join = new JButton("Unirse a sala");
		join.setEnabled(false);
		join.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				joinParty(list.getSelectedValue());
			}
		});
		panel.add(join);
		
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				boolean selected = list.getSelectedIndex() != -1;
				join.setEnabled(selected);
				details.setEnabled(selected);
			}
		});
		timer.schedule(new UpdateTask(dlm, oos, ois), 2000);
	}

	public void joinParty(Party party) {
		try {
			if(s != null && !s.isClosed()) {
				if(party != null) {
					oos.writeInt(2);
					oos.writeBytes(party.getOwner() + "\r\n");
					oos.flush();
				}
				oos.writeInt(3);
				oos.flush();
				s.shutdownOutput();
				s.close();
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		result = party;
		cd.countDown();
	}

	@Override
	public Party call() throws Exception {
		try {
			this.setVisible(true);
			cd.await();
		}finally {
			setVisible(false);
			dispose();
			timer.cancel();
		}
		return result;
	}
}
