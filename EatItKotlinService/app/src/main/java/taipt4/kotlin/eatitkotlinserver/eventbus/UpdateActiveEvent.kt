package taipt4.kotlin.eatitkotlinserver.eventbus

import taipt4.kotlin.eatitkotlinserver.model.Shipper

class UpdateActiveEvent(var shipper: Shipper, var isActive: Boolean)