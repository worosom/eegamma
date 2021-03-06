package mindgame;

import netP5.NetAddress;
import oscP5.OscMessage;
import oscP5.OscP5;
import oscP5.OscProperties;
import processing.core.PApplet;

public class OSCaction {
	public static int TRAINING_NEUTRAL = 0;
	public static int TRAINING_PULL = 1;
	public static int PLAY = 2;
	public static int PAUSE = 3;
	public static int WIN = 4;
	Mindgame parent;
	public OscP5 oscp5;

	Ball ball;
	Player[] player;

	NetAddress[] address = new NetAddress[World.BALLCOUNT];

	OscMessage[] balltop1 = new OscMessage[World.BALLCOUNT],
			balltop2 = new OscMessage[World.BALLCOUNT],
			p1Fine = new OscMessage[World.BALLCOUNT],
			p2Fine = new OscMessage[World.BALLCOUNT],
			p1SpkDist = new OscMessage[World.BALLCOUNT],
			p2SpkDist = new OscMessage[World.BALLCOUNT],
			p1ballpan = new OscMessage[World.BALLCOUNT],
			p2ballpan = new OscMessage[World.BALLCOUNT],
			p1delayL = new OscMessage[World.BALLCOUNT],
			p1delayR = new OscMessage[World.BALLCOUNT],
			p2delayL = new OscMessage[World.BALLCOUNT],
			p2delayR = new OscMessage[World.BALLCOUNT];

	/**
	 * 
	 * @param _parent
	 * @param clientname
	 *            Such as "localhost"
	 */

	OSCaction(Mindgame _parent, String clientname) {
		parent = _parent;
		OscProperties properties = new OscProperties();
		for (int i = 0; i < address.length; i++)
			address[i] = new NetAddress(clientname, 2345 + i);
		properties.setListeningPort(9000);
		properties.setDatagramSize(64);
		oscp5 = new OscP5(parent, properties);

		for (int i = 0; i < World.BALLCOUNT; i++) {
			balltop1[i] = new OscMessage("/SendA-Player1");
			balltop2[i] = new OscMessage("/SendB-Player2");
			p1Fine[i] = new OscMessage("/P1Fine");
			p2Fine[i] = new OscMessage("/P2Fine");
			p1SpkDist[i] = new OscMessage("/P1SpkDist");
			p2SpkDist[i] = new OscMessage("/P2SpkDist");
			p1ballpan[i] = new OscMessage("/BallPanP1");
			p2ballpan[i] = new OscMessage("/BallPanP2");
			p1delayL[i] = new OscMessage("/P1DelayL");
			p1delayR[i] = new OscMessage("/P1DelayR");
			p2delayL[i] = new OscMessage("/P2DelayL");
			p2delayR[i] = new OscMessage("/P2DelayR");
		}
	}

	private void update() {
		for (int i = 0; i < World.BALLCOUNT; i++) {
			ball = parent.w.balls[i];
			player = parent.w.player;
			balltop1[i].clearArguments();
			balltop2[i].clearArguments();
			p1SpkDist[i].clearArguments();
			p2SpkDist[i].clearArguments();
			p1ballpan[i].clearArguments();
			p2ballpan[i].clearArguments();
			p1Fine[i].clearArguments();
			p2Fine[i].clearArguments();
			p1delayL[i].clearArguments();
			p1delayR[i].clearArguments();
			p2delayL[i].clearArguments();
			p2delayR[i].clearArguments();
			balltop1[i].add((1.f - player[0].getNormDistBall(i) + .25f) * .8f);
			balltop2[i].add((1.f - player[1].getNormDistBall(i) + .25f) * .8f);
			p1SpkDist[i].add((float) (-Math.log(1.f - player[0]
					.getNormDistBall(i)) / Math.E) + .001f);
			p2SpkDist[i].add((float) (-Math.log(1.f - player[1]
					.getNormDistBall(i)) / Math.E) + .001f);
			p1ballpan[i].add(PApplet.map(ball.y, -parent.height / 2,
					parent.height / 2, .3f, .7f));
			p2ballpan[i].add(PApplet.map(ball.y, parent.height / 2,
					-parent.height / 2, .3f, .7f));
			p1Fine[i].add(player[0].dopplers[i]);
			p2Fine[i].add(player[1].dopplers[i]);
			p1delayL[i].add(player[0].distballsL[i] / parent.w.maxdist / 300.f
					/ .34f);
			p1delayR[i].add(player[0].distballsR[i] / parent.w.maxdist / 300.f
					/ .34f);
			p2delayL[i].add(player[1].distballsL[i] / parent.w.maxdist / 300.f
					/ .34f);
			p2delayR[i].add(player[1].distballsR[i] / parent.w.maxdist / 300.f
					/ .34f);
		}
	}

	public void send() {
		update();
		for (int i = 0; i < address.length; i++) {
			oscp5.send(balltop1[i], address[i]);
			oscp5.send(balltop2[i], address[i]);
			oscp5.send(p1Fine[i], address[i]);
			oscp5.send(p2Fine[i], address[i]);
			oscp5.send(p1ballpan[i], address[i]);
			oscp5.send(p2ballpan[i], address[i]);
			oscp5.send(p1SpkDist[i], address[i]);
			oscp5.send(p2SpkDist[i], address[i]);
			oscp5.send(p1delayL[i], address[i]);
			oscp5.send(p1delayR[i], address[i]);
			oscp5.send(p2delayL[i], address[i]);
			oscp5.send(p2delayR[i], address[i]);
		}
	}

	/**
	 * Switches the specified player audio on or off
	 * 
	 * @param which
	 *            Ball ID
	 * @param state
	 *            0 or 1 for on or off
	 */

	public void switchPlayer(int which, float state) {
		OscMessage mes = new OscMessage("/SwitchPlayer" + which);
		mes.add(state);
		NetAddress add = new NetAddress(World.ABLETONMACHINE, 8000);
		oscp5.send(mes, add);
	}

	/**
	 * Switches the specified balls audio on or off
	 * 
	 * @param which
	 *            Ball ID
	 * @param state
	 *            0 or 1 for on or off
	 */
	public void switchBall(int which, float state) {
		OscMessage mes = new OscMessage("/SwitchBall" + which);
		mes.add(state);
		NetAddress add = new NetAddress(World.ABLETONMACHINE, 8000);
		oscp5.send(mes, add);
	}
}
