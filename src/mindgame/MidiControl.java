package mindgame;

import processing.core.PApplet;
import themidibus.MidiBus;

public class MidiControl {
	Mindgame parent;
	MidiBus midibus;

	MidiControl(Mindgame _parent) {
		parent = _parent;
		initMidi();
	}

	private void initMidi() {
		for (String in : MidiBus.availableInputs())
			if (in.equals("LPD8"))
				midibus = new MidiBus(this, in, -1);
		if (midibus == null) {
			System.err.println("LPD8 not connected.");
			parent.exit();
		} else
			Mindgame.println("LPD8 init was a success.");
	}

	public void noteOn(int channel, int pitch, int vel) {

	}

	public void noteOff(int channel, int pitch, int vel) {
		if (channel == 0)
			switch (pitch) {
			// Controls for Player 1:
			case 36:
				parent.initTraining(0, World.TRAINING_NEUTRAL);
				break;
			case 37:
				parent.initTraining(0, World.TRAINING_PULL);
				break;
			case 38:
				parent.play(0);
				break;
			case 39:
				parent.pause(0);
				break;
			// Controls for Player 2:
			case 40:
				parent.initTraining(1, World.TRAINING_NEUTRAL);
				break;
			case 41:
				parent.initTraining(1, World.TRAINING_PULL);
				break;
			case 42:
				parent.play(1);
				break;
			case 43:
				parent.pause(1);
				break;
			default:
				Mindgame.println("MidiControl noteOn not mapped - pitch "
						+ pitch);
				Mindgame.println("Check the program setting on the LPD8.");
				return;
			}
	}

	public void controllerChange(int channel, int number, int value) {
		switch (channel) {
		case 0:
			switch (number) {
			case 4:
				parent.w.setDrag(PApplet.map(value, 0, 127, .002f, .005f));
				break;
			case 7:
				parent.w.player[0].setForceMult(PApplet.map(value, 0, 127, 1.f,
						70.f));
				break;
			case 8:
				parent.w.player[0].setForceThreshold(PApplet.map(value, 0, 127,
						0.f, .05f));
				break;
			}
			break;
		}

	}
}
