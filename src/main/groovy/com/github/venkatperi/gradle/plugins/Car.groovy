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
* @author Venkat Peri. RemoteReality Corporation.
*/

package com.github.venkatperi.gradle.plugins

import com.github.venkatperi.gradle.api.java.archives.Manifest
import com.github.venkatperi.gradle.api.java.archives.internal.DefaultManifest
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.internal.file.collections.FileTreeAdapter
import org.gradle.api.internal.file.collections.MapFileTree
import org.gradle.api.internal.file.copy.CopySpecImpl
import org.gradle.api.tasks.StopActionException
import org.gradle.api.tasks.bundling.Zip
import org.gradle.util.ConfigureUtil

/**
 * Assembles a CAR archive.
 */
class Car extends Zip {
    public static final String DEFAULT_EXTENSION = 'car'
    private static final String SETTINGS = 'settings'
    private static final String CAR_DIR = 'build/tmp/car'
    private static final String META_DIR = '.meta'

    private Manifest manifest
    private final CopySpecImpl metaInf

    /**
     * Constructor
     */
    Car() {
        extension = DEFAULT_EXTENSION
        manifest = new DefaultManifest(project.fileResolver)

        // Add these as separate specs, so they are not affected by the changes to the main spec
        metaInf = copyAction.rootSpec.addFirst().into(META_DIR)
        metaInf.addChild().from {
            MapFileTree manifestSource = new MapFileTree(temporaryDirFactory)
            manifestSource.add('car.manifest') {
                OutputStream outstr ->
                Manifest manifest = getManifest() ?: new DefaultManifest(null)
                manifest.writeTo(new OutputStreamWriter(outstr))
            }
            return new FileTreeAdapter(manifestSource)
        }

        copyAction.from CAR_DIR

        copyAction.mainSpec.eachFile { FileCopyDetails details ->
            if (details.path.contains('car.manifest')) {
                details.exclude()
            }
        }

        try {
            //set default params
            manifest.attributes(['group': project.group, 'version': project.version, 'description': project.description])

            manifest.attributes(['arch': project.arch,
                    'type': project.type,
                    'buildConfig': project.buildConfig,
                    'crt': project.crt,
                    'call': project.call], SETTINGS)
        }
        catch (e) {
            def err = "Error initializing manifest: " + e.message
            logger.error err
            throw new StopActionException(err)
        }
    }

    /**
     * Returns the manifest for this CAR archive.
     * @return The manifest
     */
    public Manifest getManifest() {
        return manifest;
    }

    /**
     * Sets the manifest for this CAR archive.
     *
     * @param manifest The manifest. May be null.
     */
    public void setManifest(Manifest manifest) {
        this.manifest = manifest;
    }

    /**
     * Configures the manifest for this CAR archive.
     *
     * <p>The given closure is executed to configure the manifest. The {@link org.gradle.api.java.archives.Manifest}
     * is passed to the closure as its delegate.</p>
     *
     * @param configureClosure The closure.
     * @return This.
     */
    public Car manifest(Closure configureClosure) {
        if (getManifest() == null) {
            manifest = new DefaultManifest(project.fileResolver)
        }

        ConfigureUtil.configure(configureClosure, getManifest());
        return this;
    }

    public CopySpec getMetaInf() {
        return metaInf.addChild()
    }

    /**
     * Adds content to this CAR archive's META-INF directory.
     *
     * <p>The given closure is executed to configure a {@code CopySpec}. The {@link CopySpec} is passed to the closure
     * as its delegate.</p>
     *
     * @param configureClosure The closure.
     * @return The created {@code CopySpec}
     */
    public CopySpec metaInf(Closure configureClosure) {
        return ConfigureUtil.configure(configureClosure, getMetaInf())
    }
}
