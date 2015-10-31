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
public class StressChange extends SimState /*implements sim.portrayal.inspector.Tabbable*/{

    // global settings determined by default, through command line or via GUI
    public Continuous2D field = new Continuous2D(1.0, 100, 100); // representation of space/field from sim.field.continuous.Continuous2D, bounds 100x100
    public Continuous2D targetSpace = new Continuous2D(1.0, 110, 110);
    public static int numSpeakers = 10; // number of speakers
    public static Network convos = new Network(false); // speaker relationships graph, false indicates undirected
    public Bag speakers = new Bag();
    public static int count = 0; // count of speakers updated
    public static int step = 0; // step count
    public double totalAvgVerbProb = 0.0; // to get universal average for visualization
    public double totalAvgNounProb = 0.0;

    public static double misProbP = 0.1; // mistransmission probability for N
    public static double misProbQ = 0.1; // mistransmission probability for V

    public static int freqNoun = 1000; // default frequency for nouns
    public static int freqVerb = 1000; // default frequency for verbs
    
    public static double lambda11 = 0.2; // only these two need to be set as lambda12 = (1 - (lambda11 + lambda22))
    public static double lambda22 = 0.4; // and lambda21 is always 0.0
    
    public static double targetClassLambda11 = 0.2; // only these two need to be set as lambda12 = (1 - (lambda11 + lambda22))
    public static double targetClassLambda22 = 0.4; // and lambda21 is always 0.0

    public static String distModel = "grouped"; // default distance model - options are "none", "random", "absolute", "probabilistic", "grouped", "lattice"
    public static double maxDistance = 30; // default maximum distance 
    public int x = 5; public int y = 5; // fixed x and y for lattice model
    
    public static Boolean priorClass = true; // use prefixes as prior class

    public static String model = "priorWithMistransmission"; // default if no arguments are given - other options are "mistransmission", "constraint", "constraintWithMistransmission", "prior", "priorWithMistransmission"
    public static Boolean stochastic = true; // by default stochastic model is not used
    public static int loggingLevel = 0;
    public static String logging = "none"; // default if no arguments are given - other options are "some", "all", "troubleshooting"
    public static String[] representativeWords = {"abstract", "accent", "addict", "reset", "sub-let", "a-test"};
    public static String targetWord = "address";
    public static String targetWordPrefix = "na"; // by default is "na", updated via WordPair.java when target word belongs to a prefix class
    
    // properties for "Model" tab in GUI
    public int getNumSpeakers() { return numSpeakers; }
    public void setNumSpeakers(int val) {if (val > 0) numSpeakers = val; }
    public Object domNumSpeakers() { return new Interval(10, 100); }
    public String nameNumSpeakers() {return "Number of speakers";}
    
    public int getNounFreq() { return freqNoun; }
    public void setNounFreq(int val) {if (val > 0) freqNoun = val; }
    public Object domNounFreq() { return new Interval(10, 1000); }
    public String nameNounFreq() {return "Noun frequency";}
    public String desNounFreq() {return "Noun frequency of the target word";}
    
    public int getVerbFreq() { return freqVerb; }
    public void setVerbFreq(int val) {if (val > 0) freqVerb = val; }
    public Object domVerbFreq() { return new Interval(10, 1000); }
    public String nameVerbFreq() {return "Verb frequency";}
    public String desVerbFreq() {return "Verb frequency of the target word";}
    
    public String getDistanceModel() { return distModel; }
    public void setDistanceModel(String s) { if(s.equals("none") || s.equals("random") || s.equals("absolute") || s.equals("probabilistic") || s.equals("grouped") || s.equals("lattice")) distModel = s; }
    public String nameDistanceModel() {return "Distance model";}
    public String desDistanceModel() {return "The optionsa are: none, random, absolute, probabilistic, grouped, lattice";}
    
