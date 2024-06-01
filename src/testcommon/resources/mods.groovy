MultiplatformModsDotGroovy.make {
    modLoader = 'javafml'
    loaderVersion = '[1,)'

    license = 'BSD-3-clause'
    issueTrackerUrl = 'https://github.com/lukebemishprojects/BiomeSquisher/issues'

    mod {
        modId = buildProperties.mod_id + 'tests'
        displayName = buildProperties.mod_name + ' Tests'
        version = environmentInfo.version
        displayUrl = 'https://github.com/lukebemishprojects/BiomeSquisher'

        description = 'Test mod for Biome Squisher'
        authors {
            person buildProperties.mod_author
        }

        entrypoints {
            entrypoint 'main', 'dev.lukebemish.biomesquisher.test.fabric.BiomeSquisherTest'
            entrypoint 'fabric-gametest', 'dev.lukebemish.biomesquisher.test.BiomeSquisherGameTests'
        }
    }
    mixins {
        mixin 'biomesquisher.test.mixins.json'
    }
}
