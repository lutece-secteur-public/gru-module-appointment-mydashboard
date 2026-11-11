/*
 * Copyright (c) 2002-2026, City of Paris
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
package fr.paris.lutece.plugins.appointment.modules.mydashboard.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import fr.paris.lutece.portal.service.template.HtmlMarkup;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.TemplateException;

/**
 * Renders the "My appointments" dashboard panel with HTML auto-escaping (LUT-33153). The panel is returned as a String
 * by MyAppointmentDashboardComponent.getDashboardData, and plugin-mydashboard marks it with HtmlMarkup.ofAll where it
 * puts it in its models: the panel keeps its tags there and the data in it is escaped once. The core macros and the list
 * of plugin-appointment the panel includes are replaced by stand-ins: only the FreeMarker engine is needed.
 */
public class AppointmentMyDashboardTemplatesRenderTest
{
    private static final String TEMPLATES_ROOT = "webapp/WEB-INF/templates";
    private static final String TEMPLATE_DASHBOARD = "skin/plugins/appointment/modules/mydashboard/my_appointments_dashboard.html";

    /** Stand-ins of the core macros the template calls. */
    private static final String MACROS = "macros.ftl";
    private static final String MACROS_SOURCE = "<#macro cTpl rest...><#nested></#macro>"
            + "<#macro cCard title='' class='' rest...><div class=\"card ${class}\"><h3>${title}</h3><#nested></div></#macro>";

    /** Stand-in of the list of plugin-appointment, included by the panel: it writes the form name of the model. */
    private static final String LIST = "skin/plugins/appointment/my_appointments_list.html";
    private static final String LIST_SOURCE = "<ul><li class=\"appointment\">${formTitle}</li></ul>";

    /** Stand-in of the dashboard page of plugin-mydashboard, which writes each component's HTML. */
    private static final String DASHBOARDS = "dashboards.ftl";
    private static final String DASHBOARDS_SOURCE = "<#list listDashboardsContent as content>${content}</#list>";

    private static final String HOSTILE = "Jean <b>x</b> \"q\" <script>alert(1)</script>";
    private static final String HOSTILE_ESCAPED = "Jean &lt;b&gt;x&lt;/b&gt; &quot;q&quot; &lt;script&gt;alert(1)&lt;/script&gt;";

    /**
     * Renders a template with the core configuration of the output format and the stand-ins.
     *
     * @param strTemplate
     *            the template path
     * @param model
     *            the model
     * @return the HTML
     * @throws IOException
     *             if the template cannot be read
     * @throws TemplateException
     *             if the rendering fails
     */
    private static String render( String strTemplate, Map<String, Object> model ) throws IOException, TemplateException
    {
        StringTemplateLoader standIns = new StringTemplateLoader( );
        standIns.putTemplate( MACROS, MACROS_SOURCE );
        standIns.putTemplate( LIST, LIST_SOURCE );
        standIns.putTemplate( DASHBOARDS, DASHBOARDS_SOURCE );
        Configuration cfg = new Configuration( Configuration.VERSION_2_3_0 );
        cfg.setOutputFormat( HTMLOutputFormat.INSTANCE );
        cfg.setAutoEscapingPolicy( Configuration.ENABLE_IF_DEFAULT_AUTO_ESCAPING_POLICY );
        cfg.setTemplateLoader( new MultiTemplateLoader( new TemplateLoader [ ] {
                standIns, new FileTemplateLoader( new File( TEMPLATES_ROOT ) )
        } ) );
        cfg.addAutoInclude( MACROS );
        StringWriter writer = new StringWriter( );
        cfg.getTemplate( strTemplate ).process( model, writer );

        return writer.toString( );
    }

    /**
     * The panel writes the included list as HTML inside its card, and escapes the data of the list.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testPanelKeepsTheListAndEscapesItsData( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        model.put( "formTitle", HOSTILE );
        String strHtml = render( TEMPLATE_DASHBOARD, model );

        assertTrue( strHtml.contains( "<div class=\"card secondary\"><h3>#i18n{appointment.myAppointments.name}</h3>" ), strHtml );
        assertTrue( strHtml.contains( "<ul><li class=\"appointment\">" + HOSTILE_ESCAPED + "</li></ul>" ), strHtml );
        assertFalse( strHtml.contains( "<script>" ), strHtml );
    }

    /**
     * The panel, given as a String to plugin-mydashboard and marked there with HtmlMarkup.ofAll, keeps its tags in the
     * dashboard page, and its data is not escaped a second time.
     *
     * @throws Exception
     *             if the rendering fails
     */
    @Test
    public void testPanelMarkedByTheDashboardKeepsItsHtml( ) throws Exception
    {
        Map<String, Object> model = new HashMap<>( );
        model.put( "formTitle", HOSTILE );
        String strPanel = render( TEMPLATE_DASHBOARD, model );

        Map<String, Object> modelDashboards = new HashMap<>( );
        modelDashboards.put( "listDashboardsContent", HtmlMarkup.ofAll( List.of( strPanel ) ) );
        String strHtml = render( DASHBOARDS, modelDashboards );

        assertTrue( strHtml.contains( strPanel ), strHtml );
        assertTrue( strHtml.contains( HOSTILE_ESCAPED ), strHtml );
        assertFalse( strHtml.contains( "&amp;lt;" ), strHtml );
        assertFalse( strHtml.contains( "<script>" ), strHtml );
    }
}
