package commands

import PackageNameFinder
import ResourceType
import commands.base.Command
import commands.base.RefactorRemoveAndAppend
import java.io.File

class RefactorDimensions(
    projectDir: String,
    packageNameFinder: PackageNameFinder,
    baseModule: String,
    valuesDirs: Sequence<File>
) : Command by RefactorRemoveAndAppend(ResourceType.string, projectDir, packageNameFinder, baseModule, valuesDirs)