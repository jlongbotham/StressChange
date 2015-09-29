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

## Baseline Models

The baseline models from (Sonderegger and Niyogi 2010) have specific characteristics:

1. Infinite speakers
2. Everyone speaks to everyone
3. ...

## Additions

A few optional features have been added for simulations beyond what's included in (Sonderegger and Niyogi 2010):

1. Model type "stochastic"
* Samples from probability
* Get parent averages from parents withing a specific distance
2. 

## References


