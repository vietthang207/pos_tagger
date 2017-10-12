# Overview
This is a part of speech tagger build on Java

# Usage:
Compile : javac Main.java

Copy the data files to the same directory with the Main class in order to train

Command to train: java Main build_tagger <training_file> <dev_file> <model_file>

For example: java build_tagger sents.train sents.devt model_file

Command to run test file: java Main run_tagger <test_file> <model_file> <output_file>

For example: java run_tagger sents.test model_file sents.out

