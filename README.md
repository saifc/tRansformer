# tRansformer

tRanformer is a command-line tool for moving resources to appropriate modules
and refactoring affected code in a multi-module namespaced Android project.

It’s designed to work with namespaced R class, a.k.a
**android.namespacedRClass** set to **true** in **gradle.properties**, but it
can also work without it enabled and you would ripe the same benefits if you had
minification enabled.

It has 3 commands:

### transform: 

The primary responsibility of tRansformer. Analyzes the usages of resources like drawables, colors, raws, dimensions and strings and moves them to the appropriate modules while refactoring the affected Java, Kotlin and XML files to reflect the changes.

**Usage:**

    transformer transform --project=/path/to/project --base-module=baseModule [ --resource-types=dimen,drawable,string,raw,color ]

### remove:

Removes a specific resource type from all modules except for the base module.
This is useful in case you use a localization management platform like [POEditor](https://poeditor.com/) to sync strings to the base module and want to purge modules strings before executing the *transform *command.

**Usage:**

    transformer remove --project=/path/to/project --base-module=baseModule [ --resource-types=dimen,drawable,string,raw,color ]

### verify:

Verifies the integrity of each module by executing *verifyReleaseResources*.

**Usage:**

    verify --project=/path/to/project --app-module=appModule

For more context, check this blog-post:

[Modularization & DEX count](https://medium.com/@saifc_/modularization-dex-count-798266d36a80)

### Releases

Latest release: [1.0.0](https://github.com/saifc/tRansformer/releases/tag/1.0.0)

Disclaimer: It’s a really bad idea to try this tool on a project without git.

Note: tRansformer has been tested on both macOS and Windows. If you face issues on Windows, make sure to close any application that may have the project files open like Android Studio.
