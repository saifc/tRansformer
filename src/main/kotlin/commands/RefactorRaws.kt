package commands

import PackageNameFinder
import ResourceType
import commands.base.Command
import commands.base.RefactorMoveCommand

class RefactorRaws(
    projectDir: String,
    baseModule: String,
    packageNameFinder: PackageNameFinder
) : Command by RefactorMoveCommand(ResourceType.raw, projectDir, baseModule, packageNameFinder)