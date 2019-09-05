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

package net.opentechnology.triki.core.boot;

import net.opentechnology.triki.modules.Module;
import org.apache.camel.CamelContext;
import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.request.RequestContextListener;

public class TrikiMain {

  private static final String ERROR_MISSING_CONTENT_DIR = "Must specify a content directory, with -Dcontent_dir=<dir>";

  private Logger logger = Logger.getLogger(this.getClass());
  private ApplicationContext ctx;
  private Server server;

  public static void main(String[] args) {
    TrikiMain main = new TrikiMain();
    try {
      main.addShutdownHook();
      main.initialise();
      main.start();
      main.join();
    } catch (StartupException e) {
      System.err.println(e);
    }
  }

  public void initialise() throws StartupException {
    ctx = new ClassPathXmlApplicationContext("classpath*:init*.xml");
  }

  public void start() throws StartupException {
    CachedPropertyStore props = ctx.getBean("propStore", CachedPropertyStore.class);
    props.setContentDir(getContentDir(System.getProperty("content_dir")));
    props.setPort(getPort(System.getProperty("port")));
    props.setMode(System.getProperty("mode", "prod"));

    initModules();

    ServletContextHandler sch = ctx.getBean("sch", ServletContextHandler.class);
    sch.addEventListener(new RequestContextListener());
    sch.addEventListener(new CustomContextListener(ctx));
    sch.setInitParameter("contextConfigLocation", "classpath*:web-*.xml");
    ServletHolder h = new ServletHolder(new CXFServlet());
    h.setInitOrder(1);
    sch.addServlet(h, "/*");

    initWeb();

    server = new Server(props.getPort());
    logger.info("Initialising triki on port " + props.getPort() + " and content directory " + props.getContentDir());

    try {
      server.setHandler(sch);
      server.start();
    } catch (Exception e) {
      throw new StartupException(e);
    }
  }

  private void initModules() throws StartupException {
    Module coreModule = getCtx().getBean("coreModule", Module.class);
    coreModule.initMod();
    Module authModule = getCtx().getBean("authModule", Module.class);
    authModule.initMod();
  }

  private void initWeb() throws StartupException {
    String[] beanNames = getCtx().getBeanNamesForType(Module.class);
    for (String beanName : beanNames) {
      Module module = getCtx().getBean(beanName, Module.class);
      logger.info("Initialising web for module " + beanName);
      module.initWeb();
    }
  }

  private int getPort(String port) {
    if (port == null) {
      return 8080;
    } else {
      return Integer.parseInt(port);
    }
  }

  private String getContentDir(String dir) {
    if (dir == null) {
      throw new RuntimeException(ERROR_MISSING_CONTENT_DIR);
    } else {
      return dir;
    }
  }

  public void stop() {
    try {
      server.stop();
      CamelContext camelCtx = ctx.getBean(CamelContext.class);
      camelCtx.stop();
    } catch (Exception e) {
      System.err.println(e);
    }
  }


  public ApplicationContext getCtx() {
    return ctx;
  }

  public void join() {
    try {
      server.join();
    } catch (InterruptedException e) {
      System.err.println(e);
    }
  }

  private void addShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      logger.info("Calling triki shutdown hook");
      stop();
    }));
  }

}
