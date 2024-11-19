package Animals;

import Animals.*;
import Field.*;
import Graph.*;
import java.io.Serializable;
import java.util.List;


public class Bear extends Animal implements Serializable{

    private static int RABBIT_FOOD_VALUE = 2;
    private static int FOX_FOOD_VALUE = 4;

    // The fox's food level, which is increased by eating rabbits.
    private int foodLevel;

    /**
     * Create a new rabbit. A rabbit may be created with age
     * zero (a new born) or with a random age.
     *
     * @param startWithRandomAge If true, the rabbit will have a random age.
     */

    public Bear(Boolean startWithRandomAge) {
        super(startWithRandomAge);
        startWithRandomAge = true;
        BREEDING_AGE = 10;
        MAX_AGE = 55;
        BREEDING_PROBABILITY = 0.5;
        MAX_LITTER_SIZE = 3;


        if (startWithRandomAge) {
            age = (int)(Math.random()*MAX_AGE);
            foodLevel = (int)(Math.random()*RABBIT_FOOD_VALUE);
            foodLevel += (int)(Math.random() * FOX_FOOD_VALUE);
        } else {
            // leave age at 0
            foodLevel = RABBIT_FOOD_VALUE + FOX_FOOD_VALUE;
        }
    }

    /**
     * This is what the fox does most of the time: it hunts for rabbits. In the
     * process, it might breed, die of hunger, or die of old age.
     *
     * @param currentField
     *            The field currently occupied.
     * @param updatedField
     *            The field to transfer to.
     * @param babyBearStorage
     *            A list to add newly born foxes to.
     */
    public void act(Field currentField, Field updatedField, List<Animal> babyBearStorage) {
        super.act(currentField, updatedField, babyBearStorage);
        incrementHunger();
        if (alive) {
            // New foxes are born into adjacent locations.
            int births = breed();
            for (int b = 0; b < births; b++) {
                Bear newBear = new Bear(false);
                newBear.setFoodLevel(this.foodLevel);
                babyBearStorage.add(newBear);
                Location loc = updatedField.randomAdjacentLocation(location);
                newBear.setLocation(loc);
                updatedField.put(newBear, loc);
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

    private void incrementHunger() {
        foodLevel--;
        if (foodLevel <= 0) {
            alive = false;
        }
    }
    public void setFoodLevel(int fl) {
        this.foodLevel = fl;
    }

    /**
     * Tell the fox to look for rabbits adjacent to its current location. Only
     * the first live rabbit is eaten.
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
            }else if(animal instanceof Fox){
                Fox fox = (Fox) animal;
                if(fox.isAlive()){
                    fox.setEaten();
                    foodLevel+= FOX_FOOD_VALUE;
                    return where;
                }
            }
        }
        return null;
    }
}
