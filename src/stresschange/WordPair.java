/*
 Each word pair has its own class
 */

package stresschange;

/**
 * @author James
 */
public class WordPair {
    
    public String word;
    public String prefix;
    private Double initialNounProb;
    public Double currentNounProb;
    public Double nextNounProb;
    public Double avgParentNounProb;
    private Double initialVerbProb;
    public Double currentVerbProb;
    public Double nextVerbProb;
    public Double avgParentVerbProb;
    public Double misNounPrev;
    public Double misVerbPrev;
    public Integer freqNoun;
    public Integer freqVerb;
    
    /*
    Later can add attributes like relative frequency, origin, etc.
    */
        
    public WordPair(String word, Double initialNounProb, Double initialVerbProb, String prefix){
        this.word = word;
        this.prefix = prefix;
        this.initialNounProb = initialNounProb;
        this.currentNounProb = initialNounProb;
        this.nextNounProb = initialNounProb;
        this.initialVerbProb = initialVerbProb;
        this.currentVerbProb = initialVerbProb;
        this.nextVerbProb = initialVerbProb;
        this.misNounPrev = StressChange.misProbP;
        this.misVerbPrev = StressChange.misProbQ;
        this.freqNoun = StressChange.freqNoun;
        this.freqVerb = StressChange.freqVerb;
    }
    
    public String toString(){
        return this.word + ": noun prob = " + this.nextNounProb + ", verb prob = " + this.nextVerbProb;
    }

}
