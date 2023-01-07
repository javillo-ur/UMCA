package server;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JList;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ServerLogger extends JFrame {
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	
	private JList<String> list;
	private DefaultListModel<String> dlm;
	
	private Thread main;

	public ServerLogger(Thread thread) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				main.interrupt();
			}
		});
		main = thread;
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		
		dlm = new DefaultListModel<String>();
		list = new JList<String>(dlm);
		contentPane.add(list);
		
		setTitle("Log del servidor");
	}

	public void addLog(String message) {
		dlm.addElement(message);
	}
}
