/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.billing.api.db.hibernate;

import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.openmrs.module.billing.api.db.CashierItemPriceDAO;
import org.openmrs.module.billing.api.model.CashierItemPrice;

import javax.annotation.Nonnull;
import java.util.List;

@RequiredArgsConstructor
public class HibernateCashierItemPriceDAOImpl implements CashierItemPriceDAO {
	
	private final SessionFactory sessionFactory;
	
	/** {@inheritDoc} */
	@Override
	public CashierItemPrice getCashierItemPrice(@Nonnull Integer id) {
		return sessionFactory.getCurrentSession().get(CashierItemPrice.class, id);
	}
	
	/** {@inheritDoc} */
	@Override
	public CashierItemPrice getCashierItemPriceByUuid(@Nonnull String uuid) {
		return sessionFactory.getCurrentSession()
		        .createQuery("from CashierItemPrice where uuid = :uuid", CashierItemPrice.class).setParameter("uuid", uuid)
		        .uniqueResultOptional().orElse(null);
	}
	
	/** {@inheritDoc} */
	@Override
	public List<CashierItemPrice> getCashierItemPrices(boolean includeRetired) {
		return sessionFactory.getCurrentSession().createQuery(
		    "from CashierItemPrice" + (includeRetired ? "" : " where retired = false"), CashierItemPrice.class).list();
	}
	
	/** {@inheritDoc} */
	@Override
	public CashierItemPrice saveCashierItemPrice(@Nonnull CashierItemPrice cashierItemPrice) {
		sessionFactory.getCurrentSession().saveOrUpdate(cashierItemPrice);
		return cashierItemPrice;
	}
	
	/** {@inheritDoc} */
	@Override
	public void purgeCashierItemPrice(@Nonnull CashierItemPrice cashierItemPrice) {
		sessionFactory.getCurrentSession().delete(cashierItemPrice);
	}
}
