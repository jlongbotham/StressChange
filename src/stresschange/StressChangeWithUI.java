/*
--
 */

package stresschange;
import sim.portrayal.continuous.*;
import sim.portrayal.network.*;
import sim.engine.*; 
import sim.display.*;
import sim.portrayal.simple.*;
import sim.portrayal.*;
import javax.swing.*;
import java.awt.Color;
import java.awt.*;
import java.io.IOException;

/**
 * @author James
 */
public class StressChangeWithUI extends GUIState {
    
    public Display2D fieldDisplay;
    public JFrame fieldDisplayFrame;
    ContinuousPortrayal2D fieldPortrayal = new ContinuousPortrayal2D();
    NetworkPortrayal2D connectionPortrayal = new NetworkPortrayal2D();
    
    public Display2D targetDisplay;
    public JFrame targetDisplayFrame;
    ContinuousPortrayal2D targetPortrayal = new ContinuousPortrayal2D();
    
    public Display2D probDisplay;
    public JFrame probDisplayFrame;
    ContinuousPortrayal2D abstractPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D resetPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D accentPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D atestPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D subletPortrayal = new ContinuousPortrayal2D();
    ContinuousPortrayal2D addictPortrayal = new ContinuousPortrayal2D();
    
    public static void main(String[] args) throws IOException{
        StressChangeWithUI vid = new StressChangeWithUI();
        Console c = new Console(vid);
        c.setVisible(true);
        
        StressChange.initialStress = new ReadPairs(System.getProperty("user.dir") + "/src/initialStressSmoothed.txt").OpenFile(); // read in initial pairs
    }
    
    public StressChangeWithUI() { super(new StressChange(System.currentTimeMillis())); }
    public StressChangeWithUI(SimState state) { super(state); }
    public static String getName() { return "Stress Change Simulation"; }
       
    public Object getSimulationInspectedObject() { return state; }  // non-volatile
    
    //public Inspector getInspector() { Inspector i = super.getInspector(); i.setVolatile(true); return i; }
    
    public void start() {
        super.start();
        setupPortrayals();
    }
    
    public void load(SimState state){
        super.load(state);
        setupPortrayals();
    }
    
    public void setupPortrayals(){
        StressChange speakers = (StressChange) state;
        
        // field to visualize space
        fieldPortrayal.setField(speakers.field);
        fieldPortrayal.setPortrayalForAll(new OvalPortrayal2D(1.5){ // colors change with probability of a specific noun or verb
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info){
                Speaker speaker = (Speaker)object;
                
                int probabilityShade = (int) (speaker.getTargetWordProbability(StressChange.targetWord, "n") * 255);
                if (probabilityShade > 255) probabilityShade = 255;
                paint = new Color(probabilityShade, 0, 255 - probabilityShade);
                super.draw(object, graphics, info);
            }

        });
        
        connectionPortrayal.setField(new SpatialNetwork2D(speakers.field, speakers.convos)); 
        connectionPortrayal.setPortrayalForAll(new SimpleEdgePortrayal2D());
        
        fieldDisplay.reset();
        fieldDisplay.setBackdrop(Color.lightGray);
        
        fieldDisplay.repaint();
        
        // visualization for target word space
        targetPortrayal.setField(speakers.targetSpace);
        targetPortrayal.setPortrayalForAll(new OvalPortrayal2D(Color.red, 1.5));
        
        targetDisplay.reset();
        targetDisplay.setBackdrop(Color.lightGray);
        
        targetDisplay.repaint();
        
        // visualization for probability trajectory
        for (Object object : speakers.convos.getAllNodes()) {
            Speaker speaker = (Speaker) object;

            for (WordPair word1 : speaker.words) {
                if (word1.word.equals("abstract")) {
                    abstractPortrayal.setField(word1.probSpace);
                    abstractPortrayal.setPortrayalForAll(new OvalPortrayal2D(Color.green, 1.5));
                } else if (word1.word.equals("reset")) {
                    resetPortrayal.setField(word1.probSpace);
                    resetPortrayal.setPortrayalForAll(new OvalPortrayal2D(Color.blue, 1.5));
                } else if (word1.word.equals("accent")) {
                    accentPortrayal.setField(word1.probSpace);
                    accentPortrayal.setPortrayalForAll(new OvalPortrayal2D(Color.orange, 1.5));
                } else if (word1.word.equals("a-test")) {
                    atestPortrayal.setField(word1.probSpace);
                    atestPortrayal.setPortrayalForAll(new OvalPortrayal2D(Color.black, 1.5));
                } else if (word1.word.equals("sub-let")) {
                    subletPortrayal.setField(word1.probSpace);
                    subletPortrayal.setPortrayalForAll(new OvalPortrayal2D(Color.magenta, 1.5));
                } else if (word1.word.equals("addict")) {
                    addictPortrayal.setField(word1.probSpace);
                    addictPortrayal.setPortrayalForAll(new OvalPortrayal2D(Color.yellow, 1.5));
                } 
            }
         }
        
        probDisplay.reset();
        probDisplay.setBackdrop(Color.lightGray);

        probDisplay.repaint();
    }
    
        
    public void init(Controller c){
        super.init(c);
        
        // field to visualize space
        fieldDisplay = new Display2D(600,600,this);
        fieldDisplay.setClipping(false);
        fieldDisplay.setBackdrop(Color.lightGray);
        
        fieldDisplayFrame = fieldDisplay.createFrame();
        fieldDisplayFrame.setTitle("Speaker Field");
        c.registerFrame(fieldDisplayFrame);
        fieldDisplayFrame.setVisible(true);
        fieldDisplay.attach(fieldPortrayal, "Field");
        fieldDisplay.attach(connectionPortrayal, "Connections");
        
        // visualization for target word
        targetDisplay = new Display2D(630,630,this);
        targetDisplay.setClipping(false);
        targetDisplay.setBackdrop(Color.lightGray);
        
        targetDisplayFrame = targetDisplay.createFrame();
        targetDisplayFrame.setTitle("Target Word Trajectory");
        c.registerFrame(targetDisplayFrame);
        targetDisplayFrame.setVisible(true);
        targetDisplay.attach(targetPortrayal, "Target");
                
        // visualization for overview
        probDisplay = new Display2D(630,630,this);
        probDisplay.setClipping(false);
        probDisplay.setBackdrop(Color.lightGray);       
        
        probDisplayFrame = probDisplay.createFrame();
        probDisplayFrame.setTitle("Stress Pattern Trajectories");
        c.registerFrame(probDisplayFrame);
        probDisplayFrame.setVisible(true);
        probDisplay.attach(abstractPortrayal, "abstract");
        probDisplay.attach(resetPortrayal, "reset");
        probDisplay.attach(accentPortrayal, "accent");
        probDisplay.attach(atestPortrayal, "a-test");
        probDisplay.attach(subletPortrayal, "sub-let");
        probDisplay.attach(addictPortrayal, "addict");
    }    
    
    public void quit(){
        super.quit();
        if (fieldDisplayFrame!=null) fieldDisplayFrame.dispose();
        fieldDisplayFrame = null; 
        fieldDisplay = null;
        if (targetDisplayFrame!=null) targetDisplayFrame.dispose();
        targetDisplayFrame = null; 
        targetDisplay = null;
        if (probDisplayFrame!=null) probDisplayFrame.dispose();
        probDisplayFrame = null; 
        probDisplay = null;
    }
}
