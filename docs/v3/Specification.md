## Phase III: Tech Requirement to Refactor

This is a **TECH REQUIREMENT** to refactor some API, to make them more reasonability and friendly.

## I. Make API more reasonable

1. In `com.sdd.etl.context.ETLContext`, the return type of methods `getCurrentDate` is better to be `LocalDate` than `String`.
2. In `com.sdd.etl.model.WorkflowResult`, the type of `startDate` and `endDate` are also good to be `LocalDate`
3. Thus, `com.sdd.etl.util.DateRangeGenerator.generate` should return `List<LocalDate>` instead of `List<String>`
4. **ALL** dependencies to above 3 parts need to be updated as well.

## II. Create a default INI Configure File

1. A default INI configure file **MUST** be added to cover existing components. It would be a **DEMO** for real user and friendly.
