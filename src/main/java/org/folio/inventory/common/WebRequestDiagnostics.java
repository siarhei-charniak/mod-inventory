package org.folio.inventory.common;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

import java.lang.invoke.MethodHandles;

public class WebRequestDiagnostics {
  private WebRequestDiagnostics() {

  }

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static void outputDiagnostics(RoutingContext routingContext) {

    log.info(String.format("Handling %s %s", routingContext.request().rawMethod(),
      routingContext.normalisedPath()));

    outputHeaders(routingContext);

    routingContext.next();
  }

  private static void outputHeaders(RoutingContext routingContext) {
    log.debug("Headers");

    for (String name : routingContext.request().headers().names()) {
      for (String entry : routingContext.request().headers().getAll(name))
        log.debug(String.format("%s : %s", name, entry));
    }
  }
}
