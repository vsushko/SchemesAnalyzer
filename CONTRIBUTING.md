
Contributing to SchemesAnalyzer
=============================

SchemesAnalyzer is a free and open tool for analyzing XSD schema sets. I'd love to receive contributions from the comunity and - you! There are many ways to contribute, from writing wiki posts, improving the documentation, submitting bug reports and feature requests or writing code which can be incorporated into SchemesAnalyzer itself.

Bug reports
-----------
If you think you have found a bug in SchemesAnalyzer, first make sure that you are testing against the latest version of SchemesAnalyzer - your issue may already have been fixed. If not, search the issues list on GitHub in case a similar issue has already been opened.

It is very helpful if you can prepare a reproduction of the bug. In other words, provide a small test case which I can run to confirm your bug. It makes it easier to find the problem and to fix it. Test cases should be provided as a number of steps to reproduce the bug.

Provide as much information as you can. The easier it is for me to recreate your problem, the faster it is likely to be fixed.

Feature requests
----------------
If you find yourself wishing for a feature that doesn't exist in SchemesAnalyzer, you are probably not alone. There are bound to be others out there with similar needs. Some of the features that SchemesAnalyzer has today have been added because users saw the need. Open an issue on [issues list](https://github.com/vsushko/SchemesAnalyzer/issues) on GitHub which describes the feature you would like to see, why you need it, and how it should work.

Contributing code and documentation changes
-------------------------------------------
If you would like to contribute a new feature or a bug fix to SchemesAnalyzer, please discuss your idea first on the GitHub issue. If there is no GitHub issue for your idea, please open one. It may be that somebody is already working on it, or that there are particular complexities that you should know about before starting the implementation. There are often a number of ways to fix a problem and it is important to find the right approach before spending time on a PR that cannot be merged.

### Fork and clone the repository
You will need to fork the main c code and clone it to your local machine. See
[github help page](https://help.github.com/articles/fork-a-repo) for help.

Further instructions for specific projects are given below.

### Tips for code changes
Following these tips prior to raising a pull request will speed up the review
cycle.

* Add appropriate unit tests
* Add integration tests, if applicable
* Make sure the code you add follows the (at least Intellij Idea) formatting guidelines
* Lines that are not part of your change should not be edited (e.g. don't format
  unchanged lines, don't reorder existing imports)
* Add the appropriate license headers to any new files


### Submitting your changes

Once your changes and tests are ready to submit for review:

1. Test your changes

    Run the test suite to make sure that nothing is broken.

2. Submit a pull request

    Push your local changes to your forked copy of the repository and [submit a pull request](https://help.github.com/articles/using-pull-requests). In the pull request, choose a title which sums up the changes that you have made, and in the body provide more details about what your changes do. Also mention the number of the issue where discussion has taken place, eg "Closes #123".

Then sit back and wait. There will probably be discussion about the pull request and, if any changes are needed, I would love to work with you to get your pull request merged into SchemesAnalyzer.

Contributing to the SchemesAnalyzer codebase
-------------------------------------------
**Repository:** https://github.com/vsushko/SchemesAnalyzer

JDK 1.8 is required to build SchemesAnalyzer. You must have a JDK 1.8.

SchemesAnalyzer uses the Apache maven for its build.

### Importing the project into IntelliJ IDEA
You can import the SchemesAnalyzer project into IntelliJ IDEA via:

- Select **File > Open**
- In the subsequent dialog navigate to the root pom.xml file
- In the subsequent dialog select **Open as Project**
```
### Javadoc
Good Javadoc can help with navigating and understanding code.

#### The short version
1. Always add Javadoc to new code.
2. Add Javadoc to existing code if you can.
3. Document the "why", not the "how", unless that's important to the "why".
4. Don't document anything trivial or obvious (e.g. getters and setters). In other words, the Javadoc should add some value.

### License Headers
I require license headers on all Java files. All contributed code should have the following license header:

```java
/**
 * Copyright (C) SchemesAnalyzer.2014 Vasiliy Sushko (vasiliy.sushko@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author <put-your-name-here> (your-email)
 */
```
### Running The Full Test Suite
Before submitting your changes, run the test suite to make sure that nothing is broken, with:
```sh
mvn clean install
```

Reviewing and accepting your contribution
-------------------------------------------
I review every contribution carefully to ensure that the change is of high quality and fits well with the rest of the SchemesAnalyzer codebase. If accepted, I will merge your change and usually take care of backporting it to appropriate branches ourselves.
