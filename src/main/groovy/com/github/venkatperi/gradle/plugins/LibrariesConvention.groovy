package com.github.venkatperi.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

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
class LibrariesConvention {

    /**
     * Specify the configuration that will be used for resolving package dependencies. Default is 'compile'
     */
    Configuration configuration

    /**
      * list of include directories are stored here
     */
    List<String> incDirs = []

    /**
     * Resolved list of compiler flags stored here
     */
    List<String> cxxflags = []

    /**
     * list of directories to search for libraries
     */
    List<String> libDirs = []

    /**
     * list of libs to be linked against
     */
    List<String> libs = []

    /**
     * list of linker flags
     */
    List<String> linkFlags = []

    /**
     * directory where packages are expanded and referenced for compile/link
     */
    String pkgDirName = 'build/pkg'

    /**
     * Constuctor
     *
     * @param project
     * @return
     */
    def LibrariesConvention(Project project){
        configuration = project.configurations.compile
    }
}
