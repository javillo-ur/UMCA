package graphics;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.BrokenBarrierException;

public class ButtonListener implements ActionListener {
	private int x;
	private int y;
	private ClientGame cg;
	
	public ButtonListener(int x, int y, ClientGame cg) {
		this.x = x;
		this.y = y;
		this.cg = cg;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		try {
				cg.buttonClicked(x, y);
		}catch(InterruptedException | BrokenBarrierException ex) {
			ex.printStackTrace();
		}
	}

}