    public String getModel() { return model; }
    public void setModel(String s) { if(s.equals("mistransmission") || s.equals("constraint") || s.equals("constraintWithMistransmission") || s.equals("prior") || s.equals("priorWithMistransmission")) model = s; }
    public String nameModel() {return "Simulation model";}
    public String desModel() {return "The model you want to run. Options are: mistransmission, constraint, constraintWithMistransmission, prior, priorWithMistransmission";}
    //TODO make popup, see Mason manual p.80-81
    //public Object domModel() {return new String[] { "mistransmission", "constraint", "constraintWithMistransmission", "prior", "priorWithMistransmission" };}
       
    public int getLogging() { return loggingLevel; }
    public void setLogging(int val) { loggingLevel = val; if(val == 0){logging = "none";} else if(val == 1){logging = "some";} else if(val == 2){logging = "all";} else if(val == 3){logging = "troubleshooting";} else {logging = "none";}}
    public Object domLogging() { return new Interval(0, 3); }
    public String nameLogging() {return "Logging level";}    
    public String desLogging() {return "0 = none; 1 = show words from visualization; 2 = show all words; 3 = show troubleshooting information";}

    public Boolean isStochastic() { return stochastic; }
    public void setStochastic(Boolean val) { stochastic = val; }
    public String desStochastic() {return "Determine \"heard\" examples by sampling from previous generation's average probability";}
    
    public Boolean isPriorClass() { return priorClass; }
    public void setPriorClass(Boolean val) { priorClass = val; }
    public String namePriorClass() {return "Prefix class prior";}
    public String desPriorClass() {return "Use prefixes as a class for determining prior probabilities";}
    
    public double getLambda11() { return lambda11; }
    public void setLambda11(double val) { lambda11 = val;}
    public Object domLambda11() { return new Interval(0.0, 1.0); }
    public String nameLambda11() {return "Prior {1,1} - general";}    
    public String desLambda11() {return "General prior probability for {1,1} stress pattern";}
    
    public double getLambda22() { return lambda22; }
    public void setLambda22(double val) { lambda22 = val;}
    public Object domLambda22() { return new Interval(0.0, 1.0); }
    public String nameLambda22() {return "Prior {2,2} - general";}    
    public String desLambda22() {return "General prior probability for {2,2} stress pattern";}
    
    public double getTargetClassLambda11() { return targetClassLambda11; }
    public void setTargetClassLambda11(double val) { targetClassLambda11 = val;}
    public Object domTargetClassLambda11() { return new Interval(0.0, 1.0); }
    public String nameTargetClassLambda11() {return "Prior {1,1} - target word prefix class";}    
    public String desTargetClassLambda11() {return "Prior probability for {1,1} stress pattern in the target word prefix class";}
    
    public double getTargetClassLambda22() { return targetClassLambda22; }
    public void setTargetClassLambda22(double val) { targetClassLambda22 = val;}
    public Object domTargetClassLambda22() { return new Interval(0.0, 1.0); }
    public String nameTargetClassLambda22() {return "Prior {2,2} - target word prefix class";}    
    public String desTargetClassLambda22() {return "Prior probability for {2,2} stress pattern in the target word prefix class";}
    
    public double getMisProbP() { return misProbP; }
    public void setMisProbP(double val) { misProbP = val;}
    public Object domMisProbP() { return new Interval(0.0, 1.0); }
    public String nameMisProbP() {return "Mistransmission prob N";}    
    public String desMisProbP() {return "Probability a noun will be misheard as first-syllable stress";}
    
    public double getMisProbQ() { return misProbQ; }
    public void setMisProbQ(double val) { misProbQ = val;}
    public Object domMisProbQ() { return new Interval(0.0, 1.0); }
    public String nameMisProbQ() {return "Mistransmission prob V";}    
    public String desMisProbQ() {return "Probability a verb will be misheard as second-syllable stress";}
    
    public String getTargetWord() { return targetWord; }
    public void setTargetWord(String s) { targetWord = s; }
    public String nameTargetWord() {return "Target word (n)";}
    public String desTargetWord() {return "Target word (n) for visualizations";}
    
    public static HashMap<String[], double[]> initialStress = new HashMap<>(); // initial N/V stress state, read from file in main method  

    public StressChange(long seed) {
        super(seed);
    }

