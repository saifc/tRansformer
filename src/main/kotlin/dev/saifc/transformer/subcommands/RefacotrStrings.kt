package dev.saifc.transformer.subcommands

import dev.saifc.transformer.PackageNameFinder
import dev.saifc.transformer.ResourceType
import dev.saifc.transformer.subcommands.base.Command
import dev.saifc.transformer.subcommands.base.RefactorRemoveAndAppend
import java.io.File

class RefactorStrings(
    projectDir: String,
    baseModule: String,
    valuesDirs: Sequence<File>,
    packageNameFinder : PackageNameFinder
) : Command by RefactorRemoveAndAppend(
    ResourceType.string,
    projectDir,
    baseModule,
    valuesDirs,
    packageNameFinder
)