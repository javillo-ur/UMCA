package graphics;

import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.JList;
import javax.swing.JLabel;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WaitingWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JList<String> list;
	private DefaultListModel<String> dlm = new DefaultListModel<String>();

	public WaitingWindow(String name, boolean isOwner, ClientGame parent) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel lblEsperandoAComienzo = new JLabel("Esperando a comienzo de partida");
		contentPane.add(lblEsperandoAComienzo);
		
		dlm.add(0, name);
		list = new JList<String>(dlm);
		contentPane.add(list);
		
		if(isOwner) {
			JButton start = new JButton();
			start.setText("Comenzar partida");
			contentPane.add(start);
			start.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						parent.startParty();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			});
		}
		pack();
	}

	public void addPlayer(List<String> readObject) {
		dlm.clear();
		dlm.addAll(readObject);
		pack();
	}
}
