package mindgame;

import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.PApplet;

@SuppressWarnings("serial")
public class Mindgame extends PApplet {
	public static int P1PULLING = 49;
	public static int P2PULLING = 48;

	public static void main(String[] args) {
		PApplet.main("mindgame.Mindgame");
	}

	World w;

	MidiControl mc;
	OscP5 mindcontrol;

	public static float player0Pos = 0;
	public static float player1Pos = 1200;

	boolean[] keys = new boolean[526];

	public void setup() {
		size((int) player1Pos, 320);
		frameRate(25);
		background(0);
		mc = new MidiControl(this);
		w = new World(this);
		mindcontrol = new OscP5(this, 7400);
	}

	public void draw() {
		background(0);
		w.update();
		w.osc.send();
		w.draw();
		this.frame.setTitle((int) frameRate + " fps");
	}

	public void mousePressed() {
		w.addBalls();
	}

	void oscEvent(OscMessage message) {
		// check if theOscMessage has an address pattern we are looking for
		if (message.checkAddrPattern("/COG/NEUTRAL") == true) {
			w.player[0].force = 0;
		} else if (message.checkAddrPattern("/COG/PULL") == true) {
			w.player[0].force = message.get(0).floatValue();
			println(w.player[0].force = message.get(0).floatValue());
		}
	}

	boolean checkKey(int k) {
		if (keys.length >= k) {
			return keys[k];
		}
		return false;
	}

	public void keyPressed() {
		keys[keyCode] = true;
		if (checkKey(P1PULLING)) {
			w.player[0].force = .5f;
		}
		if (checkKey(P2PULLING)) {
			w.player[1].force = .5f;
		}
	}

	public void keyReleased() {
		keys[keyCode] = false;
		if (!checkKey(P1PULLING)) {
			w.player[0].force = 0;
		}
		if (!checkKey(P2PULLING)) {
			w.player[1].force = 0;
		}
	}

	public void initTraining(int player, int stat) {
		w.setStatus(player, stat);
	}
}
