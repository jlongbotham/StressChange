/*
 * 1st model: agents change based on mistransmission
 * Mistransmission probabilities are different for verbs vs nouns, as the probability is the p(final stress)
 * 
 * 2nd model: agents change based on constraint that final stress is more probable for verbs than nouns
 * 
 * 3rd model: 
 */
package stresschange;

import java.io.IOException;
import java.util.HashMap;
import sim.engine.*;
import sim.field.continuous.*;
import sim.util.*;
import sim.field.network.*;

/**
 * @author James
 */
public class StressChange extends SimState {

    /**
     * @param args the command line arguments
     */
    public Continuous2D field = new Continuous2D(1.0, 100, 100); // representation of space/field from sim.field.continuous.Continuous2D, bounds 100x100
    public int numSpeakers = 5; // number of speakers
    public Network convos = new Network(false); // speaker relationships graph, false indicates undirected
    public Bag speakers = new Bag();
    
    public static double misProbP = 0.1; // mistransmission probability for N
    public static double misProbQ = 0.1; // mistransmission probability for V
    
    public static String model = "constraintWithMistransmission"; // default if no arguments are given - other options are "mistransmission", "constraint"
    public static String mode = "stochastic"; // default if no arguments are given - other option is "deterministic"
    public static String logging = "some"; // default if no arguments are given - other option is "all"
    public static String[] representativeWords = {"abstract", "accent", "addict", "reset", "sub-let"};
    
    public static HashMap<String, double[]> initialStress = new HashMap<>(); // initial N/V stress state, read from file in main method  

    public StressChange(long seed) {
        super(seed);
    }

    public void start() {
        super.start(); // very important!
        field.clear(); // clear the field
        convos.clear(); // clear the speakers
        
        // add some speakers to the field
        for (int i = 0; i < numSpeakers; i++) {
            Speaker speaker = new Speaker(initialStress, i);
            field.setObjectLocation(speaker,
                    new Double2D(field.getWidth() * 0.5 + random.nextDouble() - 0.5,
                            field.getHeight() * 0.5 + random.nextDouble() - 0.5));

            convos.addNode(speaker); // each speaker added to graph as a node
            schedule.scheduleRepeating(speaker);
        }
        
    /* only applies to tutorial */
        // define like/dislike relationships
        speakers = convos.getAllNodes(); // extract all speakers from the graph, returns sim.util.Bag, like an ArrayList but faster
        for (int i = 0; i < speakers.size(); i++) { // loop through Bag of speakers
            Object speaker = speakers.get(i);

            // adds a random edge for each speaker
            Object speakerB = null;
            do {
                speakerB = speakers.get(random.nextInt(speakers.numObjs));
            } while (speaker == speakerB);
            double distance = random.nextDouble();
            convos.addEdge(speaker, speakerB, new Double(distance)); // closeness could be relative distance?

        }
    /*  */
    }
        
    // not used yet
    public double getSpeakerAverage() {
        // get average over all speakers and all pairs
        for (int i = 0; i < speakers.size(); i++) {
            Object speaker = speakers.get(i);
            System.out.println(speaker);
        }
        return 1.0;
    }

    public static void main(String[] args) throws IOException {
        //doLoop(StressChange.class, args); // this is the default for MASON but doesn't allow for many customizations
        
        // get arguments from command line
        if (args.length > 0) {
            model = args[0]; // default is mistransmission
            System.out.print("COMMAND LINE ARGUMENTS ARE: " + args[0]);
            if (args.length > 1) { 
                mode = args[1];
                System.out.print(", " + args[1]);
            }
            if (args.length > 2) { 
                logging = args[2];
                System.out.print(", " + args[2]);  
            }
            System.out.println("");
        } 
        
        System.out.println("Simulating " + mode + " model with " + model + ", showing " + logging + " words");
        initialStress = new ReadPairs(System.getProperty("user.dir") + "/src/initialStress.txt").OpenFile(); // read in initial pairs
        SimState state = new StressChange(System.currentTimeMillis());
        state.start();
        do {
            System.out.println("");
            System.out.println("Generation at year " + (1500 + (state.schedule.getSteps()) * 25)); // 25-year generations
            if (!state.schedule.step(state)) {
                break;
            }
        } while (state.schedule.getSteps() < 20); // 20 generations
        state.finish();

        System.exit(0);
    }

}
