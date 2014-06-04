package mindgame;

import oscP5.OscMessage;
import processing.core.PApplet;

@SuppressWarnings("serial")
public class Mindgame extends PApplet {

	public static void main(String[] args) {
		PApplet.main("mindgame.Mindgame");
	}

	World w;
	public static float player0Pos = 0;
	public static float player1Pos = 1200;

	boolean[] keys = new boolean[526];

	public void setup() {
		size((int) player1Pos, 320);
		frameRate(25);
		background(0);
		w = new World(this);
	}

	public void draw() {
		background(0);
		w.update();
		w.physics.update();
		w.osc.send();
		w.draw();
		this.frame.setTitle((int) frameRate + " fps");
	}

	public void mousePressed() {
		w.addBalls();
	}

	void oscEvent(OscMessage message) {
		// println(message.toString());
	}

	boolean checkKey(int k) {
		if (keys.length >= k) {
			return keys[k];
		}
		return false;
	}

	public void keyPressed() {
		keys[keyCode] = true;
		if (checkKey(49)) {
			w.player[0].force = .5f;
		}
		if (checkKey(48)) {
			w.player[1].force = .5f;
		}
	}

	public void keyReleased() {
		keys[keyCode] = false;
		if (!checkKey(49)) {
			w.player[0].force = 0;
		}
		if (!checkKey(48)) {
			w.player[1].force = 0;
		}
	}

	public void ease() {

	}
}
