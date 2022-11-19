package taipt4.kotlin.eatitkotlinserver.model

class TokenModel {
    var phone: String? = null
    var token: String? = null
    constructor()

    constructor(phone: String, token: String) {
        this.phone = phone
        this.token = token
    }
}