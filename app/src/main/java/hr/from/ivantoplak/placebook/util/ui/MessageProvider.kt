package hr.from.ivantoplak.placebook.util.ui

interface MessageProvider {

    fun shortPopup(message: String)
    fun longPopup(message: String)
}