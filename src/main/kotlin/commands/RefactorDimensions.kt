package commands

import PackageNameFinder
import ResourceType
import commands.base.Command
import commands.base.RefactorRemoveAndAppend
import java.io.File

class RefactorDimensions(
    projectDir: String,
    baseModule: String,
    valuesDirs: Sequence<File>
) : Command by RefactorRemoveAndAppend(ResourceType.string, projectDir, PackageNameFinder, baseModule, valuesDirs)