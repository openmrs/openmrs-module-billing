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
package org.openmrs.module.billing.web.legacyweb.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.module.billing.web.base.controller.HeaderController;
import org.openmrs.module.billing.ModuleSettings;
import org.openmrs.module.billing.api.ISequentialReceiptNumberGeneratorService;
import org.openmrs.module.billing.api.ReceiptNumberGeneratorFactory;
import org.openmrs.module.billing.api.SequentialReceiptNumberGenerator;
import org.openmrs.module.billing.api.base.util.UrlUtil;
import org.openmrs.module.billing.api.model.SequentialReceiptNumberGeneratorModel;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

/**
 * Abstract sequential receipt number generator functionality
 */
public abstract class AbstractSequentialReceiptNumberGenerator {

    public abstract ISequentialReceiptNumberGeneratorService getService();

    public abstract String getReceiptNumberGeneratorUrl();

    @RequestMapping(method = RequestMethod.GET)
    public void render(ModelMap modelMap, HttpServletRequest request) throws IOException {
        SequentialReceiptNumberGeneratorModel model = getService().getOnly();

        modelMap.addAttribute("generator", model);

        modelMap.addAttribute("settings", ModuleSettings.loadSettings());

        HeaderController.render(modelMap, request);
    }

    @RequestMapping(method = RequestMethod.POST)
    public String post(@ModelAttribute("generator") SequentialReceiptNumberGeneratorModel generator, WebRequest request) {
        if (generator.getSeparator().equals("<space>")) {
            generator.setSeparator(" ");
        }

        // The check digit checkbox value is only bound if checked
        if (request.getParameter("includeCheckDigit") == null) {
            generator.setIncludeCheckDigit(false);
        }

        // Save the generator settings
        getService().save(generator);

        // Set the system generator
        ReceiptNumberGeneratorFactory.setGenerator(new SequentialReceiptNumberGenerator());
        return UrlUtil.redirectUrl(getReceiptNumberGeneratorUrl());
    }
}
