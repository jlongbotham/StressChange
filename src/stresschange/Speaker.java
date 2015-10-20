/*
 --
 */
package stresschange;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import sim.engine.*;
import sim.util.*;
import sim.field.network.*;

/**
 * @author James
 */
public class Speaker implements Steppable {

    public ArrayList<WordPair> words = new ArrayList<>();
    public int id = 0; 
    public String group = "none";
    
    // methods for inspectors
    public int getSpeakerID(){return id;}
    public String getSpeakerGroup(){return group;}
    public Double getTargetWordProbability(){return (double) Math.round(getTargetWordProbability(StressChange.targetWord, "n") * 100) / 100;}
    
    public StressChange speakers;

    public Speaker(HashMap initialStress, int i) {
        this.id = i; // get speaker ID for potentially tracking individuals later on
        Map<String[], double[]> map = initialStress;
        for (Map.Entry<String[], double[]> e : map.entrySet()) {
            words.add(new WordPair(e.getKey()[0], e.getValue()[0], e.getValue()[1], e.getKey()[1]));  // create WordPair objects for each word pair from the initial HashMap
        }
    }
    
    // get specific word probability for visualization
    public double getTargetWordProbability(String word, String pos){
        double prob = 0.0;
            for (WordPair word1 : words) {
            if (word1.word.equals(word)) { 
                if (pos.equals("n")) { // then update for field visualization
                    prob = word1.currentNounProb;
                } else if (pos.equals("v")) {
                    prob = word1.currentVerbProb;
                }
            }
        }
        return prob;
    }
    
