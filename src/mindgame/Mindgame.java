package mindgame;

import oscP5.OscP5;
import oscP5.OscProperties;
import processing.core.PApplet;
import processing.core.PFont;
import toxi.geom.Vec2D;
import vrpnserver.VrpnServer;

/**
 * Sound based BCI application, sends data via OSC and Midi to Ableton Live to
 * generate sound, receives OSC clip playback data from Live for leveling, sends
 * and receives BCI stimulations and feedback via vrpn.
 * 
 * @author Alexander Morosow, 2014
 * 
 */

@SuppressWarnings("serial")
public class Mindgame extends PApplet {

	public static int P1PULLING = 49;
	public static int P2PULLING = 48;

	public static void main(String[] args) {
		PApplet.main(new String[] { "--present", "mindgame.Mindgame" });
		// PApplet.main(new String[] { "mindgame.Mindgame" });
	}

	World w;

	MidiControl mc;
	OscP5 mindcontrol;
	VrpnServer[] vrpn = new VrpnServer[1];

	public static float player0Pos = -600;
	public static float player1Pos = 600;

	boolean[] keys = new boolean[526];

	long startuptime, trainstarttime, time;
	boolean training;

	PFont font;

	public void setup() {
		size((int) player1Pos * 2, 320);
		frameRate(25);
		background(0);

		mc = new MidiControl(this);

		OscProperties properties = new OscProperties();
		properties.setListeningPort(9001);
		mindcontrol = new OscP5(this, 9001);

		mindcontrol.plug(this, "controlClips", "/live/clip/info");
		for (int i = 0; i < vrpn.length; i++)
			vrpn[i] = new VrpnServer(10000 + i, 4);
		w = new World(this);

		font = createFont("Monaco", 15);
		textFont(font);

		startuptime = System.currentTimeMillis();
	}

	public void draw() {
		trainTime();
		w.update();
		w.osc.send();

		translate(width / 2, height / 2);
		background(0);
		w.draw();

		this.frame.setTitle((int) frameRate + " fps");
	}

	void trainTime() {
		time = System.currentTimeMillis();
		if (time - trainstarttime > World.TRAINDURATION && training) {
			String mes = new String();
			mes += "Terminating train segment ";
			switch (w.status[0]) {
			case (0):
				mes += ">neutral<";
				break;
			case (1):
				mes += ">pull<";
				break;
			}
			println(mes + " after " + (time - trainstarttime) + "ms");
			pause(0);
			training = !training;
		}
	}

	public void mousePressed() {
		w.addBalls();
	}

	// MindYourOsc-controls
	// vvvvvvvvvvvvvvvvvvvv
	// void oscEvent(OscMessage message) {
	// // check if theOscMessage has an address pattern we are looking for
	// if (message.checkAddrPattern("/COG/NEUTRAL") == true) {
	// w.player[0].force = 0;
	// } else if (message.checkAddrPattern("/COG/PULL") == true) {
	// w.player[0].force = message.get(0).floatValue();
	// }
	// }

	public void controlClips(int col, int row, int stat) {
		if (w.status[0] == World.WIN)
			switch (col) {
			case 6:
				advance(2);
				break;
			default:
				String me = "OSC Clip data from ableton:\n col|row|state\n ";
				me += col + " " + row + " " + stat;
				println(me);
			}
		println("controlClips: " + col + " " + row + " " + stat);
	}

	public void advance(int i) {
		w.level = i;
		w.player[0].setBehavior(w.level);
		w.setStatus(0, World.PLAY);

	}

	boolean checkKey(int k) {
		if (keys.length >= k) {
			return keys[k];
		}
		return false;
	}

	public void keyPressed() {
		keys[keyCode] = true;
		if (checkKey(P1PULLING) && w.status[0] == World.PLAY) {
			w.player[0].setForce(.5f);
		}
		if (checkKey(P2PULLING) && w.status[0] == World.PLAY) {
			w.player[1].setForce(.5f);
		}
		if (key == '2') {
			advance(2);
		}
	}

	public void keyReleased() {
		keys[keyCode] = false;
		if (!checkKey(P1PULLING))
			w.player[0].setForce(0);
		if (!checkKey(P2PULLING))
			w.player[1].setForce(0);
		if (key == 'r') {
			w.balls[0].addVelocity(Vec2D.randomVector().add(-.5f, -.5f)
					.scale(16.f));
			w.balls[1].addVelocity(Vec2D.randomVector().add(-.5f, -.5f)
					.scale(16.f));
		}
	}

	public void initTraining(int player, int stat) {
		/**
		 * OV Buttons mapping:
		 * 
		 * @param 0: Idling eyes open / closed
		 * @param 1: Stage 1 / 2
		 * @param 2: Segment Start / Stop
		 * @param 3: Train / Train Completed
		 * 
		 */
		player = 0;
		vrpn[player].updateButton(stat, true);
		vrpn[player].updateButton((stat + 1) % 2, false);
		vrpn[player].updateButton(2, true);
		vrpn[player].updateButton(3, true);
		w.setStatus(player, stat);
		trainstarttime = System.currentTimeMillis();
		training = true;
	}

	public void pause(int player) {
		player = 0;
		vrpn[player].updateButton(0, false);
		vrpn[player].updateButton(1, false);
		vrpn[player].updateButton(2, false);
		// vrpn[player].updateButton(3, false);
		w.setStatus(player, World.PAUSE);
	}

	public void play(int player) {
		player = 0;
		vrpn[player].updateButton(0, false);
		vrpn[player].updateButton(1, false);
		vrpn[player].updateButton(2, true);
		vrpn[player].updateButton(3, false);
		w.setStatus(player, World.PLAY);
	}

	public void stop() {
		for (VrpnServer s : vrpn)
			s.stop();
	}
}
