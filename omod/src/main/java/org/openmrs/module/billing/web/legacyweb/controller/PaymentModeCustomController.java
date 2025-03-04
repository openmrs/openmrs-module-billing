package org.openmrs.module.billing.web.legacyweb.controller;

import org.openmrs.module.billing.api.IPaymentModeService;
import org.openmrs.module.billing.api.model.PaymentMode;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/billing/paymentMode")
public class PaymentModeCustomController {

    @Autowired
    private IPaymentModeService paymentModeService;

    /**
     * Custom endpoint to check if a payment mode is in use.
     *
     * @param uuid The UUID of the payment mode to check.
     * @return A JSON response indicating if the payment mode is in use.
     */
    @GetMapping("/isInUse/{uuid}")
    public ResponseEntity<Map<String, Boolean>> isPaymentModeInUse(@PathVariable String uuid) {
        Map<String, Boolean> response = new HashMap<>();

        PaymentMode paymentMode = paymentModeService.getByUuid(uuid);
        if (paymentMode == null) {
            throw new IllegalArgumentException("PaymentMode with UUID " + uuid + " does not exist.");
        }

        boolean inUse = paymentModeService.isPaymentModeInUse(paymentMode.getId());
        response.put("inUse", inUse);

        return ResponseEntity.ok(response);
    }
}
