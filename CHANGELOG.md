# YACC Changelog

### 1.3

* New Feature: `Exclude By Regex' option to exclude commits from regex and JIRA checks if message matches regex
* Fixed JQL matching when a result set contains >50 issues
* Require matching name/email check is now also applied to annotated tags. Thanks [@bbaetz](https://github.com/bbaetz)!
* User name check is now case-insensitive. Thanks [@bbaetz](https://github.com/bbaetz)!
* OAuth authentication error message on push now includes OAuth setup URL. Thanks [@bbaetz](https://github.com/bbaetz)!

### 1.2

* Stash 3.0 Support!
* Commit committer email check is now case insensitive. Thanks [@chadburrus](https://github.com/chadburrus)!

### 1.1

* Fixed broken committer name check when using Stash 2.12.
* Changed commit name check to use committer name instead of author name to support cherry picks, applying patches on behalf of someone else, etc.
* Added option to ignore issue-like items that don't match JIRA project keys to minimize false positives (like UTF-8). See new option "Ignore Unknown JIRA Project Keys".
* Added support to extract JIRA issues containing "_" in the project key.

Special thanks to [@landonf](https://github.com/landonf) and [@agusmba](https://github.com/agusmba) for their help on this release!

### 1.0

* Initial Release
