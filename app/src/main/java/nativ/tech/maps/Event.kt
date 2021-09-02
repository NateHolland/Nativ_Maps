package nativ.tech.maps

open class Event<out T> (private  val content: T){

    enum class Type {
        ERROR,
        CHOOSE_FILE_NAME,
        SAVE_SUCCESSFUL,
        IMPORT_FAILED,
        EXPORT_FAILED,
        EXPORT_SUCEEDED
    }

    private var hasBeenHandled = false
    fun getContent(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }
    fun peekContent(): T = content
}