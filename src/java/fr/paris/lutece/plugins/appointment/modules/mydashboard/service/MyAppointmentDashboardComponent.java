/*
 * Copyright (c) 2002-2018, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.appointment.modules.mydashboard.service;

import fr.paris.lutece.plugins.appointment.web.AppointmentApp;
import fr.paris.lutece.plugins.mydashboard.service.MyDashboardComponent;
import fr.paris.lutece.portal.service.i18n.I18nService;
import fr.paris.lutece.portal.service.security.UserNotSignedException;
import fr.paris.lutece.portal.service.template.AppTemplateService;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * MyAppointmentDashboardComponent
 */
@ApplicationScoped
@Named
public class MyAppointmentDashboardComponent extends MyDashboardComponent
{
    /**
     * 
     */
    private static final long serialVersionUID = 5522031658740421535L;
    private static final String MESSAGE_DASHBOARD_MY_APPOINTMENTS_DESCRIPTION = "module.appointment.mydashboard.dashboardMyAppointment.description";
    private static final String DASHBOARD_COMPONENT_ID = "mydashboard.dashboardMyAppointments";
    // The list of the appointments, without the page around it (breadcrumb, title) of the XPage
    private static final String TEMPLATE_MY_APPOINTMENTS_DASHBOARD = "/skin/plugins/appointment/modules/mydashboard/my_appointments_dashboard.html";

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDashboardData( HttpServletRequest request )
    {
        try
        {
            Map<String, Object> model = new HashMap<>( );
            if ( !AppointmentApp.fillMyAppointmentsModel( request, model ) )
            {
                return null;
            }
            return AppTemplateService.getTemplate( TEMPLATE_MY_APPOINTMENTS_DASHBOARD, request.getLocale( ), model ).getHtml( );
        }
        catch( UserNotSignedException e )
        {
            return StringUtils.EMPTY;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComponentId( )
    {
        return DASHBOARD_COMPONENT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getComponentDescription( Locale locale )
    {
        return I18nService.getLocalizedString( MESSAGE_DASHBOARD_MY_APPOINTMENTS_DESCRIPTION, locale );
    }
}
