MultiplatformModsDotGroovy.make {
    modLoader = 'javafml'
    loaderVersion = '[1,)'

    license = 'BSD-3-clause'
    issueTrackerUrl = 'https://github.com/lukebemishprojects/BiomeSquisher/issues'

    mod {
        modId = buildProperties.mod_id
        displayName = buildProperties.mod_name
        version = environmentInfo.version
        displayUrl = 'https://github.com/lukebemishprojects/BiomeSquisher'

        description = buildProperties.mod_description
        authors {
            person(buildProperties.mod_author)
        }

        dependencies {
            minecraft = ">=${libs.versions.minecraft}"

            onNeoForge {
                mod 'neoforge', {
                    versionRange = ">=${libs.versions.neoforge}"
                }
            }

            onFabric {
                mod 'fabric-api', {
                    versionRange = ">=${libs.versions.fabric_api.split(/\+/)[0]}"
                }
                mod 'fabricloader', {
                    versionRange = ">=${libs.versions.fabric_loader}"
                }
            }
        }

        entrypoints {
            entrypoint 'main', 'dev.lukebemish.biomesquisher.impl.fabric.BiomeSquisherMod'
        }
    }
    mixins {
        mixin('biomesquisher.mixins.json')
    }
}
