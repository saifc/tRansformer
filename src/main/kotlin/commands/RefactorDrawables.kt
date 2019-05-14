package commands

import PackageNameFinder
import ResourceType
import commands.base.Command
import commands.base.RefactorMoveCommand

class RefactorDrawables(
    projectDir: String,
    baseModule: String
) : Command by RefactorMoveCommand(ResourceType.drawable, projectDir, PackageNameFinder, baseModule)