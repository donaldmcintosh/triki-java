/************************************************************************************
*
*   This file is part of triki
*
*   Written by Donald McIntosh (dbm@opentechnology.net) 
*
*   triki is free software: you can redistribute it and/or modify
*   it under the terms of the GNU General Public License as published by
*   the Free Software Foundation, either version 3 of the License, or
*   (at your option) any later version.
*
*   triki is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*   GNU General Public License for more details.
*
*   You should have received a copy of the GNU General Public License
*   along with triki.  If not, see <http://www.gnu.org/licenses/>.
*
************************************************************************************/

package net.opentechnology.triki.pageviews.module;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;

import net.opentechnology.triki.auth.resources.AuthenticateResource;
import net.opentechnology.triki.modules.PostRenderListener;
import net.opentechnology.triki.pageviews.async.PageView;

public class RecordPageView implements PostRenderListener{

	@Inject
	private CamelContext camelCtx;
	
	@Override
	public void rendered(String url, HttpServletRequest req, HttpSession session) {
		ProducerTemplate template = camelCtx.createProducerTemplate();
    	PageView view = new PageView();
    	view.setUrl(url);
    	view.setAgent(req.getHeader("User-Agent"));
    	view.setForwardedFor(req.getHeader("X-Forwarded-For"));
    	view.setLogin((String) session.getAttribute(AuthenticateResource.SESSION_ID));
    	template.sendBody("seda:pageviews", view);	
	}

}
