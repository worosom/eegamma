package mindgame;

import toxi.geom.Vec2D;
import toxi.physics2d.VerletParticle2D;

public class Ball extends VerletParticle2D {
	public static float maxVelocity = 150.f;
	public float winVelocity = 25.f;
	Mindgame parent;
	float radius = 10.f;

	public int prevstate = 2;
	public int state = 0;

	public int id;

	public long lasthit;
	public float llltarget, lltarget, ltarget, target;

	public int counter = 0;

	Ball(Mindgame _parent, float _x, float _y) {
		this(_parent, _x, _y, 0);
	}

	Ball(Mindgame _parent, float _x, float _y, int _id) {
		this(_parent, _x, _y, _id, randomVector().scale(8.f, 0.f));
	}

	Ball(Mindgame _parent, float _x, float _y, int _id, Vec2D _v) {
		super(_x, _y, 2);
		parent = _parent;
		id = _id;
		this.addVelocity(_v);
		spawn();
	}

	public float getNormPos() {
		return x / parent.width;
	}

	public void draw() {
		parent.stroke(0);
		parent.fill(255);
		parent.ellipse(x, y, radius, radius);
	}

	public void setState(int s) {
		prevstate = state;
		state = s;
	}

	private void check() {
		llltarget = lltarget;
		lltarget = ltarget;
		ltarget = target;
		target = (parent.time - lasthit) / 2400.f;
		lasthit = parent.time;
		// System.out.println(target);
		if (parent.w.status[0] != 4) {
			if (target < 1.1 && target > .9) {
				counter++;
				if (counter < 3) {
					parent.w.progress(counter, id);
				} else {
					parent.w.win(id);
					counter = 0;
				}
			} else if (ltarget - target < -.1 && counter > 0) {
				counter = -1;
				parent.w.progress(counter, id);
			}
		}
	}

	public void bounce(int what) {
		if (what < 3) {
			check();
			addVelocity(getVelocity().scale(-2.f, -2.f));
			parent.w.midi[id].ballHit(what);
		} else {
			addVelocity(getVelocity().scale(0.f, -2.f));
		}
	}

	public void sound(int how) {
		parent.w.midi[id].ballHint(how);
	}

	public void spawn() {
		try {
			parent.w.midi[id].ballSpawn();
		} catch (NullPointerException e) {
			System.err.println("…");
		}
		lasthit = parent.time;
	}
}
