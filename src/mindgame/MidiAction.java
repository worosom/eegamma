package mindgame;

import themidibus.MidiBus;
import themidibus.Note;

public class MidiAction {
	public int id;
	public static int PLAYER1 = 0;
	public static int PLAYER2 = 1;
	Mindgame parent;
	MidiBus midibus;

	int[] channels;

	MidiAction(Mindgame _parent, int _id) {
		parent = _parent;
		id = _id;
		midibus = new MidiBus(this, -1, "IAC-Bus 1");
		setChannels();
	}

	private void setChannels() {
		switch (id) {
		case 0:
			channels = new int[] { 0, 1, 2 };
			break;
		case 1:
			channels = new int[] { 3, 4, 5 };
			break;
		}
	}

	public void ballHit(int what) {
		switch (what) {
		case 0:
			new MidiThread(new Note(channels[0], 11,
					(int) (Math.abs(parent.w.balls[id].getVelocity().x)
							/ Ball.maxVelocity * 127.)));
			break;
		case 1:
			new MidiThread(new Note(channels[0], 16,
					(int) (Math.abs(parent.w.balls[id].getVelocity().x)
							/ Ball.maxVelocity * 127.)));
			break;
		default:
			println("Invalid hit identifier: " + what);
		}

	}

	public MidiThread ballHint(int what) {
		int velocity = 127 - (int) (Math
				.abs(parent.w.balls[id].getVelocity().x) / Ball.maxVelocity * 127.f);
		switch (what) {
		case 0:
			return new MidiThread(new Note(channels[1], 55, 127 - velocity));
		case 1:
			return new MidiThread(new Note(channels[1], 57, 127 - velocity));
		case 2:
			return new MidiThread(new Note(channels[2], 74, velocity));
		case 3:
			return new MidiThread(new Note(channels[2], 78, velocity));
		case 4:
			return new MidiThread(new Note(channels[2], 79, velocity));
		case 5:
			return new MidiThread(new Note(channels[2], 81, velocity));
		case 6:
			return new MidiThread(new Note(channels[2], 83, velocity));
		case 7:
			return new MidiThread(new Note(channels[2], 84, velocity));
		default:
			println("Invalid hint identifier: " + what);
			return null;
		}

	}

	private void println(String what) {
		System.out.println(what);
	}

	class MidiThread extends Thread {
		Note note;
		long time;

		MidiThread(Note _note) {
			this(_note, 0);
		}

		MidiThread(Note _note, long _time) {
			note = _note;
			if (_time == 0)
				time = (long) (Math.abs(parent.w.balls[id].getVelocity().x) * 20);
			else if (_time < 0)
				time = 600000;
			start();
		}

		public void run() {
			midibus.sendNoteOn(note);
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				midibus.sendNoteOff(note);
			}
			midibus.sendNoteOff(note);
		}

	}
}
