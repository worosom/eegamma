package mindgame;

import processing.core.PApplet;
import themidibus.MidiBus;
import toxi.geom.Ray2D;
import toxi.geom.Rect;
import toxi.geom.Vec2D;
import toxi.physics2d.VerletPhysics2D;

public class World {
	public static String[] OPENVIBEADDRESS = { "192.168.10.1", "openvibe-vrpn" };
	// public static String[] OPENVIBEADDRESS = {};
	public static String ABLETONMACHINE = "192.168.10.3";
	public static String MIDICLIENT = "Session 1";

	public static int TRAINING_NEUTRAL = 0;
	public static int TRAINING_PULL = 1;
	public static int PLAY = 2;
	public static int PAUSE = 3;
	public static int WIN = 4;
	public static long TRAINDURATION = 8000;

	public static int BALLCOUNT = 2;

	public float drag = .003f;

	public int[] status = new int[2];

	public int level = 1;

	Mindgame parent;
	float width, height;
	float rwidth = 200, rheight, rscale, maxdist;
	VerletPhysics2D physics;
	Rect bounds;
	MidiBus midibus;
	MidiAction[] midi = new MidiAction[BALLCOUNT];

	Player player[] = new Player[2];
	OSCaction osc;
	Zone zone[] = new Zone[3];
	Zone zoneB[] = new Zone[6];
	Ball[] balls = new Ball[BALLCOUNT];

	World(Mindgame _parent) {
		parent = _parent;
		setup();
		addBalls(-5);
		setStatus(0, PLAY);
		setStatus(1, PLAY);
	}

	private void setup() {
		width = parent.width;
		height = parent.height;
		rscale = rwidth / width;
		rheight = rscale * height;

		// World Bounds
		bounds = new Rect(-width / 2, -height / 2, width, height);
		// Maximum distance a ball can reach within the bounds
		maxdist = (float) Math
				.sqrt(width * width + height / 2. * (height / 2.));

		physics = new VerletPhysics2D();
		physics.setDrag(drag);
		physics.setTimeStep(.5f);

		osc = new OSCaction(parent, ABLETONMACHINE);
		midibus = new MidiBus(this, -1, MIDICLIENT);

		for (int i = 0; i < BALLCOUNT; i++) {
			midi[i] = new MidiAction(parent, midibus, i);
		}

		player[0] = new Player(this, 0, Mindgame.player0Pos, OPENVIBEADDRESS);
		player[1] = new Player(this, 1, Mindgame.player1Pos
				- Player.PLAYERWIDTH);

		float zoneswidth = parent.width - 2 * Player.PLAYERWIDTH;
		float space = zoneswidth / zone.length;
		for (int i = 0; i < zone.length; i++)
			zone[i] = new Zone(this, -parent.width / 2 + Player.PLAYERWIDTH + i
					* space + space / 2.f - 35.f, -parent.height / 2, 70,
					parent.height / 2, i + 2);

		space = zoneswidth / zoneB.length;
		for (int i = 0; i < zoneB.length; i++)
			zoneB[i] = new Zone(this, -parent.width / 2 + Player.PLAYERWIDTH
					+ i * space + space / 2.f - 10.f, 0, 20, parent.height / 2,
					7);

	}

	public void update() {
		physics.update();
		// Only let the players influence if they aren't training or winning
		if (status[0] == PLAY) {
			player[0].update();
		}
		if (status[1] == PLAY) {
			player[1].update();
		}

		for (Ball b : balls) {
			player[0].setDistball(b.distanceTo(player[0].p), b.id);
			player[0].setDistball(b, b.id);
			player[0].setDoppler(b.getVelocity().x, b.id);
			// player[1].setDistball(b, b.id);
			// player[1].setDistball(b.distanceTo(player[1].p), b.id);
			// player[1].setDoppler(b.getVelocity().x, b.id);
			if (!b.copy().add(b.getVelocity()).isInRectangle(bounds))
				b.bounce(3);
			if (!b.isInRectangle(bounds)
					|| (Math.abs(b.getVelocity().x) < .5 && b.x > width / 8)) {
				addBalls();
			}
		}

		for (int i = 0; i < player.length; i++)
			handleCollisions(player[i]);
		for (int i = 0; i < zone.length; i++)
			handleZone(zone[i]);
		for (int i = 0; i < zoneB.length; i++)
			handleZone(zoneB[i]);
	}

	private void handleZone(Zone z) {
		for (Ball b : balls) {
			handleZone(z, b);
		}
	}

