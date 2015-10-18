/*
--
 */

package stresschange;
import sim.portrayal.continuous.*;
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
    
    public Display2D probDisplay;
    public JFrame probDisplayFrame;
    ContinuousPortrayal2D probPortrayal = new ContinuousPortrayal2D();
    
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
        fieldPortrayal.setPortrayalForAll(new OvalPortrayal2D(){ // colors change with probability of a specific noun or verb
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info){
                Speaker speaker = (Speaker)object;
                
                int probabilityShade = (int) (speaker.getWordProbability(StressChange.targetWord, "n") * 255);
                if (probabilityShade > 255) probabilityShade = 255;
                paint = new Color(probabilityShade, 0, 255 - probabilityShade);
                super.draw(object, graphics, info);
            }

        });
        
        fieldDisplay.reset();
        fieldDisplay.setBackdrop(Color.white);
        
        fieldDisplay.repaint();
        
        // visualization for probability space
        probPortrayal.setField(speakers.probSpace);
        probPortrayal.setPortrayalForAll(new OvalPortrayal2D());
        
        probDisplay.reset();
        probDisplay.setBackdrop(Color.white);
        
        probDisplay.repaint();
        
    }
        
    public void init(Controller c){
        super.init(c);
        
        // field to visualize space
        fieldDisplay = new Display2D(600,600,this);
        fieldDisplay.setClipping(false);
        
        fieldDisplayFrame = fieldDisplay.createFrame();
        fieldDisplayFrame.setTitle("Speaker Field");
        c.registerFrame(fieldDisplayFrame);
        fieldDisplayFrame.setVisible(true);
        fieldDisplay.attach(fieldPortrayal, "Field");
        
        // visualization for probability space
        probDisplay = new Display2D(600,600,this);
        probDisplay.setClipping(false);
        
        probDisplayFrame = probDisplay.createFrame();
        probDisplayFrame.setTitle("Stress Pattern");
        c.registerFrame(probDisplayFrame);
        probDisplayFrame.setVisible(true);
        probDisplay.attach(probPortrayal, "Stress");
    }    
    
    public void quit(){
        super.quit();
        if (fieldDisplayFrame!=null) fieldDisplayFrame.dispose();
        fieldDisplayFrame = null; 
        fieldDisplay = null;
    }
}
