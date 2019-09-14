package dev.saifc.transformer.subcommands

import dev.saifc.transformer.PackageNameFinder
import dev.saifc.transformer.ResourceType
import dev.saifc.transformer.subcommands.base.Command
import dev.saifc.transformer.subcommands.base.RefactorMoveCommand

class RefactorDrawables(
    projectDir: String,
    baseModule: String,
    packageNameFinder : PackageNameFinder
) : Command by RefactorMoveCommand(
    ResourceType.drawable,
    projectDir,
    baseModule,
    packageNameFinder
)