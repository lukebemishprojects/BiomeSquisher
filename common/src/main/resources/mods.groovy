ModsDotGroovy.make {
    modLoader = 'javafml'
    loaderVersion = '[40,)'

    license = 'BSD-3-clause'
    issueTrackerUrl = 'https://github.com/lukebemishprojects/BiomeSquisher/issues'

    mod {
        modId = this.buildProperties['modid']
        displayName = this.buildProperties['modname']
        version = this.version
        group = this.group
        intermediate_mappings = 'net.fabricmc:intermediary'
        displayUrl = 'https://github.com/lukebemishprojects/BiomeSquisher'

        description = 'Squishes biomes to make room! Very WIP.'
        authors = [this.buildProperties['modauthor'] as String]

        dependencies {
            minecraft = this.minecraftVersionRange

            onForge {
                forge {
                    versionRange = ">=${this.forgeVersion}"
                }
            }

            onFabric {
                mod 'fabric-api', {
                    versionRange = ">=${this.libs.versions.fabric.api.split(/\+/)[0]}"
                }
                mod 'fabricloader', {
                    versionRange = ">=${this.libs.versions.fabric.loader}"
                }
            }
        }

        entrypoints {
            main = [
                'dev.lukebemish.biomesquisher.impl.fabric.BiomeSquisherMod'
            ]
        }
    }
    onFabric {
        mixin = [
            'biomesquisher.mixins.json'
        ]
    }
}
