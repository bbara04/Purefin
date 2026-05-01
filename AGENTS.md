# Purefin AGENTS.md

Follow these instructions before making changes:

* Understand the relevant files before editing.
* Always follow official Android recommendations and best practices.
* Always include the pages you have read in your reply, even in planning mode.
* Prefer small, targeted changes over broad refactors.
* Preserve existing architecture and naming unless the task requires a change.
* Do not introduce new dependencies without a clear reason.
* Ask for clarification or leave a note when requirements are ambiguous.

---

## Ui principles
* Ui design should align with the material ui 3 design system
* For new components check https://m3.material.io/components if there is an available solution for the problem.
* Using Box components are a red flag and should be only used when there is no other option.
* When styling components check the material ui 3 site first and use it for the decision https://m3.material.io/styles

---

## Repository Expectations

* Always use the currently used syntax in the codebase. Include examples for any naming you introduce.
* Avoid touching unrelated files.
* Do not fix unrelated existing problems in the codebase.
* Prefer consistency with the existing codebase over idealized patterns.
* When modifying a file, match its local style and conventions.
* Favor readable, maintainable code over clever code.

---

## Testing

* Running a build is sufficient to verify your implementation.
* Do not write tests unless explicitly asked to.

---

## Git and Commit Rules

When asked to prepare a commit:

* Use **Conventional Commits** format.
* Format:

  ```
  <type>(<scope>): <description>
  ```
* The description should not be too long, but must include any major change.
* Use imperative mood.
* Keep the subject concise.

**Common types:**

* `feat` – new feature
* `fix` – bug fix
* `refactor` – code change without feature or fix
* `test` – adding or updating tests
* `docs` – documentation changes
* `chore` – maintenance tasks
* `build` – build system changes
* `ci` – CI/CD changes

---

## Preferred Workflow

1. Read the task carefully.
2. Read relevant official Google/Android documentation.
3. Inspect the relevant files and nearby code.
4. Choose the simplest solution aligned with Google’s standards.
5. Check if the code can be simplified:

   * If yes, simplify it.
   * Do not reduce readability.
6. Summarize the result, including anything not verified.
