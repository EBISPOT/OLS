#!/bin/bash

if [ -z "$1" ]; then exit 1; fi
branch=$1

docker build --build-arg ols_branch=${branch} -f OlsBranchDockerfile -t ols-${branch}-debian .
