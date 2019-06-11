package chaouachi.saif.transformer.commands

import chaouachi.saif.transformer.PackageNameFinder
import chaouachi.saif.transformer.ResourceType
import chaouachi.saif.transformer.commands.base.Command
import chaouachi.saif.transformer.commands.base.RefactorRemoveAndAppend
import java.io.File

class RefactorDimensions(
    projectDir: String,
    baseModule: String,
    valuesDirs: Sequence<File>,
    packageNameFinder: PackageNameFinder
) : Command by RefactorRemoveAndAppend(
    ResourceType.string,
    projectDir,
    baseModule,
    valuesDirs,
    packageNameFinder
)