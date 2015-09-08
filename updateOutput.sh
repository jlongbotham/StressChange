
echo "--model 1: Mistransmission--"
java -jar dist/stressChange.jar deterministic some 5 5 > output/Model1/mistransmission-deterministic-5-5.output

java -jar dist/stressChange.jar deterministic some 1000 5 > output/Model1/mistransmission-deterministic-1000-5.output

java -jar dist/stressChange.jar deterministic some 5 1000 > output/Model1/mistransmission-deterministic-5-1000.output

java -jar dist/stressChange.jar deterministic some 1000 1000 > output/Model1/mistransmission-deterministic-1000-1000.output