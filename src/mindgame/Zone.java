package mindgame;

import mindgame.MidiAction.MidiThread;
import toxi.geom.Rect;

public class Zone extends Rect {
	public static int PLAYER = 0;
	public int type;

	World parent;

	boolean[] hasBall = new boolean[World.BALLCOUNT];
	MidiThread currThread;

	Zone(World _parent, float _x, float _y, float _w, float _h, int _type) {
		super(_x, _y, _w, _h);
		parent = _parent;
		type = _type;
	}

	public void enter(int what) {
		hasBall[what] = true;
		currThread = parent.midi[what].ballHint(type);
	}

	public void exit(int what) {
		hasBall[what] = false;
		if (currThread != null)
			currThread.interrupt();
	}

	public int getBallcount() {
		int num = 0;
		for (boolean a : hasBall)
			if (a)
				num++;
		return num;
	}
}