	private void handleZone(Zone z, Ball b) {
		Vec2D v = b.getVelocity();
		Object intersection = z.intersectsRay(new Ray2D(b, v), 0.f,
				v.magnitude());
		// boolean intersects = z.intersectsRect(new Rect(b.x, b.y, v.x, v.y));
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
		} else if (!b.copy().add(b.getVelocity()).isInRectangle(z)
				&& z.hasBall[b.id]) {
			b.setState(0);
			z.exit(b.id);
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
			} else if (!b.add(b.getVelocity()).isInRectangle(z)
					&& z.hasBall[b.id]) {
				b.setState(0);
				z.exit(b.id);
			}
		}
	}

	public void addBalls() {
		float x = (float) ((Math.random() - .5) * (parent.height / 8.f));
		for (int i = 0; i < BALLCOUNT; i++) {
			physics.removeParticle(balls[i]);
			if (i == 1)
				x *= -1;
			balls[i] = new Ball(parent, x, -parent.height / 2 + (i + 1)
					* (parent.height / (level + 1.f)), i, (Vec2D) Vec2D.ZERO);
		}
		if (level == 1) {
			physics.addParticle(balls[0]);
		} else {
			physics.addParticle(balls[0]);
			physics.addParticle(balls[1]);
		}
	}

	public void addBalls(float x) {
		for (int i = 0; i < BALLCOUNT; i++) {
			physics.removeParticle(balls[i]);
			balls[i] = new Ball(parent, x, -parent.height / 2 + (i + 1)
					* (parent.height / (level + 1.f)), i, (Vec2D) Vec2D.ZERO);
		}
		if (level == 1) {
			physics.addParticle(balls[0]);
		} else {
			physics.addParticle(balls[0]);
			physics.addParticle(balls[1]);
		}
	}

	public void addTrainingBalls(int player) {
		Vec2D v;
		if (player == 0)
			v = new Vec2D(-2, 0);
		else
			v = new Vec2D(2, 0);
		addTrainingBalls(player, v);
	}

	public void addTrainingBalls(int player, Vec2D velocity) {
		for (int i = 0; i < BALLCOUNT; i++) {
			physics.removeParticle(balls[i]);
			balls[i] = new Ball(parent, 0, 0, i, velocity);
			if (i == 0)
				physics.addParticle(balls[i]);
		}
	}

	/*
	 * Sets the status of the player in the world. Controls Ableton and handles
	 * the balls.
	 */
	public void setStatus(int player, int stat) {
		switch (stat) {
		case 0:
			System.out.println("Initiate >neutral< training for Player "
					+ player);
			status[player] = TRAINING_NEUTRAL;
			// initiate neutral training
			osc.switchPlayer(0, 1);
			osc.switchBall(0, 0);
			osc.switchBall(1, 1);
			addBalls(-5);
			this.player[0].setForce(0);
			break;
		case 1:
			System.out.println("Initiate >pull< training for Player " + player);
			status[player] = TRAINING_PULL;
			// initiate pull training
			// spawn one ball moving slowly towards the player
			osc.switchPlayer(0, 1);
			osc.switchBall(0, 1);
			osc.switchBall(1, 0);
			addBalls();
			addTrainingBalls(player);
			this.player[0].setForce(0);
			break;
		case 2:
			System.out.println("Initiate >play< mode for Player " + player);
			status[player] = PLAY;
			// let the player play
			drag = .003f;
			physics.setDrag(drag);
			osc.switchPlayer(0, 1);
			osc.switchBall(0, 1);
			if (level == 2)
				osc.switchBall(1, 1);
			addBalls();
			break;
		case 3:
			status[player] = PAUSE;
			osc.switchPlayer(0, 0);
			osc.switchBall(0, 0);
			if (level == 2)
				osc.switchBall(1, 0);
			addBalls();
			this.player[0].setForce(0);
			break;
		case 4:
			status[player] = WIN;
			setDrag(0);
			System.out.println("Win!");
			this.player[0].setForce(0);
			break;
		}
	}

	public void progress(int casa, int id) {
		switch (casa) {
		case -1:
			midibus.sendNoteOn(9, 1, 127);
			midibus.sendNoteOff(9, 1, 127);
			balls[id].counter++;
			break;
		case 1:
			switch (id) {
			case 0:
				midibus.sendNoteOn(9, 0, 127);
				midibus.sendNoteOff(9, 0, 127);
				break;
			case 1:
				midibus.sendNoteOn(9, 2, 127);
				midibus.sendNoteOff(9, 2, 127);
				break;
			}
			break;
		}
	}

	public void win(int stage) {
		player[0].setForce(0);
		setStatus(0, WIN);
	}

	public void tap() {
		midibus.sendNoteOn(16, 0, 127);
		midibus.sendNoteOff(16, 0, 0);
	}

	public void setDrag(float val) {
		drag = val;
		physics.setDrag(val);
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
		for (Zone z : zoneB) {
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
				parent.fill(20);
			parent.rect(p.x, p.y, p.width, p.height);
			parent.fill(255);
			parent.ellipse(p.p.x, p.p.y, p.force * 500.f, p.force * 500.f);
		}

		parent.pushMatrix();
		parent.translate(width / 2 - 15, -height / 2 + 15);
		parent.textAlign(PApplet.RIGHT, PApplet.TOP);
		parent.text("Level: " + level, 0, 0);
		parent.text("Status:\n" + stateToString(0), 0, 30);
		parent.text("Drag: " + (int) (drag * 10000), 0, 85);
		parent.text("#1: " + PApplet.max(balls[0].counter, 0), 0, 115);
		if (level == 2)
			parent.text("#2: " + PApplet.max(balls[1].counter, 0), 0, 145);

		parent.translate(-width + 30, height / 2 - 15);
		parent.fill(127);
		parent.textAlign(PApplet.LEFT, PApplet.CENTER);
		parent.text((int) (player[0].force * 100) / 100.f, 0, 0);
		parent.popMatrix();

		for (Ball b : balls)
			b.draw();
	}

	public String stateToString(int p) {
		switch (status[p]) {
		case 0:
			return "Train neutral ";
		case 1:
			return "Train pull ";
		case 2:
			return "Playing ";
		case 3:
			return "Pause ";
		case 4:
			return "Win! ";
		default:
			return "";
		}
	}
}
