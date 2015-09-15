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
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
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
    public static int numSpeakers = 10; // number of speakers
    public static Network convos = new Network(false); // speaker relationships graph, false indicates undirected
    public Bag speakers = new Bag();
    public static int count = 0; // count of speakers updated

    public static double misProbP = 0.1; // mistransmission probability for N
    public static double misProbQ = 0.1; // mistransmission probability for V

    public static int freqNoun = 0; // default frequency for nouns
    public static int freqVerb = 0; // default frequency for verbs
    
    public static double maxDistance = 10;

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
                    new Double2D(field.getWidth() * 0.5 + 10*(random.nextDouble() - 0.5),
                            field.getHeight() * 0.5 + 10*(random.nextDouble() - 0.5)));

            convos.addNode(speaker); // each speaker added to graph as a node
            schedule.scheduleRepeating(speaker);
        }

        // add edges between speakers defining closeness
        speakers = convos.getAllNodes(); // extract all speakers from the graph, returns sim.util.Bag, like an ArrayList but faster
        for (int i = 0; i < speakers.size(); i++) { // loop through Bag of speakers
            Object speaker = speakers.get(i);

            // adds a random edge for each speaker
            Object speakerB = null;
            do {
                speakerB = speakers.get(random.nextInt(speakers.numObjs));
            } while (speaker == speakerB);
            double closeness = random.nextDouble();
            convos.addEdge(speaker, speakerB, new Double(closeness)); 
        }
    }

    public static void main(String[] args) throws IOException {
        //doLoop(StressChange.class, args); // this is the default for MASON but doesn't allow for many customizations

        /* TODO: add real command line options...
        // http://commons.apache.org/proper/commons-cli/usage.html
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        
        options.addOption( "a", "all", false, "do not hide entries starting with ." );


        
        //CommandLine cmd = parser.parse(options, args); */
        
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
            if (args.length > 3) {
                freqNoun = Integer.parseInt(args[3]);
                System.out.print(", " + args[3]);
            }
            if (args.length > 4) {
                freqVerb = Integer.parseInt(args[4]);
                System.out.print(", " + args[4]);
            }
            System.out.println("");
        }

        System.out.println("Simulating " + mode + " model with " + model + ", showing " + logging + " words");
        if (freqNoun != 0) {
            System.out.println("N1 (noun frequency): " + freqNoun);
            System.out.println("N2 (verb frequency): " + freqVerb);
        } else {
            System.out.println("N1 and N2 (noun and verb frequency): random");
        }
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
