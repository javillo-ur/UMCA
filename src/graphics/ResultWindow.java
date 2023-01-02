package graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import client.Result;

import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class ResultWindow extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPane;

	public ResultWindow(Result result) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel lblEstado = new JLabel("Estado");
		contentPane.add(lblEstado);
		
		JButton btnOkay = new JButton("Okay");
		btnOkay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		btnOkay.setActionCommand("OK");
		contentPane.add(btnOkay);
		switch(result) {
			case Win:
				lblEstado.setText("Has ganado");
				break;
			case Lose:
				lblEstado.setText("Has perdido");
				break;
			default:
				lblEstado.setText("Ha sucedido un error inesperado");
		}
		
		pack();
	}
}
