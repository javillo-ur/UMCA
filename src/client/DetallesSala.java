package client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import model.Party;
import model.Player;

import java.awt.GridLayout;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class DetallesSala extends JDialog {

	private final JPanel contentPanel = new JPanel();

	/**
	 * Launch the application.
	 */

	/**
	 * Create the dialog.
	 */
	public DetallesSala(Party party, SelectParty parent) {
		setBounds(100, 100, 450, 300);
		this.setTitle(party.getName());
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new GridLayout(0, 2, 0, 0));
		{
			JLabel lblPropietario = new JLabel("Propietario");
			contentPanel.add(lblPropietario);
		}
		{
			JLabel lblNewLabel = new JLabel(party.getPlayers().get(0).getName());
			contentPanel.add(lblNewLabel);
		}
		{
			JLabel lblJugadores = new JLabel("Jugadores");
			contentPanel.add(lblJugadores);
		}
		{
			java.awt.List list = new java.awt.List();
			for(Player player : party.getPlayers())
				list.add(player.getName());
			contentPanel.add(list);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Unirse");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						parent.joinParty(party);
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Salir");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		setVisible(true);
	}
}
