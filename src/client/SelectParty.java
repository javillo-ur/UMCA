package client;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;

import model.Party;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;

import java.awt.GridLayout;
import javax.swing.JList;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.event.ActionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class SelectParty extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;
	private SelectParty own = this;
	private JButton join;
	private JButton details;
	private DefaultListModel<Party> dlm;
	private Socket s;
	
	public void setParties(List<Party> parties) {
		dlm.removeAllElements();
		for(int i = 0; i < parties.size(); i++)
			dlm.add(i, parties.get(i));
	}

	public SelectParty(Socket s) {
		this.s = s;
		ObjectOutputStream oos = null;
		ObjectInputStream ois = null;
		try {
			oos = new ObjectOutputStream(s.getOutputStream());
			ois = new ObjectInputStream(s.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("Nombre de juegador: ");
		try (Scanner sc = new Scanner(System.in)) {
			oos.writeBytes(sc.nextLine() + "\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
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
			}
		});
		details.setEnabled(false);
		panel.add(details);
		
		join = new JButton("Unirse a sala");
		join.setEnabled(false);
		panel.add(join);
		
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				boolean selected = list.getSelectedIndex() != -1;
				join.setEnabled(selected);
				details.setEnabled(selected);
			}
		});
		Timer timer = new Timer();
		timer.schedule(new UpdateTask(dlm, oos, ois), 2000);
		setVisible(true);
	}

	public void joinParty(Party party) {
		try {
			if(s != null && !s.isClosed())
				s.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
	}

}
