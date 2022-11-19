package taipt4.kotlin.eatitkotlinserver.model

class UpdateAddonModel {

    var listAddonModel: List<AddonModel>? = null

    constructor() {}

    constructor(listAddonModel: List<AddonModel>) {
        this.listAddonModel = listAddonModel
    }
}