#! /bin/bash

# Fail if something fails
set -e

# Compute next release version (date)
export BUILD_NUMBER_DATE
BUILD_NUMBER_DATE=9.0.$(date -u +%Y%m%d)
echo BUILD_NUMBER_DATE=$BUILD_NUMBER_DATE

# Set the new version on Maven modules
mvn versions:set -DnewVersion=$BUILD_NUMBER_DATE -DgenerateBackupPoms=false

# Also set the version of the licensing module
mvn -f build/pom.xml versions:set -DnewVersion=$BUILD_NUMBER_DATE -DgenerateBackupPoms=false

# Make a release commit
git commit -am "Release version $BUILD_NUMBER_DATE"

# Push the release commit to GitHub
git push origin 9.0

# Find the git SHA of the commit
export RELEASE_COMMIT=$(git rev-parse HEAD)

echo "Release commit is $RELEASE_COMMIT"

# Export to TeamCity environment variable
echo "##teamcity[setParameter name='env.RELEASE_COMMIT' value='$RELEASE_COMMIT']"
