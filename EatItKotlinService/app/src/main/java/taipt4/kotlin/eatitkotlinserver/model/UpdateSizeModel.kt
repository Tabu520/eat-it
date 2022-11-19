package taipt4.kotlin.eatitkotlinserver.model

class UpdateSizeModel {

    var listSizeModel: List<SizeModel>? = null

    constructor() {}

    constructor(listSizeModel: List<SizeModel>) {
        this.listSizeModel = listSizeModel
    }

}