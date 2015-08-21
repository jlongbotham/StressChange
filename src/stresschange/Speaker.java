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

    //private HashMap<String, double[]> stress;
    private ArrayList<WordPair> words = new ArrayList<>();
    private int id = 0;

    public Speaker(HashMap initialStress, int i) {
        this.id = i; // get speaker ID for potentially tracking individuals later on
        Map<String, double[]> map = initialStress;
        for (Map.Entry<String, double[]> e : map.entrySet()) {
            words.add(new WordPair(e.getKey(), e.getValue()[0], e.getValue()[1]));
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

    public void step(SimState state) { // method step implements sim.engine.Steppable interface, allows "Speaker" to be not just object but also agent
        StressChange speakers = (StressChange) state;
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
                
        // that was all about moving the speaker on the field
        // now we update the probabilities of their word pairs based on mistransmission

        // set the noun and verb probabilities for the next generation
        // TODO: update not based on this speaker's probability but on average of whole population in previous generation or a subset of the population
        for (int i = 0; i < words.size(); i++) {
            System.out.println(words.get(i)); // print out current state of each word pair
            words.get(i).currentNounProb = getMisNoun(speakers.misProbP, words.get(i).currentNounProb); // update noun probabilities
            words.get(i).currentVerbProb = getMisVerb(speakers.misProbQ, words.get(i).currentVerbProb); // update verb probabilities
        }

    }

    public String toString() {
        // TODO - what info should be printed for a speaker?
        // ID and summary of their word pair probabilities at a given step?
        return "";
    }

}
