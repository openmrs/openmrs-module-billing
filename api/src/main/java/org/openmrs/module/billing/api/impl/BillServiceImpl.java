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
package org.openmrs.module.billing.api.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.openmrs.GlobalProperty;
import org.openmrs.Patient;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.billing.api.BillService;
import org.openmrs.module.billing.api.IReceiptNumberGenerator;
import org.openmrs.module.billing.api.ReceiptNumberGeneratorFactory;
import org.openmrs.module.billing.api.base.PagingInfo;
import org.openmrs.module.billing.api.base.entity.impl.BaseEntityDataServiceImpl;
import org.openmrs.module.billing.api.base.entity.security.IEntityAuthorizationPrivileges;
import org.openmrs.module.billing.api.db.BillDAO;
import org.openmrs.module.billing.api.model.Bill;
import org.openmrs.module.billing.api.model.BillLineItem;
import org.openmrs.module.billing.api.model.BillStatus;
import org.openmrs.module.billing.api.model.Payment;
import org.openmrs.module.billing.api.search.BillSearch;
import org.openmrs.module.billing.api.util.PrivilegeConstants;
import org.openmrs.module.billing.util.Utils;
import org.openmrs.util.OpenmrsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Data service implementation class for {@link Bill}s.
 */
public class BillServiceImpl implements BillService {
	
	private static final int MAX_LENGTH_RECEIPT_NUMBER = 255;
	
	private static final Logger LOG = LoggerFactory.getLogger(BillServiceImpl.class);

    @Setter(onMethod_ = { @Autowired })
    private BillDAO dao;

    @Override
    public Bill getBillById(Integer id) {
        if (id == null) {
            return null;
        }

        Bill bill = dao.getBill(id);
        return bill;
    }

    @Override
    public Bill getBillByUuid(String uuid) {
        if (uuid == null || StringUtils.isBlank(uuid)) {
            return null;
        }

        Bill bill = dao.getBillByUuid(uuid);
        return bill;
    }

    @Override
    public Bill getBillByReceiptNumber(String receiptNumber) {
        if (receiptNumber == null || StringUtils.isBlank(receiptNumber) || receiptNumber.length() > MAX_LENGTH_RECEIPT_NUMBER) {
            return null;
        }

        return dao.getBillByReceiptNumber(receiptNumber);
    }
	
	@Override
	public List<Bill> getBills(final BillSearch billSearch) {
		return getBills(billSearch, null);
	}
	
	@Override
	public List<Bill> getBills(final BillSearch billSearch, PagingInfo pagingInfo) {
		if (billSearch == null) {
			return Collections.emptyList();
		} else if (billSearch.getTemplate() == null) {
            return Collections.emptyList();
		}
		
		boolean includeVoidedLineItems = billSearch.getIncludeVoidedLineItems() != null
		        && billSearch.getIncludeVoidedLineItems();
		
		return dao.getBillsByBillSearch(billSearch, pagingInfo);
	}

    /**
     * Saves the bill to the database, creating a new bill or updating an existing one.
     *
     * @param bill The bill to be saved.
     * @return The saved bill.
     * @should Generate a new receipt number if one has not been defined.
     * @should Not generate a receipt number if one has already been defined.
     * @should Throw APIException if receipt number cannot be generated.
     */
    @Override
    @Authorized({ PrivilegeConstants.MANAGE_BILLS })
    @Transactional
    public Bill saveBill(Bill bill) {
        if (bill == null) {
            throw new NullPointerException("The bill must be defined.");
        }

        dao.saveBill(Bill);
    }
}
