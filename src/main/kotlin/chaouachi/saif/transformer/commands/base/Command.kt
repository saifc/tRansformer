package chaouachi.saif.transformer.commands.base

import chaouachi.saif.transformer.Usage

interface Command {
    operator fun invoke(
        resources: Map<String, MutableList<Usage>>
    )
}