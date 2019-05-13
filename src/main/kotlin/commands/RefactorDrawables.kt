package commands

import PackageNameFinder
import ResourceType
import commands.base.RefactorMoveCommand

class RefactorDrawables(projectDir: String, packageNameFinder: PackageNameFinder, baseModule: String) :
    RefactorMoveCommand(ResourceType.drawable, projectDir, packageNameFinder, baseModule)