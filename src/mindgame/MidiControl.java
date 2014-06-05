package mindgame;

import themidibus.MidiBus;

public class MidiControl {
	Mindgame parent;
	MidiBus midibus;

	MidiControl(Mindgame _parent) {
		parent = _parent;
		midibus = new MidiBus(this, "LPD8", "LPD8");
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
		}
	}

	public void noteOff(int channel, int pitch, int vel) {

	}
}
