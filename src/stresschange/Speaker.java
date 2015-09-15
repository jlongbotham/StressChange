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
    public int id = 0;

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

    public void updateParentAverage() {
        // get average of current generation's probabilities to update probabilities in next generation
        Bag parents = speakers.convos.getAllNodes();
        System.out.println("This many parents: " + parents.size());
        // based on distances restrict the parents to those that are relatively close
        if (StressChange.maxDistance != 0) {
            Double2D speaker = speakers.field.getObjectLocation(this); // query to get location of current speaker
            Bag parentsClose = new Bag(); // reset bag of parents  
            for (int i = 0; i < parents.size(); i++) { // iterate through edges
                Double2D parent = speakers.field.getObjectLocation(parents.get(i)); // for current edge get parent location
                System.out.println("Speaker is at x: " + speaker.x + "and y: " + speaker.y);
                System.out.println("Parent is at x: " + parent.x + "and y: " + parent.y);
                double distance = Math.sqrt(Math.pow((speaker.x - parent.x),2) + Math.pow((speaker.y - parent.y),2)); // TODO a^2 + b^2 = c^2 to get distance
                System.out.println("The distance is:" + distance);
                if(distance < StressChange.maxDistance && distance != 0.0){
                    parentsClose.add(parents.get(i)); // if distance below maxDistance, add to parents bag 
                }                                     // RISK having 0 parents - maybe just take certain percentage of closest ones?
            }
            // update location (randomly?)
            parents = parentsClose;
        }  
        System.out.println("Number of close parents: " + parents.size());

        for (int i = 0; i < words.size(); i++) { // iterate through array of WordPair objects for current Speaker
            double parentNounProb = 0.0; // reset parent probabilities
            double parentVerbProb = 0.0;

            WordPair word = words.get(i);

            for (int j = 0; j < parents.size(); j++) { // iterate through Bag of Speakers at current state
                Speaker parent = (Speaker) parents.get(j);
                // if we're at a new generation, update all parents' probabilities
                if (StressChange.count % StressChange.numSpeakers == 0){
                    parent.words.get(i).currentNounProb = parent.words.get(i).nextNounProb;
                    parent.words.get(i).currentVerbProb = parent.words.get(i).nextVerbProb;
                }
                
                // get averages of parents' word pair probabilities              
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
        
        updateParentAverage();  // first get parent averages    
        StressChange.count++; // update count

        if(id >= 0 && id < 6) {System.out.println("Speaker " + id + " ================");}

        for (int i = 0; i < words.size(); i++) { // then iterate over each word pair and run model

            WordPair word = words.get(i);

            // stochastic models uses word frequencies to compute learned probabilities
            // for deterministic model it is a fixed number for all nouns and verbs
            // for stochastic model it is changed to a random number between 0 and 1000 - incidentally will never equal 1
            // TODO: maybe change test to 1st state rather than default frequency
            if (StressChange.mode.equals("stochastic")) {
                if (word.freqNoun == 1) {
                    word.freqNoun = (int) (speakers.random.nextDouble() * 999) + 1; // add 1 to avoid frequency of 0
                }
                if (word.freqVerb == 1) {
                    word.freqVerb = (int) (speakers.random.nextDouble() * 999) + 1;
                }
            }

            if (id >= 0 && id < 6) {
                if (StressChange.logging.equals("all")) {
                    System.out.println(word); // print current state of all words
                } else {
                    for (String rep : StressChange.representativeWords) {
                        if (word.word.contains(rep)) {
                            System.out.println(word); // only print current state if word is in representative array
                            break;
                        }
                    }
                }
            }

            if (StressChange.model.equals("mistransmission")) {
                mistransmission(word); // 1st model
            } else if (StressChange.model.equals("constraint")) {
                constraint(word); // 2nd model
            } else if (StressChange.model.equals("constraintWithMistransmission")) {
                constraintWithMistransmission(word); // 3rd model
            } else if (StressChange.model.equals("prior")) {
                prior(word); // 4th model
            } else if (StressChange.model.equals("priorWithMistransmission")) {
                priorWithMistransmission(word); // 5th model
            }
        }
    }

    public void mistransmission(WordPair word) { // Model 1
        // set the noun and verb probabilities for the next generation
        if (StressChange.logging.equals("all")) {
            System.out.println("BEGINNING OF MISTRANSMISSION: " + word);
        }
        if (StressChange.mode.equals("stochastic")) {
            // updated word.misNounPrev and word.misVerbPrev to ((number of randoms < misProbPQ) / word frequency)
            int numberMisheardNoun = 0;
            int numberMisheardVerb = 0;
            for (int i = 0; i < word.freqNoun; i++) {
                if (speakers.random.nextDouble() < StressChange.misProbP) {
                    numberMisheardNoun++;
                }
            }
            for (int i = 0; i < word.freqVerb; i++) {
                if (speakers.random.nextDouble() < StressChange.misProbQ) {
                    numberMisheardVerb++;
                }
            }
            word.misNounPrev = (double) numberMisheardNoun / word.freqNoun;
            word.misVerbPrev = (double) numberMisheardVerb / word.freqVerb;
        }
        // otherwise deterministically update based on initial mistransmission probabilities
        
        if (StressChange.logging.equals("all")) {
            System.out.println("AVG PARENT PROBABILITIES: noun = " + word.avgParentNounProb + ", verb = " + word.avgParentVerbProb);
        }        
        
        word.nextNounProb = getMisNoun(word.misNounPrev, word.avgParentNounProb); // update noun probabilities
        word.nextVerbProb = getMisVerb(word.misVerbPrev, word.avgParentVerbProb); // update verb probabilities

        if (StressChange.logging.equals("all")) {
            System.out.println("END OF MISTRANSMISSION: " + word);
        }
    }

    public void constraint(WordPair word) { // Model 2
        // updates noun and verb probabilities based on constraint only
        if (StressChange.logging.equals("all")) {
            System.out.println("BEGINNING OF CONSTRAINT: " + word);
        }
        if (word.avgParentNounProb < word.avgParentVerbProb) { // if constraint is met, then estimate equals expectation
            word.nextNounProb = word.avgParentNounProb;
            word.nextVerbProb = word.avgParentVerbProb;
        } else {
            word.nextNounProb = (word.avgParentNounProb + word.avgParentVerbProb) / 2; // if constraint is not met, then estimate equals average of expectations
            word.nextVerbProb = (word.avgParentNounProb + word.avgParentVerbProb) / 2;
        }

        if (StressChange.logging.equals("all")) {
            System.out.println("END OF CONSTRAINT: " + word);
        }
    }

    public void constraintWithMistransmission(WordPair word) { // Model 3
        // the same as constraint(), but on "heard" examples (i.e. mistransmission)
        constraint(word);
        mistransmission(word);
    }

    public void prior(WordPair word) { // Model 4

        if (StressChange.logging.equals("all")) {
            System.out.println("BEGINNING OF PRIOR: " + word);
        }

        // calculate learned probabilities (P) based on word frequencies sampled from parent probabilities
        double kNoun = 0.0; // number of nouns heard as final stress
        double kVerb = 0.0; // number of verbs heard as final stress

        for (int i = 0; i < word.freqNoun; i++) {
            if (speakers.random.nextDouble() <= word.currentNounProb) {
                kNoun++;
            }
        }

        for (int i = 0; i < word.freqVerb; i++) {
            if (speakers.random.nextDouble() <= word.currentVerbProb) {
                kVerb++;
            }
        }

        double p11 = ((word.freqNoun - kNoun) / word.freqNoun) * ((word.freqVerb - kVerb) / word.freqVerb);
        double p12 = ((word.freqNoun - kNoun) / word.freqNoun) * (kVerb / word.freqVerb);
        double p21 = (kNoun / word.freqNoun) * ((word.freqVerb - kVerb) / word.freqVerb);
        double p22 = (kNoun / word.freqNoun) * (kVerb / word.freqVerb);

        if (StressChange.logging.equals("all")) {
            System.out.println("P11: " + p11);
            System.out.println("P12: " + p12);
            System.out.println("P21: " + p21);
            System.out.println("P22: " + p22);
        }

        // calculate prior probabilities (lambda) based on current state of lexicon
        double lambda11 = 0.0;
        double lambda12 = 0.0;
        double lambda21 = 0.0;
        double lambda22 = 0.0;

        for (WordPair wordPair : words) {
            if (wordPair.currentNounProb < 0.5 && wordPair.currentVerbProb < 0.5) { // lambda11
                lambda11++;
            } else if (wordPair.currentNounProb < 0.5 && wordPair.currentVerbProb >= 0.5) { // lambda12
                lambda12++;
            } else if (wordPair.currentNounProb >= 0.5 && wordPair.currentVerbProb < 0.5) { // lamda21
                lambda21++;
            } else { // lambda22
                lambda22++;
            }
        }

        // get prior probabilities
        lambda11 = lambda11 / words.size();
        lambda12 = lambda12 / words.size();
        lambda21 = lambda21 / words.size();
        lambda22 = lambda22 / words.size();

        if (StressChange.logging.equals("all")) {
            System.out.println("LAMBDA11: " + lambda11);
            System.out.println("LAMBDA12: " + lambda12);
            System.out.println("LAMBDA21: " + lambda21);
            System.out.println("LAMBDA22: " + lambda22);
        }

        // update current noun and verb probabilities based on learned and prior probabilities
        word.nextNounProb = ((lambda21 * p21) + (lambda22 * p22)) / ((lambda11 * p11) + (lambda12 * p12) + (lambda21 * p21) + (lambda22 * p22));
        word.nextVerbProb = ((lambda12 * p12) + (lambda22 * p22)) / ((lambda11 * p11) + (lambda12 * p12) + (lambda21 * p21) + (lambda22 * p22));

        if (StressChange.logging.equals("all")) {
            System.out.println("END OF PRIOR: " + word);
        }
    }

    public void priorWithMistransmission(WordPair word) {
        mistransmission(word);
        prior(word);
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

        // Run chosen model
        runModel();

    }

    public String toString() {
        // TODO - what info should be printed for a speaker?
        // ID and summary of their word pair probabilities at a given step?
        return "";
    }

}
