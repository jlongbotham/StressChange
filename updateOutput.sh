
echo "--model 1: Mistransmission--"
java -jar dist/stressChange.jar mistransmission deterministic some 5 5 > output/Model1/mistransmission-deterministic-5-5.output

java -jar dist/stressChange.jar mistransmission deterministic some 1000 5 > output/Model1/mistransmission-deterministic-1000-5.output

java -jar dist/stressChange.jar mistransmission deterministic some 5 1000 > output/Model1/mistransmission-deterministic-5-1000.output

java -jar dist/stressChange.jar mistransmission deterministic some 1000 1000 > output/Model1/mistransmission-deterministic-1000-1000.output

echo "--model 2: Constraint--"
java -jar dist/stressChange.jar constraint deterministic some 5 5 > output/Model2/constraint-deterministic-5-5.output

echo "--model 3: Constraint with mistransmission--"
java -jar dist/stressChange.jar constraintWithMistransmission deterministic some 5 5 > output/Model3/constraint-mis-deterministic-5-5.output

echo "--model 4: Prior--"
java -jar dist/stressChange.jar prior deterministic some 5 5 > output/Model4/prior-deterministic-5-5.output

echo "--model 5: Prior with mistransmission--"
java -jar dist/stressChange.jar priorWithMistransmission deterministic some 5 5 > output/Model5/prior-mis-deterministic-5-5.output