# hmpps-one-plan-api
[![repo standards badge](https://img.shields.io/badge/dynamic/json?color=blue&style=flat&logo=github&label=MoJ%20Compliant&query=%24.result&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-one-plan-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-github-repositories.html#hmpps-one-plan-api "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-one-plan-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-one-plan-api)
[![Docker Repository on Quay](https://quay.io/repository/hmpps/hmpps-one-plan-api/status "Docker Repository on Quay")](https://quay.io/repository/hmpps/hmpps-one-plan-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://hmpps-one-plan-api-dev.hmpps.service.justice.gov.uk/webjars/swagger-ui/index.html?configUrl=/v3/api-docs)

# About

`One Plan` stores a person's Plan and Objective data

# Running locally
## From the shell
* Start the database with `docker compose up -d`
* Set environment variables
```shell
  export DATABASE_ENDPOINT=localhost:5432
  export DATABASE_{NAME,USERNAME,PASSWORD}=one-plan
```
* start with `gradle bootrun`

# pre-commit
To save you from the linter failing your build due to formatting, you can enable pre commit git hooks

* `brew install pre-commit`
* `pre-commit install`

See `.pre-commit-config.yaml` for more information about what will run
