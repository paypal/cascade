#!/bin/bash

if [ -z "$1" ]; then
    echo "you must set a branch from which to delete"
    exit
fi

if [ -z "$2" ]; then
    echo "you must set an author"
fi

if [ -z "$3" ]; then
    echo "you must set a changelog entry"
fi

RELEASE_BRANCH=$1
echo "preparing to release $RELEASE_BRANCH"

git branch -u origin/$RELEASE_BRANCH
git pull
git config branch.$RELEASE_BRANCH.remote origin
git config branch.$RELEASE_BRANCH.merge refs/heads/$RELEASE_BRANCH

sbt -Dchangelog.author="$2" -Dchangelog.msg="$3" "release cross with-defaults"
