ModsDotGroovy.make {
    modLoader = 'javafml'
    loaderVersion = '[1,)'

    license = 'BSD-3-clause'
    issueTrackerUrl = 'https://github.com/lukebemishprojects/BiomeSquisher/issues'

    mod {
        modId = this.buildProperties['modid']+'tests'
        displayName = this.buildProperties['modname']+ ' Tests'
        version = this.version
        displayUrl = 'https://github.com/lukebemishprojects/BiomeSquisher'

        description = 'Test mod for Biome Squisher'
        authors = [this.buildProperties['modauthor'] as String]

        entrypoints {
            main = [
                'dev.lukebemish.biomesquisher.test.fabric.BiomeSquisherTest'
            ]
            entrypoint 'fabric-gametest', [
                'dev.lukebemish.biomesquisher.test.BiomeSquisherGameTests'
            ]
        }

        dependencies {

        }
    }
    onFabric {
        mixin = [
            'biomesquisher.test.mixins.json'
        ]
    }
    onForge {
        put 'mixins', [
            ['config':'biomesquisher.test.mixins.json']
        ]
    }
}
