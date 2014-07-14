package mindgame;

import toxi.geom.Vec2D;
import toxi.physics2d.behaviors.AttractionBehavior;
import vrpn.AnalogOutputRemote;
import vrpn.AnalogRemote;

public class Player extends Zone {
	public static float PLAYERWIDTH = 100;
	public boolean runVrpn = true;
	float forceThreshold = .003f;
	float forceMult = 35.f;
	float force;

	float[] distsballs = new float[World.BALLCOUNT];
	float[] distballsL = new float[World.BALLCOUNT];
	float[] distballsR = new float[World.BALLCOUNT];
	float[] dopplers = new float[World.BALLCOUNT];

	Vec2D[] ear = new Vec2D[2];

	Vec2D p;
	AttractionBehavior playerBehavior;

	VrpnListener vrpn;
	VrpnListener listener;

	/**
	 * Player instance, extends Zone.
	 * 
	 * @param _id
	 *            Player ID, must be unique, 0 or 1
	 * @param _pos
	 *            Player Position, absolute
	 * @param address
	 *            Should this instance be connected to OpenViBE, add the address
	 *            here, such as "localhost", "openvibe-vrpn"
	 */

	Player(World _parent, int _id, float _pos, String... address) {
		super(_parent, _pos, -_parent.height / 2, PLAYERWIDTH, _parent.height,
				_id);
		parent = _parent;
		type = _id;
		setup();
		if (address.length > 1)
			vrpn = new VrpnListener(address[1], address[0]);
		else
			runVrpn = false;
		System.out.println("Player " + type + ": Running vrpn = " + runVrpn);
	}

	private void setup() {
		p = new Vec2D(x + type * PLAYERWIDTH, 0);
		playerBehavior = new AttractionBehavior(p, parent.width / 2.f
				+ parent.width / 8.f, 0);
		parent.physics.addBehavior(playerBehavior);

		for (int i = 0; i < World.BALLCOUNT; i++) {
			distsballs[i] = parent.width / 2.f;
			dopplers[i] = .5f;
		}

		if (type == 0) {
			ear[0] = new Vec2D(p.copy().add(0, -(7.f / parent.rscale)));
			ear[1] = new Vec2D(p.copy().add(0, +(7.f / parent.rscale)));
		} else {
			ear[1] = new Vec2D(p.copy().add(0, -(7.f / parent.rscale)));
			ear[0] = new Vec2D(p.copy().add(0, +(7.f / parent.rscale)));
		}
	}

	public void update() {
		if (runVrpn && parent.status[0] == World.PLAY) {
			float f = vrpn.getChannel(0);
			// System.out.println(f);
			if (f > forceThreshold) {
				setForce(f * forceMult);
				System.out.println(force);
			} else if (!parent.parent.checkKey(Mindgame.P1PULLING)
					&& !parent.parent.checkKey(Mindgame.P1PULLING))
				setForce(0);
		}
	}

	public void setForce(float f) {
		force = f;
		playerBehavior.setStrength(force);
	}

	public void setForceThreshold(float thresh) {
		forceThreshold = thresh;
		String mes = "Player " + type + ": Force Threshold set to: "
				+ forceThreshold;
		System.out.println(mes);
	}

	public void setForceMult(float mult) {
		forceMult = mult;
		String mes = "Player " + type + ": Force Multilplier set to: "
				+ forceMult;
		System.out.println(mes);
	}

	public void setBehavior(int lev) {
		if (lev == 2) {
			parent.physics.removeBehavior(playerBehavior);
			playerBehavior = new AttractionBehavior(p, parent.width / 2 + 30, 0);
			parent.physics.addBehavior(playerBehavior);
		}
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