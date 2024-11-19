package Simulator;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

import Animals.*;
import Field.*;
import Graph.*;
import processing.core.PApplet;

/**
 * A simple predator-prey simulator, based on a field containing rabbitList and
 * foxList.
 *
 * @author David J. Barnes and Michael Kolling. Modified by David Dobervich and Daniel Hutzley
 * 2007-2013.
 * @version 2006.03.30
 */
public class Simulator {
    // The default width for the grid.
    private static final int DEFAULT_WIDTH = 80;

    // The default height of the grid.
    private static final int DEFAULT_HEIGHT = 80;

    // The probability that a fox will be created in any given grid position.
    private static final double FOX_CREATION_PROBABILITY = 0.02;

    // The probability that a rabbit will be created in any given grid position.
    private static final double RABBIT_CREATION_PROBABILITY = 0.08;

    // probability that a deer will be created in any given grid position
    private static final double DEER_CREATION_PROBABILITY = 0.06;

    private static final double BEAR_CREATION_PROBABILITY = 0.03;


    // Lists of animals in the field. Separate lists are kept for ease of
    // iteration.
    private ArrayList<Animal> animalList;

    // The current state of the field.
    private Field field;

    // A second field, used to build the next stage of the simulation.
    private Field updatedField;

    // The current step of the simulation.
    private int step;

    // A graphical view of the simulation.
    private FieldDisplay view;

    // A graph of animal populations over time
    private Graph graph;

    // Processing Applet (the graphics window we draw to)
    private PApplet graphicsWindow;

    // Object to keep track of statistics of animal populations
    private FieldStats stats;

    /**
     * Construct a simulation field with default size.
     */
    public Simulator() {
        this(DEFAULT_HEIGHT, DEFAULT_WIDTH);
    }

    /**
     * Create a simulation field with the given size.
     *
     * @param height Height of the field. Must be greater than zero.
     * @param width  Width of the field. Must be greater than zero.
     */
    public Simulator(int width, int height) {
        if (width <= 0 || height <= 0) {
            System.out.println("The dimensions must be greater than zero.");
            System.out.println("Using default values.");
            height = DEFAULT_HEIGHT;
            width = DEFAULT_WIDTH;
        }

        animalList = new ArrayList<Animal>();
        field = new Field(width, height);
        updatedField = new Field(width, height);
        stats = new FieldStats();

        // Setup a valid starting point.
        reset();
    }

    public void setGUI(PApplet p, int x, int y, int display_width,
                       int display_height) {
        this.graphicsWindow = p;

        // Create a view of the state of each location in the field.
        view = new FieldDisplay(p, this.field, x, y, display_width, display_height);
        view.setColor(Rabbit.class, p.color(155, 155, 155));
        view.setColor(Fox.class, p.color(200, 0, 255));
        view.setColor(Deer.class, p.color(139, 69, 19));
        view.setColor(Bear.class, p.color(0, 0, 0));

        graph = new Graph(p, 10, display_height + 10, p.width - 10, p.height - 30, 0,
                0, 500, field.getHeight() * field.getWidth());

        graph.title = "Fox, Rabbit, and Deer Populations";
        graph.xlabel = "Time";
        graph.ylabel = "Pop.\t\t";
        graph.setColor(Rabbit.class, p.color(155, 155, 155));
        graph.setColor(Fox.class, p.color(200, 0, 255));
        graph.setColor(Deer.class, p.color(139, 69, 19));
        graph.setColor(Bear.class, p.color(0, 0, 0));
    }

    public void setGUI(PApplet p) {
        setGUI(p, 10, 10, p.width - 10, 400);
    }

    /**
     * Run the simulation from its current state for a reasonably long period,
     * e.g. 500 steps.
     */
    public void runLongSimulation() {
        simulate(500);
    }

    /**
     * Run the simulation from its current state for the given number of steps.
     * Stop before the given number of steps if it ceases to be viable.
     *
     * @param numSteps The number of steps to run for.
     */
    public void simulate(int numSteps) {
        for (int step = 1; step <= numSteps && isViable(); step++) {
            simulateOneStep();
        }
    }

    /**
     * Run the simulation from its current state for a single step. Iterate over
     * the whole field updating the state of each fox and rabbit.
     */
    public void simulateOneStep() {
        step++;
        List<Animal> newAnimals = new ArrayList<Animal>();

        // New List to hold newborn rabbitList.
        // Loop through all Rabbits. Let each run around.
        for (int i = 0; i < animalList.size(); i++) {
            Animal animal = animalList.get(i);
            animal.act(field, updatedField, newAnimals);
            if (!animal.isAlive()) {
                animalList.remove(i);
                i--;
            }


        }
        // Add new born animalList to the main list of animalList.
        animalList.addAll(newAnimals);


        // Swap the field and updatedField at the end of the step.
        Field temp = field;
        field = updatedField;
        updatedField = temp;
        updatedField.clear();

        stats.generateCounts(field);
        updateGraph();
    }

