/*
 Each word pair has its own class
 */

package stresschange;

/**
 * @author James
 */
public class WordPair {
    
    public String word; // the word itself
    public String prefix; // the prefix class
    private Double initialNounProb; // the initial probability from source file
    public Double currentNounProb; // the current probability
    public Double nextNounProb; // the next generation's probability
    public Double avgParentNounProb; // the parent generation's probability
    private Double initialVerbProb; 
    public Double currentVerbProb;
    public Double nextVerbProb;
    public Double avgParentVerbProb;
    public Double misNounPrev; // the probability of mistransmitting the noun
    public Double misVerbPrev; // the probability of mistransmitting the verb
    public Integer freqNoun; // the frequency of the noun
    public Integer freqVerb; // the frequency of the verb
    
    // prior probabilties used in models 4 and 5 for individual prefix classes
    public Double lambda11 = 0.0;
    public Double lambda12 = 0.0;
    public Double lambda21 = 0.0; // this one should always be 0.0
    public Double lambda22 = 0.0;
    public Integer prefixClassSize = 0;
        
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
