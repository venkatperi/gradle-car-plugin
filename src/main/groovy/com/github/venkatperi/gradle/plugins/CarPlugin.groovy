package com.github.venkatperi.gradle.plugins

import java.util.concurrent.Callable
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.publish.ArchivePublishArtifact
import org.gradle.api.internal.plugins.DefaultArtifactPublicationSet
import org.gradle.api.plugins.BasePlugin

/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Venkat Peri 
 */

class CarPlugin extends BasePlugin {

    private static final String STRUCTURE = 'configure'
    private static final String UNPACK = 'unpack'
    private static final String LIBS = 'libs'
    private static final String CAR = 'car'

    @Override
    def void apply(Project project) {

        //configureListToString()

        def javaConvention = new CarPluginConvention(project);
        project.getConvention().getPlugins().put("car", javaConvention);

        configureLibs(project)

        configureArchives(project, javaConvention)
    }

    /**
     * Replace List.toString with an escaped version so that string lists can be 'evaluate'd back to lists during
     * readback from a manifest file
     * @return
     */
    def configureListToString(){
        ExpandoMetaClass.enableGlobally()
        
        //call 'enableGlobally' method before adding to supplied class
        ArrayList.metaClass.toString = {-> '[' + delegate.sum { '"' + it + '",' } + ']' }
        List.metaClass.toString = {-> '[' + delegate.sum { '"' + it + '",' } + ']' }
    }

    /**
     * Configure 'libs' task
     * @param project
     * @return
     */
    private configureLibs(Project project) {
        def convention = new LibrariesConvention(project)
        project.extensions.libraries = convention

        project.task(UNPACK, type: UnpackLibrariesTask, group: STRUCTURE, description: 'Unpack dependency libraries')
        project.task(LIBS, type: LibsListTask, dependsOn: UNPACK, group: STRUCTURE, description: 'Scan dependency libraries for include/lib/flag settings')
    }

    /**
     * Configure 'car' and 'archive' task
     * @param project
     * @param pluginConvention
     */
    private void configureArchives(final Project project, final CarPluginConvention pluginConvention) {
        // project.getTasks().getByName(JavaBasePlugin.CHECK_TASK_NAME).dependsOn(TEST_TASK_NAME);

        Car jar = project.getTasks().add(CAR, Car.class);
        jar.getManifest().from(pluginConvention.getManifest());
        jar.setDescription("Assembles a CAR archive.");
        jar.setGroup(BasePlugin.BUILD_GROUP);

        //jar.from(pluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME).getOutput());
        jar.getMetaInf().from(new Callable() {
            public Object call() throws Exception {
                return pluginConvention.getMetaInf();
            }
        });

        project.getExtensions().getByType(DefaultArtifactPublicationSet.class).addCandidate(new ArchivePublishArtifact(jar));
        //project.getConfigurations().getByName('runtime').getArtifacts().add(new ArchivePublishArtifact(jar));
    }
}
