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

package net.opentechnology.triki.core.expander;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.lang.String;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.client.methods.HttpGet;

public abstract class AbstractSourceExpander {

    private final CloseableHttpClient httpclient;
    
    public AbstractSourceExpander() 
    {
        httpclient = HttpClients.createDefault();
    }
    
    public AbstractSourceExpander(CloseableHttpClient httpclient) 
    {
        this.httpclient = httpclient;
    }
    
    public String getRemoteContent(String urlStr, String mime) throws ExpanderException {
        HttpGet httpGet = new HttpGet(urlStr);
        StringWriter writer = new StringWriter();
        try
        {
            CloseableHttpResponse response = httpclient.execute(httpGet);
            IOUtils.copy(response.getEntity().getContent(), writer, "UTF-8");
            String content = writer.toString();
            return content;
        }
        catch (ClientProtocolException e)
        {
            throw new ExpanderException("", e);
        }
        catch (IOException e)
        {
           throw new ExpanderException("", e);
        }
        
    }

}
