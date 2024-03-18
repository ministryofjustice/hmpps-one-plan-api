#!/usr/bin/env bash

# Read .env file for auth info
set -o allexport
source .env set
set +o allexport

BASE_URL=https://one-plan-api-dev.hmpps.service.justice.gov.uk

function run() {
    K6_WEB_DASHBOARD=true K6_WEB_DASHBOARD_EXPORT="./$1.html" k6 run\
     -e BASE_URL=$BASE_URL -e USERNAME=$USERNAME -e PASSWORD=$PASSWORD "$1.js"
}

run get-objectives
run create-and-update-objectives
