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
 * @author Venkat Peri. RemoteReality Corp.
 */

package com.github.venkatperi.gradle.plugins

import com.github.venkatperi.gradle.api.java.archives.ManifestException
import com.github.venkatperi.gradle.api.java.archives.internal.DefaultManifest
import org.apache.commons.io.FilenameUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException

class LibsListTask extends DefaultTask {

    static final String COMPILE_SECTION = 'compile'
    static final String LINK_SECTION = 'link'
    static final String DIRS = 'dirs'
    static final String CXX_FLAGS = 'cxxflags'
    static final String FLAGS = 'flags'
    static final String LIBS = 'libs'
    static final String SETTINGS = 'settings'

    LibrariesConvention convention = project.extensions.libraries

    def UnpackLibrariesTask() {
        inputs.files convention.configuration.files
        outputs.dir convention.pkgDirName
    }

    /**
     * add item 'v1' to provided list, transforming if first by closure 'c'
     * @param v1
     * @param l
     * @param c
     * @return
     */
    def void addListItems(Object v, List l, Closure c) {
        if (v instanceof String) {
            l.add c(v)
        }
        else if (v instanceof List) {
            v.each {
                l.add c(it)
            }
        }
    }

    /**
     * This task's action
     */
    @TaskAction
    def doIt() {
        description 'Assemble list of compiler/linker settings from dependency packages'

        project.configurations.compile.files.each {
            def name = FilenameUtils.removeExtension(it.name);
            def dir = convention.pkgDirName + '/' + name
            outputs.files new File(dir)

            def mf = new File(dir + '/.meta/car.manifest')

            if (mf.exists()) {
                logger.debug "Processing package $name"

                def manifest = new DefaultManifest(project.fileResolver)
                manifest.read(mf.path)
                def sections = manifest.getSections()

                //verify that this package has compatiable settings with the current project
                try {
                    def s = sections.get('settings')
                    if (s == null) {
                        throw new ManifestException("Package $name: 'settings' section missing")
                    }

                    ['arch', 'buildConfig', 'crt', 'call'].each {
                        logger.debug "Checking package property $it"
                        def v = s.get(it)
                        def v1 = project[it]
                        if (v != v1) {
                            def err = ":Settings mismatch found for $it in package $name: Package: $v, Project: $v1"
                            logger.warn err
                        }
                    }
                }
                catch (GradleException e) {
                    def err = "Error reading 'settings' section of manifest for package $name: " + e.message
                    logger.error err
                    throw e
                }
                catch (e) {
                    def err = "Error reading 'settings' section of manifest for package $name: " + e.message
                    logger.error err
                    throw new TaskExecutionException(err)
                }


                if (sections.containsKey(COMPILE_SECTION)) {
                    def s = sections.get(COMPILE_SECTION)

                    if (s.containsKey(DIRS)) {
                        Set v1 = s.get(DIRS)
                        convention.incDirs.addAll v1.collect { "$dir/$it" }
                    }

                    if (s.containsKey(CXX_FLAGS)) {
                        Set v1 = s.get(DIRS)
                        convention.cxxflags.addAll v1
                    }
                }

                if (sections.containsKey(LINK_SECTION)) {
                    def s = sections.get(LINK_SECTION)

                    if (s.containsKey(DIRS)) {
                        Set v1 = s.get(DIRS)
                        convention.libDirs.addAll v1.collect { "$dir/$it" }
                    }

                    if (s.containsKey(FLAGS)) {
                        Set v1 = s.get(FLAGS)
                        convention.linkFlags.addAll v1

                    }

                    if (s.containsKey(LIBS)) {
                        Set v1 = s.get(LIBS)
                        convention.cxxflags.addAll v1.collect { it + '.lib'}
                    }
                }
            }
        }
    }
}
