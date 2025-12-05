package org.openmrs.module.billing.api.db.hibernate;

import lombok.Setter;
import org.openmrs.module.billing.api.db.BillDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class HibernateBillDaoImpl implements BillDAO {

    @Setter(onMethod_ = { @Autowired})
    private EntityManager entityManager;

    @Override
    public Bill getBill(@Nonnull Integer id) {
        return entityManager.find(Bill.class, id);
    }

    @Override
    public Bill getBillByUuid(@Nonnull String uuid) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Bill> cq = cb.createQuery(Bill.class);
        Root<Bill> root = cq.from(Bill.class);
        cq.select(root).where(cb.equal(root.get("uuid"), uuid));

        TypedQuery<Bill> query = entityManager.createQuery(cq);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Bill getBillByReceiptNumber(@Nonnull String receiptNumber) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Bill> cq = cb.createQuery(Bill.class);
        Root<Bill> root = cq.from(Bill.class);
        cq.select(root).where(cb.equal(root.get("receiptNumber"), receiptNumber));

        TypedQuery<Bill> query = entityManager.createQuery(cq);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
