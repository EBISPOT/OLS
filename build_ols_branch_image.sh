#!/bin/bash

if [ -z "$1" ]; then exit 1; fi
branch=$1
commit=$2
IFS=":${IFS}"

echo "ols-${*}"

#  Might want to try _not_ passing the second parameter 'commit' if it
#  is not actually supplied. It seems to break the build if set empty,
#  whereas if not handed on at all, the associated dockerfile should
#  just default to 'HEAD'.

DOCKER_BUILDKIT=1
docker build --build-arg ols_branch=${branch} \
             --build-arg ols_commit=${commit} \
             -f OlsBranchDockerfile \
             -t "ols-branch-${*}" .
