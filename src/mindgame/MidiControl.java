package mindgame;

import themidibus.MidiBus;
import vrpn.ButtonRemote;
import vrpn.ButtonRemote.ButtonUpdate;

public class MidiControl {
	Mindgame parent;
	MidiBus midibus;

	// VRPN stuff to send stimulation data to OpenViBE
	String playButtonName = "PLAY";
	ButtonRemote playRemote;
	String pauseButtonName = "PAUSE";
	ButtonRemote pauseRemote;

	MidiControl(Mindgame _parent) {
		parent = _parent;
		initMidi();
		initVrpn();
	}

	private void initMidi() {
		for (String in : MidiBus.availableInputs())
			if (in.equals("LPD8"))
				midibus = new MidiBus(this, in, -1);
		if (midibus == null) {
			System.err.println("LPD8 not connected.");
			parent.initTraining(0, World.PLAYING);
			parent.initTraining(1, World.PLAYING);
			// parent.exit();
		} else
			Mindgame.println("LPD8 init was a success.");
	}

	private void initVrpn() {
		try {
			playRemote = new ButtonRemote(playButtonName, null, null, null,
					null);
			pauseRemote = new ButtonRemote(pauseButtonName, null, null, null,
					null);
		} catch (InstantiationException e) {
			System.err.println(e.getMessage());
		}
	}

	public void noteOn(int channel, int pitch, int vel) {
		switch (pitch) {
		case 36:
			parent.initTraining(0, World.TRAINING_NEUTRAL);
			break;
		case 37:
			parent.initTraining(0, World.TRAINING_PULL);
			break;
		case 38:
			parent.initTraining(0, World.PLAYING);
			break;
		case 39:
			parent.w.addBalls();
			break;
		case 40:
			parent.initTraining(1, World.TRAINING_NEUTRAL);
			break;
		case 41:
			parent.initTraining(1, World.TRAINING_PULL);
			break;
		case 42:
			parent.initTraining(1, World.PLAYING);
			break;
		default:
			Mindgame.println("MidiControl noteOn not mapped - pitch " + pitch);
			Mindgame.println("Check the program setting on the LPD8.");
		}
	}

	public void noteOff(int channel, int pitch, int vel) {

	}

	private class VrpnControl implements vrpn.ButtonRemote.ButtonChangeListener {

		public VrpnControl(String serverName, String machine) {

		}

		public void buttonUpdate(ButtonUpdate u, ButtonRemote button) {

		}

	}
}
