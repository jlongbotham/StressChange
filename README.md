# StressChange

This project uses the [MASON simulation toolkit](https://cs.gmu.edu/~eclab/projects/mason/) to implement 5 language change models for the stress shift in English N/V pairs. 

The 5 models come from Sonderegger and Niyogi (2010) [*Combining data and mathematical models of language change*](http://www.aclweb.org/anthology/P/P10/P10-1104.pdf).

## Installation

This code was prepared and tested with Java 8.

No installation or compiling is required. The `dist` folder includes jar files for the main StressChange classes and the dependencies `mason.19.jar`, `commons-cli-1.3.1.jar` (for command line arguments) and `jmf.jar` (for recording Quicktime movies).

## Usage

### With GUI

You can run the code from the command line as follows:

`java -jar dist/stressChangeUI.jar`

Options for different parameters are available in the UI.

### From command line

You can run the code from the command line as follows:

`java -jar dist/stressChangeCL.jar [options]`

The options for different parameters are as follows:

`-help`
Show options in command line

`-model [ mistransmission | constraint | constraintWithMistransmission | prior | priorWithMistransmission ]`

Model to be used (see below for descriptions of each)

`-distModel [ none | random | absolute | probabilistic | lattice | grouped ]`

Distance model used for determining which parents transmit to which speakers (see below for description of each)

`-numSpeakers [ integer between 10 and 1000 ]`

The number of speakers 

`-stochastic`

Mistransmission between generations done by sampling from a probability of the parent generation rather that taking the probability directly (only affects models that use mistransmission)

`-logging [ none | some | all | tabular | troubleshooting ]`

none = no logging (default) 

some = shows general information about the 6 representative words and the 1 target word

all = shows general information about all words

tabular = shows general information about all words in comma-delimited tabular format for analysis in e.g. R

troubleshooting = shows in-depth information for all words 

`-targetWord [ string ]`

Show logging information for the given word

`-freqNoun [ integer between 1 and 1000 ]`
`-freqVerb [ integer between 1 and 1000 ]`

Set the noun and verb frequency of the target word

`-misProbNoun [ double between 0.0 and 1.0 ]`
`-misProbVerb [ double between 0.0 and 1.0 ]`

Set the mistransmission probabilities for nouns and verbs (only affects models that use mistransmission)

`-prior11General [ double between 0.0 and 1.0 ]`
`-prior22General [ double between 0.0 and 1.0 ]`

Set the prior probabilities for the {1,1} and {2,2} stress patterns generally (only affects models that use priors)

`-priorClass`

Determines whether words with the same prefix share the same prior across their prefix class (only affects models that use priors)

`-prior11Target [ double between 0.0 and 1.0 ]`
`-prior22Target [ double between 0.0 and 1.0 ]`

Set the prior probabilities for the {1,1} and {2,2} stress patterns for the prefix class of the target word (only affects models that use `-priorClass` and have a `-targetWord`)


## Overview

### Stress in English N/V pairs

English has many word pairs where the noun and verb forms are identical except for syllable stress, for example *per*mit (n) vs. per*mit* (v).

### Change over time

The stress patterns of these N/V pairs has been shown to change over time. Sonderegger (2009) compiled entries for 149 N/V pairs from British and American dictionaries from around 1500 to today. 

### Observed properties

Based on the diachronic observations in the N/V pair data, Sonderegger and Niyogi (2010) derive 6 properties (or "observed dynamics") of the stress pattern language change:

1. *Unstable state* - No N/V pair is observed with a {2,1} stress pattern
2. *Stable states* - Stress patterns {1,1}, {1,2} and {2,2} are all observed in the historical data
3. *Observed stable variation* - States are observed where either the N or V varies over time, but not both
4. *Sudden change* - Change can happen from one stress pattern to another
5. *Observed changes* - There are four observed changes: {1,1} &harr; {1,2} and {2,2} &harr; {1,2}
6. *Observed frequency dependence* - Change to {1,2} corresponds to a decrease in the frequency of N

(Sonderegger and Niyogi 2010, p. 1023)

The dynamical systems models in Sonderegger and Niyogi (2010) are evaluated based on whether they fulfill these observed properties.

Sonderegger (2009) made the additional observation that word pairs with the same prefix (e.g. *re-* or *de-*) have similar trajectories. And in our analysis of the data, we observed some examples of variation between stress patterns in US and UK dictionaries, for example with the noun form of *address*:

![Dialectical divergence for "address"](address-n.png)

In this figure, a y-value of 2 means only secondary stress, 1.75 means mostly secondary stress and 1.25 means mostly primary stress. You can see that US dictionaries show a change toward primary stress for *address* starting around 1935, whereas UK dictionaries continue to show only secondary stress.

For this reason, we've added two observed properties:

7. *Analogical change* - N/V pairs with same prefix tend to have the same stress pattern
8. *Dialectical divergence* - N/V pair trajectories can diverge between distant groups of speakers

## Baseline Models

The baseline models from Sonderegger and Niyogi (2010) have specific characteristics:

1. Each generation is discrete
2. Speakers in G<sub>t</sub> learn from speakers in the parent generation G<sub>t-1</sub> 
3. Every speaker learns from every parent
4. Each generation has infinite speakers (this constraint could not be met in this code)

(Sonderegger and Niyogi 2010, p. 1022)

### 1. Mistransmission

The first model is based on the assumption that language change often occurs in the "handover" between generations due to mistransmission. That is, speakers sometimes mishear what the parent generation says, influencing the language that they end up speaking.

In the context of the N/V stress patterns for these verbs, there is a clear bias toward a {1,2} stress pattern, known as Ross' generalization. One explanation for this bias is that generally in English stressed and unstressed syllables appear alternately in a sentence. As nouns often follow an unstressed article (a "trochaic-biasing" context), they tend to have primary stress. Because of this tendency, Sonderegger and Niyogi (2010) assume that mistransmission can occur in only one direction ({1,1}, {2,2} &rarr; {1,2}). 

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

All N/V pairs converge to the {1,2} stress pattern in this model.

### 2. Coupling by constraint

The coupling by constraint model takes a different approach and assumes a simple *constraint* based on Ross' Generalization that the probability of a second-stress N must be less than the probability of a second-stress V for a specific word pair.

```java
    public void constraint(WordPair word) { // Model 2
        // updates noun and verb probabilities based on constraint only

        if (word.avgParentNounProb < word.avgParentVerbProb) { // if constraint is met, then estimate equals expectation
            word.nextNounProb = word.avgParentNounProb;
            word.nextVerbProb = word.avgParentVerbProb;
        } else {
            word.nextNounProb = (word.avgParentNounProb + word.avgParentVerbProb) / 2; // if constraint is not met, then estimate equals average of expectations
            word.nextVerbProb = (word.avgParentNounProb + word.avgParentVerbProb) / 2;
        }
    }
```

All N/V pairs with stress patterns satisfy this constraint do not show any change, while any patterns where the N probability of second-stress is higher than the V probability converge until the constraint is met, usually when the N and V probabilities of second-stress are equal.

### 3. Coupling by constraint, with mistransmission

This model combines the coupling by constraint but includes mistransmission for the "heard" examples between generations.

```java
    public void constraintWithMistransmission(WordPair word) { // Model 3
        // the same as constraint(), but on "heard" examples (i.e. mistransmission)
        mistransmission(word);
        constraint(word);
    }
```

With this model, the mistransmission updates guarantee that all N/V pairs converge to a {1,2} stress pattern.

### 4. Coupling by priors

Using prior probabilities as well as observed likelihoods allows information about the lexicon in general to affect how a specific word pair's stress pattern changes over time. As with a Bayesian approach, the prior represents knowledge of stress patterns in general (e.g. that {2,1} never occurs), while the observed likelihood is based on what speakers hear from the previous generation.

```java
    public void prior(WordPair word) { // Model 4

        // calculate learned probabilities (P) based on word frequencies sampled from parent probabilities
        double kNoun = 0.0; // number of nouns heard as final stress
        double kVerb = 0.0; // number of verbs heard as final stress
                
        for (int i = 0; i < word.freqNoun; i++) {
            if (speakers.random.nextDouble() <= word.nextNounProb) {
                kNoun++;
            }
        }

        for (int i = 0; i < word.freqVerb; i++) {
            if (speakers.random.nextDouble() <= word.nextVerbProb) {
                kVerb++;
            }
        }

        double p11 = ((word.freqNoun - kNoun) / word.freqNoun) * ((word.freqVerb - kVerb) / word.freqVerb);
        double p12 = ((word.freqNoun - kNoun) / word.freqNoun) * (kVerb / word.freqVerb);
        double p21 = (kNoun / word.freqNoun) * ((word.freqVerb - kVerb) / word.freqVerb);
        double p22 = (kNoun / word.freqNoun) * (kVerb / word.freqVerb);

        // Set fixed lambda values, must sum to 1
        double lambda11 = 0.2;
        double lambda12 = 0.4;
        double lambda21 = 0.0; // this one should always be 0.0
        double lambda22 = 0.4;
        
        // update current noun and verb probabilities based on learned and prior probabilities
        word.nextNounProb = ((lambda21 * p21) + (lambda22 * p22)) / ((lambda11 * p11) + (lambda12 * p12) + (lambda21 * p21) + (lambda22 * p22));
        word.nextVerbProb = ((lambda12 * p12) + (lambda22 * p22)) / ((lambda11 * p11) + (lambda12 * p12) + (lambda21 * p21) + (lambda22 * p22));
    }
```

In this model, the stress patterns that word pairs converge to depends mostly on the lambda (prior) probabilities. However, likelihoods of 0.0 probability will of course rule out specific stress patterns, just as the lambda probability of 0.0 for the stress pattern {2,1} guarantees that it will never occur.

### 5. Coupling by priors, with mistransmission

This model includes the prior probabilities of Mode 4, but applies the updates between generations to the "mistransmitted" examples.

```java
    public void priorWithMistransmission(WordPair word) {
        // the same as prior(), but on "heard" examples (i.e. mistransmission)
        mistransmission(word);
        prior(word);
    }
```

## Additions

A few optional features have been added for simulations beyond what's included in Sonderegger and Niyogi (2010):

1. *Stochasticity* - Sample from parent probabilities rather than taking them directly
2. *Distance* - In the MASON 2D field, speakers only learn from parents within a specified distance
3. *Prefixes* - In models 4 and 5, the prior probabilities are derived from the prefix class of a pair, if applicable

### 1. Stochasticity

The deterministic models use exact probabilities between generations (default setting), while stochastic models sample from a probability distribution, allowing for a small amount of randomness.

### 2. Distance

#### None

With this default setting, no special restrictions are placed on distance, i.e. everyone speaks to everyone.

#### Random

With the random setting, each speaker speaks to half the total number of parents, chosen randomly.

#### Absolute

With the absolute setting, each speaker speaks to parents located within a specific maximum distance.

#### Probabilistic

With the probabilistic setting, whether a speaker speaks to a parent is decided by a sampling a probability based on the distance, i.e. the farther away a parent is, the less like a speaker will speak to them.

#### Lattice

With the lattive setting, speakers are initially placed in a fixed grid pattern and only speak to their immediate neighbors.

#### Grouped

With the grouped setting, speakers are initially placed into two separate groups, allowing diverting trajectories over time.

### 3. Prefixes

Using prefix classes allows having prior probabilities set per prefix class, rather than for the lexicon as a whole. This allows for representing similarities at a more fine-grained level, consistent with the observed property that word pairs with the same prefix tend to have similar stress patterns. With this setting, the prior probabilities (lambdas) are caculated based on the initial stress patterns *per prefix class*. For example, word pairs with the prefix *out* have prior probabilities of {1,2} = 0.9 and {2,2} = 0.1, while pairs with the prefix *pre* have {1,1} = 0.2, {1,2} = 0.4 and {2,2} = 0.4. If a word pair does not have a specific prefix (e.g. *affect*), the prior probabilities are caculated from the lexicon as a whole, that is {1,1} = 0.09, {1,2} = 0.43 and {2,2} = 0.48.

Without this option, all word pairs have the same prior probabilities, which can be set in the code.

## Results

### Agent-based models vs. dynamical systems

As agent-based models simulate individuals and their characteristics, they have some more flexibility in terms of what *can* be simulated. 

### Analogy and dialects

The model using priors with mistransmission as described by Sonderegger and Niyogi (2010) fulfills the six properties of stress pattern change that they identified, but it doesn't address the two additional properties identified here: analogical change and dialectical divergence. In this project, we didn't add a *model* per se, but additional features that can be added to the prior with mistransmission model.

The option "Prefix class prior" initiates separate prior probabilities per prefix class based on their initial stress patterns observed in the corpus. For example, the word *subject* is part of the *sub-* class of prefixes and has prior probabilities of {1,1} = 3.3, {2,1} = 3.3 and {2,2} = 3.3, while *rebate* belongs to the *re-* class of prefixes and has prior probabilities of {1,2} = 0.2, {2,1} = 0.0 and {2,2} = 0.8. By tying a word's prior to its prefix class, similar trajectories can be seen for words with the same prefix, fulfilling the observation of analogical change.

The distance model "grouped" initiates two distinct groups of speakers connected by a few "super speakers". This distance model combined with the option "Stochastic" often shows different *rates* of change, such as this instance for the word *address (n)*:

![Simulated dialectical divergence for "address"](divergence.png)

The group in the blue box has an probability of secondary stress for *address (n)* of 0.56, while the group in the red box has an average of 0.75, with each speaker having an individual probability within 0.03 points of its group average.

## References

Niyogi, P. (2006). *The computational nature of language learning and evolution*. Cambridge, MA:: MIT press.

Sonderegger, M. (2009). "Dynamical systems models of language variation and change: An application to an English stress shift." *Masters paper, Department of Computer Science, University of Chicago.*

Sonderegger, M., & Niyogi, P. (2010, July). "Combining data and mathematical models of language change." In *Proceedings of the 48th Annual Meeting of the Association for Computational Linguistics* (pp. 1019-1029). Association for Computational Linguistics.
