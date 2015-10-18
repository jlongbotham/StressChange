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
    
    public Display2D display;
    public JFrame displayFrame;
    ContinuousPortrayal2D fieldPortrayal = new ContinuousPortrayal2D();
    
    public static void main(String[] args) throws IOException{
        StressChangeWithUI vid = new StressChangeWithUI();
        Console c = new Console(vid);
        c.setVisible(true);
        
        StressChange.initialStress = new ReadPairs(System.getProperty("user.dir") + "/src/initialStressSmoothed.txt").OpenFile(); // read in initial pairs
    }
    public StressChangeWithUI() { super(new StressChange(System.currentTimeMillis())); }
    public StressChangeWithUI(SimState state) { super(state); }
    public static String getName() { return "Stress Change Simulation"; }
    
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
        
        fieldPortrayal.setField(speakers.field);
        fieldPortrayal.setPortrayalForAll(new OvalPortrayal2D(){
            public void draw(Object object, Graphics2D graphics, DrawInfo2D info){
                Speaker speaker = (Speaker)object;
                
                int probabilityShade = (int) (speaker.getWordProbability("address", "n") * 255);
                if (probabilityShade > 255) probabilityShade = 255;
                paint = new Color(probabilityShade, 0, 255 - probabilityShade);
                super.draw(object, graphics, info);
            }

        });
        
        display.reset();
        display.setBackdrop(Color.white);
        
        display.repaint();
    }
        
    public void init(Controller c){
        super.init(c);
        display = new Display2D(600,600,this);
        display.setClipping(false);
        
        displayFrame = display.createFrame();
        displayFrame.setTitle("Speaker Field");
        c.registerFrame(displayFrame);
        displayFrame.setVisible(true);
        display.attach(fieldPortrayal, "Field");
    }    
    
    public void quit(){
        super.quit();
        if (displayFrame!=null) displayFrame.dispose();
        displayFrame = null; 
        display = null;
    }
}
