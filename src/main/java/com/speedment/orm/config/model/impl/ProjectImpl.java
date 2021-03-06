/**
 *
 * Copyright (c) 2006-2015, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.speedment.orm.config.model.impl;

import com.speedment.orm.config.model.*;
import com.speedment.orm.config.model.aspects.Parent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author pemi
 */
public class ProjectImpl extends AbstractNamedConfigEntity implements Project {

    private ProjectManager parent;
    private final ChildHolder children;
    private String packetName, packetLocation;
    private Path configPath;

    public ProjectImpl() {
        this.children = new ChildHolder();
    }

    @Override
    protected void setDefaults() {
        setPacketLocation("src/main/java");
        setPacketName("com.company.speedment.orm.test");
        //setConfigPath(Paths.get("src/main/groovy/speedment.groovy"));
        setConfigPath(Paths.get("src/main/groovy/speedment.groovy"));
    }

    @Override
    public String getPacketName() {
        return packetName;
    }

    @Override
    public void setPacketName(String packetName) {
        this.packetName = Objects.requireNonNull(packetName);
    }

    @Override
    public String getPacketLocation() {
        return packetLocation;
    }

    @Override
    public void setPacketLocation(String packetLocation) {
        this.packetLocation = Objects.requireNonNull(packetLocation);
    }

    @Override
    public ChildHolder getChildren() {
        return children;
    }

    @Override
    public void setParentTo(Parent<?> parent) {
        setParentHelper(parent, ProjectManager.class)
                .ifPresent(p -> this.parent = p);
    }

    @Override
    public Optional<ProjectManager> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public Optional<Path> getConfigPath() {
        return Optional.ofNullable(configPath);
    }

    @Override
    public void setConfigPath(Path configPath) {
        this.configPath = configPath;
    }
}
