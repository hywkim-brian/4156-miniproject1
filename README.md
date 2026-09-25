This is the miniproject repo for 4156 in Fall 2026.

## Running PMD

I used PMD as the static bug finder for this project. PMD is run through
Maven, so there is no separate installation needed as long as Maven and Java
are installed.

From the `IndividualProject` directory, run:

```bash
mvn pmd:pmd
```

PMD writes its report to:

```text
IndividualProject/target/reports/pmd.html
```

I opened the report in a browser and reviewed the warnings to decide which
ones represented real bugs. A warning was only treated as a bug when I could
explain how it could cause incorrect behavior and verify it with a test.

To run PMD's check goal, use:

```bash
mvn pmd:check
```

The project is configured to generate the report without stopping the rest of
the build, so the report from `mvn pmd:pmd` is the main report used for this
assignment.
