# StressChange

This project uses the [MASON simulation toolkit](https://cs.gmu.edu/~eclab/projects/mason/) to implement 5 language change models for the stress shift in English N/V pairs. 

The 5 models come from (Sonderegger and Niyogi 2010) [*Combining data and mathematical models of language change*](http://www.aclweb.org/anthology/P/P10/P10-1104.pdf).

## Installation

This code was prepared and tested with Java 8.

No installation or compiling is required. The `dist` folder includes jar files for the main StressChange classes and the dependencies `mason.19.jar` and `commons-cli-1.3.1.jar`.

## Usage

You can run the code from the command line as follows:

`java -jar dist/stressChange.jar mistransmission deterministic some 5 5`

At the moment the command line arguments must be added in this exact order:

`java -jar dist/stressChange.jar [model name] [model type] [words to display in output] [noun frequency] [verb frequency]`

## Overview

### Stress in English N/V pairs

English has many word pairs where the noun and verb forms are identical except for syllable stress.

### Change over time

The stress patterns of these N/V pairs has been shown to change over time. (Sonderegger 2009) compiled entries for 149 N/V from British and American dictionaries from around 1500 to today. 

### Observed properties

1. *Unstable state* - No N/V pair is observed with a {2,1} stress pattern
2. *Stable states* - Stress patterns {1,1}, {1,2} and {2,2} are all observed in the historical data
3. *Observed stable variation* - States are observed where either the N or V varies over time, but not both
4. *Sudden change* - Change can happen from one stress pattern to another
5. *Observed changes* - There are four observed changes: {1,1} <-> {1,2} and {2,2} <-> {1,2}
6. *Observed frequency dependence* - Change to {1,2} corresponds to a decrease in the frequency of N
(Sonderegger and Niyogi 2010, p. 1023)

The dynamical systems models in (Sonderegger and Niyogi 2010) are evaluated based on whether they fulfill these observed properties.

## Baseline Models

The baseline models from (Sonderegger and Niyogi 2010) have specific characteristics:

1. Each generation is discrete
2. Speakers in G<sub>t</sub> learn from speakers in the parent generation G<sub>t-1</sub> 
3. Every speaker learns from every parent
4. Each generation has infinite speakers (this constraint could not be met in this code)
(Sonderegger and Niyogi 2010, p. 1022)


### 1. Mistransmission

```java
    public void mistransmission(WordPair word) { // Model 1
        // set the noun and verb probabilities for the next generation
        if (StressChange.logging.equals("all")) {
            System.out.println("BEGINNING OF MISTRANSMISSION: " + word);
        }
        if (StressChange.mode.equals("stochastic")) {
            // updated word.misNounPrev and word.misVerbPrev to ((number of randoms < misProbPQ) / word frequency)
            int numberMisheardNoun = 0;
            int numberMisheardVerb = 0;
            for (int i = 0; i < word.freqNoun; i++) {
                if (speakers.random.nextDouble() < StressChange.misProbP) {
                    numberMisheardNoun++;
                }
            }
            for (int i = 0; i < word.freqVerb; i++) {
                if (speakers.random.nextDouble() < StressChange.misProbQ) {
                    numberMisheardVerb++;
                }
            }
            word.misNounPrev = (double) numberMisheardNoun / word.freqNoun;
            word.misVerbPrev = (double) numberMisheardVerb / word.freqVerb;
        }
        // otherwise deterministically update based on initial mistransmission probabilities
        
        if (StressChange.logging.equals("all")) {
            System.out.println("AVG PARENT PROBABILITIES: noun = " + word.avgParentNounProb + ", verb = " + word.avgParentVerbProb);
        }        
        
        word.nextNounProb = getMisNoun(word.misNounPrev, word.avgParentNounProb); // update noun probabilities
        word.nextVerbProb = getMisVerb(word.misVerbPrev, word.avgParentVerbProb); // update verb probabilities

        if (StressChange.logging.equals("all")) {
            System.out.println("END OF MISTRANSMISSION: " + word);
        }
    }
```

### 2. Coupling by constraint

### 3. Coupling by constraint, with mistransmission

### 4. Coupling by priors

### 5. Coupling by priors, with mistransmission

## Additions

A few optional features have been added for simulations beyond what's included in (Sonderegger and Niyogi 2010):

1. *Stochasticity* - sample from parent probabilities rather than taking them directly
2. *Distance* - in the MASON 2D field, speakers only learn from parents within a specified distance
3. TBD
4. TBD

## Results

## References

Niyogi, P. (2006). *The computational nature of language learning and evolution*. Cambridge, MA:: MIT press.
Sonderegger, M. (2009). "Dynamical systems models of language variation and change: An application to an English stress shift." *Masters paper, Department of Computer Science, University of Chicago.*
(Sonderegger and Niyogi 2010) Sonderegger, M., & Niyogi, P. (2010, July). "Combining data and mathematical models of language change." In *Proceedings of the 48th Annual Meeting of the Association for Computational Linguistics* (pp. 1019-1029). Association for Computational Linguistics.
