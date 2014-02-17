package org.esurovskiy

import org.esurovskiy.LocalFileSystemStorage

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class LocalFileSystemStorageController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond LocalFileSystemStorage.list(params), model: [localFileSystemStorageInstanceCount: LocalFileSystemStorage.count()]
    }

    def show(LocalFileSystemStorage localFileSystemStorageInstance) {
        respond localFileSystemStorageInstance
    }

    def create() {
        respond new LocalFileSystemStorage(params)
    }

    @Transactional
    def save(LocalFileSystemStorage localFileSystemStorageInstance) {
        if (localFileSystemStorageInstance == null) {
            notFound()
            return
        }

        if (localFileSystemStorageInstance.hasErrors()) {
            respond localFileSystemStorageInstance.errors, view: 'create'
            return
        }

        localFileSystemStorageInstance.save flush: true

        request.withFormat {
            form {
                flash.message = message(code: 'default.created.message', args: [message(code: 'localFileSystemStorageInstance.label', default: 'LocalFileSystemStorage'), localFileSystemStorageInstance.id])
                redirect localFileSystemStorageInstance
            }
            '*' { respond localFileSystemStorageInstance, [status: CREATED] }
        }
    }

    def edit(LocalFileSystemStorage localFileSystemStorageInstance) {
        respond localFileSystemStorageInstance
    }

    @Transactional
    def update(LocalFileSystemStorage localFileSystemStorageInstance) {
        if (localFileSystemStorageInstance == null) {
            notFound()
            return
        }

        if (localFileSystemStorageInstance.hasErrors()) {
            respond localFileSystemStorageInstance.errors, view: 'edit'
            return
        }

        localFileSystemStorageInstance.save flush: true

        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'LocalFileSystemStorage.label', default: 'LocalFileSystemStorage'), localFileSystemStorageInstance.id])
                redirect localFileSystemStorageInstance
            }
            '*' { respond localFileSystemStorageInstance, [status: OK] }
        }
    }

    @Transactional
    def delete(LocalFileSystemStorage localFileSystemStorageInstance) {

        if (localFileSystemStorageInstance == null) {
            notFound()
            return
        }

        localFileSystemStorageInstance.delete flush: true

        request.withFormat {
            form {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'LocalFileSystemStorage.label', default: 'LocalFileSystemStorage'), localFileSystemStorageInstance.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'localFileSystemStorageInstance.label', default: 'LocalFileSystemStorage'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }
}
