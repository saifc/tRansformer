package commands

import PackageNameFinder
import ResourceType
import commands.base.Command
import commands.base.RefactorRemoveAndAppend
import java.io.File

class RefactorStrings(
    projectDir: String,
    baseModule: String,
    valuesDirs: Sequence<File>,
    packageNameFinder : PackageNameFinder
) : Command by RefactorRemoveAndAppend(ResourceType.string, projectDir, baseModule, valuesDirs,packageNameFinder)