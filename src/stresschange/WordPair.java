/*
 Each word pair has its own class
 */

package stresschange;

/**
 * @author James
 */
public class WordPair {
    
    private String word;
    private Double initialNounProb;
    public Double currentNounProb;
    private Double initialVerbProb;
    public Double currentVerbProb;
    
    /*
    Later can add attributes like relative frequency, origin, etc.
    */
        
    public WordPair(String word, Double initialNounProb, Double initialVerbProb){
        this.word = word;
        this.initialNounProb = initialNounProb;
        this.currentNounProb = initialNounProb;
        this.initialVerbProb = initialVerbProb;
        this.currentVerbProb = initialVerbProb;
    }
    
    public void updateNounProb(double prob){
        this.currentNounProb = prob;
    }
    
    public void updateVerbProb(double prob){
        this.currentVerbProb = prob;
    }
    
    public String toString(){
        return this.word + ", noun prob = " + this.currentNounProb + ", verb prob = " + this.currentVerbProb;
    }

}
