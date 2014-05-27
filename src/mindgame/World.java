package mindgame;

import toxi.geom.Ray2D;
import toxi.geom.Rect;
import toxi.physics2d.VerletPhysics2D;

public class World {
	public static int BALLCOUNT = 1;
	Mindgame parent;
	int width, height;
	float rwidth = 200, rheight, rscale, maxdist;
	VerletPhysics2D physics;
	Rect bounds;
	MidiAction[] midi = new MidiAction[BALLCOUNT];
	OSCaction osc;
	Player player[] = new Player[2];
	Zone zone[] = new Zone[5];
	Ball[] balls = new Ball[BALLCOUNT];

	World(Mindgame _parent) {
		parent = _parent;
		setup();
	}

	private void setup() {
		width = parent.width;
		height = parent.height;
		rscale = rwidth / width;
		rheight = rscale * height;
		maxdist = (float) Math.sqrt(width * width + height * height);
		bounds = new Rect(0, 0, width, height);
		physics = new VerletPhysics2D();
		physics.setDrag(.008f);
		physics.setTimeStep(.5f);
		osc = new OSCaction(parent);
		for (int i = 0; i < BALLCOUNT; i++) {
			midi[i] = new MidiAction(parent, i);
		}
		addBalls();
		player[0] = new Player(this, 0, 0);
		player[1] = new Player(this, 1, parent.width - Player.PLAYERWIDTH);

		float zoneswidth = parent.width - 2 * Player.PLAYERWIDTH;
		float space = zoneswidth / zone.length;
		for (int i = 0; i < zone.length; i++)
			zone[i] = new Zone(this, Player.PLAYERWIDTH + i * space + space
					/ 2.f - 25.f, 0, 50, parent.height, i + 2);
	}

	public void update() {
		player[0].update();
		player[1].update();

		for (Ball b : balls) {
			player[0].setDistball(b.distanceTo(player[0].p), b.id);
			player[1].setDistball(b.distanceTo(player[1].p), b.id);
			player[0].setDistball(b, b.id);
			player[1].setDistball(b, b.id);
			player[0].setDoppler(b.getVelocity().x, b.id);
			player[1].setDoppler(b.getVelocity().x, b.id);
			if (!b.isInRectangle(bounds))
				b.bounce(3);
		}

		for (int i = 0; i < player.length; i++)
			handleCollisions(player[i]);
		for (int i = 0; i < zone.length; i++)
			handleZones(zone[i]);
	}

	private void handleZones(Zone z) {
		for (Ball b : balls) {
			Object intersection = z.intersectsRay(
					new Ray2D(b, b.getVelocity()), 0, b.getVelocity().x);
			if (intersection != null) {
				switch (b.state) {
				case 0:
					b.setState(1);
					b.sound(z.type);
					z.enter(b.id);
					break;
				case 1:
					if (!b.add(b.getVelocity()).isInRectangle(z)) {
						b.setState(0);
						z.exit(b.id);
					}
					break;
				default:
					b.setState(0);
				}
			}
		}
	}

	private void handleCollisions(Zone z) {
		for (Ball b : balls) {
			Object intersection = z.intersectsRay(
					new Ray2D(b, b.getVelocity()), 0, b.getVelocity().x);
			if (intersection != null) {
				switch (b.state) {
				case 0:
					b.setState(1);
					b.sound(z.type);
					z.enter(b.id);
					break;
				case 1:
					if (!b.add(b.getVelocity()).isInRectangle(z)) {
						b.bounce(z.type);
						b.setState(2);
					}
					break;
				case 2:
					if (!b.add(b.getVelocity()).isInRectangle(z)) {
						z.exit(b.id);
						b.setState(0);
					}
					break;
				}
			}
		}
	}

	public void draw() {
		parent.noStroke();
		for (Zone z : zone) {
			int ballnum = z.getBallcount();
			if (ballnum > 0)
				parent.fill(ballnum * 85);
			else
				parent.fill(50);
			parent.rect(z.x, z.y, z.width, z.height);
		}
		for (Player p : player) {
			int ballnum = p.getBallcount();
			if (ballnum > 0)
				parent.fill(ballnum * 100);
			else
				parent.fill(0);
			parent.rect(p.x, 0, Player.PLAYERWIDTH, parent.height);
			parent.fill(255);
			parent.ellipse(p.p.x, p.p.y, p.force * 500.f, p.force * 500.f);
		}

		for (Ball b : balls)
			b.draw();
	}

	public void addBalls() {
		for (int i = 0; i < BALLCOUNT; i++) {
			physics.removeParticle(balls[i]);
			balls[i] = new Ball(parent, parent.width / 2, (i + 1)
					* (parent.height / (BALLCOUNT + 1.f)), i);
			physics.addParticle(balls[i]);
		}
	}
}
