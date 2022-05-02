# Version Control Concept

## New Bug, Feature, Request
- Create issue with a title describing the problem
- Add comment with further Explanation
- Add `difficulty` and `importance` labels
- (Add other labels, like `Bug` or `Feature`)
- (Add Milestone, like `UI` or `Time-related`)

## New big Project
- Create Project with `ToDo`, `Next`, `In progress`, `Done` columns
- Create Milestone (Due date)
- Create Issues like mentioned in **New Bug, Feature, Request**
- Add Project to Issues
- move Issues into `ToDo` or `Next` column

## Implementing a new Feature / Fixing a Bug
- Create Branch from Issue from `next` branch with pattern `{id}-name` in GitHub
- Link Branch to Issue
- Mention Issue with in every commit `#{id} Commit Description`
- Create Pull request from Branch into next with name equal to issue's name and add `Fixes #{id}` to close issue on merge [IDE]  (GitHub breaks link to branch)
- Link Pull request to issue
- Merge into `next` with top comment as Merge message [IDE] 
- Link `next` Pull request to issue

## New feature (Merge into main branch)
- check build status on `next` branch (no broken allowed in `main`)
- edit top comment containing summary of most relevant added features
- Merge into `main` [IDE]
- close fished Projects
- add new `Next` Pull request with empty top comment

## New Releases (Merge into release branch)
#### TODO naming version numbers, etc.
- check build status on `main` branch (no broken allowed in `release`)
- add release commit, setting logging to release, deactivating error popups, etc.
- rebase `release` branch with commit