    public void updateGraph() {
        Counter count;
        for (Counter c : stats.getCounts()) {
            graph.plotPoint(step, c.getCount(), c.getClassName());
        }
    }

    /**
     * Reset the simulation to a starting position.
     */
    public void reset() {
        step = 0;
        animalList.clear();
        field.clear();
        updatedField.clear();
        initializeBoard(field);

        if (graph != null) {
            graph.clear();
            graph.setDataRanges(0, 500, 0, field.getHeight() * field.getWidth());
        }

        // Show the starting state in the view.
        // view.showStatus(step, field);
    }

    /**
     * Populate a field with foxList and rabbitList.
     *
     * @param field The field to be populated.
     */
    private void initializeBoard(Field field) {
        Random rand = new Random();
        field.clear();
        for (int row = 0; row < field.getHeight(); row++) {
            for (int col = 0; col < field.getWidth(); col++) {
                if (rand.nextDouble() <= FOX_CREATION_PROBABILITY) {
                    Fox fox = new Fox(true);
                    fox.setLocation(col, row);
                    animalList.add(fox);
                    field.put(fox, col, row);
                } else if (rand.nextDouble() <= RABBIT_CREATION_PROBABILITY) {
                    Rabbit rabbit = new Rabbit(true);
                    rabbit.setLocation(col, row);
                    animalList.add(rabbit);
                    field.put(rabbit, col, row);
                } else if (rand.nextDouble() <= DEER_CREATION_PROBABILITY) {
                    Deer deer = new Deer(true);
                    deer.setLocation(col, row);
                    animalList.add(deer);
                    field.put(deer, col, row);
                } else if (rand.nextDouble() <= BEAR_CREATION_PROBABILITY) {
                    Bear bear = new Bear(true);
                    bear.setLocation(col, row);
                    animalList.add(bear);
                    field.put(bear, col, row);
                }
            }
        }
        Collections.shuffle(animalList);
    }

    private boolean isViable() {
        return stats.isViable(field);
    }

    public Field getField() {
        return this.field;
    }

    // Draw field if we have a gui defined
    public void drawField() {
        if ((graphicsWindow != null) && (view != null)) {
            view.drawField(this.field);
        }
    }

    public void drawGraph() {
        graph.draw();
    }

    /***
     * Set the step number of the simulator.  (Used for restoring saved games).
     * @param steps
     */
    private void setStep(int steps) {
        step = steps;
    }

    /***
     * Sets the field of the simulator.  (Used for restoring saved games).
     * @param newField
     */
    private void setField(Field newField) {
        field = newField;
    }

    /***
     * Sets the list of rabbits.  Used for restoring saved games.
     * @param newAnimalList
     */
    private void setAnimalList(ArrayList<Animal> newAnimalList) {
        animalList = newAnimalList;
    }


    // Perform an action when the mouse was clicked.
    // parameters are the x, y screen coordinates the user clicked on.
    // Note: you probably want to modify handleMouseClick(Field.Location) which
    // gives you the location they clicked on in the grid.
    public void handleMouseClick(float mouseX, float mouseY) {
        Location loc = view.gridLocationAt(mouseX, mouseY); // get grid at
        // click.
        if (loc == null) return;

        for (int x = loc.getCol() - 8; x < loc.getCol() + 8; x++) {
            for (int y = loc.getRow() - 8; y < loc.getRow() + 8; y++) {
                Location locToCheck = new Location(x, y);
                if (field.isInGrid(locToCheck)) {
                    Object animal = field.getObjectAt(locToCheck);
                    if (animal instanceof Rabbit)
                        animalList.remove((Animal) animal);
                    field.put(null, locToCheck);
                    updatedField.put(null, locToCheck);
                }
            }
        }
    }

    private void handleMouseClick(Location l) {
        System.out.println("testing");
        System.out.println("Change handleMouseClick in Simulator.Simulator.java to do something!");
    }

    public void handleMouseDrag(int mouseX, int mouseY) {
        Location loc = this.view.gridLocationAt(mouseX, mouseY); // get grid at
        // click.
        if (loc == null)
            return; // if off the screen, exit
        handleMouseDrag(loc);
    }

    private void handleMouseDrag(Location l) {

        System.out.println("test");
        System.out.println("Change handleMouseDrag in Simulator.Simulator.java to do something!");
    }
}
