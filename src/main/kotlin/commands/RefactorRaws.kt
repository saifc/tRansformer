package commands

import PackageNameFinder
import ResourceType
import commands.base.Command
import commands.base.RefactorMoveCommand

class RefactorRaws(
    projectDir: String,
    packageNameFinder: PackageNameFinder,
    baseModule: String
) : Command by RefactorMoveCommand(ResourceType.raw, projectDir, packageNameFinder, baseModule)