package mindgame;

import toxi.geom.Vec2D;
import toxi.physics2d.behaviors.AttractionBehavior;
import vrpn.AnalogOutputRemote;
import vrpn.AnalogRemote;

public class Player extends Zone {
	public static float PLAYERWIDTH = 100;
	int id;
	float force;

	float[] distsballs = new float[World.BALLCOUNT];
	float[] distballsL = new float[World.BALLCOUNT];
	float[] distballsR = new float[World.BALLCOUNT];
	float[] dopplers = new float[World.BALLCOUNT];

	Vec2D[] ear = new Vec2D[2];

	Vec2D p;
	AttractionBehavior playerBehavior;

	VrpnListener vrpn;

	Player(World _parent, int _id, float _pos) {
		super(_parent, _pos, 0, PLAYERWIDTH, _parent.parent.height, _id);
		parent = _parent;
		id = _id;
		setup();
		// vrpn = new VrpnListener("openvibe-vrpn", "vm");
	}

	private void setup() {
		p = new Vec2D(x + id * PLAYERWIDTH, y + height / 2.f);
		playerBehavior = new AttractionBehavior(p, parent.width / 2, .5f);
		parent.physics.addBehavior(playerBehavior);

		for (int i = 0; i < World.BALLCOUNT; i++) {
			distsballs[i] = parent.width / 2.f;
			dopplers[i] = .5f;
		}

		if (id == 0) {
			ear[0] = new Vec2D(p.add(-(7.f / parent.rscale), 0));
			ear[1] = new Vec2D(p.add(+(7.f / parent.rscale), 0));
		} else {
			ear[1] = new Vec2D(p.add(-(7.f / parent.rscale), 0));
			ear[0] = new Vec2D(p.add(+(7.f / parent.rscale), 0));
		}
	}

	public void update() {
		// force = (float) vrpn.getChannel(id);
		playerBehavior.setStrength(force);
	}

	public void setDoppler(float v, int which) {
		dopplers[which] = (.5f / Ball.maxVelocity * v + .5f);
	}

	public void setDistball(Vec2D d, int which) {
		distballsL[which] = d.distanceTo(ear[0]);
		distballsR[which] = d.distanceTo(ear[1]);
	}

	public void setDistball(float d, int which) {
		distsballs[which] = d;
	}

	public float getNormPos() {
		return x / parent.width;
	}

	public float getNormDistBall(int which) {
		return distsballs[which] / parent.width;
	}

	public class VrpnListener implements vrpn.AnalogRemote.AnalogChangeListener {
		double[] channels = new double[10];

		public VrpnListener(String serverName, String machine) {
			String analogName = serverName + "@" + machine;
			AnalogRemote analog = null;
			AnalogOutputRemote ao = null;
			try {
				analog = new AnalogRemote(analogName, null, null, null, null);
				ao = new AnalogOutputRemote(analogName, null, null, null, null);
				System.out.println("VRPN Connection established");
			} catch (InstantiationException e) {
				// do something b/c you couldn't create the analog
				System.err.println("We couldn't connect to analog "
						+ analogName + ".");
				System.err.println(e.getMessage());
			}

			analog.addAnalogChangeListener(this);

			ao.requestValueChange(2, 5);
		}

		public void analogUpdate(vrpn.AnalogRemote.AnalogUpdate u,
				vrpn.AnalogRemote tracker) {
			channels = u.channel;
		}

		public float getChannel(int i) {
			return (float) channels[0] / 100.f;
		}
	}
}