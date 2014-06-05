package mindgame;

import toxi.geom.Ray2D;
import toxi.geom.Rect;
import toxi.geom.Vec2D;
import toxi.physics2d.VerletPhysics2D;

public class World {
	public static int TRAINING_NEUTRAL = 0;
	public static int TRAINING_PULL = 1;
	public static int PLAYING = 2;
	public static int WIN = 3;

	public static int BALLCOUNT = 2;

	private int[] status = new int[2];

	Mindgame parent;
	int width, height;
	float rwidth = 200, rheight, rscale, maxdist;
	VerletPhysics2D physics;
	Rect bounds;
	MidiAction[] midi = new MidiAction[BALLCOUNT];
	OSCaction osc;
	Player player[] = new Player[2];
	Zone zone[] = new Zone[5];
	Zone zoneB[] = new Zone[20];
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
		physics.setDrag(.003f);
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
					/ 2.f - 35.f, 0, 70, parent.height / 2, i + 2);
		space = zoneswidth / zoneB.length;
		for (int i = 0; i < zoneB.length; i++)
			zoneB[i] = new Zone(this, Player.PLAYERWIDTH + i * space + space
					/ 2.f - 10.f, parent.height / 2, 20, parent.height / 2, 7);
		player[0].update();
		player[1].update();
	}

	public void update() {
		physics.update();
		// Only let the players influence if they aren't training
		if (status[0] + status[1] == 4) {
			player[0].update();
			player[1].update();
		}

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
		Object intersection = z.intersectsRay(new Ray2D(b, b.getVelocity()), 0,
				b.getVelocity().x);
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
			balls[i] = new Ball(parent,
					(float) (parent.width / 2 + (Math.random() - .5) * 200.f),
					(i + 1) * (parent.height / (BALLCOUNT + 1.f)), i,
					(Vec2D) Vec2D.ZERO);
			physics.addParticle(balls[i]);
		}
	}

	public void addTrainingBalls(int player) {
		for (int i = 0; i < BALLCOUNT; i++) {
			physics.removeParticle(balls[i]);
			Vec2D v;
			if (player == 0)
				v = new Vec2D(-2, 0);
			else
				v = new Vec2D(2, 0);
			balls[i] = new Ball(parent, parent.width / 2, parent.height / 2, i,
					v);
			if (i == 0)
				physics.addParticle(balls[i]);
		}
	}

	public void setStatus(int player, int stat) {
		switch (stat) {
		case 0:
			System.out.println("Initiate >neutral< training for Player "
					+ player);
			status[player] = TRAINING_NEUTRAL;
			// initiate neutral training
			// switch off the audio
			parent.w.osc.switchOffPlayer(player);
			break;
		case 1:
			System.out.println("Initiate >pull< training for Player " + player);
			status[player] = TRAINING_PULL;
			// initiate pull training
			// spawn one ball moving slowly towards the player
			parent.w.osc.switchOnPlayer(player);
			parent.w.osc.switchOnPlayer((player + 1) % 2);
			parent.w.osc.switchSecondBallOff();
			addBalls();
			addTrainingBalls(player);
			break;
		case 2:
			System.out.println("Initiate >play< mode for Player " + player);
			status[player] = PLAYING;
			// let the player play
			parent.w.osc.switchOnPlayer(player);
			parent.w.osc.switchSecondBallOn();
			addBalls();
			break;
		case 3:

			break;
		}
	}
}
