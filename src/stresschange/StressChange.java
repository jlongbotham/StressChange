/*
 * TODO:
 * - add option to "seed" different values for different groups
 */
package stresschange;

import java.io.IOException;
import java.util.HashMap;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
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
    public static int step = 0; // step count

    public static double misProbP = 0.1; // mistransmission probability for N
    public static double misProbQ = 0.1; // mistransmission probability for V

    public static int freqNoun = 1000; // default frequency for nouns
    public static int freqVerb = 1000; // default frequency for verbs

    public static String distModel = "none"; // default distance model - options are "none", "random", "absolute", "probabilistic", "grouped"
    public static double maxDistance = 20; // default maximum distance 
    
    public static String priorClass = "none"; // default classes for prior models - options are "none", "prefix"

    public static String model = "mistransmission"; // default if no arguments are given - other options are "mistransmission", "constraint", "constraintWithMistransmission", "prior", "priorWithMistransmission"
    public static String mode = "deterministic"; // default if no arguments are given - other option is "deterministic"
    public static String logging = "some"; // default if no arguments are given - other option is "all"
    public static String[] representativeWords = {"abstract", "accent", "addict", "reset", "sub-let", "a-test"};

    public static HashMap<String[], double[]> initialStress = new HashMap<>(); // initial N/V stress state, read from file in main method  

    public StressChange(long seed) {
        super(seed);
    }

    public void start() {
        super.start(); // very important!
        field.clear(); // clear the field
        convos.clear(); // clear the speakers

        // add some speakers to the field
        if (distModel.equals("grouped")) { // add two distinct groups with equal number of people
            for (int i = 0; i < (numSpeakers / 2); i++) {
                Speaker speaker = new Speaker(initialStress, i + 1);
                field.setObjectLocation(speaker,
                        new Double2D(field.getWidth() * 0.25 + 25 * (random.nextDouble() - 0.5),
                                field.getHeight() * 0.25 + 25 * (random.nextDouble() - 0.5)));

                convos.addNode(speaker); // each speaker added to graph as a node
                schedule.scheduleRepeating(speaker);
                speaker.group = "UK";
            }
            for (int i = 0; i < (numSpeakers / 2); i++) {
                Speaker speaker = new Speaker(initialStress, 2 * (i + 1));
                field.setObjectLocation(speaker,
                        new Double2D(field.getWidth() * 0.75 + 25 * (random.nextDouble() - 0.5),
                                field.getHeight() * 0.75 + 25 * (random.nextDouble() - 0.5)));

                convos.addNode(speaker); // each speaker added to graph as a node
                schedule.scheduleRepeating(speaker);
                speaker.group = "US";
            }
        } else { // otherwise just one big group
            for (int i = 0; i < numSpeakers; i++) {
                Speaker speaker = new Speaker(initialStress, i);
                field.setObjectLocation(speaker,
                        new Double2D(field.getWidth() * 0.5 + 25 * (random.nextDouble() - 0.5),
                                field.getHeight() * 0.5 + 25 * (random.nextDouble() - 0.5)));

                convos.addNode(speaker); // each speaker added to graph as a node
                schedule.scheduleRepeating(speaker);
            }
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
        // get options from command line
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();

        Option optModel = OptionBuilder.withArgName("model")
                                .hasArg()
                                .withDescription("model used for simulation")
                                .create("model");
        options.addOption(optModel);
        
        Option optMode = OptionBuilder.withArgName("mode")
                                .hasArg()
                                .withDescription("mode used for simulation")
                                .create("mode");
        options.addOption(optMode);      
        
        Option optLogging = OptionBuilder.withArgName("logging")
                                .hasArg()
                                .withDescription("logging level")
                                .create("logging");
        options.addOption(optLogging);      

        Option optFreqNoun = OptionBuilder.withArgName("freqNoun")
                                .hasArg()
                                .withDescription("noun frequency")
                                .create("freqNoun");
        options.addOption(optFreqNoun);   
        
        Option optFreqVerb = OptionBuilder.withArgName("freqVerb")
                                .hasArg()
                                .withDescription("verb frequency")
                                .create("freqVerb");
        options.addOption(optFreqVerb);   
        
        Option optDistModel = OptionBuilder.withArgName("distModel")
                                .hasArg()
                                .withDescription("distance model")
                                .create("distModel");
        options.addOption(optDistModel);   
        
        Option optPriorClass = OptionBuilder.withArgName("priorClass")
                                .hasArg()
                                .withDescription("class for prior probabilities")
                                .create("priorClass");
        options.addOption(optPriorClass);   

        try {  // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if(line.hasOption("model")) {
                model = line.getOptionValue("model");
            }
            if(line.hasOption("mode")) {
                mode = line.getOptionValue("mode");
            }
            if(line.hasOption("logging")) {
                logging = line.getOptionValue("logging");
            }
            if(line.hasOption("freqNoun")) {
                freqNoun = Integer.parseInt(line.getOptionValue("freqNoun"));
            }
            if(line.hasOption("freqVerb")) {
                freqVerb = Integer.parseInt(line.getOptionValue("freqVerb"));
            }
            if(line.hasOption("distModel")) {
                distModel = line.getOptionValue("distModel");
            }
            if(line.hasOption("priorClass")) {
                priorClass = line.getOptionValue("priorClass");
            }
        } catch ( ParseException exp) {
            System.out.println("Unexpected exception: " + exp.getMessage());
        }

        System.out.println("Simulating " + mode + " model with " + model + " and " + distModel + " distance model, showing " + logging + " words");
        if (freqNoun != 0) {
            System.out.println("N1 (noun frequency): " + freqNoun);
            System.out.println("N2 (verb frequency): " + freqVerb);
        } else {
            System.out.println("N1 and N2 (noun and verb frequency): random");
        }
        
        initialStress = new ReadPairs(System.getProperty("user.dir") + "/src/initialStressSmoothed.txt").OpenFile(); // read in initial pairs

        SimState state = new StressChange(System.currentTimeMillis());
        state.start();
        do {
            System.out.println("");
            System.out.println("Generation at year " + (1500 + (state.schedule.getSteps()) * 25)); // 25-year generations            
            if (!state.schedule.step(state)) {
                break;
            }
            step++;
        } while (state.schedule.getSteps() < 20); // 20 generations
        state.finish();

        System.exit(0);
    }

}
