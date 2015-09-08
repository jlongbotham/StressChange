/*
 Each word pair has its own class
 */

package stresschange;

/**
 * @author James
 */
public class WordPair {
    
    public String word;
    private Double initialNounProb;
    public Double currentNounProb;
    public Double avgParentNounProb;
    private Double initialVerbProb;
    public Double currentVerbProb;
    public Double avgParentVerbProb;
    public Double misNounPrev;
    public Double misVerbPrev;
    public Integer freqNoun;
    public Integer freqVerb;
    
    /*
    Later can add attributes like relative frequency, origin, etc.
    */
        
    public WordPair(String word, Double initialNounProb, Double initialVerbProb){
        this.word = word;
        this.initialNounProb = initialNounProb;
        this.currentNounProb = initialNounProb;
        this.initialVerbProb = initialVerbProb;
        this.currentVerbProb = initialVerbProb;
        this.misNounPrev = StressChange.misProbP;
        this.misVerbPrev = StressChange.misProbQ;
        this.freqNoun = StressChange.freqNoun;
        this.freqVerb = StressChange.freqVerb;
    }
    
    public String toString(){
        return this.word + ": noun prob = " + this.currentNounProb + ", verb prob = " + this.currentVerbProb;
    }

}
