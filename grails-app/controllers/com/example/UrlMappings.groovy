package com.example

class UrlMappings {
    static mappings = {
        "/bugDemo/index"(controller: 'bugDemo', action: 'index')
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(view:"/index")
        "500"(view:'/error')
        "404"(view:'/notFound')

    }
}
