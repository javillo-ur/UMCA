package graphics;

import org.apache.commons.lang3.time.StopWatch;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;

public class PrintTime extends TimerTask {
	private ClientGame cg;
	private StopWatch sw;
	private int millis;
	
	public PrintTime(StopWatch sw, ClientGame cg, int millis) {
		this.sw = sw;
		this.cg = cg;
		this.millis = millis;
	}

	@Override
	public void run() {
		try {
			cg.printTime(Math.max(0, millis - sw.getTime()));
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		}
	}
}
