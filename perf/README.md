# Running Perf tests

## Setup
* Install the k6 perf test tool
```shell
brew install k6
```
* create a `.env` file in this (the perf directory) with
```shell
USERNAME=dev username
PASSWORD=dev password
```
User will need the the `ONE_PLAN_EDIT` role assigned.
## Running

```shell
./run-dev.sh
```
Output HTML files will be created in the current directory.

## Cleanup
The tests create a few thousand records in the dev db,
you can use the `cleanup.sql` file in this directory to tidy up.
