package Animals;
import Animals.*;
import Field.*;
import Graph.*;

import java.io.Serializable;
import java.util.List;


public class Deer extends Animal implements Serializable {
    private static int RABBIT_FOOD_VALUE = 5;
    private static int BEAR_FOOD_VALUE = 2;
    private int foodLevel;

    public Deer(Boolean startWithRandomAge){
        super(startWithRandomAge);
        startWithRandomAge = true;
        BREEDING_AGE = 3;
        MAX_AGE = 50;
        BREEDING_PROBABILITY = 0.4;
        MAX_LITTER_SIZE = 8;
        /**
         * Create a new rabbit. A rabbit may be created with age
         * zero (a new born) or with a random age.
         *
         * @param startWithRandomAge If true, the rabbit will have a random age.
         */

        if (startWithRandomAge) {
            age = (int)(Math.random()*MAX_AGE);
            foodLevel = (int)(Math.random()*RABBIT_FOOD_VALUE);
            foodLevel += (int)(Math.random() * BEAR_FOOD_VALUE);
        } else {
            // leave age at 0
            foodLevel = RABBIT_FOOD_VALUE + BEAR_FOOD_VALUE;
        }
    }
    /**
     * This is what the bear does most of the time: it hunts for foxes. In the
     * process, it might breed, die of hunger, die of old age, or die to a rabbit
     *
     * @param currentField
     *            The field currently occupied.
     * @param updatedField
     *            The field to transfer to.
     * @param babyDeerStorage
     *            A list to add newly born foxes to.
     */

    public void act(Field currentField, Field updatedField, List<Animal> babyDeerStorage) {
        super.act(currentField, updatedField, babyDeerStorage);
        incrementHunger();
        if (alive) {
            // New foxes are born into adjacent locations.
            int births = breed();
            for (int b = 0; b < births; b++) {
                Deer newDeer = new Deer(false);
                newDeer.setFoodLevel(this.foodLevel);
                babyDeerStorage.add(newDeer);
                Location loc = updatedField.randomAdjacentLocation(location);
                newDeer.setLocation(loc);
                updatedField.put(newDeer, loc);
            }
            // Move towards the source of food if found.
            Location newLocation = findFood(currentField, location);
            if (newLocation == null) { // no food found - move randomly
                newLocation = updatedField.freeAdjacentLocation(location);
            }
            if (newLocation != null) {
                setLocation(newLocation);
                updatedField.put(this, newLocation);
            } else {
                // can neither move nor stay - overcrowding - all locations
                // taken
                alive = false;
            }
        }
    }


    /**
     * Make this fox more hungry. This could result in the fox's death.
     */
    private void incrementHunger() {
        foodLevel--;
        if (foodLevel <= 0) {
            alive = false;
        }
    }

    /**
     * Tell the bear to look for foxes adjacent to its current location. Only
     * the first live fox is eaten.
     *
     * @param field
     *            The field in which it must look.
     * @param location
     *            Where in the field it is located.
     * @return Where food was found, or null if it wasn't.
     */
    private Location findFood(Field field, Location location) {
        List<Location> adjacentLocations = field.adjacentLocations(location);

        for (Location where : adjacentLocations) {
            Object animal = field.getObjectAt(where);
            if (animal instanceof Rabbit) {
                Rabbit rabbit = (Rabbit) animal;
                if (rabbit.isAlive()) {
                    rabbit.setEaten();
                    foodLevel = RABBIT_FOOD_VALUE;
                    return where;
                }
            }else if(animal instanceof Bear){
                Bear bear = (Bear) animal;
                if(bear.isAlive()){
                    bear.setEaten();
                    foodLevel += BEAR_FOOD_VALUE;
                    return where;
                }
            }
        }

        return null;
    }

    public void setFoodLevel(int fl) {
        this.foodLevel = fl;
    }
}

