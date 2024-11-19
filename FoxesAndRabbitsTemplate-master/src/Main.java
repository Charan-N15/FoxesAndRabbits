import Simulator.Simulator;
import processing.core.*;

public class Main extends PApplet {
	Simulator simulator;
	boolean paused = true;

	public void settings() {
		size(560, 550);
	}

	@Override
	public void setup() {
		this.simulator = new Simulator(80, 60);
		this.simulator.setGUI(this);
	}

	@Override
	public void draw() {
		background(200);
		if (!paused)
			simulator.simulateOneStep();
		simulator.drawField();
		simulator.drawGraph();
	}

	// handle key presses
	public void keyReleased() {
		if (key == 'p' || key == 'P') { 				// 'p' toggles paused and unpaused
			paused = !paused;
		}

		if (key == 'r' || key == 'R') { 				// 'r' resets the simulator
			simulator.reset();
		}
	}

	// if mouse clicked, let the simulator handle the mouse click
	public void mouseClicked() {
		simulator.handleMouseClick(mouseX, mouseY);
	}

	// if mouse is dragged, let the simulator handle the mouse drag
	public void mouseDragged() {
		simulator.handleMouseDrag(mouseX, mouseY);
	}

	public static void main(String[] args) {
		PApplet.main(new String[] { "Main" });
	}
}