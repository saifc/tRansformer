package commands

import PackageNameFinder
import ResourceType
import commands.base.Command
import commands.base.RefactorMoveCommand

class RefactorRaws(
    projectDir: String,
    baseModule: String
) : Command by RefactorMoveCommand(ResourceType.raw, projectDir, PackageNameFinder, baseModule)