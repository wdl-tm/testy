package tm.testy.se.health;

import io.helidon.config.Config;
import io.helidon.webserver.Routing;

/**
 * Serves up a {@code "/health"} service endpoint.
 */

public final class Health {

  private Health () {}

  public static Routing routing (
    final Config config
  ) {

    return
      Routing
        .builder ()
        .register ( Liveness.service ( config ) )
        .register ( Readiness.service ( config ) )
        .build ();

  }

}
