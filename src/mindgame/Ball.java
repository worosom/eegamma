package mindgame;

import toxi.physics2d.VerletParticle2D;

public class Ball extends VerletParticle2D {
	public static float maxVelocity = 150.f;

	Mindgame parent;
	float radius = 10.f;

	public int prevstate = 2;
	public int state = 0;

	public int id;

	Ball(Mindgame _parent, float _x, float _y) {
		this(_parent, _x, _y, 0);
	}

	Ball(Mindgame _parent, float _x, float _y, int _id) {
		super(_x, _y, 2);
		parent = _parent;
		id = _id;
		this.addVelocity(randomVector().scale(8.f, 0.f));
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

	public void bounce(int what) {
		if (what < 3) {
			addVelocity(getVelocity().scale(-2.f, 0.f));

			parent.w.midi[id].ballHit(what);
		} else {
			addVelocity(getVelocity().scale(0.f, -2.f));
		}
	}

	public void sound(int how) {
		parent.w.midi[id].ballHint(how);
	}
}
