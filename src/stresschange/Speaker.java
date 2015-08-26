/*
 --
 */
package stresschange;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import sim.field.network.*;

/**
 * @author James
 */
public class Speaker implements Steppable {

    public static final double MAX_FORCE = 3.0; // from tutorial

    public ArrayList<WordPair> words = new ArrayList<>();
    private int id = 0;

    public StressChange speakers;

    public Speaker(HashMap initialStress, int i) {
        this.id = i; // get speaker ID for potentially tracking individuals later on
        Map<String, double[]> map = initialStress;
        for (Map.Entry<String, double[]> e : map.entrySet()) {
            words.add(new WordPair(e.getKey(), e.getValue()[0], e.getValue()[1]));  // create WordPair objects for each word pair from the initial HashMap
        }
    }

    // get mistransmission updates
    public double getMisNoun(double p, double alphaPrev) {
        double alpha = alphaPrev * (1 - p);
        return alpha;
    }

    public double getMisVerb(double q, double betaPrev) {
        double beta = betaPrev + ((1 - betaPrev) * q);
        return beta;
    }

    // get likelihood based on observed data - not used yet
    public double getLikelihood(double prob) {
        return prob; // actually probably don't need to calculate the likelihood specially as it would just be probability of prev generation anyway
    }

    public void updateParentAverage() {
        // get average of current generation's probabilities to update probabilities in next generation
        // TODO: allow for restricting "parents" to only those speakers within a specific distance
        Bag parents = speakers.convos.getAllNodes();
        Bag distances = speakers.convos.getEdges(this, null); // maybe can be used to find how close speakers are and restrict who current generation learns from
        System.out.println(distances.get(0));
        for (int i = 0; i < words.size(); i++) { // iterate through array of WordPair objects for current Speaker
            //System.out.println(words.get(i)); // print out current state of each word pair
            double parentNounProb = 0.0; // reset parent probabilities
            double parentVerbProb = 0.0;

            WordPair word = words.get(i);

            for (int j = 0; j < parents.size(); j++) { // iterate through Bag of Speakers at current state
                // get averages of parents' word pair probabilities
                Speaker parent = (Speaker) parents.get(j);
                parentNounProb += parent.words.get(i).currentNounProb; // add up all the probabilities
                parentVerbProb += parent.words.get(i).currentVerbProb;

                if (j + 1 == parents.size()) {
                    parentNounProb = parentNounProb / parents.size(); // and divide by size of Bag of Speakers to get average
                    parentVerbProb = parentVerbProb / parents.size();
                }

            }
            word.avgParentNounProb = parentNounProb;
            word.avgParentVerbProb = parentVerbProb;

        }
    }

    public void runModel() {
        updateParentAverage(); // first get parent averages
        
        for (int i = 0; i < words.size(); i++) { // then iterate over each word pair and run model

            WordPair word = words.get(i);
            
            System.out.println(word); // print current state
            
            if (StressChange.model.equals("mistransmission")) {
                mistransmission(word); // 1st model
            } else if (StressChange.model.equals("constraint")) {
                constraint(word); // 2nd model
            } else if (StressChange.model.equals("constraintWithMistransmission")) {
                constraintWithMistransmission(word); // 3rd model
            }
        }
    }

    public void mistransmission(WordPair word) { // Model 1
        // set the noun and verb probabilities for the next generation
        word.currentNounProb = getMisNoun(speakers.misProbP, word.avgParentNounProb); // update noun probabilities
        word.currentVerbProb = getMisVerb(speakers.misProbQ, word.avgParentVerbProb); // update verb probabilities
    }

    public void constraint(WordPair word) { // Model 2
        // updates noun and verb probabilities based on constraint only
        if (word.avgParentNounProb < word.avgParentVerbProb) { // if constraint is met, then estimate equals expectation
            word.currentNounProb = word.avgParentNounProb;
            word.currentVerbProb = word.avgParentVerbProb;
        } else { 
            word.currentNounProb = (word.avgParentNounProb + word.avgParentVerbProb) / 2; // if constraint is not met, then estimate equals average of expectations
            word.currentVerbProb = (word.avgParentNounProb + word.avgParentVerbProb) / 2;
        }
    }

    public void constraintWithMistransmission(WordPair word) { // Model 3
        // the same as constraint(), but after mistransmission
        mistransmission(word);
        updateParentAverage();  // update parent probabilities based on mistransmission
        constraint(word);
    }

    public void step(SimState state) { // method step implements sim.engine.Steppable interface, allows "Speaker" to be not just object but also agent
        speakers = (StressChange) state;
        Continuous2D field = speakers.field;

        Double2D me = speakers.field.getObjectLocation(this); // query to get location of current Speaker

        MutableDouble2D sumForces = new MutableDouble2D(); // like Double2D except you can change X and Y values after the fact, also has addIn method

        /* only applies to tutorial */
        // go through my buddies and determine how much I want to be near them
        MutableDouble2D forceVector = new MutableDouble2D();
        Bag out = speakers.convos.getEdges(this, null);
        int len = out.size();
        for (int buddy = 0; buddy < len; buddy++) {
            Edge e = (Edge) (out.get(buddy));
            double buddiness = ((Double) (e.info)).doubleValue();

            // getOtherNode grabs the guy at the opposite end of the yard
            Double2D him = speakers.field.getObjectLocation(e.getOtherNode(this));

            // the further away I am the more I want to get near
            if (buddiness >= 0) {
                forceVector.setTo((him.x - me.x) * buddiness, (him.y - me.y) * buddiness);
                if (forceVector.length() > MAX_FORCE) // I'm far enough away
                {
                    forceVector.resize(MAX_FORCE);
                } else {
                    forceVector.setTo((him.x - me.x) * buddiness, (him.y - me.y) * buddiness);
                    if (forceVector.length() > MAX_FORCE) // I'm far enough away
                    {
                        forceVector.resize(0.0);
                    } else if (forceVector.length() > 0) {
                        forceVector.resize(MAX_FORCE - forceVector.length()); // invert the distance
                    }
                }

            }
            sumForces.addIn(forceVector);
        }

        // add in a vector to the "teacher" to not get too far away, increases with distance to the yard
        //sumForces.addIn(new Double2D((field.width * 0.5 - me.x) * speakers.forceToSchoolMultiplier,
        //        (field.height * 0.5 - me.y) * speakers.forceToSchoolMultiplier));
        // add some randomness, small and constant
        //sumForces.addIn(new Double2D(speakers.randomMultiplier * (speakers.random.nextDouble() * 1.0 - 0.5),
        //        speakers.randomMultiplier * (speakers.random.nextDouble() * 1.0 - 0.5)));
        sumForces.addIn(me); // start force at present location

        speakers.field.setObjectLocation(this, new Double2D(sumForces)); // set object location to the sum, must be a Double2D (immutable)
    /* */

        // Get probabilities and run chosen model
        //updateParentAverage();
        runModel();

    }

    public String toString() {
        // TODO - what info should be printed for a speaker?
        // ID and summary of their word pair probabilities at a given step?
        return "";
    }

}