    public void getRepresentativeWordProbabilities(){ // update probSpace probabilities for visualization
        for (WordPair word1 : words) {
            if (word1.word.equals(StressChange.targetWord)) {
                speakers.targetSpace.setObjectLocation(this, new Double2D(word1.currentNounProb * 100 + 5, word1.currentVerbProb * 100 + 5));
            }
            for (String word2 : StressChange.representativeWords) { // update overview visualization
                if (word1.word.equals(word2)) {
                    word1.probSpace.setObjectLocation(this, new Double2D(word1.currentNounProb * 100 + 5, word1.currentVerbProb * 100 + 5));
                }
            }
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
        
        Bag edges = speakers.convos.getEdges(this, null);
        for (Object o : edges){
            Edge e = (Edge) o;
            speakers.convos.removeEdge(e); // remove connections
        }       
        
        if (StressChange.logging.equals("troubleshooting")){ System.out.println("Total number of potential parents: " + parents.size()); }
        
        if (! StressChange.distModel.equals("none")) {  // if there is a distance model, parents bag should be restricted
            Double2D speaker = speakers.field.getObjectLocation(this); // query to get location of current speaker
            if (StressChange.logging.equals("troubleshooting")){ System.out.println("Speaker from " + group + " is at x: " + speaker.x + " and y: " + speaker.y); }
            Bag parentsClose = new Bag(); // reset bag of parents  
            for (int i = 0; i < parents.size(); i++) { // iterate through edges
                Double2D parent = speakers.field.getObjectLocation(parents.get(i)); // for current edge get parent location
                if (StressChange.logging.equals("troubleshooting")){ System.out.println("Potential parent is at x: " + parent.x + " and y: " + parent.y); }
                double distance = Math.sqrt(Math.pow((speaker.x - parent.x),2) + Math.pow((speaker.y - parent.y),2)); // get distance
                if (StressChange.logging.equals("troubleshooting")){ System.out.println("The distance is: " + distance); }
                if (StressChange.distModel.equals("probabilistic")){ // distance is inversely proportional to whether the parent is heard 
                    if (distance < 25){ // only searching when distance is less than 50
                        if ((25 * speakers.random.nextDouble()) >= distance ){
                            parentsClose.add(parents.get(i));
                            speakers.convos.addEdge(this, parents.get(i), distance);
                        }
                    }                        
                } else if(StressChange.distModel.equals("random")){ // each speaker talks to half of the previous generation - randomly 
                    if (speakers.random.nextDouble() >= 0.5){
                        parentsClose.add(parents.get(i));
                        speakers.convos.addEdge(this, parents.get(i), distance);
                    }
                } else { // otherwise it's absolute distance
                    if (distance < StressChange.maxDistance /* && distance != 0.0 */) {  // allow speaker to be own parent?
                        parentsClose.add(parents.get(i)); // if distance below maxDistance, add to parents bag 
                        speakers.convos.addEdge(this, parents.get(i), distance);
                        if (StressChange.logging.equals("troubleshooting")){ System.out.println("Close parent"); }
                    } else if (StressChange.distModel.equals("grouped") && this.id % 10 == 0 && i % 10 == 0){ 
                        // if the speaker is a "super-speaker" he also has connections to other groups
                        // typically this number is 20% in epidemiology -- but we're going in both directions to minimizing to 10%
                        if (StressChange.logging.equals("troubleshooting")){ System.out.println("SUPERSPEAKER close parent"); }
                        parentsClose.add(parents.get(i));
                        speakers.convos.addEdge(this, parents.get(i), distance);
                    }
                } 
            }
            if (!StressChange.distModel.equals("lattice")){ // update speaker location randomly, unless using lattice distance
                speakers.field.setObjectLocation(this, 
                    new Double2D(speaker.x + 5 * (speakers.random.nextDouble() - 0.5),
                                 speaker.y + 5 * (speakers.random.nextDouble() - 0.5)));
            }
            
            parents = parentsClose; // update bag of parents with close parents
        }  
        if (StressChange.logging.equals("troubleshooting")){ System.out.println("Number of close parents: " + parents.size()); }
        
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
        if (!StressChange.logging.equals("none")) { if(id >= 0 && id < 6) {System.out.println("Speaker " + id + " ================");} }
        
        updateParentAverage();  // first get parent averages    
        StressChange.count++; // update count

        for (int i = 0; i < words.size(); i++) { // then iterate over each word pair and run model

            WordPair word = words.get(i);

            // stochastic models uses word frequencies to compute learned probabilities
            // for deterministic model it is a fixed number for all nouns and verbs
            if (StressChange.stochastic) {
                if (word.freqNoun == 1) {
                    word.freqNoun = (int) (speakers.random.nextDouble() * 999) + 1; // add 1 to avoid frequency of 0
                }
                if (word.freqVerb == 1) {
                    word.freqVerb = (int) (speakers.random.nextDouble() * 999) + 1;
                }
            }

            if (!StressChange.logging.equals("none")) {
                if (id >= 0 && id < 6) {
                    if (StressChange.logging.equals("all") || StressChange.logging.equals("troubleshooting")) {
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
        if (StressChange.logging.equals("troubleshooting")) {
            System.out.println("BEGINNING OF MISTRANSMISSION: " + word);
        }
        if (StressChange.stochastic) {
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
        
        if (StressChange.logging.equals("troubleshooting")) {
            System.out.println("AVG PARENT PROBABILITIES: noun = " + word.avgParentNounProb + ", verb = " + word.avgParentVerbProb);
        }        
        
        word.nextNounProb = getMisNoun(word.misNounPrev, word.avgParentNounProb); // update noun probabilities
        word.nextVerbProb = getMisVerb(word.misVerbPrev, word.avgParentVerbProb); // update verb probabilities

        if (StressChange.logging.equals("troubleshooting")) {
            System.out.println("END OF MISTRANSMISSION: " + word);
        }
    }

    public void constraint(WordPair word) { // Model 2
        // updates noun and verb probabilities based on constraint only
        if (StressChange.logging.equals("troubleshooting")) {
            System.out.println("BEGINNING OF CONSTRAINT: " + word);
        }
        if (word.avgParentNounProb < word.avgParentVerbProb) { // if constraint is met, then estimate equals expectation
            word.nextNounProb = word.avgParentNounProb;
            word.nextVerbProb = word.avgParentVerbProb;
        } else {
            word.nextNounProb = (word.avgParentNounProb + word.avgParentVerbProb) / 2; // if constraint is not met, then estimate equals average of expectations
            word.nextVerbProb = (word.avgParentNounProb + word.avgParentVerbProb) / 2;
        }

        if (StressChange.logging.equals("troubleshooting")) {
            System.out.println("END OF CONSTRAINT: " + word);
        }
    }

    public void constraintWithMistransmission(WordPair word) { // Model 3
        // the same as constraint(), but on "heard" examples (i.e. mistransmission)
        mistransmission(word);
        constraint(word);
    }

    public void prior(WordPair word) { // Model 4

        if (StressChange.logging.equals("troubleshooting")) {
            System.out.println("BEGINNING OF PRIOR: " + word);
        }

        // calculate learned probabilities (P) based on word frequencies sampled from parent probabilities
        double kNoun = 0.0; // number of nouns heard as final stress
        double kVerb = 0.0; // number of verbs heard as final stress
                
        for (int i = 0; i < word.freqNoun; i++) {
            if (speakers.random.nextDouble() <= word.nextNounProb) {
                kNoun++;
            }
        }

        for (int i = 0; i < word.freqVerb; i++) {
            if (speakers.random.nextDouble() <= word.nextVerbProb) {
                kVerb++;
            }
        }

        double p11 = ((word.freqNoun - kNoun) / word.freqNoun) * ((word.freqVerb - kVerb) / word.freqVerb);
        double p12 = ((word.freqNoun - kNoun) / word.freqNoun) * (kVerb / word.freqVerb);
        double p21 = (kNoun / word.freqNoun) * ((word.freqVerb - kVerb) / word.freqVerb);
        double p22 = (kNoun / word.freqNoun) * (kVerb / word.freqVerb);

        if (StressChange.logging.equals("troubleshooting")) {
            System.out.println("P11: " + p11);
            System.out.println("P12: " + p12);
            System.out.println("P21: " + p21);
            System.out.println("P22: " + p22);
        }

        if (StressChange.step == 0) { // set lambdas initially
            if (StressChange.priorClass) { // reset lambda values based on prefix class
                word.lambda11 = 0.0;
                word.lambda12 = 0.0;
                word.lambda21 = 0.0;
                word.lambda22 = 0.0;

                for (WordPair wordPair : words) { // iterate through word pairs
                    if (wordPair.prefix.equals(word.prefix) || word.prefix.equals("na")) { // for current prefix class or "na"
                        word.prefixClassSize++;
                        if (wordPair.currentNounProb < 0.5 && wordPair.currentVerbProb < 0.5) { // lambda11
                            word.lambda11++;
                        } else if (wordPair.currentNounProb < 0.5 && wordPair.currentVerbProb >= 0.5) { // lambda12
                            word.lambda12++;
                        } else if (wordPair.currentNounProb >= 0.5 && wordPair.currentVerbProb < 0.5) { // lamda21
                            word.lambda21++;
                        } else { // lambda22
                            word.lambda22++;
                        }

                    }
                }
                // get prior probabilities
                word.lambda11 = word.lambda11 / word.prefixClassSize;
                word.lambda12 = word.lambda12 / word.prefixClassSize;
                word.lambda21 = word.lambda21 / word.prefixClassSize;
                word.lambda22 = word.lambda22 / word.prefixClassSize;
            } else {  // Set fixed lambda values, must sum to 1
                word.lambda11 = 0.2;
                word.lambda12 = 0.4;
                word.lambda21 = 0.0; // this one should always be 0.0
                word.lambda22 = 0.4;
            }
        }
        
        if (StressChange.logging.equals("troubleshooting")) {
            System.out.println("LAMBDA11: " + word.lambda11);
            System.out.println("LAMBDA12: " + word.lambda12);
            System.out.println("LAMBDA21: " + word.lambda21);
            System.out.println("LAMBDA22: " + word.lambda22);
            double R = (word.freqVerb / (1 + (word.freqVerb - 1) * (word.lambda12 / word.lambda11))) * (word.freqNoun / (1 + (word.freqNoun - 1) * (word.lambda12 / word.lambda22)));
            System.out.println("R: " + R);
        }
        
        // update current noun and verb probabilities based on learned and prior probabilities
        word.nextNounProb = ((word.lambda21 * p21) + (word.lambda22 * p22)) / ((word.lambda11 * p11) + (word.lambda12 * p12) + (word.lambda21 * p21) + (word.lambda22 * p22));
        word.nextVerbProb = ((word.lambda12 * p12) + (word.lambda22 * p22)) / ((word.lambda11 * p11) + (word.lambda12 * p12) + (word.lambda21 * p21) + (word.lambda22 * p22));

        if (StressChange.logging.equals("troubleshooting")) {
            System.out.println("END OF PRIOR: " + word);
        }
    }

    public void priorWithMistransmission(WordPair word) {
        mistransmission(word);
        prior(word);
    }

    public void step(SimState state) { // method step implements sim.engine.Steppable interface, allows "Speaker" to be not just object but also agent
        speakers = (StressChange) state;

        // Run chosen model
        runModel();
        getRepresentativeWordProbabilities();

    }

    public String toString() {
        // TODO - what info should be printed for a speaker?
        // ID and summary of their word pair probabilities at a given step?
        return "";
    }

}
