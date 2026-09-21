#!/bin/sh
# ShopSphere Pro Maven Wrapper for Unix / Linux / Docker

if [ "$1" = "test" ]; then
    mvn test-compile -q
    java -Xmx256m -Xms64m -cp "target/classes:target/test-classes" com.shopsphere.ChallengeTestRunner
    exit $?
fi

mvn "$@"
