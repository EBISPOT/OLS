#!/bin/bash

set -e

if [ "$1" == "tomcat" ]
then
    export superconf=/etc/supervisord_with_tomcat.conf
else
    export superconf=/etc/supervisord.conf
fi

supervisord -c ${superconf}
