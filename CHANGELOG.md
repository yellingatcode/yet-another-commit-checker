# YACC Changelog

### 1.9

* Support for Atlassian Bitbucket Server 4.0. Thanks [@bbaetz](https://github.com/bbaetz) for the help!
* New Feature: Branch Name Regex is now applied to branches created through Stash UI. Thanks [@wadahiro](https://github.com/wadahiro)!

### 1.8

* Fixed NullPointerException when pushing and YACC global settings have not been configured

### 1.7

* Fixed error when deleting tag that doesn't exist. Thanks [@bbaetz](https://github.com/bbaetz)!
* New Feature: Global YACC configuration so you don't need to enable YACC per-repository. Thanks [@jimbethancourt](https://github.com/jimbethancourt) and [@uldisa](https://github.com/uldisa)!

### 1.6

* Compatible with Stash Data Center

### 1.5 

* New setting 'Branch Name Regex' to enforce branch naming conventions when pushing.
* Error message header and footer is now customizable, existing ASCII art can be removed (or replaced with better ASCII art :-)
* Additional text can now be added to errors with clarification or to include info on how to fix.

### 1.4

Better Stash access key support!

* Author name and email checks will be automatically skipped if using Stash SSH access keys for system use. Thanks [@bbaetz](https://github.com/bbaetz)!
* New Feature: `Exclude Service User Commits` option to easily skip all commit checks for access key / service users. Thanks [@christiangalsterer](https://github.com/christiangalsterer)!
* Internal code changes: cleaned up pom.xml dependencies, set minimum supported Stash version to 2.12. Thanks [@bbaetz](https://github.com/bbaetz)!
* Fixed author name matching by removing special characters from name before checking name with
commit to better match git behavior. Thanks [@jlss](https://github.com/jlss)!

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
