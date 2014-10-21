#!/bin/bash -xe

# Preprocess the data

./cmd.any.train
./cmd.any.train.snt
./cmd.any.train.snt.tok
./cmd.any.train.tok

# Run the aligner
./cmd.any.train.aligned
# Remove opN
./cmd.any.train.aligned.no_opN
# Extract concept table
./cmd.any.train.aligned.concepts_no_opN

# Stanford Dependency Parser
./cmd.any.train.snt.tok.deps
# Tag with IllinoisNer
./cmd.any.train.snt.IllinoisNER

