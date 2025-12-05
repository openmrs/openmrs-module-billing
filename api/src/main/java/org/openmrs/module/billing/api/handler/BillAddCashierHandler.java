package org.openmrs.module.billing.api.handler;

import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.handler.SaveHandler;
import org.openmrs.module.billing.api.model.Bill;

import java.util.Collection;
import java.util.Date;

@Handler(supports = { Bill.class })
public class BillAddCashierHandler implements SaveHandler<Bill> {
    @Override
    public void handle(Bill bill, User user, Date date, String other) {
        Provider cashier = getCurrentCashier(bill, user);

        if (cashier != null) {
            bill.setCashier(cashier);
        }
    }

    private Provider getCurrentCashier(Bill bill, User currentUser) {
        ProviderService service = Context.getProviderService();
        Collection<Provider> providers = service.getProvidersByPerson(currentUser.getPerson());

        if (providers != null && !providers.isEmpty()) {
            return providers.iterator().next();
        }

        return null;
    }
}
