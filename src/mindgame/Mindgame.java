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
	public static float player1Pos = 1280;

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

		if (keyPressed) {
			if (key == 'l') {
				w.player[1].force += .01;
			} else if (key == 'a') {
				w.player[0].force += .01;
			}
		} else if (w.player[0].force > 0) {
			w.player[0].force -= .01;
		} else if (w.player[1].force > 0) {
			w.player[1].force -= .01;
		}
	}

	public void mousePressed() {
		w.addBalls();
	}

	void oscEvent(OscMessage message) {
		// println(message.toString());
	}

	public void keyPressed() {
	}

	public void ease() {

	}
}
