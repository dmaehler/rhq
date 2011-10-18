/*
 * RHQ Management Platform
 * Copyright (C) 2011 Red Hat, Inc.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package org.rhq.enterprise.gui.coregui.client.drift.wizard;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.rhq.core.domain.common.EntityContext;
import org.rhq.core.domain.criteria.ResourceCriteria;
import org.rhq.core.domain.criteria.ResourceTypeCriteria;
import org.rhq.core.domain.resource.Resource;
import org.rhq.core.domain.resource.ResourceType;
import org.rhq.core.domain.util.PageList;
import org.rhq.enterprise.gui.coregui.client.CoreGUI;
import org.rhq.enterprise.gui.coregui.client.components.table.Table;
import org.rhq.enterprise.gui.coregui.client.components.wizard.WizardStep;
import org.rhq.enterprise.gui.coregui.client.gwt.GWTServiceLookup;
import org.rhq.enterprise.gui.coregui.client.util.message.Message;

/**
 * @author Jay Shaughnessy
 */
public class DriftAddDefinitionWizard extends AbstractDriftAddDefinitionWizard {

    private Table<?> table;

    public DriftAddDefinitionWizard(EntityContext context, ResourceType type, Table<?> table) {

        super(context, type);
        this.table = table;

        final ArrayList<WizardStep> steps = new ArrayList<WizardStep>();

        steps.add(new DriftAddDefinitionWizardInfoStep(DriftAddDefinitionWizard.this));
        steps.add(new DriftAddDefinitionWizardConfigStep(DriftAddDefinitionWizard.this));

        setSteps(steps);
    }

    @Override
    public String getWindowTitle() {
        switch (getEntityContext().getType()) {
        case SubsystemView:
            return MSG.view_drift_wizard_addTemplate_windowTitle();

        default:
            return MSG.view_drift_wizard_addDef_windowTitle();
        }
    }

    @Override
    public String getTitle() {
        switch (getEntityContext().getType()) {
        case SubsystemView:
            return MSG.view_drift_wizard_addTemplate_title(getType().getName());

        default:
            return MSG.view_drift_wizard_addDef_title(getType().getName());
        }
    }

    @Override
    public String getSubtitle() {
        return null;
    }

    @Override
    public void execute() {
        EntityContext context = getEntityContext();
        switch (context.getType()) {
        case Resource:

            ResourceCriteria rc = new ResourceCriteria();
            rc.addFilterId(context.getResourceId());
            rc.fetchResourceType(true);
            GWTServiceLookup.getDriftService().updateDriftDefinition(context, getNewDriftDefinition(),
                new AsyncCallback<Void>() {
                    public void onSuccess(Void result) {
                        CoreGUI.getMessageCenter().notify(
                            new Message(MSG.view_drift_wizard_addDef_success(getNewDriftDefinition().getName()),
                                Message.Severity.Info));
                        getView().closeDialog();
                        DriftAddDefinitionWizard.this.table.refresh();
                    }

                    public void onFailure(Throwable caught) {
                        CoreGUI.getErrorHandler().handleError(
                            MSG.view_drift_wizard_addDef_failure(getNewDriftDefinition().getName()), caught);
                        getView().closeDialog();
                    }
                });

            break;

        case SubsystemView:
            GWTServiceLookup.getDriftService().createTemplate(getType().getId(), getNewDriftDefinition(),
                new AsyncCallback<Void>() {
                    public void onSuccess(Void result) {
                        CoreGUI.getMessageCenter().notify(
                            new Message(MSG.view_drift_wizard_addTemplate_success(getNewDriftDefinition().getName()),
                                Message.Severity.Info));
                        getView().closeDialog();
                        DriftAddDefinitionWizard.this.table.refresh();
                    }

                    public void onFailure(Throwable caught) {
                        CoreGUI.getErrorHandler().handleError(
                            MSG.view_drift_wizard_addTemplate_failure(getNewDriftDefinition().getName()), caught);
                        getView().closeDialog();
                    }
                });

            break;

        default:
            throw new IllegalArgumentException("Entity Context Type not supported [" + context + "]");
        }
    }

    public static void showWizard(final EntityContext context, final Table<?> table) {
        assert context != null;

        switch (context.getType()) {
        case Resource:
            ResourceCriteria rc = new ResourceCriteria();
            rc.addFilterId(context.getResourceId());
            rc.fetchResourceType(true);
            GWTServiceLookup.getResourceService().findResourcesByCriteria(rc, new AsyncCallback<PageList<Resource>>() {
                public void onSuccess(PageList<Resource> result) {
                    if (result.isEmpty()) {
                        throw new IllegalArgumentException("Entity not found [" + context + "]");
                    }

                    final Resource resource = result.get(0);

                    // bypass type cache because this is infrequent and we don't need to cache the
                    // drift def templates
                    ResourceTypeCriteria rtc = new ResourceTypeCriteria();
                    rtc.addFilterId(resource.getResourceType().getId());
                    rtc.fetchDriftDefinitionTemplates(true);
                    GWTServiceLookup.getResourceTypeGWTService().findResourceTypesByCriteria(rtc,
                        new AsyncCallback<PageList<ResourceType>>() {
                            public void onSuccess(PageList<ResourceType> result) {
                                if (result.isEmpty()) {
                                    throw new IllegalArgumentException("Resource Type not found ["
                                        + resource.getResourceType().getId() + "]");
                                }

                                DriftAddDefinitionWizard wizard = new DriftAddDefinitionWizard(context, result.get(0),
                                    table);
                                wizard.startWizard();
                            }

                            public void onFailure(Throwable caught) {
                                CoreGUI.getErrorHandler().handleError(MSG.widget_typeTree_loadFail(), caught);
                            }
                        });
                }

                public void onFailure(Throwable caught) {
                    CoreGUI.getErrorHandler().handleError(MSG.view_inventory_resources_loadFailed(), caught);
                }
            });

            break;

        default:
            throw new IllegalArgumentException("Entity Context Type not supported [" + context + "]");
        }
    }

    @Override
    public void cancel() {
        super.cancel();
    }

}
