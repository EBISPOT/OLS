#!/bin/bash

[ -z "$1" ] && exit 1
branch=$1
commit=$2

pop_and_join () { local IFS="_"; echo "${*}"; }
named_volume="`pop_and_join ${branch} ${commit}`"

echo named volume prefix is "ols_${named_volume}"

docker volume ls | tail -n+2 | awk '{ print $2 }' | \
       grep -E "^ols_${named_volume}_.+$" | \
       xargs docker volume rm
