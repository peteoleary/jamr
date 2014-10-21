#!/bin/bash

# assumes this script (config.sh) lives in "${JAMR_HOME}/scripts/"

export JAMR_HOME="$( cd "$( dirname "${BASH_SOURCE[0]}" )/.." > /dev/null && pwd )"
export CLASSPATH=".:${JAMR_HOME}/target/scala-2.10/jamr-assembly-0.1-SNAPSHOT.jar"

# change the following enviroment variables for your configuration
export DATA_DIR="${JAMR_HOME}/data/LDC-2013-Sep"
export MODEL_DIR="${JAMR_HOME}/experiments/current"

export DATA_FILE_BASE="amr-release-proxy"
export JAMR_STAGES="train dev test"

export CDEC="${HOME}/tools/cdec"
export ILLINOIS_NER="${HOME}/tools/IllinoisNerExtended"
export ILLINOIS_NER_JAR="${ILLINOIS_NER}/target/IllinoisNerExtended-2.7.jar"
export WNHOME="${HOME}/tools/WordNet-3.0"
export SMATCH="${JAMR_HOME}/scripts/smatch_v1_0/smatch_modified.py"

