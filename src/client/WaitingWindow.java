package client;

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.JList;
import javax.swing.JLabel;
import java.awt.GridLayout;

public class WaitingWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JList<String> list;
	private DefaultListModel<String> dlm = new DefaultListModel<String>();

	public WaitingWindow() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel lblEsperandoAComienzo = new JLabel("Esperando a comienzo de partida");
		contentPane.add(lblEsperandoAComienzo);
		
		list = new JList<String>(dlm);
		contentPane.add(list);
		pack();
	}

	public void addPlayer(List<String> readObject) {
		dlm.clear();
		dlm.addAll(readObject);
		pack();
	}
}
