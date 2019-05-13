package commands

import PackageNameFinder
import ResourceType
import commands.base.RefactorMoveCommand

class RefactorRaws(projectDir: String, packageNameFinder: PackageNameFinder, baseModule: String) :
    RefactorMoveCommand(ResourceType.raw, projectDir, packageNameFinder, baseModule)