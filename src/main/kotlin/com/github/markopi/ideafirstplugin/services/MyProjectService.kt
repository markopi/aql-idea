package com.github.markopi.ideafirstplugin.services

import com.intellij.openapi.project.Project
import com.github.markopi.ideafirstplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
