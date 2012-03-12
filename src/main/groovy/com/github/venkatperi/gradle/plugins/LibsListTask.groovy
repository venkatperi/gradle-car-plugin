package com.github.venkatperi.gradle.plugins

import com.github.venkatperi.gradle.api.java.archives.internal.DefaultManifest
import org.apache.commons.io.FilenameUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

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
class LibsListTask extends DefaultTask {

    static final String COMPILE_SECTION = 'compile'
    static final String LINK_SECTION = 'link'
    static final String DIRS = 'dirs'
    static final String CXX_FLAGS = 'cxxflags'
    static final String FLAGS = 'flags'
    static final String LIBS = 'libs'

    LibrariesConvention convention =  project.extensions.libraries

    def UnpackLibrariesTask(){
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
    def addItems(Object v1, List l, Closure c) {
        Map bindingMap = [:].withDefault { it }
        def v = new GroovyShell(bindingMap as Binding).evaluate(v1)

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
    public void libs() {
        description 'Assemble list of compiler/linker settings from dependency packages'

        project.configurations.compile.files.each {
            def name = FilenameUtils.removeExtension(it.name);
            def dir = convention.pkgDirName + '/' + name

            def mf = new File(dir + '/.meta/car.manifest')

            if (mf.exists()) {

                def manifest = new DefaultManifest(project.fileResolver)
                manifest.read(mf.path)
                def sections = manifest.getSections()

                if (sections.containsKey(COMPILE_SECTION)) {
                    def s = sections.get(COMPILE_SECTION)

                    if (s.containsKey(DIRS)) {
                        addItems s.get(DIRS), convention.incDirs, { val -> "$dir/$val"}
                    }

                    if (s.containsKey(CXX_FLAGS)) {
                        addItems s.get(CXX_FLAGS), convention.cxxflags, { val -> val }
                    }
                }

                if (sections.containsKey(LINK_SECTION)) {
                    def s = sections.get(LINK_SECTION)

                    if (s.containsKey(DIRS)) {
                        addItems s.get(DIRS), convention.libDirs, { val -> "$dir/$val"}
                    }

                    if (s.containsKey(FLAGS)) {
                        addItems s.get(FLAGS), convention.linkFlags, { val -> val }
                    }

                    if (s.containsKey(LIBS)) {
                        addItems s.get(LIBS), convention.libs, { val -> val + '.lib'}
                    }
                }
            }
        }
    }
}
