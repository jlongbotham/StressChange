# StressChange

This project uses the [MASON simulation toolkit](https://cs.gmu.edu/~eclab/projects/mason/) to implement 5 language change models for the stress shift in English N/V pairs. 

The 5 models come from (Sonderegger and Niyogi 2010) [*Combining data and mathematical models of language change*](http://www.aclweb.org/anthology/P/P10/P10-1104.pdf).

## Installation

This code was prepared and tested with Java 8.

No installation or compiling is required. The `dist` folder includes jar files for the main StressChange classes and the dependencies `mason.19.jar` and `commons-cli-1.3.1.jar`.

## Usage

You can run the code from the command line as follows:

`java -jar dist/stressChange.jar -model mistransmission -mode deterministic -logging some -nounFreq 1000 -verbFreq 1000`

The options are the following:

```
java -jar dist/stressChange.jar -model    [ mitransmission | constraint | constraintWithMistransmission | prior | priorWithMistransmission ] 
                                -mode     [ deterministic | stochastic ] 
                                -logging  [ some | all ] 
                                -nounFreq [ 1000 ] 
                                -verbFreq [ 1000 ]
```

If no option is given, the first listed is the default.

## Overview

### Stress in English N/V pairs

English has many word pairs where the noun and verb forms are identical except for syllable stress.

### Change over time

The stress patterns of these N/V pairs has been shown to change over time. (Sonderegger 2009) compiled entries for 149 N/V from British and American dictionaries from around 1500 to today. 

### Observed properties

Based on the diachronic observations in the N/V pair data, (Sonderegger and Niyogi 2010) derive 6 properties (or "observed dynamics") of the stress pattern language change:

1. *Unstable state* - No N/V pair is observed with a {2,1} stress pattern
2. *Stable states* - Stress patterns {1,1}, {1,2} and {2,2} are all observed in the historical data
3. *Observed stable variation* - States are observed where either the N or V varies over time, but not both
4. *Sudden change* - Change can happen from one stress pattern to another
5. *Observed changes* - There are four observed changes: {1,1} &harr; {1,2} and {2,2} &harr; {1,2}
6. *Observed frequency dependence* - Change to {1,2} corresponds to a decrease in the frequency of N

	(Sonderegger and Niyogi 2010, p. 1023)

	The dynamical systems models in (Sonderegger and Niyogi 2010) are evaluated based on whether they fulfill these observed properties.

	(Sonderegger 2009) made the additional observation that word pairs with the same prefix (e.g. *re-* or *de-*) have similar trajectories. And in our analysis of the data, we observed some examples of variation between stress patterns in US and UK dictionaries, for example with the noun form of *address*:

	![Dialectical divergence for "address"](address-n.png)

	In this figure, a y-value of 2 means only secondary stress, 1.75 means mostly secondary stress and 1.25 means mostly primary stress. You can see that US dictionaries show a change toward primary stress for *address* starting around 1935, whereas UK dictionaries continue to show only secondary stress.

	For this reason, we've added two observed properties:

7. *Analogical change* - N/V pairs with same prefix tend to have the same stress pattern
8. *Dialectical divergence* - N/V pair trajectories can diverge between distant groups of speakers

## Baseline Models

The baseline models from (Sonderegger and Niyogi 2010) have specific characteristics:

1. Each generation is discrete
2. Speakers in G<sub>t</sub> learn from speakers in the parent generation G<sub>t-1</sub> 
3. Every speaker learns from every parent
4. Each generation has infinite speakers (this constraint could not be met in this code)

(Sonderegger and Niyogi 2010, p. 1022)

### 1. Mistransmission

The first model is based on the assumption that language change often occurs in the "handover" between generations due to mistransmission. That is, speakers sometimes mishear what the parent generation says, influencing the language that they end up speaking.

In the context of the N/V stress patterns for these verbs, there is a clear bias toward a {1,2} stress pattern, known as Ross' generalization. One explanation for this bias is that generally in English stressed and unstressed syllables appear alternately in a sentence. As nouns often follow an unstressed article (a "trochaic-biasing" context), they tend to have primary stress. Because of this tendency, (Sonderegger and Niyogi 2010) assume that mistransmission can occur in only one direction ({1,1}, {2,2} &rarr; {1,2}). 

Using the following definitions:

* &alpha; = generation average probability of pronouncing N as second stress
* &beta; = generation average probability of pronouncing V as second stress
* *p* = mistransmission probability for N
* *q* = mistransmission probability for V

The evolution equation for Model 1 is then:

*&alpha;<sub>t</sub> = &alpha;<sub>t-1</sub> (1 - p)*

*&beta;<sub>t</sub> = &beta;<sub>t-1</sub> + (1 - &beta;<sub>t-1</sub>) q*

The assymetry in the evolution equation ensures that &alpha; tends toward 0, that is decreases the probability of a second-stress N, and &beta; then tends toward 1, that is increases the probability of a second-stress V.

```java
    public double getMisNoun(double p, double alphaPrev) {
        double alpha = alphaPrev * (1 - p);
        return alpha;
    }

    public double getMisVerb(double q, double betaPrev) {
        double beta = betaPrev + ((1 - betaPrev) * q);
        return beta;
    }

    public void mistransmission(WordPair word) { // Model 1
        // set the noun and verb probabilities for the next generation
        
        word.nextNounProb = getMisNoun(word.misNounPrev, word.avgParentNounProb); // update noun probabilities
        word.nextVerbProb = getMisVerb(word.misVerbPrev, word.avgParentVerbProb); // update verb probabilities
    }
```

### 2. Coupling by constraint

### 3. Coupling by constraint, with mistransmission

### 4. Coupling by priors

### 5. Coupling by priors, with mistransmission

## Additions

A few optional features have been added for simulations beyond what's included in (Sonderegger and Niyogi 2010):

1. *Stochasticity* - Sample from parent probabilities rather than taking them directly
2. *Distance* - In the MASON 2D field, speakers only learn from parents within a specified distance
3. *Prefixes* - In models 4 and 5, the prior probabilities are derived from the prefix class of a pair, if applicable

### 1. Stochasticity

### 2. Distance

### 3. Prefixes

## Results

## References

Niyogi, P. (2006). *The computational nature of language learning and evolution*. Cambridge, MA:: MIT press.

Sonderegger, M. (2009). "Dynamical systems models of language variation and change: An application to an English stress shift." *Masters paper, Department of Computer Science, University of Chicago.*

Sonderegger, M., & Niyogi, P. (2010, July). "Combining data and mathematical models of language change." In *Proceedings of the 48th Annual Meeting of the Association for Computational Linguistics* (pp. 1019-1029). Association for Computational Linguistics.