    public void start() {
        super.start(); // very important!
        field.clear(); // clear the field
        //probSpace.clear(); // clear the probability visualization
        targetSpace.clear(); // clear the probability visualization
        convos.clear(); // clear the speakers
        count = 0;

        // add some speakers to the field
        if (distModel.equals("lattice")){ numSpeakers = 100; maxDistance = 11; } // fixed number of speakers and fixed distance for lattice
        for (int i = 0; i < numSpeakers; i++) {
            Speaker speaker = new Speaker(initialStress, i + 1);

            if (distModel.equals("grouped")) { // add two distinct groups with equal number of people
                if (i % 2 == 0) {
                    field.setObjectLocation(speaker,
                            new Double2D(field.getWidth() * 0.25 + 25 * (random.nextDouble() - 0.5),
                                    field.getHeight() * 0.25 + 25 * (random.nextDouble() - 0.5)));
                    speaker.group = "UK";
                } else {
                    field.setObjectLocation(speaker,
                            new Double2D(field.getWidth() * 0.75 + 25 * (random.nextDouble() - 0.5),
                                    field.getHeight() * 0.75 + 25 * (random.nextDouble() - 0.5)));
                    speaker.group = "US";
                }
            } else if (distModel.equals("lattice")) { // add fixed number of speakers at fixed points
                if (i != 0 && i % 10 == 0){ x += 10; y = 5;}
                field.setObjectLocation(speaker, new Double2D(x,y));
                y += 10;
            } else { // otherwise just one big group centered at the middle
                field.setObjectLocation(speaker,
                        new Double2D(field.getWidth() * 0.5 + 50 * (random.nextDouble() - 0.5),
                                field.getHeight() * 0.5 + 50 * (random.nextDouble() - 0.5)));
            }
            convos.addNode(speaker); // each speaker added to graph as a node
            schedule.scheduleRepeating(speaker);
            
            // add probability for visualization
            for (WordPair word1 : speaker.words) {
                if (word1.word.equals(targetWord)) {
                        targetSpace.setObjectLocation(speaker, new Double2D(word1.currentNounProb * 100 + 5, word1.currentVerbProb * 100 + 5));
                    }
                for (String word2 : representativeWords) {
                    if (word1.word.equals(word2)) {
                        word1.probSpace.setObjectLocation(speaker, new Double2D(word1.currentNounProb * 100 + 5, word1.currentVerbProb * 100 + 5));
                    } 
                }
            }
            
            count++;
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
        
        options.addOption("stochastic", false, "sample from previous generation's distributions");      
        
        Option optLogging = OptionBuilder.withArgName("logging")
                                .hasArg()
                                .withDescription("logging level")
                                .create("logging");
        options.addOption(optLogging);      

        Option optFreqNoun = OptionBuilder.withArgName("freqNoun")
                                .hasArg()
                                .withDescription("noun frequency of the target word")
                                .create("freqNoun");
        options.addOption(optFreqNoun);   
        
        Option optFreqVerb = OptionBuilder.withArgName("freqVerb")
                                .hasArg()
                                .withDescription("verb frequency of the target word")
                                .create("freqVerb");
        options.addOption(optFreqVerb);   
        
        Option optMisProbP = OptionBuilder.withArgName("misProbNoun")
                                .hasArg()
                                .withDescription("mistransmission probability for nouns")
                                .create("misProbP");
        options.addOption(optMisProbP);   
        
        Option optMisProbQ = OptionBuilder.withArgName("misProbVerb")
                                .hasArg()
                                .withDescription("mistransmission probability for verbs")
                                .create("misProbQ");
        options.addOption(optMisProbQ);   
        
        Option optLambda11 = OptionBuilder.withArgName("prior11General")
                                .hasArg()
                                .withDescription("general prior probability for {1,1} stress pattern")
                                .create("prior11General");
        options.addOption(optLambda11);   
        
        Option optLambda22 = OptionBuilder.withArgName("prior22General")
                                .hasArg()
                                .withDescription("general prior probability for {2,2} stress pattern")
                                .create("prior11General");
        options.addOption(optLambda22);   
        
        Option optTargetLambda11 = OptionBuilder.withArgName("prior11Target")
                                .hasArg()
                                .withDescription("prior probability for {1,1} stress pattern in the target word prefix class")
                                .create("prior11Target");
        options.addOption(optTargetLambda11);   
        
        Option optTargetLambda22 = OptionBuilder.withArgName("prior22Target")
                                .hasArg()
                                .withDescription("prior probability for {2,2} stress pattern in the target word prefix class")
                                .create("prior22Target");
        options.addOption(optTargetLambda22);   
        
        Option optDistModel = OptionBuilder.withArgName("distModel")
                                .hasArg()
                                .withDescription("distance model")
                                .create("distModel");
        options.addOption(optDistModel);   
        
        options.addOption("priorClass", false, "use word pair prefixes as a class for sharing prior probabilities");      
        
        Option optNumSpeakers = OptionBuilder.withArgName("numSpeakers")
                                .hasArg()
                                .withDescription("number of speakers")
                                .create("numSpeakers");
        options.addOption(optNumSpeakers); 
        
        Option optTargetWord = OptionBuilder.withArgName("targetWord")
                                .hasArg()
                                .withDescription("target word for visualizations")
                                .create("targetWord");
        options.addOption(optTargetWord); 

        try {  // parse the command line arguments
            CommandLine line = parser.parse(options, args);
            if(line.hasOption("model")) {
                model = line.getOptionValue("model");
            }
            if(line.hasOption("stochastic")) { 
                stochastic = true;
            } else {
                stochastic = false;
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
            if(line.hasOption("misProbP")) {
                misProbP = Integer.parseInt(line.getOptionValue("misProbP"));
            }
            if(line.hasOption("misProbQ")) {
                misProbQ = Integer.parseInt(line.getOptionValue("misProbQ"));
            }
            if(line.hasOption("prior11General")) {
                lambda11 = Integer.parseInt(line.getOptionValue("prior11General"));
            }
            if(line.hasOption("prior22General")) {
                lambda22 = Integer.parseInt(line.getOptionValue("prior22General"));
            }
            if(line.hasOption("prior11Target")) {
                targetClassLambda11 = Integer.parseInt(line.getOptionValue("prior11Target"));
            }
            if(line.hasOption("prior22Target")) {
                targetClassLambda22 = Integer.parseInt(line.getOptionValue("prior22Target"));
            }
            if(line.hasOption("distModel")) {
                distModel = line.getOptionValue("distModel");
            }
            if(line.hasOption("priorClass")) {
                priorClass = true;
            } else {
                priorClass = false;
            }
            if(line.hasOption("numSpeakers")) {
                numSpeakers = Integer.parseInt(line.getOptionValue("numSpeakers"));
            }
        } catch ( ParseException exp) {
            System.out.println("Unexpected exception: " + exp.getMessage());
        }

        if (StressChange.logging.equals("tabular")){
            System.out.println("Iteration,Speaker,Word,Prefix,Noun prob,Verb prob");
        }
        else if (!StressChange.logging.equals("none")) {
            System.out.println("Simulating " + model + " model with " + distModel + " distance model, showing " + logging + " words");
            System.out.println("N1 (target word noun frequency): " + freqNoun);
            System.out.println("N2 (target word verb frequency): " + freqVerb);
        }
        
        initialStress = new ReadPairs(System.getProperty("user.dir") + "/src/initialStressSmoothed.txt").OpenFile(); // read in initial pairs

        SimState state = new StressChange(System.currentTimeMillis());
        state.start();
        
        do {
            convos.removeAllEdges();
            if (! StressChange.logging.equals("none") && !StressChange.logging.equals("tabular")){  System.out.println(""); }
            //if (! StressChange.logging.equals("none")){  System.out.println("Generation at year " + (1500 + (state.schedule.getSteps()) * 25)); }  // 25-year generations            
            if (! StressChange.logging.equals("none") && !StressChange.logging.equals("tabular")){  System.out.println("==== ITERATION " + (state.schedule.getSteps() + 1) + " ===="); }
            if (!state.schedule.step(state)) {
                break;
            }
            step++;
        } while (state.schedule.getSteps() < 49); // maximum 50 iterations
        state.finish();

        System.exit(0);
    }

}
