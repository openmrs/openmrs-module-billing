/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.billing.api.db.hibernate;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.openmrs.module.billing.api.db.CashierItemPriceDAO;
import org.openmrs.module.billing.api.model.CashierItemPrice;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nonnull;
import java.util.List;

@RequiredArgsConstructor
public class HibernateCashierItemPriceDAOImpl implements CashierItemPriceDAO {
	
	private final SessionFactory sessionFactory;
	
	/** {@inheritDoc} */
	@Override
	@Transactional(readOnly = true)
	public CashierItemPrice getCashierItemPrice(@Nonnull Integer id) {
		return sessionFactory.getCurrentSession().get(CashierItemPrice.class, id);
	}
	
	/** {@inheritDoc} */
	@Override
	@Transactional(readOnly = true)
	public CashierItemPrice getCashierItemPriceByUuid(@Nonnull String uuid) {
		return sessionFactory.getCurrentSession()
		        .createQuery("from CashierItemPrice where uuid = :uuid", CashierItemPrice.class).setParameter("uuid", uuid)
		        .uniqueResultOptional().orElse(null);
	}
	
	/** {@inheritDoc} */
	@Override
	@Transactional(readOnly = true)
	public List<CashierItemPrice> getCashierItemPrices(boolean includeRetired) {
		return sessionFactory.getCurrentSession().createQuery(
		    "from CashierItemPrice" + (includeRetired ? "" : " where retired = false"), CashierItemPrice.class).list();
	}
	
	/** {@inheritDoc} */
	@Override
	@Transactional
	public CashierItemPrice saveCashierItemPrice(@Nonnull CashierItemPrice cashierItemPrice) {
		sessionFactory.getCurrentSession().saveOrUpdate(cashierItemPrice);
		return cashierItemPrice;
	}
	
	/** {@inheritDoc} */
	@Override
	@Transactional
	public void purgeCashierItemPrice(@Nonnull CashierItemPrice cashierItemPrice) {
		sessionFactory.getCurrentSession().delete(cashierItemPrice);
	}
}
