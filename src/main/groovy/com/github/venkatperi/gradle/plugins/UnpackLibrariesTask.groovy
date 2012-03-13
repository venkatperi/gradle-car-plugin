package com.github.venkatperi.gradle.plugins

import org.apache.commons.io.FilenameUtils
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by IntelliJ IDEA.
 * User: vperi
 * Date: 3/6/12
 * Time: 3:06 PM
 * To change this template use File | Settings | File Templates.
 */

/**
 * 'libs' task -- expands compiler dependencies and
 *  aggegregates compiler and linker dependencies (directories, flags etc)
 */
class UnpackLibrariesTask extends DefaultTask {

    LibrariesConvention convention =  project.extensions.libraries

    def UnpackLibrariesTask(){
        outputs.dir convention.pkgDirName
        inputs.files convention.configuration.getFiles()
    }
    
    /**
     * This task's action
     */
    @TaskAction
    public void libs() {
        description 'Unpack project dependencies'
                                                  
        convention.configuration.files.each {
            def name = FilenameUtils.removeExtension(it.name);
            def dir = convention.pkgDirName + '/' + name
            def x = new File(dir)
            logger.debug "processing $name"

            def f = project.zipTree(it)

            project.copy {
                into dir
                from f
            }
        }
    }
}
