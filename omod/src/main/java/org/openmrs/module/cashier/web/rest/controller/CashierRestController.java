package org.openmrs.module.cashier.web.rest.controller;

import org.openmrs.api.context.Context;
import org.openmrs.module.cashier.api.IBillableItemsService;
import org.openmrs.module.cashier.api.model.BillableService;
import org.openmrs.module.cashier.web.rest.controller.base.CashierResourceController;
import org.openmrs.module.cashier.web.rest.restmapper.BillableServiceMapper;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + CashierResourceController.KENYAEMR_CASHIER_NAMESPACE + "/api")
public class CashierRestController extends BaseRestController {
    @RequestMapping(method = RequestMethod.POST, path = "/billable-service")
    @ResponseBody
    public Object get(@RequestBody BillableServiceMapper request) {
        BillableService billableService = request.billableServiceMapper(request);
        IBillableItemsService service = Context.getService(IBillableItemsService.class);

        System.out.println("Before Processing dispense");
        service.save(billableService);
        System.out.println("Processing dispense");
        return true;
    }
}
