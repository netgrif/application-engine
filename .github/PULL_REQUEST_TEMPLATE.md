
---

#### Instruction for the developer

For starters, congratulation on opening a PR of your work. I hope you're proud of your work and yourself :wink: .
There are some things you need to do before this PR is submitted, so read carefully.

First, **set yourself as an assignee**. You can also add other people responsible for code in this PR (i.e. your team lead).

Then **set reviewers**. There should be at least two reviewers. You can set your team lead, projects owner, or senior developer
(after his or her agreement, of course) as a PR reviewer.

The last element on the right panel are labels. Labels should be based on what type of change the PR brings to the table.
You can **add one or more labels** that apply to the PR:

- _Bugfix_ (a change that fixes a bug)
- _New feature_ (a change that introduces new functionality)
- _Improvement_ (a change that improves on an existing feature)
- _Breaking change_ (fix or feature that would cause existing functionality doesn't work as expected)
- _Documentation update_ (a change makes, or requires, change to the product documentation)
- _New Dependency_ (a change that introduces new third-party dependency)
- _Critical_ (a change that is critical to a release and must not be omitted)

Next thing, please read and edit the PR description below the following way:

- If a statement is between `<` `>` symbols, replace it with your content according to the instructions in the statement.
- If a slash `/` is between two statements, choose to fill only one or the other.
- To reference a JIRA issue, use the issue key wrapped by `[ ]` brackets.
- To mention someone (or team), use at `@` symbol.
- To reference specific code, use an absolute link to the source code file with suffix `#L` and the number of the desired line of code.

For whole markdown documentation please read [GitHub Markdown](https://docs.github.com/en/github/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax).

If you read these whole instructions and have done everything, **you are the best** :+1: .
You can now safely **delete all between two horizontal lines**, so the instructions don't look bad in your PR.

---

# Description

<Please include a summary of the changes and which issue is fixed. Please also include relevant links and special instructions if applicable.>

<Fixes [JIRA-ISSUE]>/<Implements [JIRA-ISSUE]>

## Dependencies

<Please include all newly created dependencies on third party libraries or on other PR in the project.>

### Third party dependencies

- <Name of a new library/dependency [link to the new library]()> - <Reason, why it was added to the project> / <No new dependencies were introduced>

### Blocking Pull requests

<Depends on #(PR id)>/<There are no dependencies on other PR>

## How Has Been This Tested?

<Please describe the tests that you ran to verify your changes. Provide instructions so we can reproduce. Please also list any relevant details for your test configuration.>

- <Name of a test [test file](link to the test file)>

### Test Configuration

<Please describe configuration for tests to run if applicable, like program parameters, host OS, VM configuration etc.>

# Checklist:

- [ ] My code follows the style guidelines of this project
- [ ] I have performed a self-review of my own code
- [ ] My changes have been checked, personally or remotely, with @...
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have resolved all conflicts with the target branch of the PR
- [ ] I have updated and synced my code with the target branch
- [ ] I have added tests that prove my fix is effective or that my feature works
- [ ] New and existing tests pass locally with my changes:
    - [ ] Lint test
    - [ ] Unit tests
    - [ ] Integration tests
- [ ] I have checked my contribution with code analysis tools:
    - [ ] [SonarCloud](https://sonarcloud.io/project/overview?id=netgrif_application-engine)
    - [ ] [Snyk](https://app.snyk.io/org/netgrif)
- [ ] I have made corresponding changes to the documentation:
    - [ ] Developer documentation
    - [ ] User Guides
    - [ ] Migration Guides
